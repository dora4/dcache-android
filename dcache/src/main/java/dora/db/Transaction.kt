package dora.db

import android.database.sqlite.SQLiteException
import dora.db.dao.DaoFactory
import dora.db.dao.OrmDao
import dora.db.table.OrmTable

/**
 * 事务操作。
 */
object Transaction {

    /**
     * 保持public，便于在block中直接使用。
     */
    val db = Orm.getDB()

    /**
     * 执行通用事务块。
     */
    fun <T> execute(block: Transaction.() -> T) : Any = apply {
        try {
            // 开始事务
            db.beginTransaction()
            // 执行事务操作
            block()
            // 所有操作都没有问题，成功执行完成，设置成功的标志位，否则回滚所有操作
            db.setTransactionSuccessful()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    /**
     * 执行单表事务块。
     */
    fun <T : OrmTable> execute(tableClass: Class<T>, block: Transaction.(dao: OrmDao<T>) -> Unit) :
            Any = apply {
        val dao = DaoFactory.getDao(tableClass)
        try {
            // 开始事务
            db.beginTransaction()
            // 执行事务操作
            block(dao)
            // 所有操作都没有问题，成功执行完成，设置成功的标志位，否则回滚所有操作
            db.setTransactionSuccessful()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }
}