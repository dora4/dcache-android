package dora.db.type

import java.lang.reflect.Field

/**
 * Base class for data types, encapsulating the mapping between
 * database column types and Kotlin/Java types.
 * 简体中文：数据类型基类，封装数据库字段类型与 Kotlin/Java 类型的映射关系。
 *
 * @property sqlType The corresponding database column type. 简体中文：对应数据库中的字段类型
 */
abstract class DataType(val sqlType: SqlType) : DataMatcher {

    /**
     * Determines if the field matches this data type based on a Field object.
     * 简体中文：根据 Field 对象判断字段类型是否匹配。
     *
     * @param field The table field to be checked. 简体中文：要检测的表字段
     * @return true if matches, false otherwise. 简体中文：如果匹配，否则 false
     */
    override fun matches(field: Field): Boolean {
        return matches(field.type)
    }

    /**
     * Determines if the given field type matches this data type.
     * 简体中文：根据字段类型判断是否匹配。
     *
     * @param fieldType The type of the field. 简体中文：字段类型
     * @return true if matches, false otherwise. 简体中文：如果匹配，否则 false
     */
    override fun matches(fieldType: Class<*>): Boolean {
        val types = types
        for (type in types) {
            if (type.isAssignableFrom(fieldType)) {
                return true
            }
        }
        return false
    }
}
