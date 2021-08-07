package dora.db.type

class DoubleType : DataType(SqlType.REAL) {

    override val types: Array<Class<*>>
        get() = arrayOf(Double::class.java)

    companion object {
        val INSTANCE = DoubleType()
    }
}