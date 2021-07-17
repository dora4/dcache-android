package dora.cache.data

import androidx.lifecycle.LiveData
import dora.cache.data.page.IDataPager
import dora.db.OrmTable
import dora.http.DoraCallback

interface IDataFetcher<M> {
    fun callback(): DoraCallback<M>
    fun fetchData(): LiveData<M>
    fun obtainPager(): IDataPager<M>?
}