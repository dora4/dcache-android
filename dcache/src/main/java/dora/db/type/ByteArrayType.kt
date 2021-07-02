package dora.db.type

class ByteArrayType : BaseDataType(SqlType.BLOB) {

    override val types: Array<Class<*>>
        get() = arrayOf(ByteArray::class.java)

    companion object {
        val INSTANCE = ByteArrayType()
    }
}