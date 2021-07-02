package dora.db.type

class ClassType : BaseDataType(SqlType.TEXT) {

    override val types: Array<Class<*>>
        get() = arrayOf(Class::class.java)

    companion object {
        val INSTANCE = ClassType()
    }
}