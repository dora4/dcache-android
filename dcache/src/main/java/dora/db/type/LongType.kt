package dora.db.type

/**
 * A data type mapping class for Long values, mapping Kotlin/Java Long
 * to the database INTEGER type.
 * 简体中文：Long 类型的数据类型映射类，用于将 Kotlin/Java 的 Long
 * 映射到数据库中的 INTEGER 类型。
 */
class LongType : DataType(SqlType.INTEGER) {

    /**
     * Returns the list of supported Java types for this data type.Here it returns Long.class,
     * meaning this type handles Long values.
     * 简体中文：该类型所支持的实际 Java 类型列表。此处返回 Long.class，表示该类型处理 Long 数据。
     */
    override val types: Array<Class<*>>
        get() = arrayOf(Long::class.java)

    companion object {
        val INSTANCE = LongType()
    }
}