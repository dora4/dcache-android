package dora.db.constraint

/**
 * Default constraint.
 * 简体中文：默认约束。
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Default(val value: String) 