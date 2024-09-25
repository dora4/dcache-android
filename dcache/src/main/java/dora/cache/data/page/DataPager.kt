package dora.cache.data.page

import dora.cache.data.visitor.IPageDataVisitor

/**
 * Cache data pagination, used for paginating in-memory data.
 * 简体中文：缓存数据分页器，用于对内存数据进行分页。
 */
class DataPager<M>(override val models: MutableList<M>) : IDataPager<M> {

    /**
     * Indicates the current page number, recommended to start accumulating from 0.
     * 简体中文：当前是第几页，建议从0开始累加。
     */
    override var currentPage = 0

    /**
     * The number of data items per page cannot be 0, as 0 cannot be used as a divisor.
     * 简体中文：每页的数据数量不能为0，0不能做除数。
     */
    override var pageSize = 1

    private var result: ((models: MutableList<M>) -> Unit)? = null

    /**
     * The page number of the previous page.
     * 简体中文：上一页的页数。
     */
    val lastPage: Int
        get() = if (currentPage > 0) { currentPage - 1 } else 0

    /**
     * The page number of the next page.
     * 简体中文：下一页的页数。
     */
    val nextPage: Int
        get() = currentPage + 1

    override fun onResult(result: (models: MutableList<M>) -> Unit): IDataPager<M> {
        this.result = result
        return this
    }

    /**
     * Receive visits from page data visitors.
     * 简体中文：接收页面数据访问者的访问。
     */
    override fun accept(visitor: IPageDataVisitor<M>) {
        visitor.visitDataPager(this)
    }

    override fun loadData(models: MutableList<M>) {
        result?.invoke(models)
    }
}