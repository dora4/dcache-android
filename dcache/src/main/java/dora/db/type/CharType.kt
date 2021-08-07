package dora.db.type

class CharType : DataType(SqlType.TEXT) {

    override val types: Array<Class<*>>
        get() = arrayOf(Char::class.java)

    companion object {
        val INSTANCE = CharType()
    }
}