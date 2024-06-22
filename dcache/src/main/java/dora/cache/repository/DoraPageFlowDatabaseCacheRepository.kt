package dora.cache.repository

import android.content.Context
import android.util.Log
import dora.cache.holder.ListDatabaseCacheHolder
import dora.db.builder.Condition
import dora.db.builder.QueryBuilder
import dora.db.table.OrmTable
import kotlinx.coroutines.flow.MutableStateFlow
import java.lang.IllegalArgumentException

abstract class DoraPageFlowDatabaseCacheRepository<M, T : OrmTable>(context: Context)
    : DoraFlowDatabaseCacheRepository<T>(context) {

    private var pageNo: Int = 0
    private var pageSize: Int = 10

    fun getPageNo(): Int {
        return pageNo
    }

    fun getPageSize(): Int {
        return pageSize
    }

    override fun disallowForceUpdate(): Boolean {
        return true
    }

    fun isLastPage(pageNo: Int) : Boolean {
        val size = getListFlowData().value.size
        val lastPage = if (size % pageSize == 0) size / pageSize - 1 else size / pageSize
        return lastPage == pageNo
    }

    open fun setCurrentPage(pageNo: Int, pageSize: Int): DoraPageFlowDatabaseCacheRepository<M, T> {
        this.pageNo = pageNo
        this.pageSize = pageSize
        return this
    }

    override fun query(): Condition {
        val start = pageNo * pageSize
        val end = start + pageSize
        return QueryBuilder.create()
                .limit(start, end)
                .toCondition()
    }

    /**
     * 没网的情况下直接加载缓存数据。
     */
    override fun selectData(ds: DataSource): Boolean {
        var isLoaded = false
        if (!isNetworkAvailable) {
            isLoaded = ds.loadFromCache(DataSource.CacheType.DATABASE)
        }
        return if (isNetworkAvailable) {
            try {
                ds.loadFromNetwork()
                true
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
                isLoaded
            }
        } else isLoaded
    }
}