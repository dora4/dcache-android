package dora.db.constraint

/**
 * 主键约束。
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class PrimaryKey(val value: AssignType) 