package dora.cache.repository

import android.content.Context
import dora.cache.holder.DatabaseCacheHolder
import dora.cache.holder.DoraDatabaseCacheHolder
import dora.cache.holder.DoraListDatabaseCacheHolder
import dora.db.table.OrmTable

abstract class DoraFlowDatabaseCacheRepository<T: OrmTable>(context: Context)
    : BaseFlowDatabaseCacheRepository<T>(context) {

    override fun createCacheHolder(clazz: Class<T>): DatabaseCacheHolder<T> {
        return DoraDatabaseCacheHolder<T>(clazz)
    }

    override fun createListCacheHolder(clazz: Class<T>): DatabaseCacheHolder<MutableList<T>> {
        return DoraListDatabaseCacheHolder<T>(clazz)
    }
}