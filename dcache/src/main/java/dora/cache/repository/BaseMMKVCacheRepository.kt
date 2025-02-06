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

    override fun selectData(ds: DataSource): Boolean {
        val isLoaded = ds.loadFromCache(DataSource.CacheType.MMKV)
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

            override fun fetchData(description: String?, listener: OnLoadStateListener?): LiveData<M?> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType): Boolean {
                        if (type === DataSource.CacheType.MMKV) {
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
                            listener?.onLoad(OnLoadStateListener.Source.NETWORK, OnLoadStateListener.FAILURE,
                        System.currentTimeMillis() - time)
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

            override fun fetchListData(description: String?, listener: OnLoadStateListener?): LiveData<MutableList<M>> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType): Boolean {
                        if (type === DataSource.CacheType.MMKV) {
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
                            listener?.onLoad(OnLoadStateListener.Source.NETWORK, OnLoadStateListener.FAILURE,
                        System.currentTimeMillis() - time)
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

    private fun onLoadFromCache(liveData: MutableLiveData<M?>) : Boolean {
        val time = System.currentTimeMillis()
        val model = (cacheHolder as DoraMMKVCacheHolder).readCache(getCacheKey())
        model?.let {
            onInterceptData(DataSource.Type.CACHE, it)
            liveData.postValue(it)
            listener?.onLoad(OnLoadStateListener.Source.CACHE, OnLoadStateListener.SUCCESS,
        System.currentTimeMillis() - time)
            return true
        }
        listener?.onLoad(OnLoadStateListener.Source.CACHE, OnLoadStateListener.FAILURE,
    System.currentTimeMillis() - time)
        return false
    }

    private fun onLoadFromCacheList(liveData: MutableLiveData<MutableList<M>>) : Boolean {
        val time = System.currentTimeMillis()
        val models = (listCacheHolder as DoraListMMKVCacheHolder).readCache(getCacheKey())
        models?.let {
            val data = onFilterData(DataSource.Type.CACHE, it)
            onInterceptData(DataSource.Type.CACHE, data)
            liveData.postValue(it)
            listener?.onLoad(OnLoadStateListener.Source.CACHE, OnLoadStateListener.SUCCESS,
                System.currentTimeMillis() - time)
            return true
        }
        listener?.onLoad(OnLoadStateListener.Source.CACHE, OnLoadStateListener.FAILURE,
            System.currentTimeMillis() - time)
        return false
    }

    override fun onLoadFromNetwork(callback: DoraCallback<M>, listener: OnLoadStateListener?) {
    }

    override fun onLoadFromNetwork(callback: DoraListCallback<M>, listener: OnLoadStateListener?) {
    }

    override fun onLoadFromNetworkObservable(listener: OnLoadStateListener?) : Observable<M> {
        return Observable.empty()
    }

    override fun onLoadFromNetworkObservableList(listener: OnLoadStateListener?) : Observable<MutableList<M>> {
        return Observable.empty()
    }

    private fun rxOnLoadFromNetwork(liveData: MutableLiveData<M?>, listener: OnLoadStateListener? = null) {
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

    private fun rxOnLoadFromNetworkForList(liveData: MutableLiveData<MutableList<M>>, listener: OnLoadStateListener? = null) {
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
            val time = System.currentTimeMillis()
            if (isLogPrint) {
                Log.d(TAG, "【$description】$it")
            }
            onInterceptData(DataSource.Type.NETWORK, it)
            (cacheHolder as DoraMMKVCacheHolder).addNewCache(getCacheKey(), it)
            listener?.onLoad(OnLoadStateListener.Source.NETWORK, OnLoadStateListener.SUCCESS,
                System.currentTimeMillis() - time)
            liveData.postValue(it)
        }
    }

    protected open fun parseModels(models: MutableList<M>?,
                            liveData: MutableLiveData<MutableList<M>>) {
        models?.let {
            val time = System.currentTimeMillis()
            if (isLogPrint) {
                for (model in it) {
                    Log.d(TAG, "【$description】${model.toString()}")
                }
            }
            val data = onFilterData(DataSource.Type.NETWORK, it)
            onInterceptData(DataSource.Type.NETWORK, data)
            (listCacheHolder as DoraListMMKVCacheHolder).removeOldCache(getCacheKey())
            (listCacheHolder as DoraListMMKVCacheHolder).addNewCache(getCacheKey(), data)
            listener?.onLoad(OnLoadStateListener.Source.NETWORK, OnLoadStateListener.SUCCESS,
        System.currentTimeMillis() - time)
            liveData.postValue(data)
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
        listener?.onLoad(OnLoadStateListener.Source.NETWORK, OnLoadStateListener.FAILURE,
            System.currentTimeMillis() - time)
        if (isClearDataOnNetworkError) {
            clearData()
            (cacheHolder as DoraMMKVCacheHolder).removeOldCache(getCacheKey())
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
        listener?.onLoad(OnLoadStateListener.Source.NETWORK, OnLoadStateListener.FAILURE,
            System.currentTimeMillis() - time)
        if (isClearDataOnNetworkError) {
            clearListData()
            (listCacheHolder as DoraListMMKVCacheHolder).readCache(getCacheKey())
        }
    }

    abstract fun getCacheKey() : String
}