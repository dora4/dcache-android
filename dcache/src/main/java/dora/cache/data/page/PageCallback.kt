package dora.cache.data.page

interface PageCallback<M> {
    fun onResult(model: List<M>?)
}