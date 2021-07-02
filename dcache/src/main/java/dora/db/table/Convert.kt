package dora.db.table

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(RetentionPolicy.RUNTIME)
annotation class Convert(val converter: KClass<out PropertyConverter<*, *>>, val columnType: KClass<*>)