package dora.cache.repository

import android.content.Context
import android.util.Log
import dora.cache.data.fetcher.FlowDataFetcher
import dora.cache.data.fetcher.ListFlowDataFetcher
import dora.cache.data.fetcher.OnLoadStateListener
import dora.cache.data.page.DataPager
import dora.cache.data.page.IDataPager
import dora.db.builder.Condition
import dora.db.builder.QueryBuilder
import dora.db.builder.WhereBuilder
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
abstract class BaseFlowDatabaseCacheRepository<M>
constructor(context: Context) : BaseFlowRepository<M>(context) {

    protected val dataMap = HashMap<String, M>()
    protected val listDataMap = HashMap<String, MutableList<M>>()

    /**
     * 如果返回true，则放弃强制覆盖原有数据的模式，而采用map映射，注意这种方式在程序退出后会丢失map缓存的数据。
     */
    protected open fun disallowForceUpdate() : Boolean {
        return false
    }

    /**
     * 只要mapKey的值不冲突即可追加缓存，读取的时候则通过mapKey取不同条件（如接口的参数不同，请求的时间戳不同等）
     * 接口返回的数据的缓存。
     */
    protected open fun mapKey() : String {
        // 通过时间戳保证打开disallowForceUpdate后每次接口返回的数据都被缓存到map，而不是livedata
        return System.currentTimeMillis().toString()
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

    override fun createDataFetcher(): FlowDataFetcher<M> {
        return object : FlowDataFetcher<M>() {

            override fun fetchData(description: String?, listener: OnLoadStateListener?): StateFlow<M?> {
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

            override fun callback(): DoraCallback<M> {
                return object : DoraCallback<M>() {
                    override fun onSuccess(model: M) {
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

    override fun createListDataFetcher(): ListFlowDataFetcher<M> {
        return object : ListFlowDataFetcher<M>() {

            override fun fetchListData(description: String?, listener: OnLoadStateListener?): StateFlow<MutableList<M>> {
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

            override fun listCallback(): DoraListCallback<M> {
                return object : DoraListCallback<M>() {
                    override fun onSuccess(models: MutableList<M>) {
                        parseModels(models, flowData)
                    }

                    override fun onFailure(msg: String) {
                        onParseModelsFailure(msg)
                    }
                }
            }

            override fun obtainPager(): IDataPager<M> {
                return DataPager(flowData.value)
            }

            override fun clearListData() {
                flowData.value = arrayListOf()
            }
        }
    }

    private fun onLoadFromCache(flowData: MutableStateFlow<M?>) : Boolean {
        if (checkValuesNotNull()) {
            val model = databaseCacheHolder.queryCache(query())
            model?.let {
                onInterceptData(DataSource.Type.CACHE, it)
                flowData.value = it
                return true
            }
        } else throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
        return false
    }

    private fun onLoadFromCacheList(flowData: MutableStateFlow<MutableList<M>>) : Boolean {
        if (checkValuesNotNull()) {
            val models = listDatabaseCacheHolder.queryCache(query())
            models?.let {
                onInterceptData(DataSource.Type.CACHE, it)
                flowData.value = it
                return true
            }
        } else throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
        return false
    }

    /**
     * 非集合数据模式需要重写它，callback和observable二选一。
     */
    override fun onLoadFromNetwork(callback: DoraCallback<M>, listener: OnLoadStateListener?) {
    }

    /**
     * 集合数据模式需要重写它，callback和observable二选一。
     */
    override fun onLoadFromNetwork(callback: DoraListCallback<M>, listener: OnLoadStateListener?) {
    }

    /**
     * 非集合数据模式需要重写它，callback和observable二选一。
     */
    override fun onLoadFromNetworkObservable(listener: OnLoadStateListener?) : Observable<M> {
        return Observable.empty()
    }

    /**
     * 集合数据模式需要重写它，callback和observable二选一。
     */
    override fun onLoadFromNetworkObservableList(listener: OnLoadStateListener?) : Observable<MutableList<M>> {
        return Observable.empty()
    }

    private fun rxOnLoadFromNetwork(flowData: MutableStateFlow<M?>, listener: OnLoadStateListener? = null) {
        RxTransformer.doApiObserver(onLoadFromNetworkObservable(listener), object : Observer<M> {
            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(model: M) {
                parseModel(model, flowData)
            }

            override fun onError(e: Throwable) {
                onParseModelFailure(e.toString())
            }

            override fun onComplete() {
            }
        })
    }

    private fun rxOnLoadFromNetworkForList(flowData: MutableStateFlow<MutableList<M>>, listener: OnLoadStateListener? = null) {
        RxTransformer.doApiObserver(onLoadFromNetworkObservableList(listener), object : Observer<MutableList<M>> {
            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(models: MutableList<M>) {
                parseModels(models, flowData)
            }

            override fun onError(e: Throwable) {
                onParseModelsFailure(e.toString())
            }

            override fun onComplete() {
            }
        })
    }

    protected open fun parseModel(model: M, flowData: MutableStateFlow<M?>) {
        model?.let {
            if (isLogPrint) {
                Log.d(TAG, "【$description】$it")
            }
            onInterceptData(DataSource.Type.NETWORK, it)
            if (!disallowForceUpdate()) {
                if (checkValuesNotNull()) {
                    databaseCacheHolder.removeOldCache(query())
                } else throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
            } else {
                if (dataMap.containsKey(mapKey())) {
                    if (checkValuesNotNull()) {
                        databaseCacheHolder.removeOldCache(query())
                    } else throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
                } else {
                    dataMap[mapKey()] = it
                }
            }
            databaseCacheHolder.addNewCache(it)
            listener?.onLoad(OnLoadStateListener.SUCCESS)
            if (disallowForceUpdate()) {
                flowData.value = dataMap[mapKey()]
            } else {
                flowData.value = it
            }
        }
    }

    protected open fun parseModels(models: MutableList<M>?,
                                   flowData: MutableStateFlow<MutableList<M>>) {
        models?.let {
            if (isLogPrint) {
                for (model in it) {
                    Log.d(TAG, "【$description】${model.toString()}")
                }
            }
            onInterceptData(DataSource.Type.NETWORK, it)
            if (!disallowForceUpdate()) {
                if (checkValuesNotNull()) {
                    listDatabaseCacheHolder.removeOldCache(query())
                } else throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
            } else {
                if (listDataMap.containsKey(mapKey())) {
                    if (checkValuesNotNull()) {
                        listDatabaseCacheHolder.removeOldCache(query())
                    } else throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
                } else {
                    listDataMap[mapKey()] = it
                }
            }
            listDatabaseCacheHolder.addNewCache(it)
            listener?.onLoad(OnLoadStateListener.SUCCESS)
            if (disallowForceUpdate()) {
                flowData.value = listDataMap[mapKey()]!!
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
            if (checkValuesNotNull()) {
                clearData()
                databaseCacheHolder.removeOldCache(query())
            } else throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
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
            if (checkValuesNotNull()) {
                clearListData()
                listDatabaseCacheHolder.removeOldCache(query())
            } else throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
        }
    }
}