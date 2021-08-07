package dora.cache.data.fetcher

import androidx.lifecycle.LiveData
import dora.http.DoraCallback

interface IDataFetcher<M> {

    fun callback(): DoraCallback<M>
    fun fetchData(): LiveData<M?>
}