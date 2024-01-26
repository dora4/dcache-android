package dora.cache.holder

/**
 * 控制缓存和加载数据的流程。
 */
interface MMKVCacheHolder<M> : CacheHolder<M> {

    /**
     * 移除旧的数据库缓存。
     */
    fun removeOldCache(key: String)

    /**
     * 将最新的数据缓存到数据库。
     */
    fun addNewCache(key: String, model: M)

    /**
     * 读取mmkv缓存。
     */
    fun readCache(key: String) : M?
}