package dora.db.table

/**
 * 自定义字段名。
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Column(val value: String) 