package dora.cache.repository

import android.content.Context
import dora.db.table.OrmTable

abstract class DoraFlowMMKVCacheRepository<T: OrmTable>(context: Context)
    : BaseFlowMMKVCacheRepository<T>(context)