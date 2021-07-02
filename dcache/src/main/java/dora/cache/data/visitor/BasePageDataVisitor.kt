package dora.cache.data.visitor

import dora.cache.data.page.IDataPager

abstract class BasePageDataVisitor<T> : IPageDataVisitor<T> {

    override fun visitDataPager(pager: IDataPager<T>) {
        pager.onResult(getResult(pager.data, pager.data.size, pager.currentPage, pager.pageSize))
    }
}