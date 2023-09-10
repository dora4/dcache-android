package dora.cache.data.fetcher

import dora.cache.data.page.IDataPager
import dora.http.DoraListCallback
import kotlinx.coroutines.flow.StateFlow

/**
 * 用于网络数据抓取。
 */
interface IListDataFlower<M> {

    suspend fun clearListData()

    /**
     * 抓取数据的回调。
     */
    fun listCallback(): DoraListCallback<M>

    /**
     * 开始抓取数据。
     */
    suspend fun flowListData(description: String?, listener: OnLoadStateListener? = OnLoadStateListenerImpl()): StateFlow<MutableList<M>>

    fun getListFlowData() : StateFlow<MutableList<M>>

    fun obtainPager(): IDataPager<M>
}