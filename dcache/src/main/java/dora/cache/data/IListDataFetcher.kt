package dora.cache.data

import androidx.lifecycle.LiveData
import dora.cache.data.page.IDataPager
import dora.db.OrmTable
import dora.http.DoraListCallback

interface IListDataFetcher<M> {

    fun fetchListData(): LiveData<List<M>>
    fun listCallback(): DoraListCallback<M>
    fun obtainPager(): IDataPager<M>?
}