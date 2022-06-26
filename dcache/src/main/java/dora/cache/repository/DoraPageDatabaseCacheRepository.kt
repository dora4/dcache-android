package dora.cache.repository

import android.content.Context
import dora.cache.holder.CacheHolder
import dora.cache.holder.DoraCacheHolder
import dora.cache.holder.DoraListCacheHolder
import dora.db.builder.Condition
import dora.db.builder.QueryBuilder
import dora.db.builder.WhereBuilder
import dora.db.table.OrmTable

@RepositoryType(BaseRepository.CacheStrategy.DATABASE_CACHE_NO_NETWORK)
abstract class DoraPageDatabaseCacheRepository<T : OrmTable>(context: Context)
    : BaseDatabaseCacheRepository<T>(context) {

    private var pageNo: Int = 0
    private var pageSize: Int = 10

    fun getPageNo(): Int {
        return pageNo
    }

    fun getPageSize(): Int {
        return pageSize
    }

    open fun setCurrentPage(pageNo: Int, pageSize: Int): DoraPageDatabaseCacheRepository<T> {
        this.pageNo = pageNo
        this.pageSize = pageSize
        return this
    }

    override fun query(): Condition {
        val cacheSize = cacheHolder.queryCacheSize(
                WhereBuilder.create().toCondition())
        // 越界保护
        val start = pageNo * pageSize
        val end = start + pageSize
        return if (end >= cacheSize) {
            QueryBuilder.create()
                    .limit(0)
                    .toCondition()
        } else
            QueryBuilder.create()
                    .limit(start, end)
                    .toCondition()

    }

    override fun createCacheHolder(clazz: Class<T>): CacheHolder<T> {
        return DoraCacheHolder<T, T>(clazz)
    }

    override fun createListCacheHolder(clazz: Class<T>): CacheHolder<MutableList<T>> {
        return DoraListCacheHolder<T, T>(clazz)
    }
}