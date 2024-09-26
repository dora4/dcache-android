package dora.db.table

/**
 * Use it to specify a custom table name.
 * 简体中文：用它指定自定义的表名。
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Table(val value: String) 