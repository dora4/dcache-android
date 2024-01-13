package dora.cache.data.fetcher

import androidx.lifecycle.LiveData
import dora.http.DoraCallback

/**
 * 从[androidx.lifecycle.LiveData]数据载体中直接读取内存数据的数据抓取器。
 */
interface IDataFetcher<M> {

    /**
     * 清空livedata的数据。
     */
    fun clearData()

    /**
     * 抓取数据的回调。
     */
    fun callback(): DoraCallback<M>

    /**
     * 开始抓取数据。
     */
    fun fetchData(description: String?, listener: OnLoadStateListener? = OnLoadStateListenerImpl()): LiveData<M?>

    /**
     * 获取livedata。
     */
    fun getLiveData(): LiveData<M?>
}