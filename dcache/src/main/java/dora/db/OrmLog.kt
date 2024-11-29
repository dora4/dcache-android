package dora.db

import android.util.Log

/**
 * The ORM module log printer outputs all internal logs of the ORM framework through this class.
 * 简体中文：ORM模块日志打印器，ORM框架内部的日志全部通过这个类输出。
 */
object OrmLog {

    /**
     * Log printing flag.
     * 简体中文：日志打印标志位。
     */
    @JvmStatic
    private var ORM_DEBUG = true

    /**
     * Use this tag to filter ORM-related logs.
     * 简体中文：用此标签来过滤ORM相关的日志。
     */
    private const val TAG = "dora-db"

    /**
     * Whether to enable the printing of ORM logs.
     * 简体中文：是否开启ORM日志的打印。
     */
    @JvmStatic
    fun setDebugMode(debugMode: Boolean) {
        ORM_DEBUG = debugMode
    }

    @JvmStatic
    fun i(msg: String) {
        if (ORM_DEBUG) {
            Log.i(TAG, msg)
        }
    }

    @JvmStatic
    fun w(msg: String) {
        if (ORM_DEBUG) {
            Log.w(TAG, msg)
        }
    }

    @JvmStatic
    fun d(msg: String) {
        if (ORM_DEBUG) {
            Log.d(TAG, msg)
        }
    }

    @JvmStatic
    fun e(msg: String) {
        if (ORM_DEBUG) {
            Log.e(TAG, msg)
        }
    }

    @JvmStatic
    fun v(msg: String) {
        if (ORM_DEBUG) {
            Log.v(TAG, msg)
        }
    }
}