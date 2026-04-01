package dora.cache.repository.compose

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dora.cache.data.fetcher.OnLoadListener
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
     * Fetch data (Compose-friendly).
     * 简体中文：获取数据（适用于Compose）。
     */
    fun fetchData(listener: OnLoadListener? = null) {
        viewModelScope.launch {
            _loading.value = true
            try {
                onLoadFromNetwork()
                    .onEach {
                        _state.value = it
                        listener?.onLoad(OnLoadListener.Source.NETWORK, OnLoadListener.SUCCESS)
                    }
                    .catch {
                        _error.emit(it.message ?: "unknown error")
                        listener?.onLoad(OnLoadListener.Source.NETWORK, OnLoadListener.FAILURE)
                    }
                    .collect()
            } finally {
                _loading.value = false
            }
        }
    }
}