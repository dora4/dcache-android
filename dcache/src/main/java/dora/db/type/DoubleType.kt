package dora.db.type

/**
 * A data type mapping class for Double values, mapping Kotlin/Java Double
 * to the database REAL type.
 * 简体中文：Double 类型的数据类型映射类，用于将 Kotlin/Java 的 Double
 * 映射到数据库中的 REAL 类型。
 */
class DoubleType : DataType(SqlType.REAL) {

    /**
     * Returns the list of supported Java types for this data type.Here it returns Double.class,
     * meaning this type handles Double values.
     * 简体中文：该类型所支持的实际 Java 类型列表。此处返回 Double.class，表示该类型处理 Double 数据。
     */
    override val types: Array<Class<*>>
        get() = arrayOf(Double::class.java)

    companion object {
        val INSTANCE = DoubleType()
    }
}