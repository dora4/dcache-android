package dora.db.table

import java.io.Serializable

/**
 * 主键。
 */
open class PrimaryKeyEntry(primaryKeyName: String) : Serializable {

    var name: String = primaryKeyName
        private set
}