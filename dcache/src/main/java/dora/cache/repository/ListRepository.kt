package dora.cache.repository

/**
 * 每一个具体的Repository类必须配置这个注解，用来标记为List类型的数据。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ListRepository(

    /**
     * 是否打印调试日志，如设置为true，则[dora.cache.repository.BaseRepository]的fetchData()或
     * fetchListData()中的description参数将被打印出来。
     */
    val isLogPrint: Boolean = false
)