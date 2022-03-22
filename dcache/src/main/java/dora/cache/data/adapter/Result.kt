package dora.cache.data.adapter

interface Result<M> {

    fun getRealModel() : M
}