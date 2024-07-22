package dora.db.table

import java.io.Serializable

/**
 * 使用ID作为主键。
 */
class PrimaryKeyId: PrimaryKeyEntry(OrmTable.INDEX_ID), Serializable