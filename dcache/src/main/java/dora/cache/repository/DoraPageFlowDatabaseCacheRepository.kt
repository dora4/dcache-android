package dora.cache.repository

import android.content.Context
import android.util.Log
import dora.cache.data.fetcher.OnLoadStateListener
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

    fun isLastPage(totalSize: Int) : Boolean {
        val lastPage = if (totalSize % pageSize == 0) totalSize / pageSize - 1 else totalSize / pageSize
        return lastPage == pageNo
    }

    /**
     * 下拉刷新回调，可结合[setPageSize]使用。
     */
    fun onRefresh(listener: OnLoadStateListener) {
        pageNo = 0
        fetchListData(listener = listener)
    }

    /**
     * 上拉加载回调，可结合[setPageSize]使用。
     */
    fun onLoadMore(listener: OnLoadStateListener) {
        pageNo++
        fetchListData(listener = listener)
    }

    override fun disallowForceUpdate(): Boolean {
        return true
    }

    open fun setPageSize(pageSize: Int): DoraPageFlowDatabaseCacheRepository<M, T> {
        this.pageSize = pageSize
        return this
    }

    open fun setCurrentPage(pageNo: Int, pageSize: Int): DoraPageFlowDatabaseCacheRepository<M, T> {
        this.pageNo = pageNo
        this.pageSize = pageSize
        return this
    }

    override fun query(): Condition {
        val start = pageNo * pageSize
        return QueryBuilder.create()
                .limit(start, pageSize)
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