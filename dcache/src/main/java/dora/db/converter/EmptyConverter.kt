package dora.db.converter

import dora.db.table.PropertyConverter

class EmptyConverter : PropertyConverter<Any, Any> {

    override fun convertToEntityProperty(databaseValue: Any?): Any? {
        return null
    }

    override fun convertToDatabaseValue(entityProperty: Any?): Any? {
        return null
    }
}