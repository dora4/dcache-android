package dora.db.table

import dora.db.migration.OrmMigration

/**
 * Only classes that implement this interface can be operated on by [dora.db.dao.OrmDao].
 * 简体中文：实现此接口的类才能被[dora.db.dao.OrmDao]操作。
 */
interface OrmTable {

    /**
     * If true, it will drop table first and recreate the table when the table is
     * upgraded.Instead,it will expand directly on the previous table.
     * 简体中文：如果为 true，它将先删除表，然后在表升级时重新创建该表。相反，它将直接在原有表的基础上进行扩展。
     */
    val isUpgradeRecreated: Boolean

    /**
     * Use it to upgrade the ORM table data when [isUpgradeRecreated] is false.
     * 简体中文：在[isUpgradeRecreated]为false时使用它来升级ORM表数据。
     */
    val migrations: Array<OrmMigration>?

    companion object {

        /**
         * Used to create an auto-increment primary key for the table.It may be used when
         * [dora.db.constraint.AssignType] is set to [dora.db.constraint.AssignType.AUTO_INCREMENT].
         * 简体中文：用于创建表的自增主键。当[dora.db.constraint.AssignType]为
         * [dora.db.constraint.AssignType.AUTO_INCREMENT]值时可能会使用到它。
         */
        const val INDEX_ID = "_id"

        /**
         * When using Kotlin data class, if the primary key ID is of type
         * [dora.db.constraint.AssignType.AUTO_INCREMENT], you can use this default value so that
         * you don't have to pass this parameter value.
         * 简体中文：当使用kotlin data class时，主键ID如果是[dora.db.constraint.AssignType.AUTO_INCREMENT]
         * 类型，则你可以使用这个默认值，这样你可以不传递这个参数值。
         */
        const val ID_UNASSIGNED = 0L

        /**
         * Used for pagination caching.
         * 简体中文：用于分页缓存。
         */
        const val PAGINATION_KEY = "page_no"
    }
}