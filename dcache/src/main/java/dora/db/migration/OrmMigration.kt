package dora.db.migration

import dora.db.dao.OrmDao
import dora.db.table.OrmTable
import java.io.Serializable

/**
 * 定义数据库版本升级时，数据迁移的抽象。
 */
open class OrmMigration(val fromVersion: Int, val toVersion: Int) : Serializable {

    open fun <T : OrmTable> migrate(dao: OrmDao<T>) : Boolean {
        return false
    }
}