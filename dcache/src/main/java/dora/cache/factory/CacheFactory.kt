package dora.cache.factory

import dora.db.builder.Condition

interface CacheFactory<M> {

    /**
     * 该类型数据库orm框架的一些初始化操作在这里进行。
     */
    fun init()

    /**
     * 从数据库中加载数据到内存。
     */
    fun queryCache(condition: Condition) : M?

    /**
     * 移除旧的数据库缓存。
     */
    fun removeOldCache(condition: Condition)

    /**
     * 将最新的数据缓存到数据库。
     */
    fun addNewCache(model: M)
}