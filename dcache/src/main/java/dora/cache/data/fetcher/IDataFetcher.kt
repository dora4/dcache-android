package dora.cache.data.fetcher

import androidx.lifecycle.LiveData
import dora.http.DoraCallback

/**
 * 用于网络数据抓取。
 */
interface IDataFetcher<M> {

    /**
     * 清空livedata的数据。
     */
    fun clearData()

    /**
     * 抓取数据的回调。
     */
    fun callback(listener: OnLoadListener? = null): DoraCallback<M>

    /**
     * 开始抓取数据。
     */
    fun fetchData(listener: OnLoadListener? = object : OnLoadListener {
        override fun onSuccess() {
        }

        override fun onFailure(msg: String) {
        }
    }): LiveData<M?>

    /**
     * 获取livedata。
     */
    fun getLiveData(): LiveData<M?>

    interface OnLoadListener {
        fun onSuccess()
        fun onFailure(msg: String)
    }
}