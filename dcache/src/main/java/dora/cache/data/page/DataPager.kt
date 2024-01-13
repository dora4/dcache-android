package dora.cache.data.page

import dora.cache.data.visitor.IPageDataVisitor

/**
 * 用于对内存数据进行分页。
 */
class DataPager<M>(override val models: MutableList<M>) : IDataPager<M> {

    /**
     * 当前是第几页，建议从0开始累加。
     */
    override var currentPage = 0

    /**
     * 每页的数据数量不能为0，0不能做除数。
     */
    override var pageSize = 1

    private var result: ((models: MutableList<M>) -> Unit)? = null

    /**
     * 上一页的页数。
     */
    val lastPage: Int
        get() = if (currentPage > 0) { currentPage - 1 } else 0

    /**
     * 下一页的页数。
     */
    val nextPage: Int
        get() = currentPage + 1

    override fun onResult(result: (models: MutableList<M>) -> Unit): IDataPager<M> {
        this.result = result
        return this
    }

    /**
     * 接收页面数据访问者的访问。
     */
    override fun accept(visitor: IPageDataVisitor<M>) {
        visitor.visitDataPager(this)
    }

    override fun loadData(models: MutableList<M>) {
        result?.invoke(models)
    }
}