package dora.cache.data

import androidx.lifecycle.LiveData
import dora.cache.data.page.IDataPager
import dora.db.OrmTable
import dora.http.DoraCallback

interface IDataFetcher<T : OrmTable> {
    fun callback(): DoraCallback<T>
    fun fetchData(): LiveData<T>
    fun obtainPager(): IDataPager<T>?
}