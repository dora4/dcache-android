package dora.cache.holder

/**
 * Control the process of caching and loading data.
 * 简体中文：控制缓存和加载数据的流程。
 */
interface MMKVCacheHolder<M> : CacheHolder<M> {

    /**
     * Remove old database cache.
     * 简体中文：移除旧的数据库缓存。
     */
    fun removeOldCache(key: String)

    /**
     * Cache the latest data into the database.
     * 简体中文：将最新的数据缓存到数据库。
     */
    fun addNewCache(key: String, model: M)

    /**
     * Read MMKV cache.
     * 简体中文：读取mmkv缓存。
     */
    fun readCache(key: String) : M?
}