package dora.db.migration

import dora.db.dao.OrmDao
import dora.db.table.OrmTable

open class OrmMigration(val fromVersion: Int, val toVersion: Int) {

    open fun <T : OrmTable> migrate(dao: OrmDao<T>) : Boolean {
        return false
    }
}