package dora.cache.repository.compose.flow

/**
 * UI event stream (one-time events).
 * 简体中文：UI事件流（一次性事件）。
 */
sealed class UiEvent {

    /**
     * Show toast message.
     * 简体中文：显示 Toast 提示。
     */
    data class Toast(
        val msg: String
    ) : UiEvent()

    /**
     * Show snackbar message with optional action.
     * 简体中文：显示 Snackbar 提示（可带操作按钮）。
     */
    data class Snackbar(
        val msg: String,
        val action: String? = null
    ) : UiEvent()

    /**
     * Navigate to another route/page.
     * 简体中文：页面路由跳转。
     */
    data class Navigate(
        val route: String
    ) : UiEvent()

    /**
     * Pop current page.
     * 简体中文：返回上一页。
     */
    object PopBack : UiEvent()

    /**
     * Refresh current screen/data.
     * 简体中文：刷新当前页面或数据。
     */
    object Refresh : UiEvent()

    /**
     * Show dialog.
     * 简体中文：显示对话框。
     */
    data class Dialog(
        val title: String,
        val msg: String
    ) : UiEvent()
}