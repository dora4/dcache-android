package dora.cache.repository

import android.content.Context
import dora.cache.factory.DoraDatabaseCacheHolderFactory
import dora.db.table.OrmTable

abstract class DoraDatabaseCacheRepository<T : OrmTable>(context: Context)
    : BaseDatabaseCacheRepository<T, DoraDatabaseCacheHolderFactory<T>>(context)