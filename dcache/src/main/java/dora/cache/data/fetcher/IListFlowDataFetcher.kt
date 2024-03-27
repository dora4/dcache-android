package dora.cache.data.fetcher

import dora.cache.data.page.IDataPager
import dora.http.DoraListCallback
import kotlinx.coroutines.flow.StateFlow

/**
 * 从[kotlinx.coroutines.flow.StateFlow]数据载体中直接读取内存数据的集合数据抓取器。
 */
interface IListFlowDataFetcher<M> {

    /**
     * 清空flow data的数据。
     */
    fun clearListData()

    /**
     * 抓取数据的回调。
     */
    fun listCallback(): DoraListCallback<M>

    /**
     * 开始抓取数据。
     */
    fun fetchListData(description: String? = "", listener: OnLoadStateListener? = OnLoadStateListenerImpl()): StateFlow<MutableList<M>>

    /**
     * 获取livedata。
     */
    fun getListFlowData() : StateFlow<MutableList<M>>

    /**
     * 获取flow data的分页器。
     */
    fun obtainPager(): IDataPager<M>
}