package dora.cache.data.page

import dora.cache.data.visitor.IPageDataVisitor

/**
 * Cache data pagination, accessed using a visitor.
 * 简体中文：缓存数据分页器，使用访问者进行访问。
 *
 * @see IPageDataVisitor
 */
interface IDataPager<M> {

    /**
     * Set the current page number, recommended to start from 0.
     * 简体中文：设置当前是第几页，建议从0开始。
     */
    var currentPage: Int

    /**
     * How many data items are there per page?
     * 简体中文：每页有几条数据？
     *
     * @return Do not return 0, as 0 cannot be used as a divisor. 简体中文：不要返回0，0不能做除数
     */
    var pageSize: Int

    val models: MutableList<M>

    /**
     * Load the filtered page data.
     * 简体中文：加载过滤后的页面数据。
     */
    fun loadData(models: MutableList<M>)

    /**
     * It will be called back when the page data changes.
     * 简体中文：页面数据改变后，会回调它。
     */
    fun onResult(result: (models: MutableList<M>) -> Unit) : IDataPager<M>

    /**
     * Receive visits from specific visitors, as different visitors will present page data
     * according to different rules.
     * 简体中文：接收具体访问者的访问，不同的访问者将会以不同的规则呈现页面数据。
     */
    fun accept(visitor: IPageDataVisitor<M>)
}