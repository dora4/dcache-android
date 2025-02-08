package dora.cache.data.fetcher

import androidx.lifecycle.LiveData
import dora.http.DoraCallback

/**
 * A data fetcher that directly reads in-memory data from the [androidx.lifecycle.LiveData] data
 * carrier.
 * 简体中文：从[androidx.lifecycle.LiveData]数据载体中直接读取内存数据的数据抓取器。
 */
interface IDataFetcher<M> {

    /**
     * Clear the data in LiveData.
     * 简体中文：清空livedata的数据。
     */
    fun clearData()

    /**
     * Callback for fetching data.
     * 简体中文：抓取数据的回调。
     */
    fun callback(): DoraCallback<M>

    /**
     * Start fetching data.
     * 简体中文：开始抓取数据。
     */
    fun fetchData(description: String? = "", listener: OnLoadListener? =
        OnLoadListenerImpl()): LiveData<M?>

    /**
     * Get LiveData.
     * 简体中文：获取livedata。
     */
    fun getLiveData(): LiveData<M?>
}