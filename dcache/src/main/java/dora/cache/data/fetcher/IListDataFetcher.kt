package dora.cache.data.fetcher

import androidx.lifecycle.LiveData
import dora.cache.data.page.IDataPager
import dora.http.DoraListCallback

interface IListDataFetcher<M> {

    fun fetchListData(): LiveData<List<M>>
    fun listCallback(): DoraListCallback<M>
    fun obtainPager(): IDataPager<M>
}