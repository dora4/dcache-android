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
import dora.db.builder.Condition
import dora.db.builder.WhereBuilder
import dora.http.DoraCallback
import dora.http.DoraListCallback

@RepositoryType(BaseRepository.CacheStrategy.DATABASE_CACHE)
abstract class BaseDatabaseCacheRepository<M> @JvmOverloads
    constructor(context: Context) : BaseRepository<M>(context) {

    protected val dataMap = HashMap<String, M>()
    protected val listDataMap = HashMap<String, MutableList<M>>()

    /**
     * 如果返回true，则放弃强制覆盖原有数据的模式，而采用map映射。
     */
    protected open fun disallowForceUpdate() : Boolean {
        return false
    }

    /**
     * 只要mapKey的值不冲突即可追加缓存。
     */
    protected open fun mapKey() : String {
        // 默认的mapKey
        return "BaseDatabaseCacheRepository.mapKey"
    }

    /**
     * 根据查询条件进行初步的过滤从数据库加载的数据，过滤不完全则再调用onInterceptData。
     *
     * @return
     */
    protected open fun where(): Condition {
        return WhereBuilder.create().toCondition()
    }

    /**
     * 保证成员属性不为空，而成功调用where方法。
     *
     * @see BaseDatabaseCacheRepository.where
     */
    protected open fun checkValuesNotNull() : Boolean { return true }

    /**
     * 手动放入缓存数据，仅listMode为true时使用。
     */
    fun addData(data: M) {
        addData(arrayListOf(data))
    }

    /**
     * 手动放入一堆缓存数据，仅listMode为true时使用。
     */
    fun addData(data: MutableList<M>) {
        getListLiveData().value?.let {
            it.addAll(data)
            listCacheHolder.addNewCache(data)
        }
    }

    override fun createDataFetcher(): DataFetcher<M> {
        return object : DataFetcher<M>() {
            override fun fetchData(listener: IDataFetcher.OnLoadListener?): LiveData<M?> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType): Boolean {
                        if (type === DataSource.CacheType.DATABASE) {
                            if (checkValuesNotNull()) {
                                val model = cacheHolder.queryCache(where())
                                model?.let {
                                    onInterceptData(DataSource.Type.CACHE, it)
                                    liveData.postValue(it)
                                    return true
                                }
                            }
                        }
                        liveData.postValue(null)
                        return false
                    }

                    override fun loadFromNetwork() {
                        onLoadFromNetwork(callback())
                    }
                })
                return liveData
            }

            override fun callback(listener: IDataFetcher.OnLoadListener?): DoraCallback<M> {
                return object : DoraCallback<M>() {
                    override fun onSuccess(model: M) {
                        model?.let {
                            if (isLogPrint) {
                                Log.d(TAG, it.toString())
                            }
                            onInterceptData(DataSource.Type.NETWORK, it)
                            if (!disallowForceUpdate()) {
                                if (checkValuesNotNull()) {
                                    cacheHolder.removeOldCache(where())
                                }
                            } else {
                                if (dataMap.containsKey(mapKey())) {
                                    if (checkValuesNotNull()) {
                                        cacheHolder.removeOldCache(where())
                                    }
                                }
                            }
                            cacheHolder.addNewCache(it)
                            if (disallowForceUpdate()) {
                                liveData.postValue(dataMap.get(mapKey()))
                            } else {
                                liveData.postValue(it)
                            }
                        }
                        listener?.onSuccess()
                    }

                    override fun onFailure(code: Int, msg: String?) {
                        if (isLogPrint) {
                            Log.d(TAG, "$code:$msg")
                        }
                        if (isClearDataOnNetworkError) {
                            if (checkValuesNotNull()) {
                                clearData()
                                cacheHolder.removeOldCache(where())
                            }
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
                        if (type === DataSource.CacheType.DATABASE) {
                            if (checkValuesNotNull()) {
                                val models = listCacheHolder.queryCache(where())
                                models?.let {
                                    onInterceptData(DataSource.Type.CACHE, it)
                                    liveData.postValue(it)
                                    return true
                                }
                            }
                        }
                        liveData.postValue(arrayListOf())
                        return false
                    }

                    override fun loadFromNetwork() {
                        onLoadFromNetwork(listCallback(listener))
                    }
                })
                return liveData
            }

            override fun listCallback(listener: IListDataFetcher.OnLoadListener?): DoraListCallback<M> {
                return object : DoraListCallback<M>() {
                    override fun onSuccess(models: MutableList<M>) {
                        models?.let {
                            if (isLogPrint) {
                                for (model in it) {
                                    Log.d(TAG, model.toString())
                                }
                            }
                            onInterceptData(DataSource.Type.NETWORK, it)
                            if (!disallowForceUpdate()) {
                                if (checkValuesNotNull()) {
                                    listCacheHolder.removeOldCache(where())
                                }
                            } else {
                                if (listDataMap.containsKey(mapKey())) {
                                    if (checkValuesNotNull()) {
                                        listCacheHolder.removeOldCache(where())
                                    }
                                }
                            }
                            listCacheHolder.addNewCache(it)
                            if (disallowForceUpdate()) {
                                liveData.postValue(listDataMap.get(mapKey()))
                            } else {
                                liveData.postValue(it)
                            }
                        }
                        listener?.onSuccess()
                    }

                    override fun onFailure(code: Int, msg: String?) {
                        if (isLogPrint) {
                            Log.d(TAG, "$code:$msg")
                        }
                        if (isClearDataOnNetworkError) {
                            if (checkValuesNotNull()) {
                                clearListData()
                                listCacheHolder.removeOldCache(where())
                            }
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