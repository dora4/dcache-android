package dora.cache.holder

import dora.db.table.OrmTable
import dora.db.builder.Condition
import dora.db.builder.QueryBuilder
import dora.db.builder.WhereBuilder
import dora.db.dao.DaoFactory
import dora.db.dao.OrmDao

class DoraDatabaseCacheHolder<T : OrmTable>(val clazz: Class<out OrmTable>) : DatabaseCacheHolder<T> {

    private lateinit var dao: OrmDao<T>

    override fun init() {
        // 创建指定类型的OrmDao
        dao = DaoFactory.getDao(clazz) as OrmDao<T>
    }

    override fun queryCache(condition: Condition): T? {
        return dao.selectOne(WhereBuilder.create(condition)) as T?
    }

    override fun removeOldCache(condition: Condition) {
        dao.delete(WhereBuilder.create(condition))
    }

    override fun addNewCache(model: T) {
        dao.insert(model)
    }

    override fun queryCacheSize(condition: Condition): Long {
        return dao.count(WhereBuilder.create(condition))
    }
}