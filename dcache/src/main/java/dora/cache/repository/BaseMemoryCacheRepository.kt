package dora.cache.repository

import android.content.Context
import androidx.lifecycle.LiveData
import dora.cache.data.DataFetcher
import dora.cache.data.ListDataFetcher
import dora.cache.data.page.IDataPager
import dora.db.builder.WhereBuilder
import dora.http.DoraCallback
import dora.http.DoraListCallback
import dora.cache.MemoryCache
import dora.cache.data.page.DataPager
import dora.db.builder.Condition

abstract class BaseMemoryCacheRepository<M>(context: Context, clazz: Class<M>) : BaseRepository<M>(context) {

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
    override fun installDataFetcher(): DataFetcher<M> {
        return object : DataFetcher<M>() {
            override fun fetchData(): LiveData<M> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType?): Boolean {
                        try {
                            if (type === DataSource.CacheType.MEMORY) {
                                val model = MemoryCache.getCacheFromMemory(cacheName) as M
                                onInterceptData(DataSource.Type.CACHE, model)
                                liveData.setValue(model)
                            } else if (type === DataSource.CacheType.DATABASE) {
                                val model = cacheFactory.queryCache(where())
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
                        cacheFactory.removeOldCache(where())
                        cacheFactory.addNewCache(data)
                        liveData.value = data
                    }

                    override fun onFailure(code: Int, msg: String?) {
                        if (isClearDataOnNetworkError) {
                            liveData.value = null
                            MemoryCache.removeCacheAtMemory(cacheName)
                            cacheFactory.removeOldCache(where())
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
    override fun installListDataFetcher(): ListDataFetcher<M> {
        return object : ListDataFetcher<M>() {

            override fun fetchListData(): LiveData<List<M>> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType?): Boolean {
                        try {
                            if (type === DataSource.CacheType.MEMORY) {
                                val models = MemoryCache.getCacheFromMemory(cacheName) as List<M>
                                onInterceptData(DataSource.Type.CACHE, models)
                                liveData.setValue(models)
                            } else if (type === DataSource.CacheType.DATABASE) {
                                val models = listCacheFactory.queryCache(where())
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
                        listCacheFactory.removeOldCache(where())
                        listCacheFactory.addNewCache(data)
                        liveData.value = data
                    }

                    override fun onFailure(code: Int, msg: String?) {
                        if (isClearDataOnNetworkError) {
                            listCacheFactory.removeOldCache(where())
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

    protected fun onInterceptData(type: DataSource.Type, data: M) {}
    protected fun onInterceptData(type: DataSource.Type, data: List<M>) {}

    init {
        cacheFactory.init()
        listCacheFactory.init()
        cacheStrategy = DataSource.CacheStrategy.MEMORY_CACHE
    }
}