package dora.cache.repository

import android.content.Context
import dora.cache.factory.CacheFactory
import dora.cache.factory.DoraCacheFactory
import dora.cache.factory.DoraListCacheFactory
import dora.db.OrmTable

abstract class DoraMemoryCacheRepository<T: OrmTable>(context: Context, private val ormClass: Class<T>)
    : BaseMemoryCacheRepository<T>(context, ormClass) {

    override fun createCacheFactory(): CacheFactory<T> {
        return DoraCacheFactory<T, T>(ormClass)
    }

    override fun createListCacheFactory(): CacheFactory<List<T>> {
        return DoraListCacheFactory<T, T>(ormClass)
    }
}