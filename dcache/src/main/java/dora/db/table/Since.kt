package dora.db.table

/**
 * Marks the starting database version for the table's name and table's column name.
 * 简体中文：标记表名和表的列名开始的数据库版本。
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class Since(val version: Int)