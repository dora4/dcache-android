package dora.cache.repository

/**
 * Each specific Repository class must be annotated with either @Repository or @ListRepository to
 * indicate whether it is for List type data or non-List type data.
 * 简体中文：每一个具体的Repository类必须配置@Repository或@ListRepository中的一个，用来标记为List类型的数据或非
 * List类型的数据。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Repository(

    /**
     * Whether to print debug logs. If set to true, the description parameter in the fetchData()
     * method of [dora.cache.repository.BaseRepository] will be printed.
     * 简体中文：是否打印调试日志，如设置为true，则[dora.cache.repository.BaseRepository]的fetchData()中的
     * description参数将被打印出来。
     */
    val isLogPrint: Boolean = false,

    val isNotify: Boolean = false
)