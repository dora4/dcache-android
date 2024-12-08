package dora.cache.repository

import android.content.Context
import dora.db.table.OrmTable

abstract class DoraFlowDatabaseCacheRepository<T: OrmTable>(context: Context)
    : BaseFlowDatabaseCacheRepository<T>(context)