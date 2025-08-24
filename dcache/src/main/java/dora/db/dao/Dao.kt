package dora.db.dao

import dora.db.async.OrmTaskListener
import dora.db.table.OrmTable
import dora.db.builder.QueryBuilder
import dora.db.builder.WhereBuilder

/**
 * Used for abstracting database operations.
 * 简体中文：用于抽象数据库的操作。
 */
interface Dao<T : OrmTable> {

    /**
     * Insert a record.
     * 简体中文：插入一条数据。
     */
    fun insert(bean: T): Boolean

    /**
     * Insert a record.
     * 简体中文：插入一条数据。
     */
    fun insertAsync(bean: T, listener: OrmTaskListener<T>? = null)

    /**
     * Insert a record.
     * 简体中文：插入一条数据。
     */
    fun insertReturnId(bean: T): Long

    /**
     * Insert a record.
     * 简体中文：插入一条数据。
     */
    fun insertReturnIdAsync(bean: T, listener: OrmTaskListener<T>? = null)

    /**
     * Insert multiple records.
     * 简体中文：插入多条数据。
     */
    fun insert(beans: List<T>): Boolean

    /**
     * Insert multiple records.
     * 简体中文：插入多条数据。
     */
    fun insertAsync(beans: List<T>, listener: OrmTaskListener<T>? = null)

    /**
     * Delete data based on conditions.
     * 简体中文：按条件删除数据。
     */
    fun delete(builder: WhereBuilder): Boolean

    /**
     * Delete data based on conditions.
     * 简体中文：按条件删除数据。
     */
    fun deleteAsync(builder: WhereBuilder, listener: OrmTaskListener<T>? = null)

    /**
     * Delete a record.
     * 简体中文：删除一条数据。
     */
    fun delete(bean: T): Boolean

    /**
     * Delete a record.
     * 简体中文：删除一条数据。
     */
    fun deleteAsync(bean: T, listener: OrmTaskListener<T>? = null)

    /**
     * Delete all records.
     * 简体中文：删除所有数据。
     */
    fun deleteAll(): Boolean

    /**
     * Delete all records.
     * 简体中文：删除所有数据。
     */
    fun deleteAllAsync(listener: OrmTaskListener<T>? = null)

    /**
     * Query all data that meets the conditions; if any are found, update them to [newBean]; if
     * none are found, insert a new [newBean].
     * 简体中文：查询所有满足条件的数据，如果有，则全部更新为[newBean]，没有，则插入一个[newBean]。
     */
    fun insertOrUpdate(builder: WhereBuilder, newBean: T) : Boolean

    /**
     * Query all data that meets the conditions; if any are found, update them to [newBean]; if
     * none are found, insert a new [newBean].
     * 简体中文：查询所有满足条件的数据，如果有，则全部更新为[newBean]，没有，则插入一个[newBean]。
     */
    fun insertOrUpdateAsync(builder: WhereBuilder, newBean: T, listener: OrmTaskListener<T>? = null)

    /**
     * Insert or update data. If it exists, update it; if not, insert it.
     * 简体中文：插入或更新数据。如果有，则更新，没有，则插入。
     */
    fun insertOrUpdate(bean: T) : Boolean

    /**
     * Insert or update data. If it exists, update it; if not, insert it.
     * 简体中文：插入或更新数据。如果有，则更新，没有，则插入。
     */
    fun insertOrUpdateAsync(bean: T, listener: OrmTaskListener<T>? = null)

    /**
     * Insert or update data. If it exists, update it; if not, insert it.
     * 简体中文：插入或更新数据。如果有，则更新，没有，则插入。
     */
    fun insertOrUpdateReturnId(bean: T): Long

    /**
     * Insert or update data. If it exists, update it; if not, insert it.
     * 简体中文：插入或更新数据。如果有，则更新，没有，则插入。
     */
    fun insertOrUpdateReturnIdAsync(bean: T, listener: OrmTaskListener<T>? = null)

    /**
     * Update all data that meets the conditions to [newBean].
     * 简体中文：更新所有满足条件的数据为[newBean]。
     */
    fun update(builder: WhereBuilder, newBean: T): Boolean

    /**
     * Update all data that meets the conditions to [newBean].
     * 简体中文：更新所有满足条件的数据为[newBean]。
     */
    fun updateAsync(builder: WhereBuilder, newBean: T, listener: OrmTaskListener<T>? = null)

    /**
     * Update a record.
     * 简体中文：更新一条数据。
     */
    fun update(bean: T): Boolean

    /**
     * Update a record.
     * 简体中文：更新一条数据。
     */
    fun updateAsync(bean: T, listener: OrmTaskListener<T>? = null)

    /**
     * Query all data in the table.
     * 简体中文：查询该表中的所有数据。
     */
    fun selectAll(): List<T>

    /**
     * Query all data in the table.
     * 简体中文：查询该表中的所有数据。
     */
    fun selectAllAsync(listener: OrmTaskListener<T>? = null)

    /**
     * Query data based on conditions.
     * 简体中文：按条件查询数据。
     */
    fun select(builder: WhereBuilder): List<T>

    /**
     * Query data based on conditions.
     * 简体中文：按条件查询数据。
     */
    fun selectAsync(builder: WhereBuilder, listener: OrmTaskListener<T>? = null)

    /**
     * Query data based on conditions.
     * 简体中文：按条件查询数据。
     */
    fun select(builder: QueryBuilder): List<T>

    /**
     * Query data based on conditions.
     * 简体中文：按条件查询数据。
     */
    fun selectAsync(builder: QueryBuilder, listener: OrmTaskListener<T>? = null)

    /**
     * Query a specific record.
     * 简体中文：查询特定的一条数据。
     */
    fun selectOne(): T?

    /**
     * Query a specific record.
     * 简体中文：查询特定的一条数据。
     */
    fun selectOneAsync(listener: OrmTaskListener<T>? = null)

    /**
     * Query the record that matches the conditions.
     * 简体中文：查询符合条件的一条数据。
     */
    fun selectOne(builder: WhereBuilder): T?

    /**
     * Query the record that matches the conditions.
     * 简体中文：查询符合条件的一条数据。
     */
    fun selectOneAsync(builder: WhereBuilder, listener: OrmTaskListener<T>? = null)

    /**
     * Query the record that matches the conditions.
     * 简体中文：查询符合条件的一条数据。
     */
    fun selectOne(builder: QueryBuilder): T?

    /**
     * Query the record that matches the conditions.
     * 简体中文：查询符合条件的一条数据。
     */
    fun selectOneAsync(builder: QueryBuilder, listener: OrmTaskListener<T>? = null)

    /**
     * Query the total count of records. Deprecated.
     * 简体中文：查询数据总数。过时。
     */
    @Deprecated(
        message =
        "Please use count() instead.",
        level = DeprecationLevel.ERROR
    )
    fun selectCount(): Long

    /**
     * Query the total count of records.
     * 简体中文：查询数据总数。
     */
    fun count(): Long

    /**
     * Query the total count of records.
     * 简体中文：查询数据总数。
     */
    fun countAsync(listener: OrmTaskListener<T>? = null)

    /**
     * Query the total count of records that matches the conditions. Deprecated.
     * 简体中文：查询符合条件的数据总数。过时。
     */
    @Deprecated(
        message =
        "Please use count(builder: WhereBuilder) instead.",
        level = DeprecationLevel.ERROR
    )
    fun selectCount(builder: WhereBuilder): Long

    /**
     * Query the total count of records that matches the conditions.
     * 简体中文：查询符合条件的数据总数。
     */
    fun count(builder: WhereBuilder): Long

    /**
     * Query the total count of records that matches the conditions.
     * 简体中文：查询符合条件的数据总数。
     */
    fun countAsync(builder: WhereBuilder, listener: OrmTaskListener<T>? = null)

    /*
     * Query the total count of records that matches the conditions. Deprecated.
     * 简体中文：查询符合条件的数据总数。过时。
     */
    @Deprecated(
        message =
        "Please use count(builder: QueryBuilder) instead.",
        level = DeprecationLevel.ERROR
    )
    fun selectCount(builder: QueryBuilder): Long

    /**
     * Query the total count of records that matches the conditions.
     * 简体中文：查询符合条件的数据总数。
     */
    fun count(builder: QueryBuilder): Long

    /**
     * Query the total count of records that matches the conditions.
     * 简体中文：查询符合条件的数据总数。
     */
    fun countAsync(builder: QueryBuilder, listener: OrmTaskListener<T>? = null)

    /**
     * Add a new column to the table.
     * 简体中文：向表中添加新列。
     */
    fun addColumn(fieldName: String) : Boolean

    /**
     * Add a new column to the table.
     * 简体中文：向表中添加新列。
     */
    fun addColumnAsync(fieldName: String, listener: OrmTaskListener<T>? = null)

    /**
     * Rename a column in the table.
     * 简体中文：重命名表中的列。
     */
    fun renameColumn(fieldName: String, oldColumnName: String) : Boolean

    /**
     * Rename a column in the table.
     * 简体中文：重命名表中的列。
     */
    fun renameColumnAsync(fieldName: String, oldColumnName: String, listener: OrmTaskListener<T>? = null)

    /**
     * Rename the table.
     * 简体中文：重命名表。
     */
    fun renameTable(oldTableName: String) : Boolean

    /**
     * Rename the table.
     * 简体中文：重命名表。
     */
    fun renameTableAsync(oldTableName: String, listener: OrmTaskListener<T>? = null)

    /**
     * Drop the table.
     * 简体中文：删除表。
     */
    fun drop() : Boolean

    /**
     * Drop the table.
     * 简体中文：删除表。
     */
    fun dropAsync(listener: OrmTaskListener<T>? = null)
}
