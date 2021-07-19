package dora.cache.data.page

import dora.cache.data.visitor.IPageDataVisitor

class DataPager<M>(override val models: List<M>) : IDataPager<M> {
    /**
     * 建议从0开始累加。
     */
    override var currentPage = 0

    /**
     * 每页的数据数量不能为0，0不能做除数。
     */
    override var pageSize = 1
    private var callback: PageCallback<M>? = null
    val nextPage: Int
        get() = currentPage + 1

    override fun setPageCallback(callback: PageCallback<M>) {
        this.callback = callback
    }

    override fun accept(visitor: IPageDataVisitor<M>) {
        visitor.visitDataPager(this)
    }

    override fun onResult(model: List<M>) {
        callback?.onResult(model)
    }
}