package dora.cache.holder

interface CacheHolder<M> {

    /**
     * 该类型数据库orm框架的一些初始化操作在这里进行。
     */
    fun init()
}