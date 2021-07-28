package dora.cache.repository

import android.content.Context
import androidx.lifecycle.LiveData
import dora.cache.data.fetcher.DataFetcher
import dora.cache.data.fetcher.ListDataFetcher
import dora.cache.data.page.IDataPager
import dora.db.builder.WhereBuilder
import dora.http.DoraCallback
import dora.http.DoraListCallback
import dora.cache.MemoryCache
import dora.cache.data.page.DataPager
import dora.db.builder.Condition

abstract class BaseMemoryCacheRepository<M>(context: Context) : BaseRepository<M>(context) {

    /**
     * 根据查询条件进行初步的过滤从数据库加载的数据，过滤不完全则再调用onInterceptData。
     *
     * @return
     */
    protected fun where(): Condition {
        return WhereBuilder.create().toCondition()
    }

    /**
     * 在冷启动时调用，从数据库将数据加载到内存。
     */
    abstract fun loadData(): Any?
    override fun createDataFetcher(): DataFetcher<M> {
        return object : DataFetcher<M>() {
            override fun fetchData(): LiveData<M> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType): Boolean {
                        try {
                            if (type === DataSource.CacheType.MEMORY) {
                                val model = MemoryCache.getCacheFromMemory(cacheName) as M
                                onInterceptData(DataSource.Type.CACHE, model)
                                liveData.setValue(model)
                            } else if (type === DataSource.CacheType.DATABASE) {
                                val model = cacheHolder.queryCache(where())
                                if (model != null) {
                                    onInterceptData(DataSource.Type.CACHE, model)
                                    liveData.value = model
                                    MemoryCache.updateCacheAtMemory(cacheName, model)
                                }
                            }
                        } catch (e: Exception) {
                            return false
                        }
                        return true
                    }

                    override fun loadFromNetwork() {
                        onLoadFromNetwork(callback())
                    }
                })
                return liveData
            }

            override fun callback(): DoraCallback<M> {
                return object : DoraCallback<M>() {
                    override fun onSuccess(data: M) {
                        onInterceptNetworkData(data)
                        MemoryCache.updateCacheAtMemory(cacheName, data!!)
                        cacheHolder.removeOldCache(where())
                        cacheHolder.addNewCache(data)
                        liveData.value = data
                    }

                    override fun onFailure(code: Int, msg: String?) {
                        if (isClearDataOnNetworkError) {
                            liveData.value = null
                            MemoryCache.removeCacheAtMemory(cacheName)
                            cacheHolder.removeOldCache(where())
                        }
                    }

                    override fun onInterceptNetworkData(data: M) {
                        onInterceptData(DataSource.Type.NETWORK, data)
                    }
                }
            }
        }
    }

    abstract val cacheName: String
    override fun createListDataFetcher(): ListDataFetcher<M> {
        return object : ListDataFetcher<M>() {

            override fun fetchListData(): LiveData<List<M>> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType): Boolean {
                        try {
                            if (type === DataSource.CacheType.MEMORY) {
                                val models = MemoryCache.getCacheFromMemory(cacheName) as List<M>
                                onInterceptData(DataSource.Type.CACHE, models)
                                liveData.setValue(models)
                            } else if (type === DataSource.CacheType.DATABASE) {
                                val models = listCacheHolder.queryCache(where())
                                onInterceptData(DataSource.Type.CACHE, models!!)
                                liveData.value = models
                                MemoryCache.updateCacheAtMemory(cacheName, models!!)
                            }
                        } catch (e: Exception) {
                            return false
                        }
                        return true
                    }

                    override fun loadFromNetwork() {
                        onLoadFromNetwork(listCallback())
                    }
                })
                return liveData
            }

            override fun listCallback(): DoraListCallback<M> {
                return object : DoraListCallback<M>() {
                    override fun onSuccess(data: List<M>) {
                        onInterceptNetworkData(data)
                        MemoryCache.updateCacheAtMemory(cacheName, data)
                        listCacheHolder.removeOldCache(where())
                        listCacheHolder.addNewCache(data)
                        liveData.value = data
                    }

                    override fun onFailure(code: Int, msg: String?) {
                        if (isClearDataOnNetworkError) {
                            listCacheHolder.removeOldCache(where())
                            liveData.value = null
                            MemoryCache.removeCacheAtMemory(cacheName)
                        }
                    }

                    override fun onInterceptNetworkData(data: List<M>) {
                        onInterceptData(DataSource.Type.NETWORK, data)
                    }
                }
            }

            override fun obtainPager(): IDataPager<M> {
                return DataPager(liveData.value as List<M>)
            }
        }
    }

    init {
        cacheStrategy = CacheStrategy.MEMORY_CACHE
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