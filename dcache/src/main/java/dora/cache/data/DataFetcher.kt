package dora.cache.data

import androidx.lifecycle.MutableLiveData
import dora.db.OrmTable

abstract class DataFetcher<T : OrmTable> : IDataFetcher<T> {
    protected var liveData: MutableLiveData<T> = MutableLiveData()
}