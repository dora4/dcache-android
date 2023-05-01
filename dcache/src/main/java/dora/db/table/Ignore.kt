package dora.db.table

/**
 * 配置创建表时忽略的字段。
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Ignore 