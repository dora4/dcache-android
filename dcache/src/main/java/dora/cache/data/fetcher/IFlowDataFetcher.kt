package dora.cache.data.fetcher

import dora.http.DoraCallback
import kotlinx.coroutines.flow.StateFlow

/**
 * A data fetcher that directly reads in-memory data from the [kotlinx.coroutines.flow.StateFlow]
 * data carrier.
 * 简体中文：从[kotlinx.coroutines.flow.StateFlow]数据载体中直接读取内存数据的数据抓取器。
 */
interface IFlowDataFetcher<M> {

    /**
     * Clear the data in Flow data.
     * 简体中文：清空flow data的数据。
     */
    fun clearData()

    /**
     * Callback for data fetching.
     * 简体中文：抓取数据的回调。
     */
    fun callback(): DoraCallback<M>

    /**
     * Start fetching data.
     * 简体中文：开始抓取数据。
     */
    fun fetchData(description: String? = "", listener: OnLoadListener? =
        OnLoadListenerImpl()): StateFlow<M?>

    /**
     * Get Flow data.
     * 简体中文：获取flow data。
     */
    fun getFlowData(): StateFlow<M?>
}