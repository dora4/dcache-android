package dora.db.type

/**
 * A data type mapping class for Short values, mapping Kotlin/Java Short
 * to the database INTEGER type.
 * 简体中文：Short 类型的数据类型映射类，用于将 Kotlin/Java 的 Short
 * 映射到数据库中的 INTEGER 类型。
 */
class ShortType : DataType(SqlType.INTEGER) {

    /**
     * Returns the list of supported Java types for this data type.Here it returns Short.class,
     * meaning this type handles Short values.
     * 简体中文：该类型所支持的实际 Java 类型列表。此处返回 Short.class，表示该类型处理 Short 数据。
     *
     */
    override val types: Array<Class<*>>
        get() = arrayOf(Short::class.java)

    companion object {
        val INSTANCE = ShortType()
    }
}