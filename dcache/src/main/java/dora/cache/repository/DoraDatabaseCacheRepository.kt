package dora.cache.repository

import android.app.Activity
import dora.cache.holder.CacheHolder
import dora.cache.holder.DoraCacheHolder
import dora.cache.holder.DoraListCacheHolder
import dora.db.table.OrmTable

abstract class DoraDatabaseCacheRepository<T: OrmTable>(context: Activity)
    : BaseDatabaseCacheRepository<T>(context) {

    override fun createCacheHolder(clazz: Class<T>): CacheHolder<T> {
        return DoraCacheHolder<T, T>(clazz)
    }

    override fun createListCacheHolder(clazz: Class<T>): CacheHolder<MutableList<T>> {
        return DoraListCacheHolder<T, T>(clazz)
    }
}