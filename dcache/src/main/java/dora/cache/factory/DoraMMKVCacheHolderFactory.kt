package dora.cache.factory

import com.google.gson.reflect.TypeToken
import dora.cache.holder.CacheHolder
import dora.cache.holder.DoraListMMKVCacheHolder
import dora.cache.holder.DoraMMKVCacheHolder

open class DoraMMKVCacheHolderFactory<M> : MMKVCacheHolderFactory<M>() {

    override fun createCacheHolder(clazz: Class<M>): CacheHolder<M> {
        return DoraMMKVCacheHolder(clazz)
    }

    override fun createListCacheHolder(clazz: Class<M>): CacheHolder<MutableList<M>> {
        return DoraListMMKVCacheHolder((object : TypeToken<MutableList<M>>(){}.rawType)
                as Class<MutableList<M>>)
    }
}