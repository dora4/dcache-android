package dora.cache.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dora.cache.data.fetcher.IDataFetcher
import dora.cache.data.fetcher.IListDataFetcher
import dora.cache.data.fetcher.OnLoadStateListener
import dora.cache.data.fetcher.OnLoadStateListenerImpl
import dora.cache.data.page.IDataPager
import dora.cache.holder.CacheHolder
import dora.cache.factory.CacheHolderFactory
import dora.http.DoraCallback
import dora.http.DoraListCallback
import io.reactivex.Observable
import java.lang.reflect.ParameterizedType

/**
 * Data repository, base class for cache and loading process handling. A [BaseRepository] is
 * designated for either non-collection data or collection data. If it is to be used for
 * non-collection data, please configure the [Repository] annotation in the implementation class;
 * if it is for collection data, please configure the [ListRepository] annotation in the
 * implementation class. One of these annotations must be configured.
 * 简体中文：数据仓库，缓存和加载流程处理基类。一个[BaseRepository]要么用于非集合数据，要么用于集合数据。如果要用于
 * 非集合数据，请在实现类配置[Repository]注解，如果为集合数据，请在实现类配置[ListRepository]注解。必须配置其中
 * 一个注解。
 */
abstract class BaseRepository<M, F : CacheHolderFactory<M>>(val context: Context) : ViewModel(), IDataFetcher<M>, IListDataFetcher<M> {

    /**
     * Non-collection data retrieval interface.
     * 简体中文：非集合数据获取接口。
     */
    protected lateinit var dataFetcher: IDataFetcher<M>

    /**
     * Collection data retrieval interface.
     * 简体中文：集合数据获取接口。
     */
    protected lateinit var listDataFetcher: IListDataFetcher<M>

    /**
     * Abstract factory.
     * 简体中文：抽象工厂。
     */
    protected lateinit var cacheHolderFactory: F

    /**
     * Non-collection data cache interface.
     * 简体中文：非集合数据缓存接口。
     */
    protected lateinit var cacheHolder: CacheHolder<M>

    /**
     * Collection data cache interface.
     * 简体中文：集合数据缓存接口。
     */
    protected lateinit var listCacheHolder: CacheHolder<MutableList<M>>

    /**
     * True represents collection data, while false represents non-collection data.
     * 简体中文：true代表用于集合数据，false用于非集合数据。
     */
    protected var isListMode = true
        protected set

    protected var isLogPrint = false
        protected set

    protected var description: String? = javaClass.simpleName
    protected var listener: OnLoadStateListener? = OnLoadStateListenerImpl()

    /**
     * Should the data be cleared when the network fails to load?
     * 简体中文：是否在网络加载数据失败的时候清空数据。
     *
     * @return
     */
    protected val isClearDataOnNetworkError: Boolean
        protected get() = false
    protected val MClass: Class<M>
    protected abstract fun createCacheHolderFactory() : F

    protected abstract fun createDataFetcher(): IDataFetcher<M>

    protected abstract fun createListDataFetcher(): IListDataFetcher<M>

    override fun callback(): DoraCallback<M> {
        return object : DoraCallback<M>() {
            override fun onSuccess(model: M) {
                if (isLogPrint) {
                    model.let {
                        Log.d(TAG, "【$description】${model.toString()}")
                    }
                }
            }

            override fun onFailure(msg: String) {
                if (isLogPrint) {
                    Log.d(TAG, "【$description】$msg")
                }
            }
        }
    }

    override fun listCallback(): DoraListCallback<M> {
        return object : DoraListCallback<M>() {
            override fun onSuccess(models: MutableList<M>) {
                if (isLogPrint) {
                    models.let {
                        for (model in it) {
                            Log.d(TAG, "【$description】${model.toString()}")
                        }
                    }
                }
            }

            override fun onFailure(msg: String) {
                if (isLogPrint) {
                    Log.d(TAG, "【$description】$msg")
                }
            }
        }
    }

    /**
     * API interface calls for non-collection data use Retrofit interface methods that return the
     * retrofit.Call type.
     * 简体中文：非集合数据的API接口调用，Retrofit接口的方法返回retrofit.Call类型使用。
     *
     * @param callback Data callback. 简体中文：数据回调
     * @param listener If you handle network request exceptions on your own, don’t forget to
     * callback the failure status so that you can receive the status callback when fetching data.
     * 简体中文：如果你自行处理掉网络请求的异常，不要忘了回调失败的状态，以便于在fetchData的时候能收到状态的回调
     */
    protected abstract fun onLoadFromNetwork(callback: DoraCallback<M>, listener: OnLoadStateListener? = null)

    /**
     * API interface calls for collection data use Retrofit interface methods that return the
     * retrofit.Call type.
     * 简体中文：集合数据的API接口调用，Retrofit接口的方法返回retrofit.Call类型使用。
     *
     * @param callback Data callback. 简体中文：数据回调
     * @param listener If you handle network request exceptions on your own, don’t forget to
     * callback the failure status so that you can receive the status callback when fetching list
     * data.简体中文：如果你自行处理掉网络请求的异常，不要忘了回调失败的状态，以便于在fetchListData的时候能收到状态的回调
     */
    protected abstract fun onLoadFromNetwork(callback: DoraListCallback<M>, listener: OnLoadStateListener? = null)

    /**
     * API interface calls for non-collection data use Retrofit interface methods that return the
     * io.reactivex.Observable type.
     * 简体中文：非集合数据的API接口调用，Retrofit接口的方法返回io.reactivex.Observable类型使用。
     *
     * @param listener
     * If you handle network request exceptions on your own, don’t forget to callback the failure
     * status so that you can receive the status callback when fetching list data. 简体中文：如果你自
     * 行处理掉网络请求的异常，不要忘了回调失败的状态，以便于在fetchData的时候能收到状态的回调
     */
    protected abstract fun onLoadFromNetworkObservable(listener: OnLoadStateListener? = null) : Observable<M>

    /**
     * API interface calls for collection data use Retrofit interface methods that return the
     * io.reactivex.Observable type.
     * 简体中文：集合数据的API接口调用，Retrofit接口的方法返回io.reactivex.Observable类型使用。
     *
     * @param listener If you handle network request exceptions on your own, don’t forget to
     * callback the failure status so that you can receive the status callback when fetching list
     * data.简体中文：如果你自行处理掉网络请求的异常，不要忘了回调失败的状态，以便于在fetchListData的时候能收到状态
     * 的回调
     */
    protected abstract fun onLoadFromNetworkObservableList(listener: OnLoadStateListener? = null) : Observable<MutableList<M>>

    /**
     * Select data from the repository and handle the priority of data sources.
     * 简体中文：从仓库选择数据，处理数据来源的优先级。
     *
     * @param ds Source of the data. 简体中文：数据的来源
     * @return Was the data retrieved successfully? 简体中文：数据是否获取成功
     */
    protected abstract fun selectData(ds: DataSource): Boolean

    /**
     * Source of the data.
     * 简体中文：数据的来源。
     */
    interface DataSource {

        enum class Type {

            /**
             * Data comes from the network server.
             * 简体中文：数据来源于网络服务器。
             */
            NETWORK,

            /**
             * Data comes from the cache.
             * 简体中文：数据来源于缓存。
             */
            CACHE
        }

        enum class CacheType {

            /**
             * Built-in SQLite database.
             * 简体中文：内置SQLite数据库。
             */
            DATABASE,

            /**
             * MMKV cache.
             * 简体中文：MMKV缓存。
             */
            MMKV
        }

        /**
         * Load data from the cache.
         * 简体中文：从缓存中加载数据。
         *
         * @param type
         * @return
         */
        fun loadFromCache(type: CacheType): Boolean

        /**
         * Load data from the server/network.
         * 简体中文：从服务器/网络加载数据。
         */
        fun loadFromNetwork()
    }

    /**
     * Fetch non-collection data and return it to LiveData for display in the UI. Once fetched
     * successfully, it will remain in LiveData and can be accessed through [.getLiveData()].
     * 简体中文：抓取非集合数据，返回给livedata，以便于展示在UI上。抓取成功后会一直在livedata中，可以通过
     * [.getLiveData()]拿到。
     */
    override fun fetchData(description: String?, listener: OnLoadStateListener?): LiveData<M?> {
        if (description != null) {
            // 简体中文：不能让null覆盖了默认类名
            this.description = description
        }
        this.listener = listener
        return dataFetcher.fetchData(description)
    }

    /**
     * Fetch collection data and return it to LiveData for display in the UI. Once fetched
     * successfully, it will remain in LiveData and can be accessed through [.getListLiveData()].
     * 简体中文：抓取集合数据，返回给livedata，以便于展示在UI上。抓取成功后会一直在livedata中，可以通过
     * [.getListLiveData()]拿到。
     */
    override fun fetchListData(description: String?, listener: OnLoadStateListener?): LiveData<MutableList<M>> {
        if (description != null) {
            // 简体中文：不能让null覆盖了默认类名
            this.description = description
        }
        this.listener = listener
        return listDataFetcher.fetchListData(description)
    }

    /**
     * @see IDataFetcher
     */
    override fun clearData() {
        dataFetcher.clearData()
    }

    /**
     * @see IListDataFetcher
     */
    override fun clearListData() {
        listDataFetcher.clearListData()
    }

    /**
     * @see fetchData
     */
    override fun getLiveData(): LiveData<M?> {
        return dataFetcher.getLiveData()
    }

    /**
     * @see fetchListData
     */
    override fun getListLiveData(): LiveData<MutableList<M>> {
        return listDataFetcher.getListLiveData()
    }

    /**
     * @see IListDataFetcher
     */
    override fun obtainPager(): IDataPager<M> {
        return listDataFetcher.obtainPager()
    }

    /**
     * Check if the network is available, not just whether the network switch is turned on, but
     * whether it can send and receive data packets.
     * 简体中文：检测网络是否可用，非是否打开网络开关，而是是否能够收发数据包。
     *
     * @return
     */
    protected val isNetworkAvailable: Boolean
        protected get() = checkNetwork(context)

    private fun checkNetwork(context: Context): Boolean {
        val networkInfo = getActiveNetworkInfo(context)
        return networkInfo != null && networkInfo.isConnected
    }

    private fun getActiveNetworkInfo(context: Context): NetworkInfo? {
        val connectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo
    }

    protected open fun getModelType() : Class<M> {
        return getGenericType(this) as Class<M>
    }

    private fun getGenericType(obj: Any): Class<*> {
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
        val listRepository = javaClass.getAnnotation(ListRepository::class.java)
        if (repository == null && listRepository == null) {
            throw RuntimeException("@Repository or @ListRepository is required.")
        }
        if (repository != null) {
            isListMode = false
        }
        if (isListMode) {
            isLogPrint = listRepository.isLogPrint
        } else {
            isLogPrint = repository.isLogPrint
        }
        MClass = getModelType()
        Log.d(TAG, "MClass:$MClass,isListMode:$isListMode")
        cacheHolderFactory = createCacheHolderFactory()
        // Implement either CacheHolder or DataFetcher and use it.
        // 简体中文：二选一实现CacheHolder和DataFetcher并使用
        if (isListMode) {
            listCacheHolder = cacheHolderFactory.createListCacheHolder(MClass)
            listCacheHolder.init()
            listDataFetcher = createListDataFetcher()
        } else {
            cacheHolder = cacheHolderFactory.createCacheHolder(MClass)
            cacheHolder.init()
            dataFetcher = createDataFetcher()
        }
    }

    /**
     * Intercept network requests and the loaded cached data, making some modifications, using
     * non-collection mode.
     * 简体中文：拦截网络请求和缓存加载出来的数据，并做一些修改，非集合模式使用。
     */
    protected open fun onInterceptData(type: DataSource.Type, model: M) {}

    /**
     * Intercept network requests and the loaded cached data, making some modifications, using
     * collection mode.
     * 简体中文：拦截网络请求和缓存加载出来的数据，并做一些修改，集合模式使用。
     */
    protected open fun onInterceptData(type: DataSource.Type, models: MutableList<M>) {}

    /**
     * Before intercepting the data, preliminary filtering is performed to reduce the complexity of
     * editing and modifying the data. This is only applicable to collection mode.
     * 简体中文：在拦截数据之前会对数据进行初步的过滤，以降低编辑修改数据的复杂度，仅用于集合模式。
     */
    protected open fun onFilterData(type: DataSource.Type, models: MutableList<M>) : MutableList<M>{ return models }
}