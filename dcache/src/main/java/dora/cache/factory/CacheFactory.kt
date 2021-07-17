package dora.cache.factory

import dora.db.builder.Condition

interface CacheFactory<M> {

    fun init()

    fun queryCache(condition: Condition) : M?

    fun removeOldCache(condition: Condition)

    fun addNewCache(model: M)
}