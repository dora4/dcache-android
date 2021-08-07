package dora.cache.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import dora.cache.data.fetcher.DataFetcher
import dora.cache.data.fetcher.ListDataFetcher
import dora.cache.data.page.DataPager
import dora.cache.data.page.IDataPager
import dora.cache.holder.CacheHolder
import dora.cache.holder.EmptyCacheHolder
import dora.http.DoraCallback
import dora.http.DoraListCallback

@RepositoryType
abstract class BaseNoCacheRepository<M> protected constructor(context: Context) :
        BaseRepository<M>(context) {

    override fun createDataFetcher(): DataFetcher<M> {
        return object : DataFetcher<M>() {
            override fun fetchData(): LiveData<M?> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType): Boolean {
                        return false
                    }

                    override fun loadFromNetwork() {
                        onLoadFromNetwork(callback())
                    }
                })
                return liveData
            }

            override fun callback(): DoraCallback<M> {
                return object : DoraCallback<M>() {
                    override fun onSuccess(model: M) {
                        model.let {
                            if (isLogPrint) {
                                Log.d(TAG, it.toString())
                            }
                            onInterceptNetworkData(it)
                            liveData.value = it
                        }
                    }

                    override fun onFailure(code: Int, msg: String?) {
                        if (isLogPrint) {
                            Log.d(TAG, "$code:$msg")
                        }
                        if (isClearDataOnNetworkError) {
                            liveData.value = null
                        }
                    }

                    override fun onInterceptNetworkData(model: M) {
                        onInterceptData(DataSource.Type.NETWORK, model)
                    }
                }
            }
        }
    }

    override fun createListDataFetcher(): ListDataFetcher<M> {
        return object : ListDataFetcher<M>() {

            override fun fetchListData(): LiveData<List<M>> {
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

            override fun listCallback(): DoraListCallback<M> {
                return object : DoraListCallback<M>() {
                    override fun onSuccess(models: List<M>) {
                        models.let {
                            if (isLogPrint) {
                                for (model in it) {
                                    Log.d(TAG, model.toString())
                                }
                            }
                            onInterceptNetworkData(it)
                            liveData.value = it
                        }
                    }

                    override fun onFailure(code: Int, msg: String?) {
                        if (isLogPrint) {
                            Log.d(TAG, "$code:$msg")
                        }
                        if (isClearDataOnNetworkError) {
                            liveData.value = null
                        }
                    }

                    override fun onInterceptNetworkData(models: List<M>) {
                        onInterceptData(DataSource.Type.NETWORK, models)
                    }
                }
            }

            override fun obtainPager(): IDataPager<M> {
                return DataPager(liveData.value ?: arrayListOf())
            }
        }
    }

    override fun createCacheHolder(clazz: Class<M>): CacheHolder<M> {
        return EmptyCacheHolder()
    }

    override fun createListCacheHolder(clazz: Class<M>): CacheHolder<List<M>> {
        return EmptyCacheHolder()
    }

    /**
     * 非集合数据模式需要重写它。
     */
    override fun onLoadFromNetwork(callback: DoraCallback<M>) {
    }

    /**
     * 集合数据模式需要重写它。
     */
    override fun onLoadFromNetwork(callback: DoraListCallback<M>) {
    }
}