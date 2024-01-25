package dora.cache.holder

class MMKVCacheHolderFactory<M> : CacheHolderFactory<M> {

    override fun createCacheHolder(): CacheHolder<M> {
        return DoraMMKVCacheHolder<M>()
    }

    override fun createListCacheHolder(): CacheHolder<MutableList<M>> {
        return DoraListMMKVCacheHolder<M>()
    }
}