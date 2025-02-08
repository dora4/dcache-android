package dora.cache.data.fetcher

import androidx.lifecycle.LiveData
import dora.cache.data.page.IDataPager
import dora.http.DoraListCallback

/**
 * A list data fetcher that directly reads in-memory data from the [androidx.lifecycle.LiveData]
 * data carrier.
 * 简体中文：从[androidx.lifecycle.LiveData]数据载体中直接读取内存数据的列表数据抓取器。
 */
interface IListDataFetcher<M> {

    /**
     * Clear the data in LiveData.
     * 简体中文：清空livedata的数据。
     */
    fun clearListData()

    /**
     * Callback for fetching data.
     * 简体中文：抓取数据的回调。
     */
    fun listCallback(): DoraListCallback<M>

    /**
     * Start fetching data.
     * 简体中文：开始抓取数据。
     */
    fun fetchListData(description: String? = "", listener: OnLoadListener? =
        OnLoadListenerImpl()): LiveData<MutableList<M>>

    /**
     * Get LiveData.
     * 简体中文：获取livedata。
     */
    fun getListLiveData() : LiveData<MutableList<M>>

    /**
     * Get the pagination for LiveData.
     * 简体中文：获取livedata的分页器。
     */
    fun obtainPager(): IDataPager<M>
}