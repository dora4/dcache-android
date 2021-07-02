package dora.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import dora.db.exception.OrmStateException

object Orm {
    private lateinit var sDatabase: SQLiteDatabase
    private var sHelper: SQLiteOpenHelper? = null
    private const val STATE_DATABASE_NOT_EXISTS = -1
    private const val STATE_DATABASE_EXISTS = 0
    private const val STATE_DATABASE_UPDATING = 1
    private var sDatabaseState = STATE_DATABASE_NOT_EXISTS
    val isPrepared: Boolean
        get() = sDatabaseState == STATE_DATABASE_EXISTS

    val isWaitingUpdate: Boolean
        get() = sDatabaseState == STATE_DATABASE_UPDATING

    @JvmStatic
    fun update() {
        sDatabaseState = STATE_DATABASE_UPDATING
    }

    @JvmStatic
    val database: SQLiteDatabase
        get() = if (isPrepared) {
            sDatabase
        } else if (isWaitingUpdate) {
            sDatabase = sHelper!!.writableDatabase
            if (sDatabase != null) {
                sDatabaseState = STATE_DATABASE_EXISTS
            }
            sDatabase
        } else {
            throw OrmStateException("Database is not exists.")
        }

    @Synchronized
    fun init(context: Context, databaseName: String) {
        sHelper = OrmSQLiteOpenHelper(context, databaseName, 1, null)
        sDatabase = sHelper!!.writableDatabase
        if (sDatabase != null) {
            sDatabaseState = STATE_DATABASE_EXISTS
        }
    }

    @Synchronized
    fun init(context: Context, config: OrmConfig) {
        val name: String = config.databaseName
        val versionCode: Int = config.versionCode
        val tables: Array<Class<out OrmTable>>? = config.tables
        sHelper = OrmSQLiteOpenHelper(context, name, versionCode, tables)
        sDatabase = sHelper!!.writableDatabase
        if (sDatabase != null) {
            sDatabaseState = STATE_DATABASE_EXISTS
        }
    }
}