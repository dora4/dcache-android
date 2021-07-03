package dora.db

import android.database.sqlite.SQLiteException
import dora.db.dao.DaoFactory
import dora.db.dao.OrmDao
import java.lang.IllegalArgumentException

object Transaction {

    val db = Orm.getDB()

    fun execute(block: Transaction.() -> Unit) : Transaction = apply {
        try {
            db.beginTransaction()
            block()
            db.setTransactionSuccessful()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    fun <T : OrmTable> execute(tableClass: Class<T>, block: Transaction.(dao: OrmDao<T>) -> Unit) :
            Transaction = apply {
        val dao = DaoFactory.getDao(tableClass) ?: throw IllegalArgumentException("DAO not found.")
        try {
            db.beginTransaction()
            block(dao!!)
            db.setTransactionSuccessful()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }
}