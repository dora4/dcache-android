package dora.cache.repository.compose

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dora.cache.data.fetcher.OnLoadListener
import dora.cache.repository.compose.flow.UiEvent
import dora.cache.repository.compose.flow.UiState
import kotlinx.coroutines.flow.*

/**
 * Compose-first List repository (Flow only).
 * 简体中文：Compose列表Repository，仅使用Flow。
 */
abstract class BaseListComposeRepository<M>(val context: Context) : ViewModel() {

    /**
     * UI state flow.
     * 简体中文：UI状态流。
     */
    protected val _state = MutableStateFlow<UiState<List<M>>>(UiState.Idle)
    val state: StateFlow<UiState<List<M>>> = _state

    /**
     * UI event flow.
     * 简体中文：UI事件流。
     */
    protected val _event = MutableSharedFlow<UiEvent>()
    val event: SharedFlow<UiEvent> = _event

    /**
     * Flow network request (must override).
     * 简体中文：Flow网络请求，必须实现。
     */
    protected abstract fun onLoadFromNetwork(): Flow<List<M>>

    /**
     * Intercept network requests and the loaded cached data, making some modifications.
     * 简体中文：拦截网络请求和缓存加载出来的数据，并做一些修改。
     */
    protected open fun onInterceptData(type: DataSource.Type, models: MutableList<M>) {}

    /**
     * Fetch list data (Compose-friendly).
     * 简体中文：获取列表数据（适用于Compose）。
     */
    @JvmOverloads
    open fun fetchListData(listener: OnLoadListener? = null) {
        onLoadFromNetwork()
            .onStart {
                _state.value = UiState.Loading
            }.onEach {
                if (it.isEmpty()) {
                    _state.value = UiState.Empty(UiState.Source.NETWORK)
                } else {
                    _state.value = UiState.Success(it, UiState.Source.NETWORK)
                }
                onInterceptData(DataSource.Type.NETWORK, it.toMutableList())
                listener?.onLoad(OnLoadListener.Source.NETWORK, OnLoadListener.SUCCESS)
            }
            .catch {
                _state.value = UiState.Error(it.message)
                _event.emit(UiEvent.Toast(it.message ?: "unknown error"))
                listener?.onLoad(OnLoadListener.Source.NETWORK, OnLoadListener.FAILURE)
            }
            .launchIn(viewModelScope)
    }

    /**
     * Source of the data.
     * 简体中文：数据的来源。
     */
    interface DataSource {

        enum class Type {

            /**
             * Data comes from the network server.
             * 简体中文：数据来源于网络服务器。
             */
            NETWORK,

            /**
             * Data comes from the cache.
             * 简体中文：数据来源于缓存。
             */
            CACHE
        }
    }
}