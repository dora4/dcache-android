package dora.cache.repository.compose

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dora.cache.data.fetcher.OnLoadListener
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Compose-first List repository (Flow only).
 * 简体中文：Compose列表Repository，仅使用Flow。
 */
abstract class BaseListComposeRepository<M>(
    val context: Context
) : ViewModel() {

    /**
     * UI state flow.
     * 简体中文：UI状态流。
     */
    protected val _state = MutableStateFlow<List<M>>(emptyList())
    val state: StateFlow<List<M>> = _state.asStateFlow()

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
     * 简体中文：Flow网络请求（返回列表）。
     */
    protected abstract fun onLoadFromNetwork(): Flow<List<M>>

    /**
     * Fetch data.
     * 简体中文：获取列表数据。
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