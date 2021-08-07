package dora.cache.data.fetcher

import androidx.lifecycle.MutableLiveData
import dora.cache.data.page.DataPager
import dora.cache.data.page.IDataPager

abstract class ListDataFetcher<M> : IListDataFetcher<M> {

    internal var liveData: MutableLiveData<List<M>> = MutableLiveData()
    private var pager: IDataPager<M> = DataPager(liveData.value ?: arrayListOf())

    override fun obtainPager(): IDataPager<M> {
        return pager
    }
}