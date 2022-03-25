package dora.cache.data.fetcher

import androidx.lifecycle.LiveData
import dora.http.DoraCallback

interface IDataFetcher<M> {

    fun clearData()
    fun callback(listener: OnLoadListener? = null): DoraCallback<M>
    fun fetchData(listener: OnLoadListener? = null): LiveData<M?>
    fun getLiveData(): LiveData<M?>

    interface OnLoadListener {
        fun onSuccess()
        fun onFailure(code: Int, msg: String?)
    }
}