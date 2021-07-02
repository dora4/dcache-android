package dora.db.type

class ShortType : BaseDataType(SqlType.INTEGER) {
    override val types: Array<Class<*>>
        get() = arrayOf(Short::class.java)

    companion object {
        val INSTANCE = ShortType()
    }
}