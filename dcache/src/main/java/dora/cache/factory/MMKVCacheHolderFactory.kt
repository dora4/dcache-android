package dora.cache.factory

import dora.cache.holder.CacheHolder
import dora.cache.holder.DoraListMMKVCacheHolder
import dora.cache.holder.DoraMMKVCacheHolder
import java.lang.reflect.ParameterizedType

class MMKVCacheHolderFactory<M> : CacheHolderFactory<M> {

    override fun createCacheHolder(): CacheHolder<M> {
        return DoraMMKVCacheHolder<M>(getGenericType(this) as Class<M>)
    }

    override fun createListCacheHolder(): CacheHolder<MutableList<M>> {
        return DoraListMMKVCacheHolder<M>(getGenericType(this) as Class<MutableList<M>>)
    }

    private fun getGenericType(obj: Any): Class<*> {
        return if (obj.javaClass.genericSuperclass is ParameterizedType &&
            (obj.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments.isNotEmpty()) {
            (obj.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<*>
        } else (obj.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<*>
    }
}