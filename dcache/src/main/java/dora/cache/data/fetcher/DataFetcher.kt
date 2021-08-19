package dora.cache.data.fetcher

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

abstract class DataFetcher<M> : IDataFetcher<M> {

    internal var liveData: MutableLiveData<M?> = MutableLiveData()

    override fun getLiveData(): LiveData<M?> {
        return liveData
    }
}