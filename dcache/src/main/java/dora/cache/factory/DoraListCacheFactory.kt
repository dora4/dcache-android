package dora.cache.factory

import dora.db.OrmTable
import dora.db.builder.Condition
import dora.db.builder.WhereBuilder
import dora.db.dao.DaoFactory
import dora.db.dao.OrmDao

class DoraListCacheFactory<M, T : OrmTable>(var clazz: Class<out OrmTable>) : ListCacheFactory<M>() {

    lateinit var dao: OrmDao<T>

    override fun queryCache(condition: Condition): List<M>? {
        return dao.select(WhereBuilder.create(condition)) as List<M>?
    }

    override fun removeOldCache(condition: Condition) {
        dao.delete(WhereBuilder.create(condition))
    }

    override fun init() {
        dao = DaoFactory.getDao(clazz) as OrmDao<T>
    }

    override fun addNewCache(models: List<M>) {
        dao.insert(models as List<T>)
    }
}