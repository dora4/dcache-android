package dora.db.type

class FloatType : BaseDataType(SqlType.REAL) {

    override val types: Array<Class<*>>
        get() = arrayOf(Float::class.java)

    companion object {
        val INSTANCE = FloatType()
    }
}