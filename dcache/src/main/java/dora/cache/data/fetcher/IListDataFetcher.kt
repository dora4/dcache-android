package dora.cache.data.fetcher

import androidx.lifecycle.LiveData
import dora.cache.data.page.IDataPager
import dora.http.DoraListCallback

/**
 * 用于网络数据抓取。
 */
interface IListDataFetcher<M> {

    /**
     * 清空livedata的数据。
     */
    fun clearListData()

    /**
     * 抓取数据的回调。
     */
    fun listCallback(): DoraListCallback<M>

    /**
     * 开始抓取数据。
     */
    fun fetchListData(description: String?, listener: OnLoadStateListener? = OnLoadStateListenerImpl()): LiveData<MutableList<M>>

    /**
     * 获取livedata。
     */
    fun getListLiveData() : LiveData<MutableList<M>>

    /**
     * 获取livedata的分页器。
     */
    fun obtainPager(): IDataPager<M>
}