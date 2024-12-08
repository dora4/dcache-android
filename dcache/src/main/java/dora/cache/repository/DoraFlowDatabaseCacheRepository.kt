package dora.cache.repository

import android.content.Context
import dora.cache.factory.DoraDatabaseCacheHolderFactory
import dora.db.table.OrmTable

abstract class DoraFlowDatabaseCacheRepository<T: OrmTable>(context: Context)
    : BaseFlowDatabaseCacheRepository<T, DoraDatabaseCacheHolderFactory<T>>(context)