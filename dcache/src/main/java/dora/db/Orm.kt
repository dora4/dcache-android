package dora.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import dora.db.exception.OrmStateException
import dora.db.table.OrmTable

/**
 * Use it to configure the ORM framework.Call [Orm.init] during application initialization to
 * complete the configuration.
 * 简体中文：使用它对ORM框架进行配置。在应用初始化时调用[Orm.init]即可完成配置。
 */
object Orm {

    private var database: SQLiteDatabase? = null
    private var dbHelper: SQLiteOpenHelper? = null
    private const val STATE_DATABASE_NOT_EXISTS = -1
    private const val STATE_DATABASE_EXISTS = 0
    private var dbState = STATE_DATABASE_NOT_EXISTS

    @Synchronized
    fun init(context: Context, databaseName: String) {
        prepare(OrmSQLiteOpenHelper(context, databaseName, 1, null))
    }

    @Synchronized
    fun init(context: Context, config: OrmConfig) {
        val name: String = config.databaseName
        val versionCode: Int = config.versionCode
        val tables: Array<Class<out OrmTable>>? = config.tables
        prepare(context, name, versionCode, tables)
    }

    fun getDB() : SQLiteDatabase {
        if (isPrepared()) {
            return database!!
        }
        throw OrmStateException("Database is not exists.")
    }

    private fun prepare(helper: OrmSQLiteOpenHelper) {
        dbHelper = helper
        database = helper.writableDatabase
        dbState = STATE_DATABASE_EXISTS
        dbHelper!!.onCreate(database)
    }

    private fun prepare(
        context: Context,
        name: String,
        versionCode: Int,
        tables: Array<Class<out OrmTable>>?
    ) {
        dbHelper = OrmSQLiteOpenHelper(context, name, versionCode, tables)
        prepare(dbHelper as OrmSQLiteOpenHelper)
    }

    fun isPrepared() : Boolean {
        return dbState == STATE_DATABASE_EXISTS
    }
}