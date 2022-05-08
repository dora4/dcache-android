package dora.cache.data.fetcher

import androidx.lifecycle.LiveData
import dora.cache.data.page.IDataPager
import dora.http.DoraListCallback
import io.reactivex.Observable

interface IListDataFetcher<M> {

    fun clearListData()
    fun listCallback(listener: OnLoadListener? = null): DoraListCallback<M>
    fun fetchListData(listener: OnLoadListener? = object : OnLoadListener {
        override fun onSuccess() {
        }
        override fun onFailure(code: Int, msg: String?) {
        }
    }): LiveData<MutableList<M>>
    fun getListLiveData() : LiveData<MutableList<M>>
    fun obtainPager(): IDataPager<M>

    interface OnLoadListener {
        fun onSuccess()
        fun onFailure(code: Int, msg: String?)
    }
}