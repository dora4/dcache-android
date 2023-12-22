package dora.db

import dora.db.table.OrmTable

/**
 * ORM配置类。
 */
class OrmConfig private constructor(builder: Builder) {

    internal val databaseName: String
    internal val versionCode: Int
    internal val tables: Array<Class<out OrmTable>>?

    class Builder {

        internal lateinit var databaseName: String
        internal var versionCode = 1
        internal var tables: Array<Class<out OrmTable>>? = null

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