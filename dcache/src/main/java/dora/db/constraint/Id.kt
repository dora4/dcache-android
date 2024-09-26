package dora.db.constraint

/**
 * The primary key constraint uses an auto-incrementing ID, equivalent to `@Column("_id")
 * + @PrimaryKey(AssignType.AUTO_INCREMENT)`.
 * 简体中文：主键约束使用自增的id，等效于@Column("_id") + @PrimaryKey(AssignType.AUTO_INCREMENT)。
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Id 