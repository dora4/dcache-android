package dora.db.table

import dora.db.converter.PropertyConverter
import kotlin.reflect.KClass

/**
 * 配置数据类型转换器。
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Convert(val converter: KClass<out PropertyConverter<*, *>>, val columnType: KClass<*>)