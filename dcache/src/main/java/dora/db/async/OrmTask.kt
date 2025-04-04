package dora.db.async

import android.database.sqlite.SQLiteDatabase
import dora.db.dao.OrmDao
import dora.db.exception.OrmTaskException
import dora.db.table.OrmTable
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

open class OrmTask<T : OrmTable> internal constructor(
    val type: Type, val dao: OrmDao<T>,
    val parameter: Any? = null, val flags: Int
) {

    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    /**
     * @see OrmExecutor.executeTask
     */
    enum class Type {
        Insert,
        InsertList,
        InsertOrReplace,
        WhereInsertOrReplace,
        Delete,
        DeleteAll,
        UpdateByKey,
        WhereUpdate,
        DeleteByKey,
        WhereList,
        WhereUnique,
        QueryList,
        QueryAll,
        QueryUnique,
        IndexUnique,
        Count,
        WhereCount,
        QueryCount,
        AddColumn,
        RenameColumn,
        RenameTable,
        Drop,
        TransactionRunnable,
        TransactionCallable
    }

    @Volatile
    var timeStarted: Long = 0

    @Volatile
    var timeCompleted: Long = 0

    @Volatile
    var isCompleted: Boolean = false
        private set

    @Volatile
    var throwable: Throwable? = null

    /**
     * The stacktrace is captured using an exception if [.FLAG_TRACK_CREATOR_STACKTRACE] was used (null
     * otherwise). 简体中文：如果使用了 [.FLAG_TRACK_CREATOR_STACKTRACE]，则使用异常捕获堆栈跟踪（否则为 null）。
     */
    val creatorStacktrace: Exception? = if ((flags and FLAG_TRACK_CREATOR_STACKTRACE) != 0) Exception("OrmTask was created here") else null

    @Volatile
    var result: Any? = null

    /**
     * If this operation was successfully merged with other operation into a single TX, this will give the count of
     * merged operations. If the operation was not merged, it will be 0.
     * 简体中文：如果此操作已成功与其他操作合并为一个事务（TX），则返回合并操作的数量。如果操作没有合并，则返回 0。
     */
    @Volatile
    var mergedTasksCount: Int = 0

    /**
     * Each operation get a unique sequence number when the operation is enqueued. Can be used for efficiently
     * identifying/mapping operations. 简体中文：每个操作在排队时都会获得一个唯一的序列号。可以用于高效地识别/映射操作。
     */
    var sequenceNumber: Int = 0

    /**
     * The operation's result after it has completed. Waits until a result is available.
     * 简体中文：操作完成后返回结果。等待直到结果可用。
     *
     * @return The operation's result or null if the operation type does not produce any result.
     * 简体中文：操作完成后的结果。等待直到结果可用。操作的结果，如果操作类型没有产生结果，则返回null。
     * @throws [OrmTaskException]
     * @see .waitForCompletion
     */
    @Synchronized
    @Throws(OrmTaskException::class)
    fun result(): Any {
        if (!isCompleted) {
            waitForCompletion()
        }
        throwable?.let { throw OrmTaskException(this, it) }
        // all orm operations have return value.
        // 简体中文：所有ORM操作都有返回值
        return result!!
    }

    /**
     * The operation's result after it has completed. Waits until a result is available.
     * 简体中文：操作完成后返回结果。等待直到结果可用。
     *
     * @return The operation's result or null if the operation type does not produce any result.
     * 简体中文：操作完成后的结果。等待直到结果可用。操作的结果，如果操作类型没有产生结果，则返回null。
     * @throws [OrmTaskException]
     * @see .waitForCompletion
     */
    @Synchronized
    @Throws(OrmTaskException::class)
    fun <R> result(clazz: Class<R>): R {
        if (!isCompleted) {
            waitForCompletion()
        }
        if (throwable != null) {
            throw OrmTaskException(this, throwable!!)
        }
        return result?.takeIf { clazz.isInstance(it) } as? R
            ?: throw OrmTaskException("The result type does not match the expected type: ${clazz.name}")
    }

    val isMergeTx: Boolean
        get() = (flags and FLAG_MERGE_TX) != 0

    fun getDatabase(): SQLiteDatabase {
        return dao.getDB()
    }

    /**
     * @return true if this operation is mergeable with the given operation. Checks for null,
     * [.FLAG_MERGE_TX], and if the database instances match. 简体中文：判断此操作是否可以与指定的操作合并。
     * 会检查 null、[.FLAG_MERGE_TX] 以及数据库实例是否匹配。
     */
    fun isMergeableWith(other: OrmTask<T>?): Boolean {
        return other != null && isMergeTx && other.isMergeTx && getDatabase() == other.getDatabase()
    }

    @get:Throws(OrmTaskException::class)
    val duration: Long
        get() {
            if (timeCompleted == 0L) {
                throw OrmTaskException("This operation did not yet complete")
            } else {
                return timeCompleted - timeStarted
            }
        }

    val isFailed: Boolean
        get() = throwable != null

    /**
     * Waits until the operation is complete. If the thread gets interrupted, any
     * [InterruptedException] will be rethrown as a [OrmTaskException].
     * 简体中文：等待操作完成。如果线程被中断，任何 [InterruptedException] 都将被重新抛出为 [OrmTaskException]。
     *
     * @return Result if any, see [.getResult] 简体中文：结果（如果有），请参阅 [.getResult]。
     */
    @Synchronized
    @Throws(OrmTaskException::class)
    fun waitForCompletion(): Any? {
        lock.lock()
        try {
            while (!isCompleted) {
                try {
                    condition.await()
                } catch (e: InterruptedException) {
                    throw OrmTaskException("Interrupted while waiting for operation to complete.\n${e.message}")
                }
            }
            throwable?.let { throw OrmTaskException(this, it) }
            return result
        } finally {
            lock.unlock()
        }
    }

    /**
     * Waits until the operation is complete, but at most the given amount of milliseconds.If the
     * thread gets interrupted, any [InterruptedException] will be rethrown as a [OrmTaskException].
     * 简体中文：等待操作完成，但最多等待指定的毫秒数。如果线程被中断，任何 [InterruptedException] 都会被重新抛出
     * 为 [OrmTaskException]。
     *
     * @return true if the operation completed in the given time frame. 简体中文：如果操作在指定的时间范
     * 围内完成，则返回 true。
     */
    @Synchronized
    @Throws(OrmTaskException::class)
    fun waitForCompletion(maxMillis: Int): Boolean {
        lock.lock()
        try {
            if (!isCompleted) {
                try {
                    val completedInTime = condition.await(maxMillis.toLong(), TimeUnit.MILLISECONDS)
                    if (!completedInTime && !isCompleted) {
                        return false
                    }
                } catch (e: InterruptedException) {
                    throw OrmTaskException("Interrupted while waiting for operation to complete.\n${e.message}")
                }
            }
            return isCompleted
        } finally {
            lock.unlock()
        }
    }

    /**
     * Called when the operation is done. Notifies any threads waiting for this operation's completion.
     * 简体中文：在操作完成时调用，通知所有等待该操作完成的线程。
     */
    fun setCompleted() {
        lock.lock() // 获取锁
        try {
            isCompleted = true
            condition.signalAll()
        } finally {
            lock.unlock()
        }
    }

    val isCompletedSuccessfully: Boolean
        get() = isCompleted && throwable == null

    /**
     * Reset to prepare another execution run.
     * 简体中文：重置以准备另一次执行运行。
     */
    fun reset() {
        timeStarted = 0
        timeCompleted = 0
        isCompleted = false
        throwable = null
        result = null
        mergedTasksCount = 0
    }

    companion object {
        const val FLAG_MERGE_TX: Int = 1
        const val FLAG_STOP_QUEUE_ON_EXCEPTION: Int = 1 shl 1
        const val FLAG_TRACK_CREATOR_STACKTRACE: Int = 1 shl 2
    }
}
