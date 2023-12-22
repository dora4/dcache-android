package dora.db.table

import java.io.Serializable

/**
 * 主键。
 */
open class PrimaryKeyEntry : Serializable {

    var name: String
        private set
    var value: String
        private set

    constructor(primaryKeyName: String, primaryKeyValue: Number) {
        name = primaryKeyName
        value = primaryKeyValue.toString()
    }

    constructor(primaryKeyName: String, primaryKeyValue: String) {
        name = primaryKeyName
        value = primaryKeyValue
    }
}