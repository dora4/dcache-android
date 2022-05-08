package dora.cache.data.fetcher

import androidx.lifecycle.LiveData
import dora.http.DoraCallback
import io.reactivex.Observable

interface IDataFetcher<M> {

    fun clearData()
    fun callback(listener: OnLoadListener? = null): DoraCallback<M>
    fun fetchData(listener: OnLoadListener? = object : OnLoadListener {
        override fun onSuccess() {
        }

        override fun onFailure(code: Int, msg: String?) {
        }
    }): LiveData<M?>
    fun getLiveData(): LiveData<M?>

    interface OnLoadListener {
        fun onSuccess()
        fun onFailure(code: Int, msg: String?)
    }
}