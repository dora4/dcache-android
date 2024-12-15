package dora.cache.repository

import android.content.Context
import dora.cache.factory.DoraMMKVCacheHolderFactory
import dora.cache.factory.MMKVCacheHolderFactory

abstract class DoraFlowMMKVCacheRepository<M>(context: Context)
    : BaseFlowMMKVCacheRepository<M>(context) {

    override fun createCacheHolderFactory(): MMKVCacheHolderFactory<M> {
        return DoraMMKVCacheHolderFactory<M>()
    }
}