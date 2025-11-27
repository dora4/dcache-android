package dora.db.type

/**
 * A data type mapping class for Float values, mapping Kotlin/Java Float
 * to the database REAL type.
 * 简体中文：Float 类型的数据类型映射类，用于将 Kotlin/Java 的 Float
 * 映射到数据库中的 REAL 类型。
 */
class FloatType : DataType(SqlType.REAL) {

    /**
     * Returns the list of supported Java types for this data type.Here it returns Float.class,
     * meaning this type handles Float values.
     * 简体中文：该类型所支持的实际 Java 类型列表。此处返回 Float.class，表示该类型处理 Float 数据。
     */
    override val types: Array<Class<*>>
        get() = arrayOf(Float::class.java)

    companion object {
        val INSTANCE = FloatType()
    }
}