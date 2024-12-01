package dora.db.async

import android.os.Handler
import android.os.Looper
import android.os.Message
import dora.db.OrmLog
import dora.db.Transaction
import dora.db.builder.QueryBuilder
import dora.db.builder.WhereBuilder
import dora.db.exception.OrmTaskException
import dora.db.table.OrmTable
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal class OrmExecutor<T : OrmTable> : Runnable, Handler.Callback {

    private val queue: BlockingQueue<OrmTask<T>> = LinkedBlockingQueue()
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    @Volatile
    private var executorRunning = false

    @Volatile
    var maxTaskCountToMerge: Int = 50

    @Volatile
    var listener: OrmTaskListener<T>? = null

    @Volatile
    var listenerMainThread: OrmTaskListener<T>? = null

    @Volatile
    var waitForMergeMillis: Int = 50

    private var countTasksEnqueued = 0
    private var countTasksCompleted = 0

    private var handlerMainThread: Handler? = null
    private var lastSequenceNumber = 0
    private var stopQueue: Boolean = false

    fun resetQueueStopFlag() {
        stopQueue = false
    }

    private fun shouldStopQueue(task: OrmTask<T>): Boolean {
        return (task.flags and OrmTask.FLAG_STOP_QUEUE_ON_EXCEPTION) != 0
    }

    fun enqueue(task: OrmTask<T>) {
        synchronized(this) {
            task.sequenceNumber = ++lastSequenceNumber
            queue.add(task)
            countTasksEnqueued++
            if (!executorRunning) {
                executorRunning = true
                executor.execute(this)
            }
        }
    }

    @get:Synchronized
    val isCompleted: Boolean
        get() = lock.withLock {
            countTasksEnqueued == countTasksCompleted
        }

    /**
     * Waits until all enqueued operations are complete. If the thread gets interrupted, any
     * [InterruptedException] will be rethrown as a [OrmTaskException].
     * 简体中文：等待直到所有排队的操作完成。如果线程被中断，任何 [InterruptedException] 都会被重新抛出为
     * [OrmTaskException]。
     */
    @Synchronized
    @Throws(OrmTaskException::class)
    fun waitForCompletion() {
        lock.lock()
        try {
            while (!isCompleted) {
                try {
                    condition.await()
                } catch (e: InterruptedException) {
                    throw OrmTaskException("Interrupted while waiting for all tasks to complete.\n$e", e)
                }
            }
        } finally {
            lock.unlock()
        }
    }

    /**
     * Waits until all enqueued operations are complete, but at most the given amount of milliseconds.
     * If the thread gets interrupted, any [InterruptedException] will be rethrown as a [OrmTaskException].
     * 简体中文：等待直到所有排队的操作完成，但最多等待指定的毫秒数。如果线程被中断，任何 [InterruptedException]
     * 都会被重新抛出为 [OrmTaskException]。
     *
     * @return true if operations completed in the given time frame.
     */
    @Synchronized
    @Throws(OrmTaskException::class)
    fun waitForCompletion(maxMillis: Int = 0): Boolean {
        lock.withLock {
            val deadline = System.currentTimeMillis() + maxMillis
            while (!isCompleted) {
                val remainingTime = deadline - System.currentTimeMillis()
                if (remainingTime <= 0 || maxMillis == 0) {
                    break
                }
                try {
                    condition.await(remainingTime, TimeUnit.MILLISECONDS)
                } catch (e: InterruptedException) {
                    throw OrmTaskException("Interrupted while waiting: ${e.message}", e)
                }
            }
            return isCompleted
        }
    }

    override fun run() {
        try {
            try {
                while (true) {
                    var task = queue.poll(1, TimeUnit.SECONDS)
                    try {
                        if (task == null) {
                            synchronized(this) {
                                task = queue.poll()
                                if (task == null) {
                                    executorRunning = false
                                    return
                                }
                            }
                        }

                        if (task.isMergeTx) {
                            val task2 =
                                queue.poll(waitForMergeMillis.toLong(), TimeUnit.MILLISECONDS)
                            if (task2 != null) {
                                if (task.isMergeableWith(task2)) {
                                    mergeTxAndExecute(task, task2)
                                } else {
                                    executeTaskAndPostCompleted(task)
                                    executeTaskAndPostCompleted(task2)
                                }
                                continue
                            }
                        }
                        executeTaskAndPostCompleted(task)
                    } catch (e: Exception) {
                        listener?.onFailed(task, e)
                    }
                }
            } catch (e: InterruptedException) {
                OrmLog.w(Thread.currentThread().name + " was interrupted.\n" + e)
            } catch (e: OrmTaskException) {
                throw RuntimeException(e)
            }
        } finally {
            executorRunning = false
        }
    }

    @Throws(OrmTaskException::class)
    private fun mergeTxAndExecute(task1: OrmTask<T>, task2: OrmTask<T>) {
        val mergedTasks = ArrayList<OrmTask<T>>()
        mergedTasks.add(task1)
        mergedTasks.add(task2)
        var success = false
        Transaction.execute {
            for (i in mergedTasks.indices) {
                val task = mergedTasks[i]
                executeTask(task)
                if (task.isFailed) {
                    break
                }
                if (i == mergedTasks.size - 1) {
                    val peekedTask = queue.peek()
                    if (i < maxTaskCountToMerge && task.isMergeableWith(peekedTask)) {
                        val removedTask = queue.remove()
                        if (removedTask !== peekedTask) {
                            throw OrmTaskException("Internal error: peeked task did not match removed task")
                        }
                        mergedTasks.add(removedTask)
                    } else {
                        success = true
                        break
                    }
                }
            }
        }
        if (success) {
            val mergedCount = mergedTasks.size
            for (task in mergedTasks) {
                task.mergedTasksCount = mergedCount
                handleTaskCompleted(task)
            }
        } else {
            OrmLog.i(
                "Reverted merged transaction because one of the tasks failed. Executing tasks one by " +
                        "one instead..."
            )
            for (task in mergedTasks) {
                task.reset()
                executeTaskAndPostCompleted(task)
            }
        }
    }

    private fun handleTaskCompleted(task: OrmTask<T>) {
        task.setCompleted()
        listener?.onCompleted(task)
        listenerMainThread?.let {
            getMainThreadHandler().obtainMessage(1, task).sendToTarget()
        }
        lock.withLock {
            countTasksCompleted++
            if (countTasksCompleted == countTasksEnqueued) {
                condition.signalAll()
            }
        }
    }

    private fun getMainThreadHandler(): Handler {
        return handlerMainThread ?: synchronized(this) {
            handlerMainThread ?: Handler(Looper.getMainLooper(), this).also { handlerMainThread = it }
        }
    }

    private fun executeTaskAndPostCompleted(task: OrmTask<T>) {
        executeTask(task)
        handleTaskCompleted(task)
    }

    private fun executeTask(task: OrmTask<T>) {
        if (stopQueue) {
            OrmLog.w("Task queue stopped due to previous exception.")
            return
        }

        task.timeStarted = System.currentTimeMillis()
        try {
            when (task.type) {
                OrmTask.Type.Insert -> task.result = task.dao.insert(task.parameter as T)
                OrmTask.Type.InsertList -> task.result = task.dao.insert(task.parameter as List<T>)
                OrmTask.Type.Delete -> task.result = task.dao.delete((task.parameter as WhereBuilder))
                OrmTask.Type.DeleteByKey -> task.result = task.dao.delete(task.parameter as T)
                OrmTask.Type.DeleteAll -> task.result = task.dao.deleteAll()
                OrmTask.Type.InsertOrReplace -> task.result = task.dao.insertOrUpdate(task.parameter as T)
                OrmTask.Type.UpdateByKey -> task.result = task.dao.update(task.parameter as T)
                OrmTask.Type.WhereList -> task.result =
                    task.dao.select((task.parameter as WhereBuilder))
                OrmTask.Type.QueryList -> task.result =
                    task.dao.select((task.parameter as QueryBuilder))
                OrmTask.Type.QueryAll -> task.result = task.dao.selectAll()
                OrmTask.Type.WhereUnique -> task.result =
                    task.dao.selectOne((task.parameter as WhereBuilder))
                OrmTask.Type.QueryUnique -> task.result =
                    task.dao.selectOne((task.parameter as QueryBuilder))
                OrmTask.Type.IndexUnique -> task.result =
                    task.dao.selectOne()
                OrmTask.Type.Count -> task.result = task.dao.count()
                OrmTask.Type.WhereCount -> task.result =
                    task.dao.count((task.parameter as WhereBuilder))
                OrmTask.Type.QueryCount -> task.result =
                    task.dao.count((task.parameter as QueryBuilder))
                OrmTask.Type.AddColumn -> task.result =
                    task.dao.addColumn((task.parameter as String))
                OrmTask.Type.RenameTable -> task.result =
                    task.dao.renameTable((task.parameter as String))
                OrmTask.Type.Drop -> task.result =
                    task.dao.drop()
                OrmTask.Type.WhereInsertOrReplace,
                OrmTask.Type.WhereUpdate,
                OrmTask.Type.RenameColumn -> {
                    task.result = (task.parameter as Callable<*>).call()
                }
                OrmTask.Type.TransactionRunnable -> executeTransactionRunnable(task)
                OrmTask.Type.TransactionCallable -> executeTransactionCallable(task)
                else -> throw OrmTaskException("Unsupported operation: " + task.type)
            }
        } catch (th: Throwable) {
            task.throwable = th
            if ((task.flags and OrmTask.FLAG_STOP_QUEUE_ON_EXCEPTION) != 0) {
                stopQueue = true
            }
            task.creatorStacktrace?.let { stacktrace ->
                OrmLog.e(
                    "Task failed: ${task.type}, Sequence: ${task.sequenceNumber}. Created at:"
                            + stacktrace
                )
            }
        } finally {
            task.timeCompleted = System.currentTimeMillis()
        }
    }

    private fun executeTransactionRunnable(task: OrmTask<T>) {
        Transaction.execute {
            (task.parameter as Runnable).run()
        }
    }

    @Throws(Exception::class)
    private fun executeTransactionCallable(task: OrmTask<T>) {
        Transaction.execute {
            task.result = (task.parameter as Callable<*>).call()
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        val listenerToCall = listenerMainThread
        listenerToCall?.onCompleted(msg.obj as OrmTask<T>)
        return false
    }

    companion object {
        private val executor: ExecutorService = Executors.newCachedThreadPool()
    }
}
