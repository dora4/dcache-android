package dora.db.table

open class PrimaryKeyEntity {

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