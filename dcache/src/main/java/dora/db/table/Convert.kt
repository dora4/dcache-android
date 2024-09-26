package dora.db.table

import dora.db.converter.PropertyConverter
import kotlin.reflect.KClass

/**
 * Use it to specify a converter [PropertyConverter] for non-basic data types.
 * 简体中文：用它指定非基本数据类型的转换器[PropertyConverter]。
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Convert(val converter: KClass<out PropertyConverter<*, *>>, val columnType: KClass<*>)