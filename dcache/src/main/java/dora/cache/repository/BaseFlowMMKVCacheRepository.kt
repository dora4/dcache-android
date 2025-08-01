package dora.cache.repository

import android.content.Context
import android.util.Log
import dora.cache.data.fetcher.FlowDataFetcher
import dora.cache.data.fetcher.ListFlowDataFetcher
import dora.cache.data.fetcher.OnLoadListener
import dora.cache.data.notify.IDataPublisher
import dora.cache.data.notify.IListDataPublisher
import dora.cache.data.page.DataPager
import dora.cache.data.page.IDataPager
import dora.cache.holder.DoraListMMKVCacheHolder
import dora.cache.holder.DoraMMKVCacheHolder
import dora.cache.factory.MMKVCacheHolderFactory
import dora.http.DoraCallback
import dora.http.DoraListCallback
import dora.http.rx.RxUtils
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.flow.MutableStateFlow

abstract class BaseFlowMMKVCacheRepository<M>(context: Context) : BaseFlowRepository<M, MMKVCacheHolderFactory<M>>(context) {

    override fun selectData(ds: DataSource, l: OnLoadListener) {
        val isLoaded = ds.loadFromCache(DataSource.CacheType.MMKV)
        l.onLoad(OnLoadListener.Source.CACHE, if (isLoaded) OnLoadListener.SUCCESS else OnLoadListener.FAILURE)
        if (isNetworkAvailable) {
            ds.loadFromNetwork()
        }
    }

    override fun createDataFetcher(): FlowDataFetcher<M> {
        return object : FlowDataFetcher<M>() {

            override fun fetchData(description: String?, listener: OnLoadListener?): MutableStateFlow<M?> {
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
                            return onLoadFromCache(flowData)
                        }
                        flowData.value = null
                        if (isNotify) IDataPublisher.DEFAULT.send(getModelType(), null)
                        return false
                    }

                    override fun loadFromNetwork() {
                        try {
                            rxOnLoadFromNetwork(flowData, delegate)
                            onLoadFromNetwork(callback(), delegate)
                        } catch (ignore: Exception) {
                            delegate.onLoad(OnLoadListener.Source.NETWORK, OnLoadListener.FAILURE)
                        }
                    }
                }, delegate)
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
                if (isNotify) IDataPublisher.DEFAULT.send(getModelType(), null)
            }
        }
    }

    override fun createListDataFetcher(): ListFlowDataFetcher<M> {
        return object : ListFlowDataFetcher<M>() {

            override fun fetchListData(description: String?, listener: OnLoadListener?): MutableStateFlow<MutableList<M>> {
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
                            return onLoadFromCacheList(flowData)
                        }
                        flowData.value = arrayListOf()
                        if (isNotify) IListDataPublisher.DEFAULT.send(getModelType(), arrayListOf())
                        return false
                    }

                    override fun loadFromNetwork() {
                        try {
                            rxOnLoadFromNetworkForList(flowData, delegate)
                            onLoadFromNetwork(listCallback(), delegate)
                        } catch (ignore: Exception) {
                            delegate.onLoad(OnLoadListener.Source.NETWORK, OnLoadListener.FAILURE)
                        }
                    }
                }, delegate)
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
                return DataPager(flowData.value ?: arrayListOf())
            }

            override fun clearListData() {
                flowData.value = arrayListOf()
                if (isNotify) IListDataPublisher.DEFAULT.send(getModelType(), arrayListOf())
            }
        }
    }

    private fun onLoadFromCache(flowData: MutableStateFlow<M?>) : Boolean {
        val model = (cacheHolder as DoraMMKVCacheHolder).readCache(getCacheKey())
        model?.let {
            onInterceptData(DataSource.Type.CACHE, it)
            flowData.value = it
            if (isNotify) IDataPublisher.DEFAULT.send(getModelType(), it)
            listener?.onLoad(OnLoadListener.Source.CACHE, OnLoadListener.SUCCESS)
            return true
        }
        listener?.onLoad(OnLoadListener.Source.CACHE, OnLoadListener.FAILURE)
        return false
    }

    private fun onLoadFromCacheList(liveData: MutableStateFlow<MutableList<M>>) : Boolean {
        val models = (listCacheHolder as DoraListMMKVCacheHolder).readCache(getCacheKey())
        if (models != null && models.size > 0) {
            val data = onFilterData(DataSource.Type.CACHE, models)
            onInterceptData(DataSource.Type.CACHE, data)
            liveData.value = data
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

    private fun rxOnLoadFromNetwork(flowData: MutableStateFlow<M?>, listener: OnLoadListener? = null) {
        val flowable = backPressure(onLoadFromNetworkObservable(listener))
        requestQueue.add(flowable)
        RxUtils.doApiObserver(flowable.toObservable(), object : Observer<M> {
            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(model: M & Any) {
                parseModel(model, flowData)
            }

            override fun onError(e: Throwable) {
                onParseModelFailure(e.toString())
            }

            override fun onComplete() {
            }
        })
    }

    private fun rxOnLoadFromNetworkForList(flowData: MutableStateFlow<MutableList<M>>, listener: OnLoadListener? = null) {
        val flowable = backPressureList(onLoadFromNetworkObservableList(listener))
        requestQueue.addList(flowable)
        RxUtils.doApiObserver(flowable.toObservable(), object : Observer<MutableList<M>> {
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
            (cacheHolder as DoraMMKVCacheHolder).addNewCache(getCacheKey(), it)
            listener?.onLoad(OnLoadListener.Source.NETWORK, OnLoadListener.SUCCESS)
            flowData.value = it
            if (isNotify) IDataPublisher.DEFAULT.send(getModelType(), it)
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
            val data = onFilterData(DataSource.Type.NETWORK, it)
            onInterceptData(DataSource.Type.NETWORK, data)
            (listCacheHolder as DoraListMMKVCacheHolder).removeOldCache(getCacheKey())
            (listCacheHolder as DoraListMMKVCacheHolder).addNewCache(getCacheKey(), data)
            listener?.onLoad(OnLoadListener.Source.NETWORK, OnLoadListener.SUCCESS)
            flowData.value = data
            if (isNotify) IListDataPublisher.DEFAULT.send(getModelType(), data)
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