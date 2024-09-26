package dora.db.table

/**
 * Use it to specify a custom column name.
 * 简体中文：用它指定自定义的列名。
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Column(val value: String) 