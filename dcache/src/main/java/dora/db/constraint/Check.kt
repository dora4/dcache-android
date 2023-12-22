package dora.db.constraint

/**
 * 检查约束。
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Check(val value: String) 