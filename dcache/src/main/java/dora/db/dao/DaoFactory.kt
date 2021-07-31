package dora.db.dao

import dora.db.table.OrmTable
import java.lang.IllegalArgumentException
import java.util.*

object DaoFactory {

    private val DAO_MAP: MutableMap<Class<out OrmTable>, OrmDao<*>> = HashMap()
    private val LOCK1 = Any()
    private val LOCK2 = Any()
    private val LOCK3 = Any()

    fun <T : OrmTable> removeDao(beanClass: Class<T>) {
        synchronized(DaoFactory::class.java) {
            if (DAO_MAP.containsKey(beanClass)) {
                DAO_MAP.remove(beanClass)
            }
        }
    }

    fun <T : OrmTable> getDao(beanClass: Class<T>): OrmDao<T> {
        synchronized(LOCK1) {
            return if (DAO_MAP.containsKey(beanClass)) {
                DAO_MAP.get(beanClass) as OrmDao<T>
            } else {
                val dao = OrmDao<T>(beanClass)
                DAO_MAP[beanClass] = dao
                dao
            }
        }
    }

    fun <T : OrmTable> getDao(className: String): OrmDao<T> {
        synchronized(LOCK2) {
            try {
                return getDao(Class.forName(className) as Class<T>)
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
        }
        throw IllegalArgumentException("DAO not found")
    }

    fun <T : OrmTable> getDao(bean: T): OrmDao<T> {
        synchronized(LOCK3) { return getDao(bean.javaClass) as OrmDao<T> }
    }
}