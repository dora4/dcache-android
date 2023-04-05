package dora.db.table

interface OrmTable {

    /**
     * Gets the unique identifier's value.
     *
     * @return The primary key value.
     */
    fun getPrimaryKey(): PrimaryKeyEntry

    /**
     * @return If true, it will drop table first and recreate the table when the table is
     * upgraded.Instead,it will expand directly on the previous table.
     */
    fun isUpgradeRecreated(): Boolean

    companion object {

        const val INDEX_ID = "_id"
    }
}