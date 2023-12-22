package dora.db.constraint

/**
 * 主键约束使用自增id，等效于@Column("_id") + @PrimaryKey(AssignType.AUTO_INCREMENT)。
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Id 