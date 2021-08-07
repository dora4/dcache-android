package dora.db.type

class ByteArrayType : DataType(SqlType.BLOB) {

    override val types: Array<Class<*>>
        get() = arrayOf(ByteArray::class.java)

    companion object {
        val INSTANCE = ByteArrayType()
    }
}