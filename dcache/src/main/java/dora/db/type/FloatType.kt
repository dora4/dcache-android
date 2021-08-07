package dora.db.type

class FloatType : DataType(SqlType.REAL) {

    override val types: Array<Class<*>>
        get() = arrayOf(Float::class.java)

    companion object {
        val INSTANCE = FloatType()
    }
}