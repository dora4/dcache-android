package dora.db

import android.util.Log

object OrmLog {

    private var ORM_DEBUG = true
    private const val TAG = "dora-db"

    fun setDebugMode(debugMode: Boolean) {
        ORM_DEBUG = debugMode
    }

    fun i(msg: String) {
        if (ORM_DEBUG) {
            Log.i(TAG, msg)
        }
    }

    fun w(msg: String) {
        if (ORM_DEBUG) {
            Log.w(TAG, msg)
        }
    }

    fun d(msg: String) {
        if (ORM_DEBUG) {
            Log.d(TAG, msg)
        }
    }

    fun e(msg: String) {
        if (ORM_DEBUG) {
            Log.e(TAG, msg)
        }
    }

    fun v(msg: String) {
        if (ORM_DEBUG) {
            Log.v(TAG, msg)
        }
    }
}