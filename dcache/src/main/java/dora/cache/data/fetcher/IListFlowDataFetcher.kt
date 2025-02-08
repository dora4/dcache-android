package dora.cache.data.fetcher

import dora.cache.data.page.IDataPager
import dora.http.DoraListCallback
import kotlinx.coroutines.flow.StateFlow

/**
 * A data fetcher that directly reads in-memory data from the [kotlinx.coroutines.flow.StateFlow]
 * data carrier.
 * 简体中文：从[kotlinx.coroutines.flow.StateFlow]数据载体中直接读取内存数据的数据抓取器。
 */
interface IListFlowDataFetcher<M> {

    /**
     * Clear the data in Flow data.
     * 简体中文：清空flow data的数据。
     */
    fun clearListData()

    /**
     * Callback for data fetching.
     * 简体中文：抓取数据的回调。
     */
    fun listCallback(): DoraListCallback<M>

    /**
     * Start fetching data.
     * 简体中文：开始抓取数据。
     */
    fun fetchListData(description: String? = "", listener: OnLoadListener? =
        OnLoadListenerImpl()): StateFlow<MutableList<M>>

    /**
     * Get Flow data.
     * 简体中文：获取flow data。
     */
    fun getListFlowData() : StateFlow<MutableList<M>>

    /**
     * Get the pagination for Flow data.
     * 简体中文：获取flow data的分页器。
     */
    fun obtainPager(): IDataPager<M>
}