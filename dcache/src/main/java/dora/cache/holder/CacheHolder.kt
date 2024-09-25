package dora.cache.holder

interface CacheHolder<M> {

    /**
     * Some initialization operations for this type of caching framework are performed here, such
     * as the initialization of the database ORM framework and the MMKV caching framework.
     * 简体中文：该类型缓存框架的一些初始化操作在这里进行。如数据库ORM框架、MMKV缓存框架的初始化。
     */
    fun init()
}