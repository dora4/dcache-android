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
import dora.cache.holder.DoraListMMKVCacheHolder
import dora.cache.holder.DoraMMKVCacheHolder
import dora.cache.factory.MMKVCacheHolderFactory
import dora.http.DoraCallback
import dora.http.DoraListCallback
import dora.http.rx.RxTransformer
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

abstract class BaseMMKVCacheRepository<M>(context: Context) : BaseRepository<M, MMKVCacheHolderFactory<M>>(context) {

    override fun selectData(ds: DataSource, l: OnLoadListener) {
        val isLoaded = ds.loadFromCache(DataSource.CacheType.MMKV)
        l.onLoad(OnLoadListener.Source.CACHE, if (isLoaded) OnLoadListener.SUCCESS else OnLoadListener.FAILURE)
        if (isNetworkAvailable) {
            ds.loadFromNetwork()
        }
    }

    override fun createDataFetcher(): DataFetcher<M> {
        return object : DataFetcher<M>() {

            override fun fetchData(description: String?, listener: OnLoadListener?): LiveData<M?> {
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
                        if (type === DataSource.CacheType.MMKV) {
                            return onLoadFromCache(liveData)
                        }
                        liveData.postValue(null)
                        return false
                    }

                    override fun loadFromNetwork() {
                        try {
                            rxOnLoadFromNetwork(liveData, delegate)
                            onLoadFromNetwork(callback(), delegate)
                        } catch (ignore: Exception) {
                            delegate.onLoad(OnLoadListener.Source.NETWORK, OnLoadListener.FAILURE)
                        }
                    }
                }, delegate)
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
                        if (type === DataSource.CacheType.MMKV) {
                            return onLoadFromCacheList(liveData)
                        }
                        liveData.postValue(arrayListOf())
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

    private fun onLoadFromCache(liveData: MutableLiveData<M?>) : Boolean {
        val model = (cacheHolder as DoraMMKVCacheHolder).readCache(getCacheKey())
        model?.let {
            onInterceptData(DataSource.Type.CACHE, it)
            liveData.postValue(it)
            listener?.onLoad(OnLoadListener.Source.CACHE, OnLoadListener.SUCCESS)
            return true
        }
        listener?.onLoad(OnLoadListener.Source.CACHE, OnLoadListener.FAILURE)
        return false
    }

    private fun onLoadFromCacheList(liveData: MutableLiveData<MutableList<M>>) : Boolean {
        val models = (listCacheHolder as DoraListMMKVCacheHolder).readCache(getCacheKey())
        if (models != null && models.size > 0) {
            val data = onFilterData(DataSource.Type.CACHE, models)
            onInterceptData(DataSource.Type.CACHE, data)
            liveData.postValue(data)
            listener?.onLoad(OnLoadListener.Source.CACHE, OnLoadListener.SUCCESS)
            return true
        }
        listener?.onLoad(OnLoadListener.Source.CACHE, OnLoadListener.FAILURE)
        return false
    }

    override fun onLoadFromNetwork(callback: DoraCallback<M>, listener: OnLoadListener?) {
    }

    override fun onLoadFromNetwork(callback: DoraListCallback<M>, listener: OnLoadListener?) {
    }

    override fun onLoadFromNetworkObservable(listener: OnLoadListener?) : Observable<M> {
        return Observable.empty()
    }

    override fun onLoadFromNetworkObservableList(listener: OnLoadListener?) : Observable<MutableList<M>> {
        return Observable.empty()
    }

    private fun rxOnLoadFromNetwork(liveData: MutableLiveData<M?>, listener: OnLoadListener? = null) {
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

    private fun rxOnLoadFromNetworkForList(liveData: MutableLiveData<MutableList<M>>, listener: OnLoadListener? = null) {
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

    protected open fun parseModel(model: M, liveData: MutableLiveData<M?>) {
        model?.let {
            if (isLogPrint) {
                Log.d(TAG, "【$description】$it")
            }
            onInterceptData(DataSource.Type.NETWORK, it)
            (cacheHolder as DoraMMKVCacheHolder).addNewCache(getCacheKey(), it)
            listener?.onLoad(OnLoadListener.Source.NETWORK, OnLoadListener.SUCCESS)
            liveData.postValue(it)
        }
    }

    protected open fun parseModels(models: MutableList<M>?,
                            liveData: MutableLiveData<MutableList<M>>) {
        models?.let {
            if (isLogPrint) {
                for (model in it) {
                    Log.d(TAG, "【$description】${model.toString()}")
                }
            }
            val data = onFilterData(DataSource.Type.NETWORK, it)
            onInterceptData(DataSource.Type.NETWORK, data)
            (listCacheHolder as DoraListMMKVCacheHolder).removeOldCache(getCacheKey())
            (listCacheHolder as DoraListMMKVCacheHolder).addNewCache(getCacheKey(), data)
            listener?.onLoad(OnLoadListener.Source.NETWORK, OnLoadListener.SUCCESS)
            liveData.postValue(data)
        }
    }

    protected open fun onParseModelFailure(msg: String) {
        if (isLogPrint) {
            if (description == null || description == "") {
                description = javaClass.simpleName
            }
            Log.d(TAG, "【${description}】$msg")
        }
        listener?.onLoad(OnLoadListener.Source.NETWORK, OnLoadListener.FAILURE)
        if (isClearDataOnNetworkError) {
            clearData()
            (cacheHolder as DoraMMKVCacheHolder).removeOldCache(getCacheKey())
        }
    }

    protected open fun onParseModelsFailure(msg: String) {
        if (isLogPrint) {
            if (description == null || description == "") {
                description = javaClass.simpleName
            }
            Log.d(TAG, "【${description}】$msg")
        }
        listener?.onLoad(OnLoadListener.Source.NETWORK, OnLoadListener.FAILURE)
        if (isClearDataOnNetworkError) {
            clearListData()
            (listCacheHolder as DoraListMMKVCacheHolder).readCache(getCacheKey())
        }
    }

    abstract fun getCacheKey() : String
}