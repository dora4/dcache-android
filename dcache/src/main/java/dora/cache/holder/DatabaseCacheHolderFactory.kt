package dora.cache.holder

import dora.db.table.OrmTable

class DatabaseCacheHolderFactory<M, T : OrmTable>(var clazz: Class<out OrmTable>) : CacheHolderFactory<M> {

    override fun createCacheHolder(): CacheHolder<M> {
        return DoraDatabaseCacheHolder<M, T>(clazz)
    }

    override fun createListCacheHolder(): CacheHolder<MutableList<M>> {
        return DoraListDatabaseCacheHolder<M, T>(clazz)
    }
}