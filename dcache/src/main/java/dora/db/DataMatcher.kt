package dora.db

import java.lang.reflect.Field

interface DataMatcher {

    val types: Array<Class<*>>
    fun matches(field: Field): Boolean
    fun matches(fieldType: Class<*>): Boolean
}