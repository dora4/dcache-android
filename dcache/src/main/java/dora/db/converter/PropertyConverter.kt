package dora.db.converter

/**
 * Property converter, used to convert entity class objects and the values stored in the database
 * file.
 * 简体中文：属性转换器，用于转换实体类对象和存储在数据库文件中的值。
 */
interface PropertyConverter<P, D> {

    /**
     * Convert database values to an entity class. Decode the database values and assign them to
     * the entity class.
     * 简体中文：数据库的值转换为实体类。解析数据库的值解码后赋值给实体类。
     */
    fun convertToEntityProperty(databaseValue: D?): P?

    /**
     * Convert  an entity class to database values. Encode the entity class and save it to the
     * database.
     * 简体中文：实体类转换为数据库的值。将实体类编码后保存到数据库。
     */
    fun convertToDatabaseValue(entityProperty: P?): D?
}