package dora.db.type

/**
 * A data type mapping class for Int values, mapping Kotlin/Java Int
 * to the database INTEGER type.
 * 简体中文：Int 类型的数据类型映射类，用于将 Kotlin/Java 的 Int
 * 映射到数据库中的 INTEGER 类型。
 */
class IntType : DataType(SqlType.INTEGER) {

    /**
     * Returns the list of supported Java types for this data type.Here it returns Int.class,
     * meaning this type handles Int values.
     * 简体中文：该类型所支持的实际 Java 类型列表。此处返回 Int.class，表示该类型处理 Int 数据。
     */
    override val types: Array<Class<*>>
        get() = arrayOf(Int::class.java)

    companion object {
        val INSTANCE = IntType()
    }
}