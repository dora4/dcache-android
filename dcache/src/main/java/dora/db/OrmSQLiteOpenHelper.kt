package dora.db

import android.content.Context
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import dora.db.table.OrmTable
import dora.db.table.TableManager
import java.lang.reflect.InvocationTargetException

class OrmSQLiteOpenHelper(context: Context, name: String, version: Int,
                          private val tables: Array<Class<out OrmTable>>?) :
        SQLiteOpenHelper(context, name, null, version, DatabaseErrorHandler {
            dbObj -> OrmLog.e(dbObj.toString()) }) {

    override fun onCreate(db: SQLiteDatabase) {
        if (tables != null && tables.isNotEmpty()) {
            for (table in tables) {
                TableManager.createTable(table)
            }
        }
    }

    private fun <T> newOrmTableInstance(clazz: Class<T>): T? {
        val constructors = clazz.declaredConstructors
        for (c in constructors) {
            c.isAccessible = true
            val cls = c.parameterTypes
            if (cls.isEmpty()) {
                try {
                    return c.newInstance() as T
                } catch (e: InstantiationException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                } catch (e: InvocationTargetException) {
                    e.printStackTrace()
                }
            } else {
                val objs = arrayOfNulls<Any>(cls.size)
                for (i in cls.indices) {
                    objs[i] = getPrimitiveDefaultValue(cls[i])
                }
                try {
                    return c.newInstance(*objs) as T
                } catch (e: InstantiationException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                } catch (e: InvocationTargetException) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    private fun getPrimitiveDefaultValue(clazz: Class<*>): Any? {
        return if (clazz.isPrimitive) {
            if (clazz == Boolean::class.javaPrimitiveType) false else 0
        } else null
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (tables != null && tables.isNotEmpty() && newVersion > oldVersion) {
            for (i in tables.indices) {
                val table = tables[i]
                val ormTable: OrmTable? = newOrmTableInstance(tables[i])
                ormTable?.let {
                    val isRecreated = it.isUpgradeRecreated
                    if (isRecreated) {
                        Transaction.execute {
                            TableManager.dropTable(table)
                            TableManager.createTable(table)
                        }
                    } else {
                        TableManager.upgradeTable(table)
                    }
                }
            }
        }
    }
}