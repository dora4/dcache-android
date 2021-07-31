package dora.cache.repository

import android.content.Context
import androidx.lifecycle.LiveData
import dora.cache.data.fetcher.DataFetcher
import dora.cache.data.fetcher.ListDataFetcher
import dora.cache.data.page.DataPager
import dora.cache.data.page.IDataPager
import dora.db.builder.Condition
import dora.db.builder.WhereBuilder
import dora.http.DoraCallback
import dora.http.DoraListCallback

@RepositoryType(BaseRepository.CacheStrategy.DATABASE_CACHE)
abstract class BaseDatabaseCacheRepository<M>(context: Context) : BaseRepository<M>(context) {

    /**
     * 根据查询条件进行初步的过滤从数据库加载的数据，过滤不完全则再调用onInterceptData。
     *
     * @return
     */
    protected fun where(): Condition {
        return WhereBuilder.create().toCondition()
    }

    override fun createDataFetcher(): DataFetcher<M> {
        return object : DataFetcher<M>() {
            override fun fetchData(): LiveData<M> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType): Boolean {
                        if (type === DataSource.CacheType.DATABASE) {
                            val entity = cacheHolder.queryCache(where())
                            if (entity != null) {
                                onInterceptData(DataSource.Type.CACHE, entity)
                                liveData.value = entity
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
                    override fun onSuccess(model: M) {
                        onInterceptNetworkData(model)
                        cacheHolder.removeOldCache(where())
                        cacheHolder.addNewCache(model)
                        liveData.value = model
                    }

                    override fun onFailure(code: Int, msg: String?) {
                        if (isClearDataOnNetworkError) {
                            liveData.value = null
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

    override fun createListDataFetcher(): ListDataFetcher<M> {
        return object : ListDataFetcher<M>() {

            override fun fetchListData(): LiveData<List<M>> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType): Boolean {
                        if (type === DataSource.CacheType.DATABASE) {
                            val entities = listCacheHolder.queryCache(where())
                            if (entities != null && entities.isNotEmpty()) {
                                onInterceptData(DataSource.Type.CACHE, entities)
                                liveData.value = entities
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
                    override fun onSuccess(models: List<M>) {
                        onInterceptNetworkData(models)
                        listCacheHolder.removeOldCache(where())
                        listCacheHolder.addNewCache(models)
                        liveData.value = models
                    }

                    override fun onFailure(code: Int, msg: String?) {
                        if (isClearDataOnNetworkError) {
                            liveData.value = null
                            listCacheHolder.removeOldCache(where())
                        }
                    }

                    override fun onInterceptNetworkData(data: List<M>) {
                        onInterceptData(DataSource.Type.NETWORK, data)
                    }
                }
            }

            override fun obtainPager(): IDataPager<M> {
                return DataPager(liveData.value ?: arrayListOf())
            }
        }
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