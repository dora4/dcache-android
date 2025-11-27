package dora.db.type

/**
 * A data type mapping class for Byte values, mapping Kotlin/Java Byte
 * to the database INTEGER type.
 * 简体中文：Byte 类型的数据类型映射类，用于将 Kotlin/Java 的 Byte
 * 映射到数据库中的 INTEGER 类型。
 */
class ByteType : DataType(SqlType.INTEGER) {

    /**
     * Returns the list of supported Java types for this data type.Here it returns Byte.class,
     * meaning this type handles Byte values.
     * 简体中文：该类型所支持的实际 Java 类型列表。此处返回 Byte.class，表示该类型处理 Byte 数据。
     */
    override val types: Array<Class<*>>
        get() = arrayOf(Byte::class.java)

    companion object {
        val INSTANCE = ByteType()
    }
}