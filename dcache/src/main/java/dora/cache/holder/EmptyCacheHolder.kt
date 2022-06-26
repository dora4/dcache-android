package dora.cache.holder

import dora.db.builder.Condition

class EmptyCacheHolder<M> : CacheHolder<M> {

    override fun init() {
    }

    override fun queryCache(condition: Condition): M? {
        return null
    }

    override fun removeOldCache(condition: Condition) {
    }

    override fun addNewCache(model: M) {
    }

    override fun queryCacheSize(condition: Condition): Long {
        return 0
    }
}