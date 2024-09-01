package dora.cache.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dora.cache.data.fetcher.OnLoadStateListener
import dora.db.builder.Condition
import dora.db.builder.QueryBuilder
import dora.db.table.OrmTable
import dora.http.DoraCallback
import dora.http.DoraListCallback
import dora.cache.DoraPageListCallback
import dora.cache.data.fetcher.ListDataFetcher
import dora.cache.data.page.DataPager
import dora.cache.data.page.IDataPager
import dora.cache.holder.DoraListDatabaseCacheHolder
import dora.db.builder.WhereBuilder
import dora.db.dao.DaoFactory
import io.reactivex.Observable
import java.lang.IllegalArgumentException

@ListRepository
abstract class DoraPageDatabaseCacheRepository<T : OrmTable>(context: Context)
    : DoraDatabaseCacheRepository<T>(context) {

    private var pageNo: Int = 0
    private var pageSize: Int = 10
    private var totalSize: Int = 0

    open fun onLoadFromNetwork(callback: DoraPageListCallback<T>, listener: OnLoadStateListener?) {
        this.totalSize = callback.getTotalSize()
    }

    override fun createListDataFetcher(): ListDataFetcher<T> {
        return object : ListDataFetcher<T>() {

            override fun fetchListData(description: String?, listener: OnLoadStateListener?): LiveData<MutableList<T>> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType): Boolean {
                        if (type === DataSource.CacheType.DATABASE) {
                            return onLoadFromCacheList(liveData)
                        }
                        liveData.postValue(arrayListOf())
                        return false
                    }

                    override fun loadFromNetwork() {
                        try {
                            rxOnLoadFromNetworkForList(liveData, listener)
                            onLoadFromNetwork(listCallback(), listener)
                        } catch (ignore: Exception) {
                            listener?.onLoad(OnLoadStateListener.FAILURE)
                        }
                    }
                })
                return liveData
            }

            override fun listCallback(): DoraPageListCallback<T> {
                return object : DoraPageListCallback<T>() {
                    override fun onSuccess(totalSize: Int, models: MutableList<T>) {
                        super.onSuccess(totalSize, models)
                        parseModels(models, liveData)
                    }

                    override fun onFailure(msg: String) {
                        onParseModelsFailure(msg)
                    }
                }
            }

            override fun obtainPager(): IDataPager<T> {
                return DataPager(liveData.value ?: arrayListOf())
            }

            override fun clearListData() {
                liveData.postValue(arrayListOf())
            }
        }
    }

    final override fun onLoadFromNetwork(callback: DoraCallback<T>, listener: OnLoadStateListener?) {
        super.onLoadFromNetwork(callback, listener)
    }

    final override fun onLoadFromNetworkObservable(listener: OnLoadStateListener?): Observable<T> {
        return super.onLoadFromNetworkObservable(listener)
    }

    final override fun onInterceptData(type: DataSource.Type, model: T) {
        super.onInterceptData(type, model)
    }

    final override fun onParseModelFailure(msg: String) {
        super.onParseModelFailure(msg)
    }

    final override fun onLoadFromNetwork(callback: DoraListCallback<T>, listener: OnLoadStateListener?) {
        super.onLoadFromNetwork(callback, listener)
    }

    fun getPageNo(): Int {
        return pageNo
    }

    fun getPageSize(): Int {
        return pageSize
    }

    fun getTotalSize() : Int {
        return totalSize
    }

    /**
     * 能否加载下一页。
     */
    private fun canLoadMore() : Boolean {
        return !isLastPage()
    }

    fun isLastPage() : Boolean {
        val lastPage = if (totalSize % pageSize == 0) totalSize / pageSize - 1 else totalSize / pageSize
        return lastPage == pageNo
    }

    fun isOutOfPageRange() : Boolean {
        val lastPage = if (totalSize % pageSize == 0) totalSize / pageSize - 1 else totalSize / pageSize
        return pageNo < 0 || lastPage < pageNo
    }

    fun observeData(owner: LifecycleOwner, adapter: AdapterDelegate<T>) {
        getListLiveData().observe(owner) {
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
        if (canLoadMore()) {
            pageNo++
            fetchListData(listener = listener)
        } else {
            listener.onLoad(OnLoadStateListener.FAILURE)
        }
    }

    /**
     * 上拉加载高阶函数，可结合[setPageSize]使用。
     */
    @JvmOverloads
    fun onLoadMore(block: ((Boolean) -> Unit)? = null) {
        if (canLoadMore()) {
            pageNo++
            fetchListData(listener = object : OnLoadStateListener {
                override fun onLoad(state: Int) {
                    block?.invoke(state == OnLoadStateListener.SUCCESS)
                }
            })
        } else {
            block?.invoke(false)
        }
    }

    open fun setPageSize(pageSize: Int): DoraPageDatabaseCacheRepository<T> {
        this.pageSize = pageSize
        return this
    }

    open fun setCurrentPage(pageNo: Int, pageSize: Int): DoraPageDatabaseCacheRepository<T> {
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

    override fun onLoadFromCacheList(liveData: MutableLiveData<MutableList<T>>) : Boolean {
        if (!checkValuesNotNull()) throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
        totalSize = (listCacheHolder as DoraListDatabaseCacheHolder<T>)
            .queryCacheSize(query()).toInt()
        if (isOutOfPageRange()) {
            listener?.onLoad(OnLoadStateListener.FAILURE)
            return false
        }
        val models = (listCacheHolder as DoraListDatabaseCacheHolder<T>).queryCache(query())
        models?.let {
            if (it.size > 0) {
                onInterceptData(DataSource.Type.CACHE, it)
                liveData.postValue(it)
                listener?.onLoad(OnLoadStateListener.SUCCESS)
                return true
            } else {
                listener?.onLoad(OnLoadStateListener.FAILURE)
                return false
            }
        }
        listener?.onLoad(OnLoadStateListener.FAILURE)
        return false
    }

    override fun parseModels(models: MutableList<T>?,
                                   liveData: MutableLiveData<MutableList<T>>) {
        models?.let {
            if (isLogPrint) {
                for (model in it) {
                    Log.d(TAG, "【$description】$model")
                }
            }
            onInterceptData(DataSource.Type.NETWORK, it)
            if (!checkValuesNotNull()) throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
            (listCacheHolder as DoraListDatabaseCacheHolder<T>).removeOldCache(query())
            (listCacheHolder as DoraListDatabaseCacheHolder<T>).addNewCache(it)
            if (it.size > 0) {
                listener?.onLoad(OnLoadStateListener.SUCCESS)
            } else {
                listener?.onLoad(OnLoadStateListener.FAILURE)
            }
            if (disallowForceUpdate()) {
                val oldValue = liveData.value
                oldValue?.addAll(it)
                liveData.value = oldValue
            } else {
                liveData.postValue(it)
            }
        }
    }
}