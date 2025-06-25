package dora.db.constraint

import dora.db.table.OrmTable
import kotlin.reflect.KClass

/**
 * Foreign key constraint.
 * 简体中文：外键约束。
 * @since 3.4.2
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ForeignKey(val tableClass: KClass<out OrmTable>)
