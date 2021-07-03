package dora.db

class OrmConfig private constructor(builder: Builder) {

    val databaseName: String
    val versionCode: Int
    val tables: Array<Class<out OrmTable>>?

    class Builder {
        lateinit var databaseName: String
        var versionCode = 1
        var tables: Array<Class<out OrmTable>>? = null
        fun database(name: String): Builder {
            databaseName = name
            return this
        }

        fun version(code: Int): Builder {
            versionCode = code
            return this
        }

        fun tables(vararg tables: Class<out OrmTable>): Builder {
            this.tables = arrayOf(*tables)
            return this
        }

        fun build(): OrmConfig {
            return OrmConfig(this)
        }
    }

    init {
        databaseName = builder.databaseName
        versionCode = builder.versionCode
        tables = builder.tables
    }
}