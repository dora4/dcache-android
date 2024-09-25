package dora.cache.holder

import dora.db.builder.Condition

/**
 * Control the flow of caching and loading data.
 * 简体中文：控制缓存和加载数据的流程。
 */
interface DatabaseCacheHolder<M> : CacheHolder<M> {

    /**
     * Load data from the database into memory.
     * 简体中文：从数据库中加载数据到内存。
     */
    fun queryCache(condition: Condition) : M?

    /**
     * Query the number of cached records.
     * 简体中文：查询缓存记录数量。
     */
    fun queryCacheSize(condition: Condition) : Long

    /**
     * Remove old database cache.
     * 简体中文：移除旧的数据库缓存。
     */
    fun removeOldCache(condition: Condition)

    /**
     * Cache the latest data into the database.
     * 简体中文：将最新的数据缓存到数据库。
     */
    fun addNewCache(model: M)
}