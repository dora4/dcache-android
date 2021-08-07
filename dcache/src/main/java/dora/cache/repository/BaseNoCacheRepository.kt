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

/**
 * 不启用缓存的repository，通常用它进行网络数据返回的测试。
 */
@RepositoryType
abstract class BaseNoCacheRepository<M> protected constructor(context: Context) :
        BaseRepository<M>(context) {

    override fun createDataFetcher(): DataFetcher<M> {
        return object : DataFetcher<M>() {
            override fun fetchData(): LiveData<M> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType): Boolean {
                        Log.d("BaseNoCacheRepository", "loadFromCache")
                        return false
                    }

                    override fun loadFromNetwork() {
                        Log.d("BaseNoCacheRepository", "loadFromNetwork")
                        onLoadFromNetwork(callback())
                    }
                })
                return liveData
            }

            override fun callback(): DoraCallback<M> {
                return object : DoraCallback<M>() {
                    override fun onSuccess(model: M) {
                        Log.d("BaseNoCacheRepository", "onSuccess:$model")
                        onInterceptNetworkData(model)
                        liveData.value = model
                    }

                    override fun onFailure(code: Int, msg: String?) {
                        Log.d("BaseNoCacheRepository", "onFailure:$msg")
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
                        Log.d("BaseNoCacheRepository", "loadFromCache")
                        return false
                    }

                    override fun loadFromNetwork() {
                        Log.d("BaseNoCacheRepository", "loadFromNetwork")
                        onLoadFromNetwork(listCallback())
                    }
                })
                return liveData
            }

            override fun listCallback(): DoraListCallback<M> {
                return object : DoraListCallback<M>() {
                    override fun onSuccess(models: List<M>) {
                        Log.d("BaseNoCacheRepository", "onSuccess:$models")
                        onInterceptNetworkData(models)
                        liveData.value = models
                    }

                    override fun onFailure(code: Int, msg: String?) {
                        Log.d("BaseNoCacheRepository", "onFailure:$msg")
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