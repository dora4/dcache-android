package dora.db.type

class ByteType : DataType(SqlType.INTEGER) {

    override val types: Array<Class<*>>
        get() = arrayOf(Byte::class.java)

    companion object {
        val INSTANCE = ByteType()
    }
}