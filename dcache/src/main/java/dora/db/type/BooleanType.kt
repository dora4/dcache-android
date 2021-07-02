package dora.db.type

class BooleanType : BaseDataType(SqlType.INTEGER) {
    override val types: Array<Class<*>>
        get() = arrayOf(Boolean::class.java)

    companion object {
        val INSTANCE = BooleanType()
    }
}