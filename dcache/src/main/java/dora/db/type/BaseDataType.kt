package dora.db.type

import dora.db.DataMatcher
import java.lang.reflect.Field

abstract class BaseDataType(val sqlType: SqlType) : DataMatcher {

    override fun matches(field: Field): Boolean {
        val types = types
        for (type in types) {
            if (type.isAssignableFrom(field!!.type)) {
                return true
            }
        }
        return false
    }
}