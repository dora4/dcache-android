package dora.cache.data.fetcher

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class FlowDataFetcher<M> : IFlowDataFetcher<M> {

    protected var flowData: MutableStateFlow<M?> = MutableStateFlow<M?>(null)

    override fun getFlowData(): StateFlow<M?> {
        return flowData
    }
}