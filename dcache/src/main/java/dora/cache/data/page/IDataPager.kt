package dora.cache.data.page

import dora.cache.data.visitor.IPageDataVisitor

interface IDataPager<M> : PageCallback<M> {
    fun setPageCallback(callback: PageCallback<M>)
    fun accept(visitor: IPageDataVisitor<M>)

    /**
     * 设置当前是第几页。
     *
     * @param currentPage 建议从0开始
     */
    var currentPage: Int

    /**
     * 每页有几条数据？
     *
     * @return 不要返回0，0不能做除数
     */
    var pageSize: Int
    val model: List<M>
}