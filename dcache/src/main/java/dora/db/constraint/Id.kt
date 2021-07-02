package dora.db.table

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * 等效于@Column("_id") + @PrimaryKey(AssignType.AUTO_INCREMENT)。
 */
@Target(AnnotationTarget.FIELD)
@Retention(RetentionPolicy.RUNTIME)
annotation class Id 