package dora.cache.factory

import dora.cache.holder.CacheHolder
import dora.cache.holder.DoraDatabaseCacheHolder
import dora.cache.holder.DoraListDatabaseCacheHolder
import dora.db.table.OrmTable

class DoraDatabaseCacheHolderFactory<T : OrmTable>(clazz: Class<T>) :
    DatabaseCacheHolderFactory<T>(clazz) {

    override fun createCacheHolder(clazz: Class<T>): CacheHolder<T> {
        return DoraDatabaseCacheHolder<T>(clazz)
    }

    override fun createListCacheHolder(clazz: Class<T>): CacheHolder<MutableList<T>> {
        return DoraListDatabaseCacheHolder<T>(clazz)
    }
}