package dora.cache.repository.compose.flow

/**
 * UI state holder for Compose layer.
 * 简体中文：Compose 层 UI 状态容器。
 */
sealed class UiState<out T> {

    /**
     * Initial idle state before any loading starts.
     * 简体中文：初始空闲状态，尚未开始加载数据。
     */
    object Idle : UiState<Nothing>()

    /**
     * Loading state.
     * 简体中文：加载状态。
     */
    object Loading : UiState<Nothing>()

    /**
     * Success state with data payload.
     * Includes data source information (cache or network).
     * 简体中文：成功状态，包含数据及数据来源（缓存或网络）。
     */
    data class Success<T>(
        val data: T,
        val source: Source = Source.NETWORK
    ) : UiState<T>()

    /**
     * Empty state when data is successfully loaded but has no content.
     * 简体中文：空数据状态，表示请求成功但无数据。
     */
    object Empty : UiState<Nothing>()

    /**
     * Error state when loading or processing fails.
     * Optional error message and error code.
     * 简体中文：错误状态，表示加载或处理失败，可携带错误信息与错误码。
     */
    data class Error(
        val msg: String? = null,
        val code: Int? = null
    ) : UiState<Nothing>()

    /**
     * Source of data origin.
     * 简体中文：数据来源标识。
     */
    enum class Source {

        /**
         * Data loaded from local cache.
         * 简体中文：数据来源于本地缓存。
         */
        CACHE,

        /**
         * Data loaded from network server.
         * 简体中文：数据来源于网络请求。
         */
        NETWORK
    }
}