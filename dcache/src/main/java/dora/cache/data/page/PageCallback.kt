package dora.cache.data.page

interface PageCallback<M> {
    fun onResult(models: List<M>)
}