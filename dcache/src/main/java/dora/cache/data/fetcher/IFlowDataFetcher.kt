package dora.cache.data.fetcher

import dora.http.DoraCallback
import kotlinx.coroutines.flow.StateFlow

/**
 * 用于网络数据抓取。
 */
interface IFlowDataFetcher<M> {

    /**
     * 清空flow data的数据。
     */
    fun clearData()

    /**
     * 抓取数据的回调。
     */
    fun callback(): DoraCallback<M>

    /**
     * 开始抓取数据。
     */
    fun fetchData(description: String?, listener: OnLoadStateListener? = OnLoadStateListenerImpl()): StateFlow<M?>

    /**
     * 获取flow data。
     */
    fun getFlowData(): StateFlow<M?>
}