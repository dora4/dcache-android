package dora.cache.holder

import dora.db.builder.Condition

/**
 * 控制缓存和加载数据的流程。
 */
interface DatabaseCacheHolder<M> : CacheHolder<M> {

    /**
     * 从数据库中加载数据到内存。
     */
    fun queryCache(condition: Condition) : M?

    /**
     * 查询缓存记录数量。
     */
    fun queryCacheSize(condition: Condition) : Long

    /**
     * 移除旧的数据库缓存。
     */
    fun removeOldCache(condition: Condition)

    /**
     * 将最新的数据缓存到数据库。
     */
    fun addNewCache(model: M)
}