package dora.db.table

import java.io.Serializable

class PrimaryKeyId(id: Long) : PrimaryKeyEntry(OrmTable.INDEX_ID, id), Serializable