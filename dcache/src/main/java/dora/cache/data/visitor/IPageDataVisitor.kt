package dora.cache.data.visitor

import dora.cache.data.page.IDataPager

/**
 * 简体中文：分页数据的访问者，不破坏数据的原有结构，访问数据。
 *
 * @param <M>
 */
interface IPageDataVisitor<M> {

    /**
     * A visitor for paginated data that accesses data without altering its original structure.
     * 简体中文：访问数据分页器。
     *
     * @param pager
     */
    fun visitDataPager(pager: IDataPager<M>)

    /**
     * Filter out a page of data that meets the requirements.
     * 简体中文：过滤出符合要求的一页数据。
     *
     * @param model Sample data. 简体中文：样本数据
     * @param totalCount  Total number of data items. 简体中文：数据总条数
     * @param currentPage Current page number. 简体中文：当前第几页
     * @param pageSize    Number of data items per page. 简体中文：每页数据条数
     * @return Data for the current page. 简体中文：该页的数据
     */
    fun filterPageData(models: MutableList<M>, totalCount: Int, currentPage: Int,
                       pageSize: Int): MutableList<M>
}