package dora.cache.repository.compose

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dora.cache.data.fetcher.OnLoadListener
import kotlinx.coroutines.flow.*

/**
 * Compose-first repository (Flow only, no Rx / callback).
 * 简体中文：Compose优先的Repository，仅使用Flow，不再依赖Rx或Callback。
 */
abstract class BaseComposeRepository<M>(val context: Context) : ViewModel() {

    /**
     * UI state flow.
     * 简体中文：UI状态流。
     */
    protected val _state = MutableStateFlow<M?>(null)
    val state: StateFlow<M?> = _state.asStateFlow()

    /**
     * Loading state.
     * 简体中文：加载状态。
     */
    protected val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    /**
     * Error state.
     * 简体中文：错误状态。
     */
    protected val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error

    /**
     * Flow network request (must override).
     * 简体中文：Flow网络请求，必须实现。
     */
    protected abstract fun onLoadFromNetwork(): Flow<M>

    /**
     * Intercept network requests and the loaded cached data, making some modifications.
     * 简体中文：拦截网络请求和缓存加载出来的数据，并做一些修改。
     */
    protected open fun onInterceptData(type: DataSource.Type, model: M) {}

    /**
     * Fetch data (Compose-friendly).
     * 简体中文：获取数据（适用于Compose）。
     */
    fun fetchData(listener: OnLoadListener? = null) {
        onLoadFromNetwork()
            .onStart {
                _loading.value = true
            }
            .onEach { data ->
                onInterceptData(DataSource.Type.NETWORK, data)
                _state.value = data
                listener?.onLoad(
                    OnLoadListener.Source.NETWORK,
                    OnLoadListener.SUCCESS
                )
            }
            .catch { e ->
                _error.emit(e.message ?: "unknown error")
                listener?.onLoad(
                    OnLoadListener.Source.NETWORK,
                    OnLoadListener.FAILURE
                )
            }
            .onCompletion {
                _loading.value = false
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