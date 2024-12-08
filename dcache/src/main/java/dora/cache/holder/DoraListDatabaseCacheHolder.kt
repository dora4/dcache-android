package dora.cache.holder

import dora.db.table.OrmTable
import dora.db.builder.Condition
import dora.db.builder.QueryBuilder
import dora.db.builder.WhereBuilder
import dora.db.dao.DaoFactory
import dora.db.dao.OrmDao

/**
 * Built-in ListCacheHolder, default implementation.
 * 简体中文：内置的ListCacheHolder，默认实现。
 */
class DoraListDatabaseCacheHolder<T : OrmTable>(var clazz: Class<T>)
    : ListDatabaseCacheHolder<T>() {

    private lateinit var dao: OrmDao<T>

    override fun init() {
        // Create an OrmDao of the specified type.
        // 简体中文：创建指定类型的OrmDao
        dao = DaoFactory.getDao(clazz)
    }

    override fun queryCache(condition: Condition): MutableList<T>? {
        return dao.select(QueryBuilder.create(condition)) as MutableList<T>?
    }

    override fun removeOldCache(condition: Condition) {
        dao.delete(WhereBuilder.create(condition))
    }

    override fun addNewCache(models: MutableList<T>) {
        dao.insert(models)
    }

    override fun queryCacheSize(condition: Condition): Long {
        // Note that QueryBuilder is not used here because it only assesses the number of cached
        // data entries and does not involve limit filtering, order by sorting, or other operations;
        // it is solely for pagination caching.
        // 简体中文：注意这里没有用QueryBuilder，因为只是对缓存数据的数量进行评估，并不会牵扯到limit过滤、order
        // by排序等操作，仅用于分页缓存
        return dao.count(WhereBuilder.create(condition))
    }
}