package dora.cache.data.page

interface PageCallback<T> {
    fun onResult(data: List<T>?)
}