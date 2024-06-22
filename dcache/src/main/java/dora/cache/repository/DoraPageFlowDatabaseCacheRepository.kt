package dora.cache.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import dora.cache.data.fetcher.OnLoadStateListener
import dora.db.builder.Condition
import dora.db.builder.QueryBuilder
import dora.db.table.OrmTable
import kotlinx.coroutines.flow.count

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

    suspend fun observeData(adapter: AdapterDelegate<T>) {
        getListFlowData().collect {
            if (pageNo == 0) {
                adapter.setList(it)
            } else {
                adapter.addData(it)
            }
        }
    }

    interface AdapterDelegate<T> {

        fun setList(data: MutableList<T>)
        fun addData(data: MutableList<T>)
    }

    /**
     * 下拉刷新回调，可结合[setPageSize]使用。
     */
    fun onRefresh(listener: OnLoadStateListener) {
        pageNo = 0
        fetchListData(listener = listener)
    }

    /**
     * 下拉刷新高阶函数，可结合[setPageSize]使用。
     */
    @JvmOverloads
    fun onRefresh(block: ((Boolean) -> Unit)? = null) {
        pageNo = 0
        fetchListData(listener = object : OnLoadStateListener {
            override fun onLoad(state: Int) {
                block?.invoke(state == OnLoadStateListener.SUCCESS)
            }
        })
    }

    /**
     * 上拉加载回调，可结合[setPageSize]使用。
     */
    fun onLoadMore(listener: OnLoadStateListener) {
        pageNo++
        fetchListData(listener = listener)
    }

    /**
     * 上拉加载高阶函数，可结合[setPageSize]使用。
     */
    @JvmOverloads
    fun onLoadMore(block: ((Boolean) -> Unit)? = null) {
        pageNo++
        fetchListData(listener = object : OnLoadStateListener {
            override fun onLoad(state: Int) {
                block?.invoke(state == OnLoadStateListener.SUCCESS)
            }
        })
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