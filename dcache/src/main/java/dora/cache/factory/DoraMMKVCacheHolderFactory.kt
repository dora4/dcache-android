package dora.cache.factory

import android.content.Context
import com.google.gson.reflect.TypeToken
import dora.cache.holder.CacheHolder
import dora.cache.holder.DoraListMMKVCacheHolder
import dora.cache.holder.DoraMMKVCacheHolder

open class DoraMMKVCacheHolderFactory<M>(val context: Context) : MMKVCacheHolderFactory<M>() {

    override fun createCacheHolder(clazz: Class<M>): CacheHolder<M> {
        return DoraMMKVCacheHolder(context, clazz)
    }

    override fun createListCacheHolder(clazz: Class<M>): CacheHolder<MutableList<M>> {
        return DoraListMMKVCacheHolder(context, object : TypeToken<MutableList<M>>(){})
    }
}