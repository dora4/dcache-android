package dora.cache.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dora.cache.data.fetcher.DataFetcher
import dora.cache.data.fetcher.ListDataFetcher
import dora.cache.data.fetcher.OnLoadListener
import dora.cache.data.page.DataPager
import dora.cache.data.page.IDataPager
import dora.cache.factory.DatabaseCacheHolderFactory
import dora.cache.holder.DatabaseCacheHolder
import dora.cache.holder.ListDatabaseCacheHolder
import dora.db.builder.Condition
import dora.db.builder.QueryBuilder
import dora.db.builder.WhereBuilder
import dora.http.DoraCallback
import dora.http.DoraListCallback
import dora.http.rx.RxTransformer
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.lang.IllegalArgumentException

/**
 * Repository using the built-in SQLite database for caching.
 * 简体中文：使用内置SQLite数据库进行缓存的仓库。
 */
abstract class BaseDatabaseCacheRepository<M, F : DatabaseCacheHolderFactory<M>>(context: Context) : BaseRepository<M, F>(context) {

    /**
     * Perform initial filtering of the data loaded from the database based on query conditions;
     * if the filtering is incomplete, then call onInterceptData. Typically, this is used to specify
     * filtering conditions for offline data in case of network disconnection.
     * 简体中文：根据查询条件进行初步的过滤从数据库加载的数据，过滤不完全则再调用onInterceptData。通常在断网情况下，
     * 指定离线数据的过滤条件。
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
     * Ensure that member properties are not null when successfully calling database query methods
     * to improve query reliability. For example, use it to validate properties: a != null &&
     * b != null && c != null.
     * 简体中文：保证成员属性不为空，而成功调用数据库查询方法，提高查询可靠性。比如用来校验属性，a != null &&
     * b != null && c != null。
     *
     * @see BaseDatabaseCacheRepository.query
     */
    @Deprecated(message = "Use checkParamsValid() instead.",
        replaceWith = ReplaceWith("checkParamsValid"),
        level = DeprecationLevel.ERROR)
    protected open fun checkValuesNotNull() : Boolean { return true }

    /**
     * Validate the legality of request parameters, not just null checks. For Kotlin, you can use
     * the lateinit keyword to help ensure non-nullability.
     * 简体中文：校验请求参数合法性，不仅仅是null校验，对于kotlin而言，可用lateinit关键字帮你校验空。
     *
     * @see BaseDatabaseCacheRepository.query
     * @since 2.4.14
     */
    protected open fun checkParamsValid() : Boolean { return true }

    /**
     * Manually adding data also requires synchronizing the data with the backend.
     * 简体中文：手动添加数据，也需要同步数据给后端。
     */
    interface OnSyncListener<M> {

        /**
         * In this callback, call the REST API to synchronize data with the backend; isSingle
         * indicates whether it is a single data entry.
         * 简体中文：在此回调中调用REST API同步数据给后端，isSingle表示是否为单条数据。
         */
        fun onSyncData(isSingle: Boolean, data: MutableList<M>)
    }

    /**
     * Manually add cached data, used only when listMode is true. Note that it will only be
     * appended to the cache. Please call the interface to update the new data to the server so that
     * this part of the data will also be available during the next API request.
     * 简体中文：手动放入缓存数据，仅listMode为true时使用，注意只会追加到缓存里面去，请调用接口将新数据也更新到服务
     * 端，以致于下次请求api接口时也会有这部分数据。
     */
    fun addData(data: M, listener: OnSyncListener<M>?) {
        if (isListMode) {
            addData(arrayListOf(data), listener)
        }
    }

    /**
     * Manually add a batch of cached data, used only when listMode is true. Note that it will only
     * be appended to the cache. Please call the interface to update the new data to the server so
     * that this part of the data will also be available during the next API request.
     * 简体中文：手动放入一堆缓存数据，仅listMode为true时使用，注意只会追加到缓存里面去，请调用接口将新数据也更新到
     * 服务端，以致于下次请求api接口时也会有这部分数据。
     */
    fun addData(data: MutableList<M>, listener: OnSyncListener<M>?) {
        if (data.size == 0) return
        if (isListMode) {
            getListLiveData().value?.let {
                it.addAll(data)
                (listCacheHolder as ListDatabaseCacheHolder<M>).addNewCache(data)
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

    override fun createDataFetcher(): DataFetcher<M> {
        return object : DataFetcher<M>() {

            override fun fetchData(description: String?, listener: OnLoadListener?): LiveData<M?> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType): Boolean {
                        if (type === DataSource.CacheType.DATABASE) {
                            return onLoadFromCache(liveData)
                        }
                        liveData.postValue(null)
                        return false
                    }

                    override fun loadFromNetwork() {
                        val time = System.currentTimeMillis()
                        try {
                            rxOnLoadFromNetwork(liveData, listener)
                            onLoadFromNetwork(callback(), listener)
                        } catch (ignore: Exception) {
                            listener?.onLoad(OnLoadListener.Source.NETWORK, OnLoadListener.FAILURE, System.currentTimeMillis() - time)
                        }
                    }
                })
                return liveData
            }

            override fun callback(): DoraCallback<M> {
                return object : DoraCallback<M>() {
                    override fun onSuccess(model: M) {
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

    override fun createListDataFetcher(): ListDataFetcher<M> {
        return object : ListDataFetcher<M>() {

            override fun fetchListData(description: String?, listener: OnLoadListener?): LiveData<MutableList<M>> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType): Boolean {
                        if (type === DataSource.CacheType.DATABASE) {
                            return onLoadFromCacheList(liveData)
                        }
                        liveData.postValue(arrayListOf())
                        return false
                    }

                    override fun loadFromNetwork() {
                        val time = System.currentTimeMillis()
                        try {
                            rxOnLoadFromNetworkForList(liveData, listener)
                            onLoadFromNetwork(listCallback(), listener)
                        } catch (ignore: Exception) {
                            listener?.onLoad(OnLoadListener.Source.NETWORK, OnLoadListener.FAILURE, System.currentTimeMillis() - time)
                        }
                    }
                })
                return liveData
            }

            override fun listCallback(): DoraListCallback<M> {
                return object : DoraListCallback<M>() {
                    override fun onSuccess(models: MutableList<M>) {
                        parseModels(models, liveData)
                    }

                    override fun onFailure(msg: String) {
                        onParseModelsFailure(msg)
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

    protected open fun onLoadFromCache(liveData: MutableLiveData<M?>) : Boolean {
        if (!checkParamsValid()) throw IllegalArgumentException(
            "Please check parameters, checkParamsValid returned false.")
        val time = System.currentTimeMillis()
        val model = (cacheHolder as DatabaseCacheHolder<M>).queryCache(query())
        model?.let {
            onInterceptData(DataSource.Type.CACHE, it)
            liveData.postValue(it)
            listener?.onLoad(OnLoadListener.Source.CACHE, OnLoadListener.SUCCESS, System.currentTimeMillis() - time)
            return true
        }
        listener?.onLoad(OnLoadListener.Source.CACHE, OnLoadListener.FAILURE, System.currentTimeMillis() - time)
        return false
    }

    protected open fun onLoadFromCacheList(liveData: MutableLiveData<MutableList<M>>) : Boolean {
        if (!checkParamsValid()) throw IllegalArgumentException(
            "Please check parameters, checkParamsValid returned false.")
        val time = System.currentTimeMillis()
        val models = (listCacheHolder as ListDatabaseCacheHolder<M>).queryCache(query())
        models?.let {
            val data = onFilterData(DataSource.Type.CACHE, it)
            onInterceptData(DataSource.Type.CACHE, data)
            liveData.postValue(data)
            listener?.onLoad(OnLoadListener.Source.CACHE, OnLoadListener.SUCCESS, System.currentTimeMillis() -time)
            return true
        }
        listener?.onLoad(OnLoadListener.Source.CACHE, OnLoadListener.FAILURE, System.currentTimeMillis() - time)
        return false
    }

    /**
     * In non-collection data mode, it needs to be overridden; either a callback or an observable
     * can be used.
     * 简体中文：非集合数据模式需要重写它，callback和observable二选一。
     */
    override fun onLoadFromNetwork(callback: DoraCallback<M>, listener: OnLoadListener?) {
    }

    /**
     * In collection data mode, it needs to be overridden; either a callback or an observable can be
     * used.
     * 简体中文：集合数据模式需要重写它，callback和observable二选一。
     */
    override fun onLoadFromNetwork(callback: DoraListCallback<M>, listener: OnLoadListener?) {
    }

    /**
     * In non-collection data mode, it needs to be overridden; either a callback or an observable
     * must be used.
     * 简体中文：非集合数据模式需要重写它，callback和observable二选一。
     */
    override fun onLoadFromNetworkObservable(listener: OnLoadListener?) : Observable<M> {
        return Observable.empty()
    }

    /**
     * In collection data mode, it needs to be overridden; either a callback or an observable must
     * be used.
     * 简体中文：集合数据模式需要重写它，callback和observable二选一。
     */
    override fun onLoadFromNetworkObservableList(listener: OnLoadListener?) : Observable<MutableList<M>> {
        return Observable.empty()
    }

    protected fun rxOnLoadFromNetwork(liveData: MutableLiveData<M?>, listener: OnLoadListener? = null) {
        RxTransformer.doApiObserver(onLoadFromNetworkObservable(listener), object : Observer<M> {
            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(model: M & Any) {
                parseModel(model, liveData)
            }

            override fun onError(e: Throwable) {
                onParseModelFailure(e.toString())
            }

            override fun onComplete() {
            }
        })
    }

    protected fun rxOnLoadFromNetworkForList(liveData: MutableLiveData<MutableList<M>>, listener: OnLoadListener? = null) {
        RxTransformer.doApiObserver(onLoadFromNetworkObservableList(listener), object : Observer<MutableList<M>> {
            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(models: MutableList<M>) {
                parseModels(models, liveData)
            }

            override fun onError(e: Throwable) {
                onParseModelsFailure(e.toString())
            }

            override fun onComplete() {
            }
        })
    }

    /**
     * Whether to enable append mode, which is only effective in list mode.
     * 简体中文：是否开启追加模式，仅list模式有效。
     */
    protected open fun disallowForceUpdate() : Boolean {
        return false
    }

    protected open fun parseModel(model: M, liveData: MutableLiveData<M?>) {
        val time = System.currentTimeMillis()
        model.let {
            if (isLogPrint) {
                Log.d(TAG, "【$description】$it")
            }
            onInterceptData(DataSource.Type.NETWORK, it)
            if (!checkParamsValid()) throw IllegalArgumentException(
                "Please check parameters, checkParamsValid returned false.")
            (cacheHolder as DatabaseCacheHolder<M>).removeOldCache(query())
            (cacheHolder as DatabaseCacheHolder<M>).addNewCache(it)
            listener?.onLoad(OnLoadListener.Source.NETWORK, OnLoadListener.SUCCESS, System.currentTimeMillis() - time)
            liveData.postValue(it)
        }
    }

    protected open fun parseModels(models: MutableList<M>?,
                            liveData: MutableLiveData<MutableList<M>>) {
        val time = System.currentTimeMillis()
        models?.let {
            if (isLogPrint) {
                for (model in it) {
                    Log.d(TAG, "【$description】$model")
                }
            }
            val data = onFilterData(DataSource.Type.NETWORK, it)
            onInterceptData(DataSource.Type.NETWORK, data)
            if (!checkParamsValid()) throw IllegalArgumentException(
                "Please check parameters, checkParamsValid returned false.")
            if (!disallowForceUpdate()) {
                (listCacheHolder as ListDatabaseCacheHolder<M>).removeOldCache(query())
            }
            (listCacheHolder as ListDatabaseCacheHolder<M>).addNewCache(data)
            listener?.onLoad(OnLoadListener.Source.NETWORK, OnLoadListener.SUCCESS, System.currentTimeMillis() - time)
            if (disallowForceUpdate()) {
                val oldValue = liveData.value
                oldValue?.addAll(data)
                liveData.value = oldValue
            } else {
                liveData.postValue(data)
            }
        }
    }

    protected open fun onParseModelFailure(msg: String) {
        val time = System.currentTimeMillis()
        if (isLogPrint) {
            if (description == null || description == "") {
                description = javaClass.simpleName
            }
            Log.d(TAG, "【${description}】$msg")
        }
        listener?.onLoad(OnLoadListener.Source.NETWORK, OnLoadListener.FAILURE, System.currentTimeMillis() - time)
        if (isClearDataOnNetworkError) {
            if (!checkParamsValid()) throw IllegalArgumentException(
                "Please check parameters, checkParamsValid returned false.")
            clearData()
            (cacheHolder as DatabaseCacheHolder<M>).removeOldCache(query())
        }
    }

    protected open fun onParseModelsFailure(msg: String) {
        val time = System.currentTimeMillis()
        if (isLogPrint) {
            if (description == null || description == "") {
                description = javaClass.simpleName
            }
            Log.d(TAG, "【${description}】$msg")
        }
        listener?.onLoad(OnLoadListener.Source.NETWORK, OnLoadListener.FAILURE, System.currentTimeMillis() - time)
        if (isClearDataOnNetworkError) {
            if (!checkParamsValid()) throw IllegalArgumentException(
                "Please check parameters, checkParamsValid returned false.")
            clearListData()
            (listCacheHolder as ListDatabaseCacheHolder<M>).removeOldCache(query())
        }
    }
}