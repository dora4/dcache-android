package dora.db.type

class IntType : DataType(SqlType.INTEGER) {

    override val types: Array<Class<*>>
        get() = arrayOf(Int::class.java)

    companion object {
        val INSTANCE = IntType()
    }
}