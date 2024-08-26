package dora.cache.data.adapter

interface PageResult<M> : Result<M> {

    fun getTotalSize() : Int
}