package dora.db

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import dora.db.dao.DaoFactory
import dora.db.dao.OrmDao
import dora.db.table.OrmTable

/**
 * Transaction operations ensure that multiple database actions either all succeed or, if any fail,
 * the system rolls back to the initial state.
 * 简体中文：事务操作，使多个数据库操作要么全部成功，要么则回滚至最初状态。
 */
object Transaction {

    /**
     * The [android.database.sqlite.SQLiteDatabase] object can be directly used in the
     * [execute] function.
     * 简体中文：[android.database.sqlite.SQLiteDatabase]对象，可以在[execute]函数中直接使用。
     */
    val db: SQLiteDatabase
        get() = Orm.getDB()

    /**
     * Execute a general transaction block.
     * 简体中文：执行通用事务块。
     */
    internal fun <T> execute(db: SQLiteDatabase, block: Transaction.() -> T) : Any = apply {
        try {
            // Begin the transaction.
            // 简体中文：开始事务
            db.beginTransaction()
            // Execute the transaction operation.
            // 简体中文：执行事务操作
            block()
            // Set the flag indicating that all operations were executed successfully.
            // 简体中文：设置所有操作执行成功的标志位
            db.setTransactionSuccessful()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        } finally {
            // End the transaction.
            // 简体中文：结束事务
            db.endTransaction()
        }
    }

    /**
     * Execute a general transaction block.
     * 简体中文：执行通用事务块。
     */
    fun <T> execute(block: Transaction.() -> T) : Any = apply {
        try {
            // Begin the transaction.
            // 简体中文：开始事务
            db.beginTransaction()
            // Execute the transaction operation.
            // 简体中文：执行事务操作
            block()
            // Set the flag indicating that all operations were executed successfully.
            // 简体中文：设置所有操作执行成功的标志位
            db.setTransactionSuccessful()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        } finally {
            // End the transaction.
            // 简体中文：结束事务
            db.endTransaction()
        }
    }

    /**
     * Execute a single-table transaction block.
     * 简体中文：执行单表事务块。
     */
    internal fun <T : OrmTable> execute(db: SQLiteDatabase, tableClass: Class<T>, block: Transaction.(dao: OrmDao<T>) -> Unit) :
            Any = apply {
        val dao = DaoFactory.getDao(tableClass)
        try {
            // Begin the transaction.
            // 简体中文：开始事务
            db.beginTransaction()
            // Execute the transaction operation.
            // 简体中文：执行事务操作
            block(dao)
            // Set the flag indicating that all operations were executed successfully.
            // 简体中文：设置所有操作执行成功的标志位
            db.setTransactionSuccessful()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        } finally {
            // End the transaction.
            // 简体中文：结束事务
            db.endTransaction()
        }
    }

    /**
     * Execute a single-table transaction block.
     * 简体中文：执行单表事务块。
     */
    fun <T : OrmTable> execute(tableClass: Class<T>, block: Transaction.(dao: OrmDao<T>) -> Unit) :
            Any = apply {
        val dao = DaoFactory.getDao(tableClass)
        try {
            // Begin the transaction.
            // 简体中文：开始事务
            db.beginTransaction()
            // Execute the transaction operation.
            // 简体中文：执行事务操作
            block(dao)
            // Set the flag indicating that all operations were executed successfully.
            // 简体中文：设置所有操作执行成功的标志位
            db.setTransactionSuccessful()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        } finally {
            // End the transaction.
            // 简体中文：结束事务
            db.endTransaction()
        }
    }
}