package dora.db.exception

import dora.db.OrmLog
import dora.db.async.OrmTask
import java.sql.SQLException

class OrmTaskException : SQLException {

    private var failedTask: OrmTask<*>? = null

    constructor(failedTask: OrmTask<*>?) {
        this.failedTask = failedTask
    }

    constructor(error: String, failedTask: OrmTask<*>?) : super(error) {
        this.failedTask = failedTask
    }

    constructor(error: String) : super(error) {
    }

    constructor(error: String, cause: Throwable, failedTask: OrmTask<*>?) : super(error) {
        this.failedTask = failedTask
        safeInitCause(cause)
    }

    constructor(failedTask: OrmTask<*>, th: Throwable) {
        this.failedTask = failedTask
        safeInitCause(th)
    }

    constructor(failedTask: OrmTask<*>, e: Exception) {
        this.failedTask = failedTask
        safeInitCause(e)
    }

    private fun safeInitCause(cause: Throwable) {
        try {
            initCause(cause)
        } catch (e: Throwable) {
            OrmLog.e("Could not set initial cause.\n$e")
            OrmLog.e("Initial cause is:$cause")
        }
    }
}
