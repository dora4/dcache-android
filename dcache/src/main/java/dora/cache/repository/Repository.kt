package dora.cache.repository

/**
 * 每一个具体的Repository类必须配置@Repository或@ListRepository中的一个，用来标记为List类型的数据或非List
 * 类型的数据。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Repository(

    /**
     * 是否打印调试日志，如设置为true，则[dora.cache.repository.BaseRepository]的fetchData()中的
     * description参数将被打印出来。
     */
    val isLogPrint: Boolean = false
)