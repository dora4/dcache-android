package dora.cache.data.page

import dora.cache.data.visitor.IPageDataVisitor

class DataPager<T>(override val data: List<T>) : IDataPager<T> {
    /**
     * 建议从0开始累加。
     */
    override var currentPage = 0

    /**
     * 每页的数据数量不能为0，0不能做除数。
     */
    override var pageSize = 1
    private var callback: PageCallback<T>? = null
    val nextPage: Int
        get() = currentPage + 1

    override fun setPageCallback(callback: PageCallback<T>) {
        this.callback = callback
    }

    override fun accept(visitor: IPageDataVisitor<T>) {
        visitor.visitDataPager(this)
    }

    override fun onResult(data: List<T>?) {
        callback!!.onResult(data)
    }
}