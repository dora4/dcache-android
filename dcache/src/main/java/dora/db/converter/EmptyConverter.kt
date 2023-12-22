package dora.db.converter

/**
 * 默认实现，空实现，一般不会用在实际场景。
 */
class EmptyConverter : PropertyConverter<Any, Any> {

    override fun convertToEntityProperty(databaseValue: Any?): Any? {
        return null
    }

    override fun convertToDatabaseValue(entityProperty: Any?): Any? {
        return null
    }
}