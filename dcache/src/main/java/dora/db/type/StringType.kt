package dora.db.type

/**
 * A data type mapping class for String values, mapping Kotlin/Java String
 * to the database TEXT type.
 * 简体中文：字符串类型的数据类型映射类，用于将 Kotlin/Java 的 String 类型
 * 映射到数据库中的 TEXT 类型。
 */
class StringType : DataType(SqlType.TEXT) {

    /**
     *
     * Returns the list of supported Java types for this data type.Here it returns String.class,
     * meaning this type only handles String.
     * 简体中文：该类型所支持的实际 Java 类型列表。此处返回 String.class，表示仅支持 String。
     *
     */
    override val types: Array<Class<*>>
        get() = arrayOf(String::class.java)

    companion object {
        val INSTANCE = StringType()
    }
}