package dora.cache.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import androidx.lifecycle.LiveData
import dora.cache.data.IDataFetcher
import dora.cache.data.IListDataFetcher
import dora.cache.data.page.IDataPager
import dora.db.OrmTable
import dora.http.DoraCallback
import dora.http.DoraListCallback

/**
 * 数据仓库，扩展它来支持数据的三级缓存，即从云端服务器的数据库、手机本地数据库和手机内存中读取需要的数据，以支持用户
 * 手机在断网情况下也能显示以前的数据。一个[BaseRepository]要么用于非集合数据，要么用于集合数据。如果要用于
 * 非集合数据，请配置[Repository]注解将[.listData]的值设置为false。
 */
abstract class BaseRepository<T : OrmTable> protected constructor(var context: Context) : IDataFetcher<T>, IListDataFetcher<T> {
    /**
     * 缓存策略。
     */
    @JvmField
    protected var cacheStrategy = DataSource.CacheStrategy.NO_CACHE

    /**
     * 非集合数据获取接口。
     */
    lateinit var dataFetcher: IDataFetcher<T>

    /**
     * 集合数据获取接口。
     */
    lateinit var listDataFetcher: IListDataFetcher<T>

    /**
     * true代表用于集合数据，false用于非集合数据。
     */
    var isListData = true
        protected set

    /**
     * 是否在网络加载数据失败的时候清空数据。
     *
     * @return
     */
    protected val isClearDataOnNetworkError: Boolean
        protected get() = false

    /**
     * 安装默认的非集合数据抓取。
     *
     * @return
     */
    protected abstract fun installDataFetcher(): IDataFetcher<T>

    /**
     * 安装默认的集合数据抓取。
     *
     * @return
     */
    protected abstract fun installListDataFetcher(): IListDataFetcher<T>
    override fun callback(): DoraCallback<T> {
        return object : DoraCallback<T>() {
            override fun onSuccess(data: T) {
                Log.d(TAG, data.toString())
            }

            override fun onFailure(code: Int, msg: String?) {
                Log.d(TAG, "$code:$msg")
            }
        }
    }

    override fun listCallback(): DoraListCallback<T> {
        return object : DoraListCallback<T>() {
            override fun onSuccess(data: List<T>) {
                for (t in data) {
                    Log.d(TAG, t.toString())
                }
            }

            override fun onFailure(code: Int, msg: String?) {
                Log.d(TAG, "$code:$msg")
            }
        }
    }

    /**
     * 非集合数据的API接口调用。
     *
     * @param callback
     */
    protected fun onLoadFromNetwork(callback: DoraCallback<T>) {}

    /**
     * 集合数据的API接口调用。
     *
     * @param callback
     */
    protected fun onLoadFromNetwork(callback: DoraListCallback<T>) {}

    /**
     * 从三级缓存仓库选择数据。
     *
     * @param ds 数据的来源
     * @return 数据是否获取成功
     */
    protected fun selectData(ds: DataSource): Boolean {
        if (cacheStrategy == DataSource.CacheStrategy.NO_CACHE) {
            if (isNetworkAvailable) {
                return try {
                    ds.loadFromNetwork()
                    true
                } catch (e: Exception) {
                    Log.e(TAG, e.message!!)
                    false
                }
            }
        } else if (cacheStrategy == DataSource.CacheStrategy.DATABASE_CACHE) {
            val isLoaded = ds.loadFromCache(DataSource.CacheType.DATABASE)
            return if (isNetworkAvailable) {
                try {
                    ds.loadFromNetwork()
                    true
                } catch (e: Exception) {
                    Log.e(TAG, e.message!!)
                    false
                }
            } else isLoaded
        } else if (cacheStrategy == DataSource.CacheStrategy.MEMORY_CACHE) {
            val isLoaded = ds.loadFromCache(DataSource.CacheType.MEMORY)
            if (!isLoaded) {
                ds.loadFromCache(DataSource.CacheType.DATABASE)
            }
            return if (isNetworkAvailable) {
                try {
                    ds.loadFromNetwork()
                    true
                } catch (e: Exception) {
                    Log.e(TAG, e.message!!)
                    false
                }
            } else isLoaded
        } else if (cacheStrategy == DataSource.CacheStrategy.DATABASE_CACHE_NO_NETWORK) {
            var isLoaded = false
            if (!isNetworkAvailable) {
                isLoaded = ds.loadFromCache(DataSource.CacheType.DATABASE)
            }
            return if (isNetworkAvailable) {
                try {
                    ds.loadFromNetwork()
                    true
                } catch (e: Exception) {
                    Log.e(TAG, e.message!!)
                    false
                }
            } else isLoaded
        }
        return false
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
                const val DATABASE_CACHE_NO_NETWORK = 3
            }
        }

        /**
         * 从缓存中加载数据。
         *
         * @param type
         * @return
         */
        fun loadFromCache(type: CacheType?): Boolean

        /**
         * 从服务器/网络加载数据。
         */
        fun loadFromNetwork()
    }

    override fun fetchData(): LiveData<T> {
        return dataFetcher.fetchData()
    }

    override fun fetchListData(): LiveData<List<T>> {
        return listDataFetcher.fetchListData()
    }

    override fun obtainPager(): IDataPager<T>? {
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

    companion object {
        protected const val TAG = "BaseRepository"
    }

    init {
        val repository = javaClass.getAnnotation(Repository::class.java)
        if (repository != null) {
            cacheStrategy = repository.cacheStrategy
            isListData = repository.isListData
        }
        if (isListData) {
            listDataFetcher = installListDataFetcher()
        } else {
            dataFetcher = installDataFetcher()
        }
    }
}