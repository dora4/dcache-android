package dora.cache.data.result

interface Result<M> {

    fun getRealModel() : M
}