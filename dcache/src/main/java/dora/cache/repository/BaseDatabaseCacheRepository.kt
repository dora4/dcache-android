package dora.cache.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dora.cache.data.fetcher.DataFetcher
import dora.cache.data.fetcher.IDataFetcher
import dora.cache.data.fetcher.IListDataFetcher
import dora.cache.data.fetcher.ListDataFetcher
import dora.cache.data.page.DataPager
import dora.cache.data.page.IDataPager
import dora.db.builder.Condition
import dora.db.builder.QueryBuilder
import dora.db.builder.WhereBuilder
import dora.http.DoraCallback
import dora.http.DoraListCallback
import dora.rx.RxTransformer
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.lang.IllegalArgumentException

/**
 * 使用内置SQLite数据库进行缓存的仓库。
 */
abstract class BaseDatabaseCacheRepository<M> @JvmOverloads
    constructor(context: Context) : BaseRepository<M>(context) {

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

    override fun createDataFetcher(): DataFetcher<M> {
        return object : DataFetcher<M>() {
            override fun fetchData(listener: IDataFetcher.OnLoadListener?): LiveData<M?> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType): Boolean {
                        if (type === DataSource.CacheType.DATABASE) {
                            return onLoadFromCache(liveData)
                        }
                        liveData.postValue(null)
                        return false
                    }

                    override fun loadFromNetwork() {
                        rxOnLoadFromNetwork(listener, liveData)
                        onLoadFromNetwork(callback(listener))
                    }
                })
                return liveData
            }

            override fun callback(listener: IDataFetcher.OnLoadListener?): DoraCallback<M> {
                return object : DoraCallback<M>() {
                    override fun onSuccess(model: M) {
                        parseModel(model, listener, liveData)
                    }

                    override fun onFailure(msg: String) {
                        onParseModelFailure(msg, listener)
                    }
                }
            }

            override fun clearData() {
                liveData.postValue(null)
            }
        }
    }

    override fun createListDataFetcher(): ListDataFetcher<M> {
        return object : ListDataFetcher<M>() {

            override fun fetchListData(listener: IListDataFetcher.OnLoadListener?): LiveData<MutableList<M>> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType): Boolean {
                        if (type === DataSource.CacheType.DATABASE) {
                            return onLoadFromCacheList(liveData)
                        }
                        liveData.postValue(arrayListOf())
                        return false
                    }

                    override fun loadFromNetwork() {
                        rxOnLoadFromNetwork(listener, liveData)
                        onLoadFromNetwork(listCallback(listener))
                    }
                })
                return liveData
            }

            override fun listCallback(listener: IListDataFetcher.OnLoadListener?): DoraListCallback<M> {
                return object : DoraListCallback<M>() {
                    override fun onSuccess(models: MutableList<M>) {
                        parseModels(models, listener, liveData)
                    }

                    override fun onFailure(msg: String) {
                        onParseModelsFailure(msg, listener)
                    }
                }
            }

            override fun obtainPager(): IDataPager<M> {
                return DataPager(liveData.value ?: arrayListOf())
            }

            override fun clearListData() {
                liveData.postValue(arrayListOf())
            }
        }
    }

    private fun onLoadFromCache(liveData: MutableLiveData<M?>) : Boolean {
        if (checkValuesNotNull()) {
            val model = cacheHolder.queryCache(query())
            model?.let {
                onInterceptData(DataSource.Type.CACHE, it)
                liveData.postValue(it)
                return true
            }
        } else throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
        return false
    }

    private fun onLoadFromCacheList(liveData: MutableLiveData<MutableList<M>>) : Boolean {
        if (checkValuesNotNull()) {
            val models = listCacheHolder.queryCache(query())
            models?.let {
                onInterceptData(DataSource.Type.CACHE, it)
                liveData.postValue(it)
                return true
            }
        } else throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
        return false
    }

    /**
     * 非集合数据模式需要重写它，callback和observable二选一。
     */
    override fun onLoadFromNetwork(callback: DoraCallback<M>) {
    }

    /**
     * 集合数据模式需要重写它，callback和observable二选一。
     */
    override fun onLoadFromNetwork(callback: DoraListCallback<M>) {
    }

    /**
     * 非集合数据模式需要重写它，callback和observable二选一。
     */
    override fun onLoadFromNetworkObservable() : Observable<M> {
        return Observable.empty()
    }

    /**
     * 集合数据模式需要重写它，callback和observable二选一。
     */
    override fun onLoadFromNetworkObservableList() : Observable<MutableList<M>> {
        return Observable.empty()
    }

    @JvmOverloads
    private fun rxOnLoadFromNetwork(listener: IDataFetcher.OnLoadListener?, liveData: MutableLiveData<M?>) {
        RxTransformer.doApi(onLoadFromNetworkObservable(), object : Observer<M> {
            override fun onSubscribe(d: Disposable?) {
            }

            override fun onNext(model: M) {
                parseModel(model, listener, liveData)
            }

            override fun onError(e: Throwable?) {
                onParseModelFailure(e.toString(), listener)
            }

            override fun onComplete() {
            }
        })
    }

    @JvmOverloads
    private fun rxOnLoadFromNetwork(listener: IListDataFetcher.OnLoadListener?, liveData: MutableLiveData<MutableList<M>>) {
        RxTransformer.doApi(onLoadFromNetworkObservableList(), object : Observer<MutableList<M>> {
            override fun onSubscribe(d: Disposable?) {
            }

            override fun onNext(models: MutableList<M>?) {
                parseModels(models, listener, liveData)
            }

            override fun onError(e: Throwable?) {
                onParseModelsFailure(e.toString(), listener)
            }

            override fun onComplete() {
            }
        })
    }

    @JvmOverloads
    protected open fun parseModel(model: M, listener: IDataFetcher.OnLoadListener?, liveData: MutableLiveData<M?>) {
        model?.let {
            if (isLogPrint) {
                Log.d(TAG, it.toString())
            }
            onInterceptData(DataSource.Type.NETWORK, it)
            if (!disallowForceUpdate()) {
                if (checkValuesNotNull()) {
                    cacheHolder.removeOldCache(query())
                } else throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
            } else {
                if (dataMap.containsKey(mapKey())) {
                    if (checkValuesNotNull()) {
                        cacheHolder.removeOldCache(query())
                    } else throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
                } else {
                    dataMap[mapKey()] = it
                }
            }
            cacheHolder.addNewCache(it)
            if (disallowForceUpdate()) {
                liveData.postValue(dataMap[mapKey()])
            } else {
                liveData.postValue(it)
            }
        }
        listener?.onSuccess()
    }

    @JvmOverloads
    protected open fun parseModels(models: MutableList<M>?, listener: IListDataFetcher.OnLoadListener?,
                            liveData: MutableLiveData<MutableList<M>>) {
        models?.let {
            if (isLogPrint) {
                for (model in it) {
                    Log.d(TAG, model.toString())
                }
            }
            onInterceptData(DataSource.Type.NETWORK, it)
            if (!disallowForceUpdate()) {
                if (checkValuesNotNull()) {
                    listCacheHolder.removeOldCache(query())
                } else throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
            } else {
                if (listDataMap.containsKey(mapKey())) {
                    if (checkValuesNotNull()) {
                        listCacheHolder.removeOldCache(query())
                    } else throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
                } else {
                    listDataMap[mapKey()] = it
                }
            }
            listCacheHolder.addNewCache(it)
            if (disallowForceUpdate()) {
                liveData.postValue(listDataMap[mapKey()])
            } else {
                liveData.postValue(it)
            }
        }
        listener?.onSuccess()
    }

    @JvmOverloads
    protected open fun onParseModelFailure(msg: String, listener: IDataFetcher.OnLoadListener?) {
        if (isLogPrint) {
            Log.d(TAG, msg)
        }
        if (isClearDataOnNetworkError) {
            if (checkValuesNotNull()) {
                clearData()
                cacheHolder.removeOldCache(query())
            } else throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
        }
        listener?.onFailure(msg)
    }

    @JvmOverloads
    protected open fun onParseModelsFailure(msg: String, listener: IListDataFetcher.OnLoadListener?) {
        if (isLogPrint) {
            Log.d(TAG, msg)
        }
        if (isClearDataOnNetworkError) {
            if (checkValuesNotNull()) {
                clearListData()
                listCacheHolder.removeOldCache(query())
            } else throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
        }
        listener?.onFailure(msg)
    }
}