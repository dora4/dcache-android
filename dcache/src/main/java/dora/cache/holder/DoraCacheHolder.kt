package dora.cache.holder

import dora.db.table.OrmTable
import dora.db.builder.Condition
import dora.db.builder.WhereBuilder
import dora.db.dao.DaoFactory
import dora.db.dao.OrmDao

class DoraCacheHolder<M, T : OrmTable>(var clazz: Class<out OrmTable>) : CacheHolder<M> {

    lateinit var dao: OrmDao<T>

    override fun init() {
        dao = DaoFactory.getDao(clazz) as OrmDao<T>
    }

    override fun queryCache(condition: Condition): M? {
        return dao.selectOne(WhereBuilder.create(condition)) as M?
    }

    override fun removeOldCache(condition: Condition) {
        dao.delete(WhereBuilder.create(condition))
    }

    override fun addNewCache(model: M) {
        dao.insert(model as T)
    }

    override fun queryCacheSize(condition: Condition): Long {
        return dao.selectCount(WhereBuilder.create(condition))
    }
}