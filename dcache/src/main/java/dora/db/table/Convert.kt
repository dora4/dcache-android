package dora.db.table

import dora.db.converter.PropertyConverter
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Convert(val converter: KClass<out PropertyConverter<*, *>>, val columnType: KClass<*>)