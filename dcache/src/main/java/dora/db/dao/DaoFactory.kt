package dora.db.dao

import dora.db.table.OrmTable
import java.lang.IllegalArgumentException
import java.util.*

object DaoFactory {

    private val DAO_MAP: MutableMap<Class<out OrmTable>, OrmDao<*>> = HashMap()
    private val LOCK = Any()

    fun <T : OrmTable> removeDao(beanClass: Class<T>) {
        synchronized(DaoFactory::class.java) {
            if (DAO_MAP.containsKey(beanClass)) {
                DAO_MAP.remove(beanClass)
            }
        }
    }

    fun <T : OrmTable> getDao(beanClass: Class<T>): OrmDao<T> {
        synchronized(LOCK) {
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
        synchronized(LOCK) {
            try {
                return getDao(Class.forName(className) as Class<T>)
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
        }
        throw IllegalArgumentException("DAO not found")
    }

    fun <T : OrmTable> getDao(bean: T): OrmDao<T> {
        synchronized(LOCK) { return getDao(bean.javaClass) as OrmDao<T> }
    }
}