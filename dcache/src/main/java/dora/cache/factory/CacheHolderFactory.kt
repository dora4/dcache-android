package dora.cache.factory

import dora.cache.holder.CacheHolder

/**
 * 生产[CacheHolder]产品簇的抽象工厂接口。
 */
interface CacheHolderFactory<M> {

    /**
     * 创建一个非集合模式的[CacheHolder]
     */
    fun createCacheHolder() : CacheHolder<M>

    /**
     * 创建一个集合模式的[CacheHolder]
     */
    fun createListCacheHolder() : CacheHolder<MutableList<M>>
}