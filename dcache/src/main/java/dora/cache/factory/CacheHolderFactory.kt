package dora.cache.factory

import dora.cache.holder.CacheHolder

/**
 * An abstract factory interface for producing a cluster of [CacheHolder] products.
 * 简体中文：生产[CacheHolder]产品簇的抽象工厂接口。
 */
interface CacheHolderFactory<M> {

    /**
     * Create a non-collection mode [CacheHolder].
     * 简体中文：创建一个非集合模式的[CacheHolder]。
     */
    fun createCacheHolder() : CacheHolder<M>

    /**
     * Create a collection mode [CacheHolder].
     * 简体中文：创建一个集合模式的[CacheHolder]。
     */
    fun createListCacheHolder() : CacheHolder<MutableList<M>>
}