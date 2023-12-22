package dora.db.table

import java.io.Serializable

/**
 * 使用ID作为主键。
 */
class PrimaryKeyId(id: Long) : PrimaryKeyEntry(OrmTable.INDEX_ID, id), Serializable