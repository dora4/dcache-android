package dora.cache.repository

/**
 * Each specific Repository class must be annotated with either @Repository or @ListRepository to
 * indicate whether it is for List type data or non-List type data.
 * 简体中文：每一个具体的 Repository 类必须配置 @Repository 或 @ListRepository 中的一个，
 * 用来标记为 List 类型的数据或非 List 类型的数据。
 *
 * @param isLogPrint
 *   Whether to print debug logs. If true, the description in BaseRepository.fetchData() will be printed.
 *   简体中文：是否打印调试日志，如设置为 true，则 BaseRepository.fetchData() 中的 description 参数将被打印。
 *
 * @param isNotify
 *   Whether to also send a notification for memory cache at the same time.
 *   简体中文：是否同时发送通知用于内存缓存。
 *
 * @param dropLatestOnSameParams
 *   When the parameter values are the same, apply backpressure by retaining all requests by default.
 *   If true, drop the most recent (latest) request instead of retaining it.
 *   简体中文：参数相同时，默认保留所有请求。若设置为 true，则丢弃最新的一次请求。
 *
 * @param dropPreviousOnDifferentParams
 *   When the parameter values are different, apply backpressure by retaining all requests by default.
 *   If true, drop all previous requests and keep only the latest one.
 *   简体中文：参数不同时，默认保留所有请求。若设置为 true，则丢弃之前所有请求，只保留最新的一次。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Repository(
    val isLogPrint: Boolean = false,
    val isNotify: Boolean = false,
    val dropLatestOnSameParams: Boolean = false,
    val dropPreviousOnDifferentParams: Boolean = false
)
