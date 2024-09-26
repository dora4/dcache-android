package dora.db.constraint

/**
 * Check constraint.
 * 简体中文：检查约束。
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Check(val value: String) 