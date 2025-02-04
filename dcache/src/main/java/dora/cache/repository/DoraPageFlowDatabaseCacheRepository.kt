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
import dora.cache.factory.DoraDatabaseCacheHolderFactory
import dora.cache.holder.ListDatabaseCacheHolder
import dora.db.builder.WhereBuilder
import io.reactivex.Observable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.lang.IllegalArgumentException

@ListRepository
abstract class DoraPageFlowDatabaseCacheRepository<T : OrmTable>(context: Context)
    : DoraFlowDatabaseCacheRepository<T>(context) {

    /**
     * Which page.
     * 简体中文：第几页。
     */
    private var pageNo: Int = 0

    /**
     * The page size, which should remain consistent during use. It is not recommended to modify it
     * except during initialization. If modified, filtering conditions should also be added in
     * [parseModels].
     * 简体中文：每页的大小，使用过程中保持一致，除初始化外不建议修改，如果修改，则也需要在[parseModels]中添加过滤条件。
     */
    private var pageSize: Int = 10

    /**
     * Total number of data items.
     * 简体中文：数据总条数。
     */
    private var totalSize: Int = 0

    open fun onLoadFromNetwork(callback: DoraPageListCallback<T>, listener: OnLoadStateListener?) {
        this.totalSize = callback.getTotalSize()
    }

    final override fun onLoadFromNetwork(callback: DoraCallback<T>, listener: OnLoadStateListener?) {
        super.onLoadFromNetwork(callback, listener)
    }

    final override fun onLoadFromNetwork(callback: DoraListCallback<T>, listener: OnLoadStateListener?) {
        super.onLoadFromNetwork(callback, listener)
    }

    final override fun onLoadFromNetworkObservable(listener: OnLoadStateListener?): Observable<T> {
        return super.onLoadFromNetworkObservable(listener)
    }

    final override fun onLoadFromNetworkObservableList(listener: OnLoadStateListener?): Observable<MutableList<T>> {
        return super.onLoadFromNetworkObservableList(listener)
    }

    final override fun onParseModelFailure(msg: String) {
        super.onParseModelFailure(msg)
    }

    override fun createCacheHolderFactory(): DoraDatabaseCacheHolderFactory<T> {
        return DoraDatabaseCacheHolderFactory<T>()
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
     * Whether the next page can be loaded.
     * 简体中文：能否加载下一页。
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
     * Pull-down refresh higher-order function, which can be used in conjunction with [setPageSize].
     * 简体中文：下拉刷新回调，可结合[setPageSize]使用。
     */
    fun onRefresh(listener: OnLoadStateListener) {
        pageNo = 0
        fetchListData(listener = listener)
    }

    /**
     * Pull-down refresh higher-order function, which can be used in conjunction with [setPageSize].
     * 简体中文：下拉刷新高阶函数，可结合[setPageSize]使用。
     */
    @JvmOverloads
    fun onRefresh(block: ((Boolean) -> Unit)? = null) {
        pageNo = 0
        fetchListData(listener = object : OnLoadStateListener {
            override fun onLoad(from: OnLoadStateListener.Source, state: Int, tookTime: Long) {
                block?.invoke(state == OnLoadStateListener.SUCCESS)
            }
        })
    }

    /**
     * Pull-up loading callback, which can be used in conjunction with [setPageSize].
     * 简体中文：上拉加载回调，可结合[setPageSize]使用。
     */
    fun onLoadMore(listener: OnLoadStateListener) {
        val time = System.currentTimeMillis()
        if (canLoadMore()) {
            pageNo++
            fetchListData(listener = listener)
        } else {
            listener.onLoad(OnLoadStateListener.Source.OTHER, OnLoadStateListener.FAILURE,
                System.currentTimeMillis() - time)
        }
    }

    /**
     * Pull-up loading higher-order function, which can be used in conjunction with [setPageSize].
     * 简体中文：上拉加载高阶函数，可结合[setPageSize]使用。
     */
    @JvmOverloads
    fun onLoadMore(block: ((Boolean) -> Unit)? = null) {
        if (canLoadMore()) {
            pageNo++
            fetchListData(listener = object : OnLoadStateListener {
                override fun onLoad(from: OnLoadStateListener.Source, state: Int, tookTime: Long) {
                    block?.invoke(state == OnLoadStateListener.SUCCESS)
                }
            })
        } else {
            block?.invoke(false)
        }
    }

    open fun setPageSize(pageSize: Int): DoraPageFlowDatabaseCacheRepository<T> {
        this.pageSize = pageSize
        return this
    }

    open fun setCurrentPage(pageNo: Int, pageSize: Int): DoraPageFlowDatabaseCacheRepository<T> {
        this.pageNo = pageNo
        this.pageSize = pageSize
        return this
    }

    /**
     * When using a paginated cache with the [OrmTable] implementation class, you must specify the
     * property corresponding to [pageNo].
     * 简体中文：使用分页缓存的[OrmTable]实现类，必须指定[pageNo]对应的属性。
     */
    open fun getPaginationKey() : String {
        return OrmTable.PAGINATION_KEY
    }

    override fun query(): Condition {
        val start = pageNo * pageSize
        return QueryBuilder.create()
            .limit(start, pageSize)
            .toCondition()
    }

    /**
     * Load cached data directly when there is no network connection.
     * 简体中文：没网的情况下直接加载缓存数据。
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
        if (!checkParamsValid()) throw IllegalArgumentException(
            "Please check parameters, checkParamsValid returned false.")
        val time = System.currentTimeMillis()
        totalSize = (listCacheHolder as ListDatabaseCacheHolder<T>)
            .queryCacheSize(query()).toInt()
        if (isOutOfPageRange()) {
            listener?.onLoad(OnLoadStateListener.Source.CACHE, OnLoadStateListener.FAILURE,
                System.currentTimeMillis() - time)
            return false
        }
        val models = (listCacheHolder as ListDatabaseCacheHolder<T>).queryCache(query())
        models?.let {
            if (it.size > 0) {
                val data = onFilterData(DataSource.Type.CACHE, it)
                onInterceptData(DataSource.Type.CACHE, data)
                flowData.value = data
                listener?.onLoad(OnLoadStateListener.Source.CACHE, OnLoadStateListener.SUCCESS,
                    System.currentTimeMillis() - time)
                return true
            } else {
                listener?.onLoad(OnLoadStateListener.Source.CACHE, OnLoadStateListener.FAILURE,
                    System.currentTimeMillis() - time)
                return false
            }
        }
        listener?.onLoad(OnLoadStateListener.Source.CACHE, OnLoadStateListener.FAILURE,
            System.currentTimeMillis() - time)
        return false
    }

    override fun parseModels(models: MutableList<T>?,
                                   flowData: MutableStateFlow<MutableList<T>>) {
        models?.let {
            val time = System.currentTimeMillis()
            if (isLogPrint) {
                for (model in it) {
                    Log.d(TAG, "【$description】${model.toString()}")
                }
            }
            val data = onFilterData(DataSource.Type.NETWORK, it)
            onInterceptData(DataSource.Type.NETWORK, data)
            if (!checkParamsValid()) throw IllegalArgumentException(
                "Please check parameters, checkParamsValid returned false.")
            // 简体中文：追加分页的条件
            val whereBuilder = WhereBuilder.create(query()).andWhereEqualTo(getPaginationKey(), pageNo)
            val condition = QueryBuilder.create(query()).where(whereBuilder).toCondition()
            if (!disallowForceUpdate()) {
                (listCacheHolder as ListDatabaseCacheHolder<T>).removeOldCache(condition)
            }
            (listCacheHolder as ListDatabaseCacheHolder<T>).addNewCache(data)
            if (data.size > 0) {
                listener?.onLoad(OnLoadStateListener.Source.NETWORK, OnLoadStateListener.SUCCESS,
                    System.currentTimeMillis() - time)
            } else {
                listener?.onLoad(OnLoadStateListener.Source.NETWORK, OnLoadStateListener.FAILURE,
                    System.currentTimeMillis() - time)
            }
            if (disallowForceUpdate()) {
                val oldValue = flowData.value
                oldValue.addAll(data)
                flowData.value = oldValue
            } else {
                flowData.value = data
            }
        }
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
                        val time = System.currentTimeMillis()
                        try {
                            rxOnLoadFromNetworkForList(flowData, listener)
                            onLoadFromNetwork(listCallback(), listener)
                        } catch (ignore: Exception) {
                            listener?.onLoad(OnLoadStateListener.Source.NETWORK, OnLoadStateListener.FAILURE,
                                System.currentTimeMillis() - time)
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
}