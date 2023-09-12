package dora.cache.data.fetcher

import dora.cache.data.page.DataPager
import dora.cache.data.page.IDataPager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class ListDataFlower<M> : IListDataFlower<M> {

    internal var flowData: MutableStateFlow<MutableList<M>> = MutableStateFlow(arrayListOf())

    private var pager: IDataPager<M> = DataPager(flowData.value)

    override fun getListFlowData(): StateFlow<MutableList<M>> {
        return flowData
    }

    override fun obtainPager(): IDataPager<M> {
        return pager
    }
}