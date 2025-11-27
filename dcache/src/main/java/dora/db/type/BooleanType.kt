package dora.db.type

/**
 * A data type mapping class for Boolean values, mapping Kotlin/Java Boolean
 * to the database INTEGER type.
 * 简体中文：Boolean 类型的数据类型映射类，用于将 Kotlin/Java 的 Boolean
 * 映射到数据库中的 INTEGER 类型。
 */
class BooleanType : DataType(SqlType.INTEGER) {

    /**
     * Returns the list of supported Java types for this data type.Here it returns Boolean.class,
     * meaning this type handles Boolean values.
     * 简体中文：该类型所支持的实际 Java 类型列表。此处返回 Boolean.class，表示该类型处理 Boolean 数据。
     */
    override val types: Array<Class<*>>
        get() = arrayOf(Boolean::class.java)

    companion object {
        val INSTANCE = BooleanType()
    }
}