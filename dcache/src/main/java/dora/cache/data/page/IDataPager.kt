package dora.cache.data.page

import dora.cache.data.visitor.IPageDataVisitor

interface IDataPager<T> : PageCallback<T> {
    fun setPageCallback(callback: PageCallback<T>)
    fun accept(visitor: IPageDataVisitor<T>)

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
    val data: List<T>
}