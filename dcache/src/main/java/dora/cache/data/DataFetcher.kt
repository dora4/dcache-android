package dora.cache.data

import androidx.lifecycle.MutableLiveData
import dora.db.OrmTable

abstract class DataFetcher<M> : IDataFetcher<M> {
    protected var liveData: MutableLiveData<M> = MutableLiveData()
}