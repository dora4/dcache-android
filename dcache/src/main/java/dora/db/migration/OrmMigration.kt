package dora.db.migration

import dora.db.dao.OrmDao
import dora.db.table.OrmTable
import java.io.Serializable

/**
 * Used to define all operations during data migration when upgrading the database version. For
 * example, val MIGRATION_1_2 = OrmMigration(1, 2).
 * 简体中文：用于定义数据库版本升级时，数据迁移时的所有操作。例如val MIGRATION_1_2 = OrmMigration(1, 2)。
 *
 * @see OrmTable
 */
open class OrmMigration(val fromVersion: Int, val toVersion: Int) : Serializable {

    /**
     * Used when upgrading by a single version only.
     * 简体中文：只升一个版本时使用。
     */
    constructor(fromVersion: Int) : this(fromVersion, fromVersion + 1)

    /**
     * @see [dora.db.table.TableManager.createTable]
     * @see [dora.db.dao.OrmDao.addColumn]
     * @see [dora.db.dao.OrmDao.renameColumn]
     */
    open fun <T : OrmTable> migrate(dao: OrmDao<T>) : Boolean {
        return false
    }
}