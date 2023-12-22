package dora.db.constraint

/**
 * 默认约束。
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Default(val value: String) 