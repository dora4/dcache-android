package dora.cache.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import dora.cache.data.fetcher.DataFetcher
import dora.cache.data.fetcher.ListDataFetcher
import dora.cache.data.page.IDataPager
import dora.db.builder.WhereBuilder
import dora.http.DoraCallback
import dora.http.DoraListCallback
import dora.cache.MemoryCache
import dora.cache.data.fetcher.IDataFetcher
import dora.cache.data.fetcher.IListDataFetcher
import dora.cache.data.page.DataPager
import dora.db.builder.Condition
import dora.rx.RxTransformer
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

@RepositoryType(BaseRepository.CacheStrategy.MEMORY_CACHE)
abstract class BaseMemoryCacheRepository<M>(context: Context) : BaseRepository<M>(context) {

    /**
     * 不要随意修改。
     */
    abstract var cacheName: String

    /**
     * 根据查询条件进行初步的过滤从数据库加载的数据，过滤不完全则再调用onInterceptData。
     *
     * @return
     */
    open fun where(): Condition {
        return WhereBuilder.create().toCondition()
    }

    /**
     * 在冷启动时调用，从数据库将数据加载到内存。
     */
    internal fun loadCacheInternal(): Any? {
        return if (isListMode) {
            onLoadListData()
        } else {
            onLoadData()
        }
    }

    open fun onLoadData() : M? {
        return null
    }

    open fun onLoadListData() : List<M>? {
        return null
    }

    override fun createDataFetcher(): DataFetcher<M> {
        return object : DataFetcher<M>() {
            override fun fetchData(listener: IDataFetcher.OnLoadListener?): LiveData<M?> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType): Boolean {
                        try {
                            if (type === DataSource.CacheType.MEMORY) {
                                val model = MemoryCache.getCacheFromMemory(cacheName) as M
                                model?.let {
                                    onInterceptData(DataSource.Type.CACHE, it)
                                    liveData.postValue(it)
                                }
                            } else if (type === DataSource.CacheType.DATABASE) {
                                val model = cacheHolder.queryCache(where())
                                model?.let {
                                    onInterceptData(DataSource.Type.CACHE, it)
                                    liveData.postValue(it)
                                    MemoryCache.updateCacheAtMemory(cacheName, it)
                                    return true
                                }
                                return false
                            }
                        } catch (e: Exception) {
                            return false
                        }
                        return true
                    }

                    override fun loadFromNetwork() {
                        RxTransformer.doApi(onLoadFromNetworkObservable(), object : Observer<M> {
                            override fun onSubscribe(d: Disposable?) {
                            }

                            override fun onNext(model: M) {
                                model?.let {
                                    if (isLogPrint) {
                                        Log.d(TAG, it.toString())
                                    }
                                    onInterceptData(DataSource.Type.NETWORK, it)
                                    MemoryCache.updateCacheAtMemory(cacheName, it as Any)
                                    cacheHolder.removeOldCache(where())
                                    cacheHolder.addNewCache(it)
                                    liveData.postValue(it)
                                }
                                listener?.onSuccess()
                            }

                            override fun onError(e: Throwable?) {
                                if (isLogPrint) {
                                    Log.d(TAG, e.toString())
                                }
                                if (isClearDataOnNetworkError) {
                                    clearData()
                                    MemoryCache.removeCacheAtMemory(cacheName)
                                    cacheHolder.removeOldCache(where())
                                }
                                listener?.onFailure(e.toString())
                            }

                            override fun onComplete() {
                            }

                        })
                        onLoadFromNetwork(callback())
                    }
                })
                return liveData
            }

            override fun callback(listener: IDataFetcher.OnLoadListener?): DoraCallback<M> {
                return object : DoraCallback<M>() {
                    override fun onSuccess(model: M) {
                        model?.let {
                            if (isLogPrint) {
                                Log.d(TAG, it.toString())
                            }
                            onInterceptData(DataSource.Type.NETWORK, it)
                            MemoryCache.updateCacheAtMemory(cacheName, it as Any)
                            cacheHolder.removeOldCache(where())
                            cacheHolder.addNewCache(it)
                            liveData.postValue(it)
                        }
                        listener?.onSuccess()
                    }

                    override fun onFailure(msg: String) {
                        if (isLogPrint) {
                            Log.d(TAG, msg)
                        }
                        if (isClearDataOnNetworkError) {
                            clearData()
                            MemoryCache.removeCacheAtMemory(cacheName)
                            cacheHolder.removeOldCache(where())
                        }
                        listener?.onFailure(msg)
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
                        try {
                            if (type === DataSource.CacheType.MEMORY) {
                                val models = MemoryCache.getCacheFromMemory(cacheName) as MutableList<M>
                                models?.let {
                                    onInterceptData(DataSource.Type.CACHE, it)
                                    liveData.postValue(it)
                                }
                            } else if (type === DataSource.CacheType.DATABASE) {
                                val models = listCacheHolder.queryCache(where())
                                models?.let {
                                    onInterceptData(DataSource.Type.CACHE, it)
                                    liveData.postValue(it)
                                    MemoryCache.updateCacheAtMemory(cacheName, it)
                                    return true
                                }
                                return false
                            }
                        } catch (e: Exception) {
                            return false
                        }
                        return true
                    }

                    override fun loadFromNetwork() {
                        RxTransformer.doApi(onLoadFromNetworkObservableList(), object : Observer<MutableList<M>> {
                            override fun onSubscribe(d: Disposable?) {
                            }

                            override fun onNext(models: MutableList<M>?) {
                                models?.let {
                                    if (isLogPrint) {
                                        for (model in it) {
                                            Log.d(TAG, model.toString())
                                        }
                                    }
                                    onInterceptData(DataSource.Type.NETWORK, it)
                                    MemoryCache.updateCacheAtMemory(cacheName, it)
                                    listCacheHolder.removeOldCache(where())
                                    listCacheHolder.addNewCache(it)
                                    liveData.postValue(it)
                                }
                                listener?.onSuccess()
                            }

                            override fun onError(e: Throwable?) {
                                if (isLogPrint) {
                                    Log.d(TAG, e.toString())
                                }
                                if (isClearDataOnNetworkError) {
                                    listCacheHolder.removeOldCache(where())
                                    clearListData()
                                    MemoryCache.removeCacheAtMemory(cacheName)
                                }
                                listener?.onFailure(e.toString())
                            }

                            override fun onComplete() {
                            }
                        })
                        onLoadFromNetwork(listCallback())
                    }
                })
                return liveData
            }

            override fun listCallback(listener: IListDataFetcher.OnLoadListener?): DoraListCallback<M> {
                return object : DoraListCallback<M>() {
                    override fun onSuccess(models: MutableList<M>) {
                        models?.let {
                            if (isLogPrint) {
                                for (model in it) {
                                    Log.d(TAG, model.toString())
                                }
                            }
                            onInterceptData(DataSource.Type.NETWORK, it)
                            MemoryCache.updateCacheAtMemory(cacheName, it)
                            listCacheHolder.removeOldCache(where())
                            listCacheHolder.addNewCache(it)
                            liveData.postValue(it)
                        }
                        listener?.onSuccess()
                    }

                    override fun onFailure(msg: String) {
                        if (isLogPrint) {
                            Log.d(TAG, msg)
                        }
                        if (isClearDataOnNetworkError) {
                            listCacheHolder.removeOldCache(where())
                            clearListData()
                            MemoryCache.removeCacheAtMemory(cacheName)
                        }
                        listener?.onFailure(msg)
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
}