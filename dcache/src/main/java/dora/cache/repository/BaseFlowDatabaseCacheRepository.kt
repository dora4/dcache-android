package dora.cache.repository

import android.content.Context
import android.util.Log
import dora.cache.data.fetcher.FlowDataFetcher
import dora.cache.data.fetcher.ListFlowDataFetcher
import dora.cache.data.fetcher.OnLoadStateListener
import dora.cache.data.page.DataPager
import dora.cache.data.page.IDataPager
import dora.cache.factory.DatabaseCacheHolderFactory
import dora.cache.holder.DatabaseCacheHolder
import dora.cache.holder.ListDatabaseCacheHolder
import dora.db.builder.Condition
import dora.db.builder.QueryBuilder
import dora.db.builder.WhereBuilder
import dora.db.table.OrmTable
import dora.http.DoraCallback
import dora.http.DoraListCallback
import dora.http.rx.RxTransformer
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.lang.IllegalArgumentException

/**
 * 使用内置SQLite数据库进行缓存的仓库。
 */
abstract class BaseFlowDatabaseCacheRepository<T : OrmTable>
constructor(context: Context) : BaseFlowRepository<T, DatabaseCacheHolderFactory<T>>(context) {

    /**
     * 是否开启追加模式，仅list模式有效。
     */
    protected open fun disallowForceUpdate() : Boolean {
        return false
    }

    /**
     * 根据查询条件进行初步的过滤从数据库加载的数据，过滤不完全则再调用onInterceptData。通常在断网情况下，指定
     * 离线数据的过滤条件。
     *
     * @return
     */
    @Deprecated(message = "Use query() instead.",
            replaceWith = ReplaceWith("query"),
            level = DeprecationLevel.ERROR)
    protected open fun where(): Condition {
        return WhereBuilder.create().toCondition()
    }

    protected open fun query(): Condition {
        return QueryBuilder.create().toCondition()
    }


    /**
     * 手动添加数据，也需要同步数据给后端。
     */
    interface OnSyncListener<T> {

        /**
         * 在此回调中调用REST API同步数据给后端，isSingle是否为单条数据。
         */
        fun onSyncData(isSingle: Boolean, data: MutableList<T>)
    }

    /**
     * 手动放入缓存数据，仅listMode为true时使用，注意只会追加到缓存里面去，请调用接口将新数据也更新到服务端，以致
     * 于下次请求api接口时也会有这部分数据。
     */
    fun addData(data: T, listener: OnSyncListener<T>?) {
        if (isListMode) {
            addData(arrayListOf(data), listener)
        }
    }

    /**
     * 手动放入一堆缓存数据，仅listMode为true时使用，注意只会追加到缓存里面去，请调用接口将新数据也更新到服务端，
     * 以致于下次请求api接口时也会有这部分数据。
     */
    fun addData(data: MutableList<T>, listener: OnSyncListener<T>?) {
        if (data.size == 0) return
        if (isListMode) {
            getListFlowData().value.let {
                it.addAll(data)
                (listCacheHolder as ListDatabaseCacheHolder<T>).addNewCache(data)
                listener?.onSyncData(data.size == 1, data)
            }
        }
    }

    override fun selectData(ds: DataSource): Boolean {
        val isLoaded = ds.loadFromCache(DataSource.CacheType.DATABASE)
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

    override fun createDataFetcher(): FlowDataFetcher<T> {
        return object : FlowDataFetcher<T>() {

            override fun fetchData(description: String?, listener: OnLoadStateListener?): StateFlow<T?> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType): Boolean {
                        if (type === DataSource.CacheType.DATABASE) {
                            return onLoadFromCache(flowData)
                        }
                        flowData.value = null
                        return false
                    }

                    override fun loadFromNetwork() {
                        try {
                            rxOnLoadFromNetwork(flowData, listener)
                            onLoadFromNetwork(callback(), listener)
                        } catch (ignore: Exception) {
                            listener?.onLoad(OnLoadStateListener.FAILURE)
                        }
                    }
                })
                return flowData
            }

            override fun callback(): DoraCallback<T> {
                return object : DoraCallback<T>() {
                    override fun onSuccess(model: T) {
                        parseModel(model, flowData)
                    }

                    override fun onFailure(msg: String) {
                        onParseModelFailure(msg)
                    }
                }
            }

            override fun clearData() {
                flowData.value = null
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

            override fun listCallback(): DoraListCallback<T> {
                return object : DoraListCallback<T>() {
                    override fun onSuccess(models: MutableList<T>) {
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

    protected fun onLoadFromCache(flowData: MutableStateFlow<T?>) : Boolean {
        if (!checkValuesNotNull()) throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
        val model = (cacheHolder as DatabaseCacheHolder<T>).queryCache(query())
        model?.let {
            onInterceptData(DataSource.Type.CACHE, it)
            flowData.value = it
            return true
        }
        return false
    }

    protected fun onLoadFromCacheList(flowData: MutableStateFlow<MutableList<T>>) : Boolean {
        if (!checkValuesNotNull()) throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
        val models = (listCacheHolder as ListDatabaseCacheHolder<T>).queryCache(query())
        models?.let {
            onInterceptData(DataSource.Type.CACHE, it)
            flowData.value = it
            return true
        }
        return false
    }

    /**
     * 非集合数据模式需要重写它，callback和observable二选一。
     */
    override fun onLoadFromNetwork(callback: DoraCallback<T>, listener: OnLoadStateListener?) {
    }

    /**
     * 集合数据模式需要重写它，callback和observable二选一。
     */
    override fun onLoadFromNetwork(callback: DoraListCallback<T>, listener: OnLoadStateListener?) {
    }

    /**
     * 非集合数据模式需要重写它，callback和observable二选一。
     */
    override fun onLoadFromNetworkObservable(listener: OnLoadStateListener?) : Observable<T> {
        return Observable.empty()
    }

    /**
     * 集合数据模式需要重写它，callback和observable二选一。
     */
    override fun onLoadFromNetworkObservableList(listener: OnLoadStateListener?) : Observable<MutableList<T>> {
        return Observable.empty()
    }

    protected fun rxOnLoadFromNetwork(flowData: MutableStateFlow<T?>, listener: OnLoadStateListener? = null) {
        RxTransformer.doApiObserver(onLoadFromNetworkObservable(listener), object : Observer<T> {
            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(model: T) {
                parseModel(model, flowData)
            }

            override fun onError(e: Throwable) {
                onParseModelFailure(e.toString())
            }

            override fun onComplete() {
            }
        })
    }

    protected fun rxOnLoadFromNetworkForList(flowData: MutableStateFlow<MutableList<T>>, listener: OnLoadStateListener? = null) {
        RxTransformer.doApiObserver(onLoadFromNetworkObservableList(listener), object : Observer<MutableList<T>> {
            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(models: MutableList<T>) {
                parseModels(models, flowData)
            }

            override fun onError(e: Throwable) {
                onParseModelsFailure(e.toString())
            }

            override fun onComplete() {
            }
        })
    }

    protected open fun parseModel(model: T, flowData: MutableStateFlow<T?>) {
        model.let {
            if (isLogPrint) {
                Log.d(TAG, "【$description】$it")
            }
            onInterceptData(DataSource.Type.NETWORK, it)
            if (!checkValuesNotNull()) throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
            (cacheHolder as DatabaseCacheHolder<T>).removeOldCache(query())
            (cacheHolder as DatabaseCacheHolder<T>).addNewCache(it)
            listener?.onLoad(OnLoadStateListener.SUCCESS)
            flowData.value = it
        }
    }

    protected open fun parseModels(models: MutableList<T>?,
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
            listener?.onLoad(OnLoadStateListener.SUCCESS)
            if (disallowForceUpdate()) {
                val oldValue = flowData.value
                oldValue.addAll(it)
                flowData.value = oldValue
            } else {
                flowData.value = it
            }
        }
    }

    protected open fun onParseModelFailure(msg: String) {
        if (isLogPrint) {
            if (description == null) {
                description = javaClass.simpleName
            }
            Log.d(TAG, "【${description}】$msg")
        }
        listener?.onLoad(OnLoadStateListener.FAILURE)
        if (isClearDataOnNetworkError) {
            if (!checkValuesNotNull()) throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
            clearData()
            (cacheHolder as DatabaseCacheHolder<T>).removeOldCache(query())
        }
    }

    protected open fun onParseModelsFailure(msg: String) {
        if (isLogPrint) {
            if (description == null) {
                description = javaClass.simpleName
            }
            Log.d(TAG, "【${description}】$msg")
        }
        listener?.onLoad(OnLoadStateListener.FAILURE)
        if (isClearDataOnNetworkError) {
            if (!checkValuesNotNull()) throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
            clearListData()
            (listCacheHolder as ListDatabaseCacheHolder<T>).removeOldCache(query())
        }
    }
}