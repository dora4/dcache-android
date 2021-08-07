package dora.db.type

class StringType : DataType(SqlType.TEXT) {

    override val types: Array<Class<*>>
        get() = arrayOf(String::class.java)

    companion object {
        val INSTANCE = StringType()
    }
}