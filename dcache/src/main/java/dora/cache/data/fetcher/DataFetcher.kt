package dora.cache.data.fetcher

import androidx.lifecycle.MutableLiveData

abstract class DataFetcher<M> : IDataFetcher<M> {
    protected var liveData: MutableLiveData<M> = MutableLiveData()
}