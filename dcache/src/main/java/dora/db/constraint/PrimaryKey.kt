package dora.db.constraint

/**
 * Primary key constraint.
 * 简体中文：主键约束。
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class PrimaryKey(val value: AssignType) 