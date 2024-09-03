package dora.cache.holder

import dora.db.table.OrmTable
import dora.db.builder.Condition
import dora.db.builder.QueryBuilder
import dora.db.builder.WhereBuilder
import dora.db.dao.DaoFactory
import dora.db.dao.OrmDao

/**
 * 内置的ListCacheHolder，默认实现。
 */
class DoraListDatabaseCacheHolder<T : OrmTable>(var clazz: Class<out OrmTable>)
    : ListDatabaseCacheHolder<T>() {

    private lateinit var dao: OrmDao<T>

    override fun init() {
        // 创建指定类型的OrmDao
        dao = DaoFactory.getDao(clazz) as OrmDao<T>
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
        // 注意这里没有用QueryBuilder，因为只是对缓存数据的数量进行评估，并不会牵扯到limit过滤、order by排序等
        // 操作，仅用于分页缓存
        return dao.count(WhereBuilder.create(condition))
    }
}