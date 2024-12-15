package dora.cache.repository

import android.content.Context
import dora.cache.factory.DoraMMKVCacheHolderFactory
import dora.cache.factory.MMKVCacheHolderFactory

abstract class DoraMMKVCacheRepository<M>(context: Context)
    : BaseMMKVCacheRepository<M>(context) {

    override fun createCacheHolderFactory(): MMKVCacheHolderFactory<M> {
        return DoraMMKVCacheHolderFactory<M>()
    }
}