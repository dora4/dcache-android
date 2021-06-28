package dora.cache.data

import androidx.lifecycle.MutableLiveData
import dora.cache.data.page.DataPager
import dora.cache.data.page.IDataPager
import dora.db.OrmTable
import java.util.*

abstract class ListDataFetcher<T : OrmTable> : IListDataFetcher<T> {

    protected var pager: IDataPager<T>? = null
    protected var liveData: MutableLiveData<List<T>> = MutableLiveData()

    fun fetchPager(): IDataPager<T> {
        return DataPager(if (liveData!!.value == null) ArrayList() else liveData!!.value).also { pager = it }
    }
}