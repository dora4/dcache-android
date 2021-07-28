package dora.cache.repository

import android.content.Context
import dora.cache.holder.CacheHolder
import dora.cache.holder.DoraCacheHolder
import dora.cache.holder.DoraListCacheHolder
import dora.db.OrmTable

abstract class DoraDatabaseCacheRepository<T: OrmTable>(context: Context)
    : BaseDatabaseCacheRepository<T>(context) {

    override fun createCacheHolder(clazz: Class<T>): CacheHolder<T> {
        return DoraCacheHolder<T, T>(clazz)
    }

    override fun createListCacheHolder(clazz: Class<T>): CacheHolder<List<T>> {
        return DoraListCacheHolder<T, T>(clazz)
    }
}