package dora.cache.repository

import android.content.Context
import dora.db.table.OrmTable

abstract class DoraMMKVCacheRepository<T : OrmTable>(context: Context)
    : BaseMMKVCacheRepository<T>(context)