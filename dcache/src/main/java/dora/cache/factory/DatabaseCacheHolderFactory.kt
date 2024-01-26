package dora.cache.factory

import dora.cache.holder.CacheHolder
import dora.cache.holder.DoraDatabaseCacheHolder
import dora.cache.holder.DoraListDatabaseCacheHolder
import dora.db.table.OrmTable

class DatabaseCacheHolderFactory<T : OrmTable>(val clazz: Class<out OrmTable>) :
    CacheHolderFactory<T> {

    override fun createCacheHolder(): CacheHolder<T> {
        return DoraDatabaseCacheHolder<T>(clazz)
    }

    override fun createListCacheHolder(): CacheHolder<MutableList<T>> {
        return DoraListDatabaseCacheHolder<T>(clazz)
    }
}