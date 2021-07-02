package dora.cache.repository

import android.content.Context
import androidx.lifecycle.LiveData
import dora.cache.data.DataFetcher
import dora.cache.data.ListDataFetcher
import dora.cache.data.page.IDataPager
import dora.db.OrmTable
import dora.db.builder.WhereBuilder
import dora.db.dao.DaoFactory
import dora.db.dao.OrmDao
import dora.http.DoraCallback
import dora.http.DoraListCallback
import dora.util.KeyValueUtils

abstract class BaseMemoryCacheRepository<T : OrmTable>(context: Context, clazz: Class<T>) : BaseRepository<T>(context) {
    val dao: OrmDao<T>

    /**
     * 根据查询条件进行初步的过滤从数据库加载的数据，过滤不完全则再调用onInterceptData。
     *
     * @return
     */
    protected fun where(): WhereBuilder {
        return WhereBuilder.create()
    }

    /**
     * 在冷启动时调用，从数据库将数据加载到内存。
     */
    abstract fun loadData(): Any?
    override fun installDataFetcher(): DataFetcher<T> {
        return object : DataFetcher<T>() {
            override fun fetchData(): LiveData<T> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType?): Boolean {
                        try {
                            if (type === DataSource.CacheType.MEMORY) {
                                val model = KeyValueUtils.getCacheFromMemory(cacheName) as T
                                onInterceptData(DataSource.Type.CACHE, model)
                                liveData.setValue(model)
                            } else if (type === DataSource.CacheType.DATABASE) {
                                val model = dao.selectOne(where())
                                if (model != null) {
                                    onInterceptData(DataSource.Type.CACHE, model)
                                    liveData.value = model
                                    KeyValueUtils.updateCacheAtMemory(cacheName, model)
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

            override fun callback(): DoraCallback<T> {
                return object : DoraCallback<T>() {
                    override fun onSuccess(data: T) {
                        onInterceptNetworkData(data)
                        KeyValueUtils.updateCacheAtMemory(cacheName, data)
                        dao.delete(where())
                        dao.insert(data)
                        liveData.value = data
                    }

                    override fun onFailure(code: Int, msg: String?) {
                        if (isClearDataOnNetworkError) {
                            liveData.value = null
                            KeyValueUtils.removeCacheAtMemory(cacheName)
                            dao.delete(where())
                        }
                    }

                    override fun onInterceptNetworkData(data: T) {
                        onInterceptData(DataSource.Type.NETWORK, data)
                    }
                }
            }

            override fun obtainPager(): IDataPager<T>? {
                return null
            }
        }
    }

    abstract val cacheName: String
    override fun installListDataFetcher(): ListDataFetcher<T> {
        return object : ListDataFetcher<T>() {

            override fun fetchListData(): LiveData<List<T>> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType?): Boolean {
                        try {
                            if (type === DataSource.CacheType.MEMORY) {
                                val models = KeyValueUtils.getCacheFromMemory(cacheName) as List<T>
                                onInterceptData(DataSource.Type.CACHE, models)
                                liveData.setValue(models)
                            } else if (type === DataSource.CacheType.DATABASE) {
                                val models = dao.select(where())
                                onInterceptData(DataSource.Type.CACHE, models)
                                liveData.value = models
                                KeyValueUtils.updateCacheAtMemory(cacheName, models)
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

            override fun listCallback(): DoraListCallback<T> {
                return object : DoraListCallback<T>() {
                    override fun onSuccess(data: List<T>) {
                        onInterceptNetworkData(data)
                        KeyValueUtils.updateCacheAtMemory(cacheName, data)
                        dao.delete(where())
                        dao.insert(data)
                        liveData.value = data
                    }

                    override fun onFailure(code: Int, msg: String?) {
                        if (isClearDataOnNetworkError) {
                            dao.delete(where())
                            liveData.value = null
                            KeyValueUtils.removeCacheAtMemory(cacheName)
                        }
                    }

                    override fun onInterceptNetworkData(data: List<T>) {
                        onInterceptData(DataSource.Type.NETWORK, data)
                    }
                }
            }


            override fun obtainPager(): IDataPager<T>? {
                return null
            }
        }
    }

    protected fun onInterceptData(type: DataSource.Type, data: T) {}
    protected fun onInterceptData(type: DataSource.Type, data: List<T>) {}

    init {
        dao = DaoFactory.getDao(clazz)
        cacheStrategy = DataSource.CacheStrategy.MEMORY_CACHE
    }
}