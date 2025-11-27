package dora.db.type

import java.lang.reflect.Field

/**
 * Interface for matching data types, used to determine which data type
 * a table field should use in the database.
 * 简体中文：数据类型匹配器接口，用于判断表字段在数据库中应使用哪种数据类型。
 */
interface DataMatcher {

    /**
     * After converting from Java to Kotlin, using an array here is actually unnecessary.
     * 简体中文：从Java转为Kotlin后，其实也没必要使用数组。
     */
    val types: Array<Class<*>>

    /**
     * Checks if the field matches this data type based on a Field object.
     * 简体中文：根据 Field 对象判断字段类型是否匹配。
     *
     * @param field The table field to be checked. 简体中文：要检测的表字段
     * @return true if matches, false otherwise 简体中文：如果匹配，否则 false
     */
    fun matches(field: Field): Boolean

    /**
     * Checks if the field type matches this data type.
     * 简体中文：根据字段类型判断是否匹配。
     *
     * @param fieldType The type of the field. 简体中文：字段类型
     * @return true if matches, false otherwise 简体中文：如果匹配，否则 false
     */
    fun matches(fieldType: Class<*>): Boolean
}
