package dora.cache.holder

abstract class ListDatabaseCacheHolder<M> : DatabaseCacheHolder<MutableList<M>> {

    /**
     * 上次返回给UI层展示的数据缓存。
     */
    val cache: MutableList<M> = arrayListOf()
}