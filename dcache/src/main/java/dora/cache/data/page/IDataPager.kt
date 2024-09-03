package dora.cache.data.page

import dora.cache.data.visitor.IPageDataVisitor

/**
 * 缓存数据分页器，使用访问者进行访问。
 *
 * @see IPageDataVisitor
 */
interface IDataPager<M> {

    /**
     * 设置当前是第几页，建议从0开始。
     */
    var currentPage: Int

    /**
     * 每页有几条数据？
     *
     * @return 不要返回0，0不能做除数
     */
    var pageSize: Int

    val models: MutableList<M>

    /**
     * 加载过滤后的页面数据。
     */
    fun loadData(models: MutableList<M>)

    /**
     * 页面数据改变后，会回调它。
     */
    fun onResult(result: (models: MutableList<M>) -> Unit) : IDataPager<M>

    /**
     * 接收具体访问者的访问，不同的访问者将会以不同的规则呈现页面数据。
     */
    fun accept(visitor: IPageDataVisitor<M>)
}