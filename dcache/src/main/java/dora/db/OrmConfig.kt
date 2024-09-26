package dora.db

import dora.db.table.OrmTable

/**
 * ORM configuration information class, which specifies the global ORM configuration details using
 * [Orm.init] during application initialization.
 * 简体中文：ORM配置信息类，在应用初始化时，使用[Orm.init]指定全局的ORM配置信息。
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