package dora.cache.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dora.cache.data.fetcher.OnLoadListener
import dora.db.builder.Condition
import dora.db.builder.QueryBuilder
import dora.db.table.OrmTable
import dora.http.DoraCallback
import dora.http.DoraListCallback
import dora.cache.DoraPageListCallback
import dora.cache.data.fetcher.ListDataFetcher
import dora.cache.data.notify.IListDataPublisher
import dora.cache.data.page.DataPager
import dora.cache.data.page.IDataPager
import dora.cache.factory.DoraDatabaseCacheHolderFactory
import dora.cache.holder.ListDatabaseCacheHolder
import dora.db.builder.WhereBuilder
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalArgumentException

@ListRepository
abstract class DoraPageDatabaseCacheRepository<T : OrmTable>(context: Context)
    : DoraDatabaseCacheRepository<T>(context) {

    /**
     * The current page number.
     * 简体中文：第几页。
     */
    private var pageNo: Int = 0

    /**
     * The size of each page should remain consistent during use. Modifying it is not recommended
     * except during initialization; if modified, filtering conditions should also be added in
     * [parseModels].
     * 简体中文：每页的大小，使用过程中保持一致，除初始化外不建议修改，如果修改，则也需要在[parseModels]中添加过滤条件。
     */
    private var pageSize: Int = 10

    /**
     * Total number of data entries.
     * 简体中文：数据总条数。
     */
    private var totalSize: Int = 0

    open fun onLoadFromNetwork(callback: DoraPageListCallback<T>, listener: OnLoadListener?) {
        this.totalSize = callback.getTotalSize()
    }

    final override fun onLoadFromNetwork(callback: DoraCallback<T>, listener: OnLoadListener?) {
        super.onLoadFromNetwork(callback, listener)
    }

    final override fun onLoadFromNetworkObservable(listener: OnLoadListener?): Observable<T> {
        return super.onLoadFromNetworkObservable(listener)
    }

    final override fun onLoadFromNetwork(callback: DoraListCallback<T>, listener: OnLoadListener?) {
        super.onLoadFromNetwork(callback, listener)
    }

    final override fun onLoadFromNetworkObservableList(listener: OnLoadListener?): Observable<MutableList<T>> {
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
     * Can the next page be loaded?
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
     * Pull-to-refresh callback, which can be used in conjunction with [setPageSize].
     * 简体中文：下拉刷新回调，可结合[setPageSize]使用。
     */
    fun onRefresh(listener: OnLoadListener) {
        pageNo = 0
        fetchListData(listener = listener)
    }

    /**
     * Pull-to-refresh higher-order function, which can be used in conjunction with [setPageSize].
     * 简体中文：下拉刷新高阶函数，可结合[setPageSize]使用。
     */
    @JvmOverloads
    fun onRefresh(block: ((Boolean) -> Unit)? = null) {
        pageNo = 0
        fetchListData(listener = object : OnLoadListener {
            override fun onLoad(from: OnLoadListener.Source, state: Int) {
                block?.invoke(state == OnLoadListener.SUCCESS)
            }
        })
    }

    /**
     * Pull-to-load callback, which can be used in conjunction with [setPageSize].
     * 简体中文：上拉加载回调，可结合[setPageSize]使用。
     */
    fun onLoadMore(listener: OnLoadListener) {
        if (canLoadMore()) {
            pageNo++
            fetchListData(listener = listener)
        } else {
            listener.onLoad(OnLoadListener.Source.OTHER, OnLoadListener.FAILURE)
        }
    }

    /**
     * Pull-to-load higher-order function, which can be used in conjunction with [setPageSize].
     * 简体中文：上拉加载高阶函数，可结合[setPageSize]使用。
     */
    @JvmOverloads
    fun onLoadMore(block: ((Boolean) -> Unit)? = null) {
        if (canLoadMore()) {
            pageNo++
            fetchListData(listener = object : OnLoadListener {
                override fun onLoad(from: OnLoadListener.Source, state: Int) {
                    block?.invoke(state == OnLoadListener.SUCCESS)
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

    /**
     * Using the [OrmTable] implementation class with pagination cache, the property corresponding
     * to [pageNo] must be specified.
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
     * Load cached data directly in the absence of an internet connection.
     * 简体中文：没网的情况下直接加载缓存数据。
     */
    override fun selectData(ds: DataSource, l: OnLoadListener) {
        val isLoaded = ds.loadFromCache(DataSource.CacheType.DATABASE)
        l.onLoad(OnLoadListener.Source.CACHE, if (isLoaded) OnLoadListener.SUCCESS else OnLoadListener.FAILURE)
        if (isNetworkAvailable) {
            ds.loadFromNetwork()
        }
    }

    override fun onLoadFromCacheList(liveData: MutableLiveData<MutableList<T>>) : Boolean {
        if (!checkParamsValid()) throw IllegalArgumentException(
            "Please check parameters, checkParamsValid returned false.")
        totalSize = (listCacheHolder as ListDatabaseCacheHolder<T>)
            .queryCacheSize(query()).toInt()
        if (isOutOfPageRange()) {
            listener?.onLoad(OnLoadListener.Source.CACHE, OnLoadListener.FAILURE)
            return false
        }
        val models = (listCacheHolder as ListDatabaseCacheHolder<T>).queryCache(query())
        models?.let {
            if (it.size > 0) {
                val data = onFilterData(DataSource.Type.CACHE, it)
                onInterceptData(DataSource.Type.CACHE, data)
                liveData.postValue(data)
                if (isNotify) IListDataPublisher.DEFAULT.send(getModelType(), data)
                listener?.onLoad(OnLoadListener.Source.CACHE, OnLoadListener.SUCCESS)
                return true
            } else {
                listener?.onLoad(OnLoadListener.Source.CACHE, OnLoadListener.FAILURE)
                return false
            }
        }
        listener?.onLoad(OnLoadListener.Source.CACHE, OnLoadListener.FAILURE)
        return false
    }

    override fun parseModels(models: MutableList<T>?,
                                   liveData: MutableLiveData<MutableList<T>>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                models?.let {
                    if (isLogPrint) {
                        for (model in it) {
                            Log.d(TAG, "【$description】$model")
                        }
                    }
                    val data = onFilterData(DataSource.Type.NETWORK, it)
                    onInterceptData(DataSource.Type.NETWORK, data)
                    if (!checkParamsValid()) throw IllegalArgumentException(
                        "Please check parameters, checkParamsValid returned false."
                    )
                    // Append pagination conditions.
                    // 简体中文：追加分页的条件
                    val whereBuilder =
                        WhereBuilder.create(query()).andWhereEqualTo(getPaginationKey(), pageNo)
                    val condition = QueryBuilder.create(query()).where(whereBuilder).toCondition()
                    if (!disallowForceUpdate()) {
                        (listCacheHolder as ListDatabaseCacheHolder<T>).removeOldCache(condition)
                    }
                    (listCacheHolder as ListDatabaseCacheHolder<T>).addNewCache(data)
                    withContext(Dispatchers.Main) {
                        if (data.size > 0) {
                            listener?.onLoad(OnLoadListener.Source.NETWORK, OnLoadListener.SUCCESS)
                        } else {
                            listener?.onLoad(OnLoadListener.Source.NETWORK, OnLoadListener.FAILURE)
                        }
                    }
                    if (disallowForceUpdate()) {
                        val oldValue = liveData.value
                        oldValue?.addAll(data)
                        liveData.value = oldValue
                        if (isNotify) IListDataPublisher.DEFAULT.send(
                            getModelType(),
                            oldValue ?: arrayListOf()
                        )
                    } else {
                        liveData.postValue(data)
                        if (isNotify) IListDataPublisher.DEFAULT.send(getModelType(), data)
                    }
                }
            }
        }
    }

    override fun createListDataFetcher(): ListDataFetcher<T> {
        return object : ListDataFetcher<T>() {

            override fun fetchListData(description: String?, listener: OnLoadListener?): LiveData<MutableList<T>> {
                val startTime = System.currentTimeMillis()
                val delegate = object : OnLoadListener {
                    override fun onLoad(from: OnLoadListener.Source, state: Int) {
                        val endTime = System.currentTimeMillis()
                        if (isLogPrint) {
                            Log.d(TAG, "【$description】${from.name}: finished at ${endTime - startTime} ms")
                        }
                        listener?.onLoad(from, state)
                    }
                }
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType): Boolean {
                        if (type === DataSource.CacheType.DATABASE) {
                            return onLoadFromCacheList(liveData)
                        }
                        liveData.postValue(arrayListOf())
                        if (isNotify) IListDataPublisher.DEFAULT.send(getModelType(), arrayListOf())
                        return false
                    }

                    override fun loadFromNetwork() {
                        try {
                            rxOnLoadFromNetworkForList(liveData, delegate)
                            onLoadFromNetwork(listCallback(), delegate)
                        } catch (ignore: Exception) {
                            delegate.onLoad(OnLoadListener.Source.NETWORK, OnLoadListener.FAILURE)
                        }
                    }
                }, delegate)
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
                if (isNotify) IListDataPublisher.DEFAULT.send(getModelType(), arrayListOf())
            }
        }
    }
}