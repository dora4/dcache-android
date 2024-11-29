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

internal class OrmExecutor : Runnable, Handler.Callback {

    private val queue: BlockingQueue<OrmTask<out OrmTable>> = LinkedBlockingQueue()

    @Volatile
    private var executorRunning = false

    @Volatile
    var maxOperationCountToMerge: Int = 50

    @Volatile
    var listener: OrmTaskListener? = null

    @Volatile
    var listenerMainThread: OrmTaskListener? = null

    @Volatile
    var waitForMergeMillis: Int = 50

    private var countOperationsEnqueued = 0
    private var countOperationsCompleted = 0

    private var handlerMainThread: Handler? = null
    private var lastSequenceNumber = 0

    fun <T : OrmTable> enqueue(task: OrmTask<T>) {
        synchronized(this) {
            task.sequenceNumber = ++lastSequenceNumber
            queue.add(task)
            countOperationsEnqueued++
            if (!executorRunning) {
                executorRunning = true
                executor.execute(this)
            }
        }
    }

    @get:Synchronized
    val isCompleted: Boolean
        get() = countOperationsEnqueued == countOperationsCompleted

    /**
     * Waits until all enqueued operations are complete. If the thread gets interrupted, any
     * [InterruptedException] will be rethrown as a [OrmTaskException].
     * 简体中文：等待直到所有排队的操作完成。如果线程被中断，任何 [InterruptedException] 都会被重新抛出为
     * [OrmTaskException]。
     */
    @Synchronized
    @Throws(OrmTaskException::class)
    fun waitForCompletion() {
        while (!isCompleted) {
            try {
                (this as Object).wait()
            } catch (e: InterruptedException) {
                throw OrmTaskException("Interrupted while waiting for all operations to complete.\n$e")
            }
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
    fun waitForCompletion(maxMillis: Int): Boolean {
        if (!isCompleted) {
            try {
                (this as Object).wait(maxMillis.toLong())
            } catch (e: InterruptedException) {
                throw OrmTaskException("Interrupted while waiting for all operations to complete.\n$e")
            }
        }
        return isCompleted
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
    private fun mergeTxAndExecute(task1: OrmTask<out OrmTable>, task2: OrmTask<out OrmTable>) {
        val mergedTasks = ArrayList<OrmTask<out OrmTable>>()
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
                    val peekedOp = queue.peek()
                    if (i < maxOperationCountToMerge && task.isMergeableWith(peekedOp)) {
                        val removedOp = queue.remove()
                        if (removedOp !== peekedOp) {
                            throw OrmTaskException("Internal error: peeked op did not match removed op")
                        }
                        mergedTasks.add(removedOp)
                    } else {
                        db.setTransactionSuccessful()
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
                "Reverted merged transaction because one of the operations failed. Executing operations one by " +
                        "one instead..."
            )
            for (task in mergedTasks) {
                task.reset()
                executeTaskAndPostCompleted(task)
            }
        }
    }

    private fun handleTaskCompleted(task: OrmTask<*>) {
        task.setCompleted()
        val listenerToCall = listener
        listenerToCall?.onCompleted(task)
        if (listenerMainThread != null) {
            if (handlerMainThread == null) {
                handlerMainThread = Handler(Looper.getMainLooper(), this)
            }
            val msg = handlerMainThread!!.obtainMessage(1, task)
            handlerMainThread!!.sendMessage(msg)
        }
        synchronized(this) {
            countOperationsCompleted++
            if (countOperationsCompleted == countOperationsEnqueued) {
                (this as Object).notifyAll()
            }
        }
    }

    private fun executeTaskAndPostCompleted(task: OrmTask<*>) {
        executeTask(task)
        handleTaskCompleted(task)
    }

    private fun <T : OrmTable> executeTask(task: OrmTask<T>) {
        task.timeStarted = System.currentTimeMillis()
        try {
            when (task.type) {
                OrmTask.Type.Insert -> task.dao.insert(task.parameter as T)
                OrmTask.Type.InsertList -> task.dao.insert(task.parameter as List<T>)
                OrmTask.Type.Delete -> task.dao.delete((task.parameter as WhereBuilder))
                OrmTask.Type.DeleteByKey -> task.dao.delete(task.parameter as T)
                OrmTask.Type.DeleteAll -> task.dao.deleteAll()
                OrmTask.Type.InsertOrReplace -> task.dao.insertOrUpdate(task.parameter as T)
                OrmTask.Type.UpdateByKey -> task.dao.update(task.parameter as T)
                OrmTask.Type.WhereList -> task.result =
                    task.dao.select((task.parameter as WhereBuilder))
                OrmTask.Type.QueryList -> task.result =
                    task.dao.select((task.parameter as QueryBuilder))
                OrmTask.Type.QueryAll -> task.result = task.dao.selectAll()
                OrmTask.Type.WhereUnique -> task.result =
                    task.dao.selectOne((task.parameter as WhereBuilder))
                OrmTask.Type.QueryUnique -> task.result =
                    task.dao.selectOne((task.parameter as QueryBuilder))
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
        }
        task.timeCompleted = System.currentTimeMillis()
    }

    private fun executeTransactionRunnable(task: OrmTask<*>) {
        Transaction.execute {
            (task.parameter as Runnable).run()
        }
    }

    @Throws(Exception::class)
    private fun executeTransactionCallable(task: OrmTask<*>) {
        Transaction.execute {
            task.result = (task.parameter as Callable<*>).call()
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        val listenerToCall = listenerMainThread
        listenerToCall?.onCompleted(msg.obj as OrmTask<*>)
        return false
    }

    companion object {
        private val executor: ExecutorService = Executors.newCachedThreadPool()
    }
}
