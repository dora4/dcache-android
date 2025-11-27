package dora.cache.data.visitor

import dora.cache.data.page.IDataPager

/**
 * Base class for paginated data visitors, used to process data in pages.
 * 简体中文：分页数据访问器基类，用于对分页数据进行处理。
 */
abstract class BasePageDataVisitor<M> : IPageDataVisitor<M> {

    override fun visitDataPager(pager: IDataPager<M>) {
        pager.loadData(filterPageData(pager.models, pager.models.size,
            pager.currentPage, pager.pageSize))
    }
}