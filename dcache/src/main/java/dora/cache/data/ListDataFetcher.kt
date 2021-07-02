package dora.cache.data

import androidx.lifecycle.MutableLiveData
import dora.cache.data.page.DataPager
import dora.cache.data.page.IDataPager
import dora.db.OrmTable

abstract class ListDataFetcher<T : OrmTable> : IListDataFetcher<T> {

    protected var pager: IDataPager<T>? = null
    protected var liveData: MutableLiveData<List<T>> = MutableLiveData()

    override fun obtainPager(): IDataPager<T>? {
        return pager
    }
}