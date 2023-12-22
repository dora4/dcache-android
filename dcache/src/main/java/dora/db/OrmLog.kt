package dora.db

import android.util.Log

/**
 * ORM日志打印，ORM框架内部的日志全部通过这个类输出。
 */
object OrmLog {

    /**
     * 日志打印标志位。
     */
    private var ORM_DEBUG = true

    /**
     * 日志过滤标签。
     */
    private const val TAG = "dora-db"

    /**
     * 是否开启全局日志打印。
     */
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