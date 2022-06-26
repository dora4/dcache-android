package dora.cache.holder

import dora.db.builder.Condition

abstract class ListCacheHolder<M> : CacheHolder<MutableList<M>> {

    /**
     * 追加模式旧缓存的条件。
     */
    val cacheConditions : MutableList<Condition> = arrayListOf()
}