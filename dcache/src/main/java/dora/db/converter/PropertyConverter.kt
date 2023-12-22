package dora.db.converter

/**
 * 属性转换器，将内存中的对象和存储在数据库文件中的值相互转换。
 */
interface PropertyConverter<P, D> {

    /**
     * 数据库的值转换为实体类。
     */
    fun convertToEntityProperty(databaseValue: D?): P?

    /**
     * 实体类转换为数据库的值。
     */
    fun convertToDatabaseValue(entityProperty: P?): D?
}