package dora.cache.data.adapter

/**
 * The adapter interface for paginated data.
 * 简体中文：分页数据的适配接口。
 *
 * @see Result
 */
interface PageResult<M> : Result<M> {

    fun getTotalSize() : Int
}