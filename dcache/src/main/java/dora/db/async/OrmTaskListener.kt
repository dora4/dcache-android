package dora.db.async

import dora.db.exception.OrmTaskException
import dora.db.table.OrmTable

interface OrmTaskListener<T : OrmTable> {

    fun onCompleted(task: OrmTask<T>)

    fun onFailed(task: OrmTask<T>, e: OrmTaskException)
}
