package dora.db.converter

/**
 * Default empty implementation; this class is generally not used in practical scenarios.
 * 简体中文：默认的空实现，这个类一般不会用在实际场景。
 */
class EmptyConverter : PropertyConverter<Any, Any> {

    override fun convertToEntityProperty(databaseValue: Any?): Any? {
        return null
    }

    override fun convertToDatabaseValue(entityProperty: Any?): Any? {
        return null
    }
}