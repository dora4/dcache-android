package dora.db.async

import android.database.sqlite.SQLiteDatabase
import dora.db.dao.OrmDao
import dora.db.exception.OrmTaskException
import dora.db.table.OrmTable

open class OrmTask<T : OrmTable> internal constructor(
    val type: Type, val dao: OrmDao<T>,
    val parameter: Any? = null, val flags: Int
) {

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
    val creatorStacktrace: Exception?

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

    init {
        creatorStacktrace =
            if ((flags and FLAG_TRACK_CREATOR_STACKTRACE) != 0) Exception("OrmTask was created here") else null
    }

    /**
     * The operation's result after it has completed. Waits until a result is available.
     *
     * @return The operation's result or null if the operation type does not produce any result.
     * @throws [OrmTaskException] if the operation produced an exception
     * @see .waitForCompletion
     */
    @Synchronized
    @Throws(OrmTaskException::class)
    fun getResult(): Any? {
        if (!isCompleted) {
            waitForCompletion()
        }
        if (throwable != null) {
            throw OrmTaskException(this, throwable!!)
        }
        return result
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
    fun isMergeableWith(other: OrmTask<*>?): Boolean {
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
        while (!isCompleted) {
            try {
                (this as Object).wait()
            } catch (e: InterruptedException) {
                throw OrmTaskException("Interrupted while waiting for operation to complete.\n$e")
            }
        }
        return result
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
        if (!isCompleted) {
            try {
                (this as Object).wait(maxMillis.toLong())
            } catch (e: InterruptedException) {
                throw OrmTaskException("Interrupted while waiting for operation to complete.\n$e")
            }
        }
        return isCompleted
    }

    /**
     * Called when the operation is done. Notifies any threads waiting for this operation's completion.
     * 简体中文：在操作完成时调用，通知所有等待该操作完成的线程。
     */
    @Synchronized
    fun setCompleted() {
        isCompleted = true
        (this as Object).notifyAll()
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
