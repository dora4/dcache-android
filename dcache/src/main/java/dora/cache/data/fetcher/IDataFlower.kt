package dora.cache.data.fetcher

import dora.http.DoraCallback
import kotlinx.coroutines.flow.StateFlow

/**
 * 用于网络数据抓取。
 */
interface IDataFlower<M> {

    suspend fun clearData()

    /**
     * 抓取数据的回调。
     */
    fun callback(): DoraCallback<M>

    /**
     * 开始抓取数据。
     */
    suspend fun flowData(description: String?, listener: OnLoadStateListener? = OnLoadStateListenerImpl()): StateFlow<M?>

    fun getFlowData(): StateFlow<M?>
}