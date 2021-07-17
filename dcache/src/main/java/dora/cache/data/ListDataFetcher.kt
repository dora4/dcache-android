package dora.cache.data

import androidx.lifecycle.MutableLiveData
import dora.cache.data.page.IDataPager

abstract class ListDataFetcher<M> : IListDataFetcher<M> {

    protected var pager: IDataPager<M>? = null
    protected var liveData: MutableLiveData<List<M>> = MutableLiveData()

    override fun obtainPager(): IDataPager<M>? {
        return pager
    }
}