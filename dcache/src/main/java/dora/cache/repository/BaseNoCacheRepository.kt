package dora.cache.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import dora.cache.data.fetcher.DataFetcher
import dora.cache.data.fetcher.IDataFetcher
import dora.cache.data.fetcher.IListDataFetcher
import dora.cache.data.fetcher.ListDataFetcher
import dora.cache.data.page.DataPager
import dora.cache.data.page.IDataPager
import dora.cache.holder.CacheHolder
import dora.cache.holder.EmptyCacheHolder
import dora.http.DoraCallback
import dora.http.DoraListCallback
import dora.rx.RxTransformer
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

@RepositoryType
abstract class BaseNoCacheRepository<M> protected constructor(context: Context) :
        BaseRepository<M>(context) {

    override fun createDataFetcher(): DataFetcher<M> {
        return object : DataFetcher<M>() {
            override fun fetchData(listener: IDataFetcher.OnLoadListener?): LiveData<M?> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType): Boolean {
                        return false
                    }

                    override fun loadFromNetwork() {
                        RxTransformer.doApi(onLoadFromNetworkObservable(), object : Observer<M> {
                            override fun onSubscribe(d: Disposable?) {
                            }

                            override fun onNext(model: M) {
                                model.let {
                                    if (isLogPrint) {
                                        Log.d(TAG, it.toString())
                                    }
                                    onInterceptData(DataSource.Type.NETWORK, it)
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
                                }
                                listener?.onFailure(-1, e.toString())
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
                        model.let {
                            if (isLogPrint) {
                                Log.d(TAG, it.toString())
                            }
                            onInterceptData(DataSource.Type.NETWORK, it)
                            liveData.postValue(it)
                        }
                        listener?.onSuccess()
                    }

                    override fun onFailure(code: Int, msg: String?) {
                        if (isLogPrint) {
                            Log.d(TAG, "$code:$msg")
                        }
                        if (isClearDataOnNetworkError) {
                            clearData()
                        }
                        listener?.onFailure(code, msg)
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
                        return false
                    }

                    override fun loadFromNetwork() {
                        onLoadFromNetwork(listCallback())
                    }
                })
                return liveData
            }

            override fun listCallback(listener: IListDataFetcher.OnLoadListener?): DoraListCallback<M> {
                return object : DoraListCallback<M>() {
                    override fun onSuccess(models: MutableList<M>) {
                        models.let {
                            if (isLogPrint) {
                                for (model in it) {
                                    Log.d(TAG, model.toString())
                                }
                            }
                            onInterceptData(DataSource.Type.NETWORK, it)
                            liveData.postValue(it)
                        }
                        listener?.onSuccess()
                    }

                    override fun onFailure(code: Int, msg: String?) {
                        if (isLogPrint) {
                            Log.d(TAG, "$code:$msg")
                        }
                        if (isClearDataOnNetworkError) {
                            clearListData()
                        }
                        listener?.onFailure(code, msg)
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

    override fun createCacheHolder(clazz: Class<M>): CacheHolder<M> {
        return EmptyCacheHolder()
    }

    override fun createListCacheHolder(clazz: Class<M>): CacheHolder<MutableList<M>> {
        return EmptyCacheHolder()
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