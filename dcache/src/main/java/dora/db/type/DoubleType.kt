package dora.db.type

class DoubleType : BaseDataType(SqlType.REAL) {

    override val types: Array<Class<*>>
        get() = arrayOf(Double::class.java)

    companion object {
        val INSTANCE = DoubleType()
    }
}