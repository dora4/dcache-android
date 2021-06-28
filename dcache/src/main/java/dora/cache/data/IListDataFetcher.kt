package dora.cache.data

import androidx.lifecycle.LiveData
import dora.cache.data.page.IDataPager
import dora.db.OrmTable
import dora.http.DoraListCallback

interface IListDataFetcher<T : OrmTable> {

    fun fetchListData(): LiveData<List<T>>
    fun listCallback(): DoraListCallback<T>
    fun obtainPager(): IDataPager<T>?
}