package dora.db.type

import java.lang.reflect.Field

/**
 * 匹配表字段的类型应该在数据中使用何种类型。
 */
interface DataMatcher {

    // 转为kotlin后其实也没必要使用数组
    val types: Array<Class<*>>

    /**
     * 通过field检测是否匹配类型。
     */
    fun matches(field: Field): Boolean

    /**
     * 通过fieldType检测是否匹配类型。
     */
    fun matches(fieldType: Class<*>): Boolean
}