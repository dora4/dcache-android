package dora.cache.data.visitor

import dora.cache.data.page.IDataPager

abstract class BasePageDataVisitor<M> : IPageDataVisitor<M> {

    override fun visitDataPager(pager: IDataPager<M>) {
        pager.onResult(getResult(pager.models, pager.models.size, pager.currentPage, pager.pageSize))
    }
}