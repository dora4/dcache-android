package dora.cache.data.fetcher

import androidx.lifecycle.MutableLiveData
import dora.cache.data.page.DataPager
import dora.cache.data.page.IDataPager

abstract class ListDataFetcher<M> : IListDataFetcher<M> {

    protected var liveData: MutableLiveData<List<M>> = MutableLiveData()
    private var pager: IDataPager<M> = DataPager(liveData.value as List<M>)

    override fun obtainPager(): IDataPager<M> {
        return pager
    }
}