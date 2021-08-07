package dora.cache.repository

import android.content.Context
import android.util.Log
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
            override fun fetchData(): LiveData<M?> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType): Boolean {
                        if (type === DataSource.CacheType.DATABASE) {
                            val model = cacheHolder.queryCache(where())
                            model?.let {
                                onInterceptData(DataSource.Type.CACHE, it)
                                liveData.value = it
                                return true
                            }
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
                        model.let {
                            if (isLogPrint) {
                                Log.d(TAG, it.toString())
                            }
                            onInterceptNetworkData(it)
                            cacheHolder.removeOldCache(where())
                            cacheHolder.addNewCache(it)
                            liveData.value = it
                        }
                    }

                    override fun onFailure(code: Int, msg: String?) {
                        if (isLogPrint) {
                            Log.d(TAG, "$code:$msg")
                        }
                        if (isClearDataOnNetworkError) {
                            liveData.value = null
                            cacheHolder.removeOldCache(where())
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
                        if (type === DataSource.CacheType.DATABASE) {
                            val models = listCacheHolder.queryCache(where())
                            models?.let {
                                onInterceptData(DataSource.Type.CACHE, it)
                                liveData.value = it
                                return true
                            }
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
                        models.let {
                            if (isLogPrint) {
                                for (model in it) {
                                    Log.d(TAG, model.toString())
                                }
                            }
                            onInterceptNetworkData(it)
                            listCacheHolder.removeOldCache(where())
                            listCacheHolder.addNewCache(it)
                            liveData.value = it
                        }
                    }

                    override fun onFailure(code: Int, msg: String?) {
                        if (isLogPrint) {
                            Log.d(TAG, "$code:$msg")
                        }
                        if (isClearDataOnNetworkError) {
                            liveData.value = null
                            listCacheHolder.removeOldCache(where())
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