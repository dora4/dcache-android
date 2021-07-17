package dora.cache.repository

import android.content.Context
import androidx.lifecycle.LiveData
import dora.cache.data.DataFetcher
import dora.cache.data.ListDataFetcher
import dora.cache.data.page.IDataPager
import dora.db.builder.Condition
import dora.db.builder.WhereBuilder
import dora.http.DoraCallback
import dora.http.DoraListCallback

abstract class BaseDatabaseCacheRepository<M>(context: Context, var clazz: Class<M>) : BaseRepository<M>(context) {

    abstract val isNoNetworkMode: Boolean

    /**
     * 根据查询条件进行初步的过滤从数据库加载的数据，过滤不完全则再调用onInterceptData。
     *
     * @return
     */
    protected fun where(): Condition {
        return WhereBuilder.create().toCondition()
    }

    override fun installDataFetcher(): DataFetcher<M> {
        return object : DataFetcher<M>() {
            override fun fetchData(): LiveData<M> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType?): Boolean {
                        if (type === DataSource.CacheType.DATABASE) {
                            val entity = cacheFactory.queryCache(where())
                            if (entity != null) {
                                onInterceptData(DataSource.Type.CACHE, entity)
                                liveData.setValue(entity)
                            }
                            return true
                        }
                        liveData.value = null
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
                    override fun onSuccess(data: M) {
                        onInterceptNetworkData(data)
                        cacheFactory.removeOldCache(where())
                        cacheFactory.addNewCache(data)
                        liveData.setValue(data)
                    }

                    override fun onFailure(code: Int, msg: String?) {
                        if (isClearDataOnNetworkError) {
                            liveData.value = null
                            cacheFactory.removeOldCache(where())
                        }
                    }

                    override fun onInterceptNetworkData(data: M) {
                        onInterceptData(DataSource.Type.NETWORK, data)
                    }
                }
            }

            override fun obtainPager(): IDataPager<M>? {
                return null
            }
        }
    }

    override fun installListDataFetcher(): ListDataFetcher<M> {
        return object : ListDataFetcher<M>() {

            override fun fetchListData(): LiveData<List<M>> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType?): Boolean {
                        if (type === DataSource.CacheType.DATABASE) {
                            val entities = listCacheFactory.queryCache(where())
                            if (entities != null && entities.isNotEmpty()) {
                                onInterceptData(DataSource.Type.CACHE, entities)
                                liveData.setValue(entities)
                            }
                            return true
                        }
                        liveData.value = null
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
                    override fun onSuccess(data: List<M>) {
                        onInterceptNetworkData(data)
                        listCacheFactory.removeOldCache(where())
                        listCacheFactory.addNewCache(data)
                        liveData.setValue(data)
                    }

                    override fun onFailure(code: Int, msg: String?) {
                        if (isClearDataOnNetworkError) {
                            liveData.value = null
                            listCacheFactory.removeOldCache(where())
                        }
                    }

                    override fun onInterceptNetworkData(data: List<M>) {
                        onInterceptData(DataSource.Type.NETWORK, data)
                    }
                }
            }

            override fun obtainPager(): IDataPager<M>? {
                return null
            }
        }
    }

    protected fun onInterceptData(type: DataSource.Type, data: M) {}
    protected fun onInterceptData(type: DataSource.Type, data: List<M>) {}

    init {
        cacheFactory.init()
        listCacheFactory.init()
        if (isNoNetworkMode) {
            cacheStrategy = DataSource.CacheStrategy.DATABASE_CACHE_NO_NETWORK
        } else{
            cacheStrategy = DataSource.CacheStrategy.DATABASE_CACHE
        }
    }
 }