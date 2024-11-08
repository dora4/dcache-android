package dora.db

import android.content.Context
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import dora.db.dao.OrmDao
import dora.db.exception.OrmMigrationException
import dora.db.migration.OrmMigration
import dora.db.table.OrmTable
import dora.db.table.TableManager
import java.lang.reflect.InvocationTargetException

/**
 * A helper class to manage database creation and version management.You need to create all tables
 * in the [Orm.init] function and it is not recommended to create tables using [TableManager].
 * If you need to upgrade the table structure, use an array of [OrmMigration] to specify each
 * version change.
 * 简体中文：一个帮助类，用于管理数据库的创建和版本管理。你需要在[Orm.init]函数中创建所有表，不建议使用
 * [TableManager]自行创建表。如果你需要升级数据的表结构，需要使用[OrmMigration]的数组指定每一次版本的变动。
 */
class OrmSQLiteOpenHelper(context: Context, name: String, version: Int,
                          private val tables: Array<Class<out OrmTable>>?) :
        SQLiteOpenHelper(context, name, null, version, DatabaseErrorHandler {
            dbObj -> OrmLog.e(dbObj.toString()) }) {

    override fun onCreate(db: SQLiteDatabase) {
        if (!tables.isNullOrEmpty()) {
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
        if (!tables.isNullOrEmpty() && newVersion > oldVersion) {
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
                        var curVersion = oldVersion
                        if (it.migrations == null) {
                            return@let
                        }
                        for (migration in it.migrations!!) {
                            if (migration.fromVersion >= migration.toVersion) {
                                throw OrmMigrationException(
                                    "fromVersion can't be more than toVersion," +
                                            "either fromVersion can't be equal to toVersion."
                                )
                            }
                            if (migration.toVersion <= curVersion) {
                                continue
                            }
                            if (migration.fromVersion == curVersion) {
                                Orm.prepare()
                                val ok = Transaction.execute(it.javaClass as Class<out OrmTable>) { dao ->
                                    migration.migrate(dao)
                                } as Boolean
                                if (ok) {
                                    curVersion = migration.toVersion
                                    OrmLog.d("${it.javaClass.name}'s version has succeeded upgraded to $curVersion")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}