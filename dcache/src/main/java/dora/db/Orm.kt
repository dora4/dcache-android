package dora.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import dora.db.exception.OrmStateException
import dora.db.table.OrmTable

/**
 * ORM框架入口。
 */
object Orm {

    private var database: SQLiteDatabase? = null
    private var dbHelper: SQLiteOpenHelper? = null
    private const val STATE_DATABASE_NOT_EXISTS = -1
    private const val STATE_DATABASE_EXISTS = 0
    private var dbState = STATE_DATABASE_NOT_EXISTS

    fun getDB() : SQLiteDatabase {
        if (isPrepared()) {
            return database!!
        }
        throw OrmStateException("Database is not exists.")
    }

    fun isPrepared() : Boolean {
        return dbState == STATE_DATABASE_EXISTS
    }

    @Synchronized
    fun init(context: Context, databaseName: String) {
        dbHelper = OrmSQLiteOpenHelper(context, databaseName, 1, null)
        database = dbHelper!!.writableDatabase
        dbState = STATE_DATABASE_EXISTS
        dbHelper!!.onCreate(database)
    }

    @Synchronized
    fun init(context: Context, config: OrmConfig) {
        val name: String = config.databaseName
        val versionCode: Int = config.versionCode
        val tables: Array<Class<out OrmTable>>? = config.tables
        dbHelper = OrmSQLiteOpenHelper(context, name, versionCode, tables)
        database = dbHelper!!.writableDatabase
        dbState = STATE_DATABASE_EXISTS
        dbHelper!!.onCreate(database)
    }
}