package dora.db.async

import dora.db.table.OrmTable

interface OrmTaskListener {

    fun onCompleted(task: OrmTask<out OrmTable>)

    fun onFailed(task: OrmTask<out OrmTable>, e: Exception)
}
