package dora.cache.repository

import android.content.Context
import dora.db.table.OrmTable

abstract class DoraDatabaseCacheRepository<T : OrmTable>(context: Context)
    : BaseDatabaseCacheRepository<T>(context)