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

abstract class BaseDatabaseCacheNoNetworkRepository<T : OrmTable>(context: Context, clazz: Class<T>) : BaseRepository<T>(context) {
    val dao: OrmDao<T>

    /**
     * 根据查询条件进行初步的过滤从数据库加载的数据，过滤不完全则再调用onInterceptData。
     *
     * @return
     */
    protected fun where(): WhereBuilder {
        return WhereBuilder.create()
    }

    override fun installDataFetcher(): DataFetcher<T> {
        return object : DataFetcher<T>() {
            override fun fetchData(): LiveData<T> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType?): Boolean {
                        if (type === DataSource.CacheType.DATABASE) {
                            val entity = dao.selectOne(where())
                            if (entity != null) {
                                onInterceptData(DataSource.Type.CACHE, entity)
                                liveData.setValue(entity)
                            }
                            return true
                        }
                        liveData.setValue(null)
                        return false
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
                        dao.delete(where())
                        dao.insert(data)
                        liveData.setValue(data)
                    }

                    override fun onFailure(code: Int, msg: String?) {
                        if (isClearDataOnNetworkError) {
                            liveData.setValue(null)
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

    override fun installListDataFetcher(): ListDataFetcher<T> {
        return object : ListDataFetcher<T>() {

            override fun fetchListData(): LiveData<List<T>> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType?): Boolean {
                        if (type === DataSource.CacheType.DATABASE) {
                            val entities = dao.select(where())
                            if (entities != null && entities.size > 0) {
                                onInterceptData(DataSource.Type.CACHE, entities)
                                liveData.setValue(entities)
                            }
                            return true
                        }
                        liveData.setValue(null)
                        return false
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
                        dao.delete(where())
                        dao.insert(data)
                        liveData.setValue(data)
                    }

                    override fun onFailure(code: Int, msg: String?) {
                        if (isClearDataOnNetworkError) {
                            liveData.setValue(null)
                            dao.delete(where())
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
        cacheStrategy = DataSource.CacheStrategy.DATABASE_CACHE_NO_NETWORK
    }
}