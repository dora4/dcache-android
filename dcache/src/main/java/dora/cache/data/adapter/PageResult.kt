package dora.cache.data.adapter

/**
 * 分页数据的适配接口。
 *
 * @see Result
 */
interface PageResult<M> : Result<M> {

    fun getTotalSize() : Int
}