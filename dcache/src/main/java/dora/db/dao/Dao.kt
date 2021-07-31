package dora.db.dao

import dora.db.table.OrmTable
import dora.db.builder.QueryBuilder
import dora.db.builder.WhereBuilder

interface Dao<T : OrmTable> {
    fun insert(bean: T): Boolean
    fun insert(beans: List<T>): Boolean
    fun delete(builder: WhereBuilder): Boolean
    fun delete(bean: T): Boolean
    fun deleteAll(): Boolean
    fun update(builder: WhereBuilder, newBean: T): Boolean
    fun update(bean: T): Boolean

    @Deprecated("")
    fun updateAll(newBean: T): Boolean
    fun selectAll(): List<T>
    fun select(builder: WhereBuilder): List<T>
    fun select(builder: QueryBuilder): List<T>
    fun selectOne(): T?
    fun selectOne(builder: WhereBuilder): T?
    fun selectOne(builder: QueryBuilder): T?
    fun selectCount(): Long
    fun selectCount(builder: WhereBuilder): Long
    fun selectCount(builder: QueryBuilder): Long
}