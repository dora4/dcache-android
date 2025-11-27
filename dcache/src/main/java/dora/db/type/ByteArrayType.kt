package dora.db.type

/**
 * A data type mapping class for ByteArray values, mapping Kotlin/Java ByteArray
 * to the database BLOB type.
 * 简体中文：ByteArray 类型的数据类型映射类，用于将 Kotlin/Java 的 ByteArray
 * 映射到数据库中的 BLOB 类型。
 */
class ByteArrayType : DataType(SqlType.BLOB) {

    /**
     * Returns the list of supported Java types for this data type.Here it returns ByteArray.class,
     * meaning this type handles ByteArray values.
     * 简体中文：该类型所支持的实际 Java 类型列表。此处返回 ByteArray.class，表示该类型处理 ByteArray 数据。
     */
    override val types: Array<Class<*>>
        get() = arrayOf(ByteArray::class.java)

    companion object {
        val INSTANCE = ByteArrayType()
    }
}