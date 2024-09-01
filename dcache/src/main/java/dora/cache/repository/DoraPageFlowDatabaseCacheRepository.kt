package dora.cache.repository

import android.content.Context
import android.util.Log
import dora.cache.data.fetcher.OnLoadStateListener
import dora.db.builder.Condition
import dora.db.builder.QueryBuilder
import dora.db.table.OrmTable
import dora.http.DoraCallback
import dora.http.DoraListCallback
import dora.cache.DoraPageListCallback
import dora.cache.data.fetcher.ListFlowDataFetcher
import dora.cache.data.page.DataPager
import dora.cache.data.page.IDataPager
import dora.cache.holder.ListDatabaseCacheHolder
import dora.db.builder.WhereBuilder
import io.reactivex.Observable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.lang.IllegalArgumentException

@ListRepository
abstract class DoraPageFlowDatabaseCacheRepository<M, T : OrmTable>(context: Context)
    : DoraFlowDatabaseCacheRepository<T>(context) {

    /**
     * 第几页。
     */
    private var pageNo: Int = 0

    /**
     * 每页的大小，使用过程中保持一致，除初始化外不建议修改，如果修改，则也需要在[query]中添加过滤条件。
     */
    private var pageSize: Int = 10

    /**
     * 数据总条数。
     */
    private var totalSize: Int = 0

    open fun onLoadFromNetwork(callback: DoraPageListCallback<T>, listener: OnLoadStateListener?) {
        this.totalSize = callback.getTotalSize()
    }

    override fun createListDataFetcher(): ListFlowDataFetcher<T> {
        return object : ListFlowDataFetcher<T>() {

            override fun fetchListData(description: String?, listener: OnLoadStateListener?): StateFlow<MutableList<T>> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType): Boolean {
                        if (type === DataSource.CacheType.DATABASE) {
                            return onLoadFromCacheList(flowData)
                        }
                        flowData.value = arrayListOf()
                        return false
                    }

                    override fun loadFromNetwork() {
                        try {
                            rxOnLoadFromNetworkForList(flowData, listener)
                            onLoadFromNetwork(listCallback(), listener)
                        } catch (ignore: Exception) {
                            listener?.onLoad(OnLoadStateListener.FAILURE)
                        }
                    }
                })
                return flowData
            }

            override fun listCallback(): DoraPageListCallback<T> {
                return object : DoraPageListCallback<T>() {

                    override fun onSuccess(totalSize: Int, models: MutableList<T>) {
                        super.onSuccess(totalSize, models)
                        parseModels(models, flowData)
                    }

                    override fun onFailure(msg: String) {
                        onParseModelsFailure(msg)
                    }
                }
            }

            override fun obtainPager(): IDataPager<T> {
                return DataPager(flowData.value)
            }

            override fun clearListData() {
                flowData.value = arrayListOf()
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

    final override fun onLoadFromNetworkObservableList(listener: OnLoadStateListener?): Observable<MutableList<T>> {
        return super.onLoadFromNetworkObservableList(listener)
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

    open fun setPageSize(pageSize: Int): DoraPageFlowDatabaseCacheRepository<M, T> {
        this.pageSize = pageSize
        return this
    }

    open fun setCurrentPage(pageNo: Int, pageSize: Int): DoraPageFlowDatabaseCacheRepository<M, T> {
        this.pageNo = pageNo
        this.pageSize = pageSize
        return this
    }

    /**
     * 使用分页缓存的[OrmTable]实现类，必须指定[pageNo]对应的属性。
     */
    open fun getPaginationKey() : String {
        return OrmTable.PAGINATION_KEY
    }

    override fun query(): Condition {
        val start = pageNo * pageSize
        return QueryBuilder.create()
            .where(WhereBuilder.create().addWhereEqualTo(getPaginationKey(), pageNo))
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

    override fun onLoadFromCacheList(flowData: MutableStateFlow<MutableList<T>>) : Boolean {
        if (!checkValuesNotNull()) throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
        totalSize = (listCacheHolder as ListDatabaseCacheHolder<T>)
            .queryCacheSize(query()).toInt()
        if (isOutOfPageRange()) {
            listener?.onLoad(OnLoadStateListener.FAILURE)
            return false
        }
        val models = (listCacheHolder as ListDatabaseCacheHolder<T>).queryCache(query())
        models?.let {
            if (it.size > 0) {
                onInterceptData(DataSource.Type.CACHE, it)
                flowData.value = it
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
                                   flowData: MutableStateFlow<MutableList<T>>) {
        models?.let {
            if (isLogPrint) {
                for (model in it) {
                    Log.d(TAG, "【$description】${model.toString()}")
                }
            }
            onInterceptData(DataSource.Type.NETWORK, it)
            if (!checkValuesNotNull()) throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
            (listCacheHolder as ListDatabaseCacheHolder<T>).removeOldCache(query())
            (listCacheHolder as ListDatabaseCacheHolder<T>).addNewCache(it)
            if (it.size > 0) {
                listener?.onLoad(OnLoadStateListener.SUCCESS)
            } else {
                listener?.onLoad(OnLoadStateListener.FAILURE)
            }
            if (disallowForceUpdate()) {
                val oldValue = flowData.value
                oldValue.addAll(it)
                flowData.value = oldValue
            } else {
                flowData.value = it
            }
        }
    }
}