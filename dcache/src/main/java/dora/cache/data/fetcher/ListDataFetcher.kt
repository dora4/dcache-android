package dora.cache.data.fetcher

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dora.cache.data.page.DataPager
import dora.cache.data.page.IDataPager

abstract class ListDataFetcher<M> : IListDataFetcher<M> {

    internal var liveData: MutableLiveData<List<M>> = MutableLiveData()
    private var pager: IDataPager<M> = DataPager(liveData.value ?: arrayListOf())

    override fun getListLiveData(): LiveData<List<M>> {
        return liveData
    }

    override fun obtainPager(): IDataPager<M> {
        return pager
    }
}