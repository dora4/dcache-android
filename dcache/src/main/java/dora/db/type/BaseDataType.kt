package dora.db.type

import dora.db.DataMatcher
import java.lang.reflect.Field

abstract class BaseDataType(val sqlType: SqlType) : DataMatcher {

    override fun matches(field: Field): Boolean {
        return matches(field.type)
    }

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