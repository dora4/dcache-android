package dora.db.type

/**
 * A data type mapping class for Class values, mapping Kotlin/Java Class
 * to the database TEXT type.
 * 简体中文：Class 类型的数据类型映射类，用于将 Kotlin/Java 的 Class
 * 映射到数据库中的 TEXT 类型。
 */
class ClassType : DataType(SqlType.TEXT) {

    /**
     * Returns the list of supported Java types for this data type.Here it returns Class.class,
     * meaning this type handles Class values.
     * 简体中文：该类型所支持的实际 Java 类型列表。此处返回 Class.class，表示该类型处理 Class 数据。
     */
    override val types: Array<Class<*>>
        get() = arrayOf(Class::class.java)

    companion object {
        val INSTANCE = ClassType()
    }
}