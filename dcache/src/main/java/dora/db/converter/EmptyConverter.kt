package dora.db.converter

class EmptyConverter : PropertyConverter<Any, Any> {

    override fun convertToEntityProperty(databaseValue: Any?): Any? {
        return null
    }

    override fun convertToDatabaseValue(entityProperty: Any?): Any? {
        return null
    }
}