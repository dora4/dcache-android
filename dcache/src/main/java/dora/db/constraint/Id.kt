package dora.db.constraint

/**
 * 等效于@Column("_id") + @PrimaryKey(AssignType.AUTO_INCREMENT)。
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Id 