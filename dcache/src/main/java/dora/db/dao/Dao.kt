package dora.db.dao

import dora.db.table.OrmTable
import dora.db.builder.QueryBuilder
import dora.db.builder.WhereBuilder

interface Dao<T : OrmTable> {

    /**
     * 插入一条数据。
     */
    fun insert(bean: T): Boolean

    /**
     * 插入多条数据。
     */
    fun insert(beans: List<T>): Boolean

    /**
     * 按条件删除数据。
     */
    fun delete(builder: WhereBuilder): Boolean

    /**
     * 删除一条数据。
     */
    fun delete(bean: T): Boolean

    /**
     * 删除所有数据。
     */
    fun deleteAll(): Boolean

    /**
     * 插入或更新数据。如果有，则更新，没有，则插入。
     */
    fun insertOrUpdate(bean: T) : Boolean

    /**
     * 将满足条件的所有数据更新。
     */
    fun update(builder: WhereBuilder, newBean: T): Boolean

    /**
     * 更新一条数据。
     */
    fun update(bean: T): Boolean

    /**
     * 更新所有数据。过时，很少将数据更新成相同的，除非是重置操作。
     */
    @Deprecated(
        message =
        "防止错误调用污染数据，请使用update(builder: WhereBuilder, newBean: T)替代",
        level = DeprecationLevel.WARNING
    )
    fun updateAll(newBean: T): Boolean

    /**
     * 查询所有数据。
     */
    fun selectAll(): List<T>

    /**
     * 按条件查询数据。
     */
    fun select(builder: WhereBuilder): List<T>

    /**
     * 按条件查询数据。
     */
    fun select(builder: QueryBuilder): List<T>

    /**
     * 查询唯一一条数据。
     */
    fun selectOne(): T?

    /**
     * 查询最符合条件的一条数据。
     */
    fun selectOne(builder: WhereBuilder): T?

    /**
     * 查询最符合条件的一条数据。
     */
    fun selectOne(builder: QueryBuilder): T?

    /**
     * 查询数据总数。
     */
    fun selectCount(): Long

    /**
     * 查询符合条件的数据总数。过时。
     */
    @Deprecated(
        message =
        "请使用count(builder: WhereBuilder)替代",
        level = DeprecationLevel.ERROR
    )
    fun selectCount(builder: WhereBuilder): Long

    /**
     * 查询符合条件的数据总数。
     */
    fun count(builder: WhereBuilder): Long

    /**
     * 查询符合条件的数据总数。过时。
     */
    @Deprecated(
        message =
        "请使用count(builder: QueryBuilder)替代",
        level = DeprecationLevel.ERROR
    )
    fun selectCount(builder: QueryBuilder): Long

    /**
     * 查询符合条件的数据总数。
     */
    fun count(builder: QueryBuilder): Long
}