package dora.db.constraint

import dora.db.table.OrmTable
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ForeignKey(val tableClass: KClass<out OrmTable>)
