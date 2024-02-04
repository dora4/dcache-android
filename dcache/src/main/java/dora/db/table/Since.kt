package dora.db.table

/**
 * 标记表的字段名称开始的版本。
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class Since(val columnName: String, val version: Int = 1)