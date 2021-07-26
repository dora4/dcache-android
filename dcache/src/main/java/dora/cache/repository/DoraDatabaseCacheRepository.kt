package dora.cache.repository

import android.content.Context
import dora.cache.factory.CacheFactory
import dora.cache.factory.DoraCacheFactory
import dora.cache.factory.DoraListCacheFactory
import dora.db.OrmTable

abstract class DoraDatabaseCacheRepository<T: OrmTable>(context: Context)
    : BaseDatabaseCacheRepository<T>(context) {

    override fun createCacheFactory(clazz: Class<T>): CacheFactory<T> {
        return DoraCacheFactory<T, T>(clazz)
    }

    override fun createListCacheFactory(clazz: Class<T>): CacheFactory<List<T>> {
        return DoraListCacheFactory<T, T>(clazz)
    }
}