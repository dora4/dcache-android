package dora.db.type

class LongType : DataType(SqlType.INTEGER) {

    override val types: Array<Class<*>>
        get() = arrayOf(Long::class.java)

    companion object {
        val INSTANCE = LongType()
    }
}