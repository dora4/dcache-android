package dora.db.type

class BooleanType : DataType(SqlType.INTEGER) {

    override val types: Array<Class<*>>
        get() = arrayOf(Boolean::class.java)

    companion object {
        val INSTANCE = BooleanType()
    }
}