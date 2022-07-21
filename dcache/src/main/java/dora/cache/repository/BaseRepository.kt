package dora.cache.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dora.cache.data.fetcher.IDataFetcher
import dora.cache.data.fetcher.IListDataFetcher
import dora.cache.data.page.IDataPager
import dora.cache.holder.CacheHolder
import dora.http.DoraCallback
import dora.http.DoraListCallback
import io.reactivex.Observable
import java.lang.reflect.ParameterizedType

/**
 * 数据仓库，扩展它来支持数据的三级缓存，即从云端服务器的数据库、手机本地数据库和手机内存中读取需要的数据，以支持用户
 * 手机在断网情况下也能显示以前的数据。一个[BaseRepository]要么用于非集合数据，要么用于集合数据。如果要用于
 * 非集合数据，请配置[Repository]注解将[.isListMode]的值设置为false。
 */
abstract class BaseRepository<M>(val context: Context) : ViewModel(), IDataFetcher<M>, IListDataFetcher<M> {

    /**
     * 非集合数据获取接口。
     */
    protected lateinit var dataFetcher: IDataFetcher<M>

    /**
     * 集合数据获取接口。
     */
    protected lateinit var listDataFetcher: IListDataFetcher<M>

    protected lateinit var cacheHolder: CacheHolder<M>

    protected lateinit var listCacheHolder: CacheHolder<MutableList<M>>

    /**
     * true代表用于集合数据，false用于非集合数据。
     */
    protected var isListMode = true
        protected set

    protected var isLogPrint = false
        protected set

    /**
     * 是否在网络加载数据失败的时候清空数据。
     *
     * @return
     */
    protected val isClearDataOnNetworkError: Boolean
        protected get() = false

    protected abstract fun createDataFetcher(): IDataFetcher<M>

    protected abstract fun createListDataFetcher(): IListDataFetcher<M>

    protected abstract fun createCacheHolder(clazz: Class<M>): CacheHolder<M>

    protected abstract fun createListCacheHolder(clazz: Class<M>): CacheHolder<MutableList<M>>

    /**
     * 手动放入缓存数据，仅listMode为true时使用，注意只会追加到缓存里面去，请调用接口将新数据也更新到服务端，以致
     * 于下次请求api接口时也会有这部分数据。
     */
    fun addData(data: M) {
        if (isListMode) {
            addData(arrayListOf(data))
        }
    }

    /**
     * 手动放入一堆缓存数据，仅listMode为true时使用，注意只会追加到缓存里面去，请调用接口将新数据也更新到服务端，
     * 以致于下次请求api接口时也会有这部分数据。
     */
    fun addData(data: MutableList<M>) {
        if (isListMode) {
            getListLiveData().value?.let {
                it.addAll(data)
                listCacheHolder.addNewCache(data)
            }
        }
    }

    /**
     * 保证成员属性不为空，而成功调用where方法。
     *
     * @see BaseDatabaseCacheRepository.where
     */
    protected open fun checkValuesNotNull() : Boolean { return true }

    @JvmOverloads
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

            override fun onFailure(msg: String) {
                if (isLogPrint) {
                    Log.d(TAG, msg)
                }
                listener?.onFailure(msg)
            }
        }
    }

    @JvmOverloads
    override fun listCallback(listener: IListDataFetcher.OnLoadListener?): DoraListCallback<M> {
        return object : DoraListCallback<M>() {
            override fun onSuccess(models: MutableList<M>) {
                if (isLogPrint) {
                    models.let {
                        for (model in it) {
                            Log.d(TAG, model.toString())
                        }
                    }
                }
                listener?.onSuccess()
            }

            override fun onFailure(msg: String) {
                if (isLogPrint) {
                    Log.d(TAG, msg)
                }
                listener?.onFailure(msg)
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

    protected abstract fun onLoadFromNetworkObservable() : Observable<M>

    protected abstract fun onLoadFromNetworkObservableList() : Observable<MutableList<M>>

    /**
     * 从三级缓存仓库选择数据。
     *
     * @param ds 数据的来源
     * @return 数据是否获取成功
     */
    protected abstract fun selectData(ds: DataSource): Boolean

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
            DATABASE, CUSTOM
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

    @JvmOverloads
    override fun fetchData(listener: IDataFetcher.OnLoadListener?): LiveData<M?> {
        return dataFetcher.fetchData(listener)
    }

    @JvmOverloads
    override fun fetchListData(listener: IListDataFetcher.OnLoadListener?): LiveData<MutableList<M>> {
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

    override fun getListLiveData(): LiveData<MutableList<M>> {
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
        const val TAG = "dcache"
    }

    init {
        val repository = javaClass.getAnnotation(Repository::class.java)
                ?: throw RuntimeException("@Repository is required.")
        isListMode = repository.isListMode
        isLogPrint = repository.isLogPrint
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

    protected open fun onInterceptData(type: DataSource.Type, models: MutableList<M>) {}
}