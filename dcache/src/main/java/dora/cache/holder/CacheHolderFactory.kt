package dora.cache.holder

/**
 * 生产[CacheHolder]产品簇的抽象工厂接口。
 */
interface CacheHolderFactory<M> {

    fun createCacheHolder() : CacheHolder<M>

    fun createListCacheHolder() : CacheHolder<MutableList<M>>
}