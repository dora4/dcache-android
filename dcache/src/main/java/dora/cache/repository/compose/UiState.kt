package dora.cache.repository.compose

sealed class UiState<out T> {

    /**
     * Loading state.
     * 简体中文：加载中状态。
     */
    object Loading : UiState<Nothing>()

    /**
     * Success state.
     * 简体中文：成功状态。
     */
    data class Success<T>(val data: T, val fromCache: Boolean) : UiState<T>()

    /**
     * Error state.
     * 简体中文：错误状态。
     */
    data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>()
}