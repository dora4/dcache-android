package dora.db.table

import dora.db.migration.OrmMigration

/**
 * 所有数据表的类都要实现此接口。
 */
interface OrmTable {

    /**
     * Gets the unique identifier's value.
     *
     * @return The primary key value.
     */
    val primaryKey: PrimaryKeyEntry

    /**
     * @return If true, it will drop table first and recreate the table when the table is
     * upgraded.Instead,it will expand directly on the previous table.
     */
    val isUpgradeRecreated: Boolean

    /**
     * Upgrade orm table data while isUpgradeRecreated is false.
     */
    val migrations: Array<OrmMigration>?

    companion object {

        const val INDEX_ID = "_id"
    }
}