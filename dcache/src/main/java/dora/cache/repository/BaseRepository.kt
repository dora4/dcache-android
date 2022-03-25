package dora.cache.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import androidx.annotation.IntDef
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dora.cache.data.fetcher.IDataFetcher
import dora.cache.data.fetcher.IListDataFetcher
import dora.cache.data.page.IDataPager
import dora.cache.holder.CacheHolder
import dora.http.DoraCallback
import dora.http.DoraListCallback
import java.lang.RuntimeException
import java.lang.reflect.ParameterizedType

/**
 * 数据仓库，扩展它来支持数据的三级缓存，即从云端服务器的数据库、手机本地数据库和手机内存中读取需要的数据，以支持用户
 * 手机在断网情况下也能显示以前的数据。一个[BaseRepository]要么用于非集合数据，要么用于集合数据。如果要用于
 * 非集合数据，请配置[Repository]注解将[.isListMode]的值设置为false。
 */
abstract class BaseRepository<M>(val context: Context) : ViewModel(), IDataFetcher<M>, IListDataFetcher<M> {

    /**
     * 缓存策略。
     */
    protected var cacheStrategy = CacheStrategy.NO_CACHE

    /**
     * 非集合数据获取接口。
     */
    protected lateinit var dataFetcher: IDataFetcher<M>

    /**
     * 集合数据获取接口。
     */
    protected lateinit var listDataFetcher: IListDataFetcher<M>

    protected lateinit var cacheHolder: CacheHolder<M>

    protected lateinit var listCacheHolder: CacheHolder<List<M>>

    /**
     * true代表用于集合数据，false用于非集合数据。
     */
    protected var isListMode = true
        protected set

    protected var isLogPrint = false
        protected set

    /**
     * 是否在网络加载数据失败的时候清空数据，不建议为true。
     *
     * @return
     */
    protected val isClearDataOnNetworkError: Boolean
        protected get() = false

    protected abstract fun createDataFetcher(): IDataFetcher<M>

    protected abstract fun createListDataFetcher(): IListDataFetcher<M>

    protected abstract fun createCacheHolder(clazz: Class<M>): CacheHolder<M>

    protected abstract fun createListCacheHolder(clazz: Class<M>): CacheHolder<List<M>>

    override fun callback(listener: IDataFetcher.OnLoadListener?): DoraCallback<M> {
        return object : DoraCallback<M>() {
            override fun onSuccess(model: M) {
                if (isLogPrint) {
                    model.let {
                        Log.d(TAG, it.toString())
                    }
                }
                listener?.onSuccess()
            }

            override fun onFailure(code: Int, msg: String?) {
                if (isLogPrint) {
                    Log.d(TAG, "$code:$msg")
                }
                listener?.onFailure(code, msg)
            }
        }
    }

    override fun listCallback(listener: IListDataFetcher.OnLoadListener?): DoraListCallback<M> {
        return object : DoraListCallback<M>() {
            override fun onSuccess(models: List<M>) {
                if (isLogPrint) {
                    models.let {
                        for (model in it) {
                            Log.d(TAG, model.toString())
                        }
                    }
                }
                listener?.onSuccess()
            }

            override fun onFailure(code: Int, msg: String?) {
                if (isLogPrint) {
                    Log.d(TAG, "$code:$msg")
                }
                listener?.onFailure(code, msg)
            }
        }
    }

    /**
     * 非集合数据的API接口调用。
     *
     * @param callback
     */
    protected abstract fun onLoadFromNetwork(callback: DoraCallback<M>)

    /**
     * 集合数据的API接口调用。
     *
     * @param callback
     */
    protected abstract fun onLoadFromNetwork(callback: DoraListCallback<M>)

    /**
     * 从三级缓存仓库选择数据。
     *
     * @param ds 数据的来源
     * @return 数据是否获取成功
     */
    protected fun selectData(ds: DataSource): Boolean {
        if (cacheStrategy == CacheStrategy.NO_CACHE) {
            if (isNetworkAvailable) {
                return try {
                    ds.loadFromNetwork()
                    true
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                    false
                }
            }
        } else if (cacheStrategy == CacheStrategy.DATABASE_CACHE) {
            val isLoaded = ds.loadFromCache(DataSource.CacheType.DATABASE)
            return if (isNetworkAvailable) {
                try {
                    ds.loadFromNetwork()
                    true
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                    isLoaded
                }
            } else isLoaded
        } else if (cacheStrategy == CacheStrategy.MEMORY_CACHE) {
            val isLoaded = ds.loadFromCache(DataSource.CacheType.MEMORY)
            if (!isLoaded) {
                ds.loadFromCache(DataSource.CacheType.DATABASE)
            }
            return if (isNetworkAvailable) {
                try {
                    ds.loadFromNetwork()
                    true
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                    isLoaded
                }
            } else isLoaded
        } else if (cacheStrategy == CacheStrategy.DATABASE_CACHE_NO_NETWORK) {
            var isLoaded = false
            if (!isNetworkAvailable) {
                isLoaded = ds.loadFromCache(DataSource.CacheType.DATABASE)
            }
            return if (isNetworkAvailable) {
                try {
                    ds.loadFromNetwork()
                    true
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                    isLoaded
                }
            } else isLoaded
        }
        return false
    }

    @Target(AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.SOURCE)
    @IntDef(CacheStrategy.NO_CACHE, CacheStrategy.DATABASE_CACHE,
            CacheStrategy.MEMORY_CACHE, CacheStrategy.DATABASE_CACHE_NO_NETWORK)
    annotation class Strategy

    interface CacheStrategy {
        companion object {
            /**
             * 默认策略，不启用缓存。
             */
            const val NO_CACHE = 0

            /**
             * 数据库缓存，通常用于断网的情况，在打开界面前从数据库读取离线数据。
             */
            const val DATABASE_CACHE = 1

            /**
             * 内存缓存，通常用于需要在app冷启动的时候将数据库的数据先加载到内存，以后打开界面直接从内存中去拿数据。
             */
            const val MEMORY_CACHE = 2

            /**
             * 和[.DATABASE_CACHE]的不同之处在于，只有在没网的情况下才会加载数据库的缓存数据。
             */
            @Deprecated("")
            const val DATABASE_CACHE_NO_NETWORK = 3
        }
    }

    /**
     * 数据的来源。
     */
    interface DataSource {

        enum class Type {
            /**
             * 数据来源于网络服务器。
             */
            NETWORK,

            /**
             * 数据来源于缓存。
             */
            CACHE
        }

        enum class CacheType {
            DATABASE, MEMORY
        }

        /**
         * 从缓存中加载数据。
         *
         * @param type
         * @return
         */
        fun loadFromCache(type: CacheType): Boolean

        /**
         * 从服务器/网络加载数据。
         */
        fun loadFromNetwork()
    }

    override fun fetchData(listener: IDataFetcher.OnLoadListener?): LiveData<M?> {
        return dataFetcher.fetchData(listener)
    }

    override fun fetchListData(listener: IListDataFetcher.OnLoadListener?): LiveData<List<M>> {
        return listDataFetcher.fetchListData(listener)
    }

    override fun clearData() {
        dataFetcher.clearData()
    }

    override fun clearListData() {
        listDataFetcher.clearListData()
    }

    override fun getLiveData(): LiveData<M?> {
        return dataFetcher.getLiveData()
    }

    override fun getListLiveData(): LiveData<List<M>> {
        return listDataFetcher.getListLiveData()
    }

    override fun obtainPager(): IDataPager<M> {
        return listDataFetcher.obtainPager()
    }

    /**
     * 检测网络是否可用。
     *
     * @return
     */
    protected val isNetworkAvailable: Boolean
        protected get() = checkNetwork(context)

    protected fun checkNetwork(context: Context): Boolean {
        val networkInfo = getActiveNetworkInfo(context)
        return networkInfo != null && networkInfo.isConnected
    }

    private fun getActiveNetworkInfo(context: Context): NetworkInfo? {
        val connectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo
    }

    private fun getGenericType(obj: Any): Class<*>? {
        return if (obj.javaClass.genericSuperclass is ParameterizedType &&
                (obj.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments.isNotEmpty()) {
            (obj.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<*>
        } else (obj.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<*>
    }

    companion object {
        internal const val TAG = "dcache"
    }

    init {
        val repositoryType = javaClass.superclass.getAnnotation(RepositoryType::class.java)
        if (repositoryType != null) {
            cacheStrategy = repositoryType.cacheStrategy
        } else {
            throw RuntimeException("RepositoryType is not defined")
        }
        val repository = javaClass.getAnnotation(Repository::class.java)
        if (repository != null) {
            isListMode = repository.isListMode
            isLogPrint = repository.isLogPrint
        }
        val MClass: Class<M> = getGenericType(this) as Class<M>
        Log.d(TAG, "MClass:$MClass,isListMode:$isListMode")
        if (isListMode) {
            listCacheHolder = createListCacheHolder(MClass)
            listCacheHolder.init()
            listDataFetcher = createListDataFetcher()
        } else {
            cacheHolder = createCacheHolder(MClass)
            cacheHolder.init()
            dataFetcher = createDataFetcher()
        }
    }

    protected open fun onInterceptData(type: DataSource.Type, model: M) {}

    protected open fun onInterceptData(type: DataSource.Type, models: List<M>) {}
}