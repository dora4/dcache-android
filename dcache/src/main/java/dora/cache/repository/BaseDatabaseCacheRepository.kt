package dora.cache.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dora.cache.data.fetcher.DataFetcher
import dora.cache.data.fetcher.ListDataFetcher
import dora.cache.data.fetcher.OnLoadStateListener
import dora.cache.data.page.DataPager
import dora.cache.data.page.IDataPager
import dora.cache.factory.DatabaseCacheHolderFactory
import dora.cache.holder.DoraDatabaseCacheHolder
import dora.cache.holder.DoraListDatabaseCacheHolder
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
import java.lang.IllegalArgumentException

/**
 * 使用内置SQLite数据库进行缓存的仓库。
 */
abstract class BaseDatabaseCacheRepository<T : OrmTable>
constructor(context: Context) : BaseRepository<T, DatabaseCacheHolderFactory<T>>(context) {

    /**
     * 保证成员属性不为空，而成功调用数据库查询方法，提高查询可靠性。比如用来校验属性，a != null && b != null
     * && c != null。
     *
     * @see BaseDatabaseCacheRepository.query
     */
    protected open fun checkValuesNotNull() : Boolean { return true }

    /**
     * 手动添加数据，也需要同步数据给后端。
     */
    interface OnSyncListener<M> {

        /**
         * 在此回调中调用REST API同步数据给后端，isSingle是否为单条数据。
         */
        fun onSyncData(isSingle: Boolean, data: MutableList<M>)
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
            getListLiveData().value?.let {
                it.addAll(data)
                (listCacheHolder as DoraListDatabaseCacheHolder<T>).addNewCache(data)
                listener?.onSyncData(data.size == 1, data)
            }
        }
    }

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

    override fun createDataFetcher(): DataFetcher<T> {
        return object : DataFetcher<T>() {

            override fun fetchData(description: String?, listener: OnLoadStateListener?): LiveData<T?> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType): Boolean {
                        if (type === DataSource.CacheType.DATABASE) {
                            return onLoadFromCache(liveData)
                        }
                        liveData.postValue(null)
                        return false
                    }

                    override fun loadFromNetwork() {
                        try {
                            rxOnLoadFromNetwork(liveData, listener)
                            onLoadFromNetwork(callback(), listener)
                        } catch (ignore: Exception) {
                            listener?.onLoad(OnLoadStateListener.FAILURE)
                        }
                    }
                })
                return liveData
            }

            override fun callback(): DoraCallback<T> {
                return object : DoraCallback<T>() {
                    override fun onSuccess(model: T) {
                        parseModel(model, liveData)
                    }

                    override fun onFailure(msg: String) {
                        onParseModelFailure(msg)
                    }
                }
            }

            override fun clearData() {
                liveData.postValue(null)
            }
        }
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

            override fun listCallback(): DoraListCallback<T> {
                return object : DoraListCallback<T>() {
                    override fun onSuccess(models: MutableList<T>) {
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

    private fun onLoadFromCache(liveData: MutableLiveData<T?>) : Boolean {
        if (!checkValuesNotNull()) throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
        val model = (cacheHolder as DoraDatabaseCacheHolder<T>).queryCache(query())
        model?.let {
            onInterceptData(DataSource.Type.CACHE, it)
            liveData.postValue(it)
            return true
        }
        return false
    }

    private fun onLoadFromCacheList(liveData: MutableLiveData<MutableList<T>>) : Boolean {
        if (!checkValuesNotNull()) throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
        val models = (listCacheHolder as DoraListDatabaseCacheHolder<T>).queryCache(query())
        models?.let {
            onInterceptData(DataSource.Type.CACHE, it)
            liveData.postValue(it)
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

    private fun rxOnLoadFromNetwork(liveData: MutableLiveData<T?>, listener: OnLoadStateListener? = null) {
        RxTransformer.doApiObserver(onLoadFromNetworkObservable(listener), object : Observer<T> {
            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(model: T) {
                parseModel(model, liveData)
            }

            override fun onError(e: Throwable) {
                onParseModelFailure(e.toString())
            }

            override fun onComplete() {
            }
        })
    }

    private fun rxOnLoadFromNetworkForList(liveData: MutableLiveData<MutableList<T>>, listener: OnLoadStateListener? = null) {
        RxTransformer.doApiObserver(onLoadFromNetworkObservableList(listener), object : Observer<MutableList<T>> {
            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(models: MutableList<T>) {
                parseModels(models, liveData)
            }

            override fun onError(e: Throwable) {
                onParseModelsFailure(e.toString())
            }

            override fun onComplete() {
            }
        })
    }

    protected open fun parseModel(model: T, liveData: MutableLiveData<T?>) {
        model.let {
            if (isLogPrint) {
                Log.d(TAG, "【$description】$it")
            }
            onInterceptData(DataSource.Type.NETWORK, it)
            if (!checkValuesNotNull()) throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
            (cacheHolder as DoraDatabaseCacheHolder<T>).removeOldCache(query())
            (cacheHolder as DoraDatabaseCacheHolder<T>).addNewCache(it)
            listener?.onLoad(OnLoadStateListener.SUCCESS)
            liveData.postValue(it)
        }
    }

    protected open fun parseModels(models: MutableList<T>?,
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
            listener?.onLoad(OnLoadStateListener.SUCCESS)
            if (disallowForceUpdate()) {
                val oldValue = liveData.value
                oldValue?.addAll(it)
                liveData.value = oldValue
            } else {
                liveData.postValue(it)
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
            (cacheHolder as DoraDatabaseCacheHolder<T>).removeOldCache(query())
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
            (listCacheHolder as DoraListDatabaseCacheHolder<T>).removeOldCache(query())
        }
    }
}