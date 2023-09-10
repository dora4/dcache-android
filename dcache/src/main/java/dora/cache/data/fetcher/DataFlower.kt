package dora.cache.data.fetcher

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class DataFlower<M> : IDataFlower<M> {

    internal var flowData: MutableStateFlow<M?> = MutableStateFlow(null)

    override fun getFlowData(): StateFlow<M?> {
        return flowData
    }
}