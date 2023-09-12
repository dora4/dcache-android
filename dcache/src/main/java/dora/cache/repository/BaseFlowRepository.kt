package dora.cache.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dora.cache.data.fetcher.IDataFetcher
import dora.cache.data.fetcher.IDataFlower
import dora.cache.data.fetcher.IListDataFetcher
import dora.cache.data.fetcher.IListDataFlower
import dora.cache.data.fetcher.OnLoadStateListener
import dora.cache.data.fetcher.OnLoadStateListenerImpl
import dora.cache.data.page.IDataPager
import dora.cache.holder.CacheHolder
import dora.http.DoraCallback
import dora.http.DoraListCallback
import io.reactivex.Observable
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.lang.reflect.ParameterizedType

/**
 * 数据仓库，缓存和加载流程处理基类。一个[BaseFlowRepository]要么用于非集合数据，要么用于集合数据。如果要用于
 * 非集合数据，请在实现类配置[Repository]注解将[.isListMode]的值设置为false，默认为集合模式。注意，
 * 无论是集合模式还是非集合模式，Repository注解都是必须的。
 */
abstract class BaseFlowRepository<M>(protected val context: Context) : ViewModel(), IDataFlower<M>, IListDataFlower<M> {

    /**
     * 非集合数据获取接口。
     */
    protected lateinit var dataFetcher: IDataFlower<M>

    /**
     * 集合数据获取接口。
     */
    protected lateinit var listDataFetcher: IListDataFlower<M>

    /**
     * 非集合数据缓存接口。
     */
    protected lateinit var cacheHolder: CacheHolder<M>

    /**
     * 集合数据缓存接口。
     */
    protected lateinit var listCacheHolder: CacheHolder<MutableList<M>>

    /**
     * true代表用于集合数据，false用于非集合数据。
     */
    protected var isListMode = true
        protected set

    protected var isLogPrint = false
        protected set

    protected var description: String? = javaClass.simpleName
    protected var listener: OnLoadStateListener? = OnLoadStateListenerImpl()

    /**
     * 是否在网络加载数据失败的时候清空数据。
     *
     * @return
     */
    protected val isClearDataOnNetworkError: Boolean
        protected get() = false

    protected abstract fun createDataFetcher(): IDataFlower<M>

    protected abstract fun createListDataFetcher(): IListDataFlower<M>

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
            getListFlowData().value.let {
                it.addAll(data)
                listCacheHolder.addNewCache(data)
            }
        }
    }

    /**
     * 保证成员属性不为空，而成功调用数据库查询方法，提高查询可靠性。比如用来校验属性，a != null && b != null
     * && c != null。
     *
     * @see BaseDatabaseCacheRepository.query
     */
    protected open fun checkValuesNotNull() : Boolean { return true }

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
     * 非集合数据的API接口调用，Retrofit接口的方法返回retrofit.Call类型使用。
     *
     * @param callback 数据回调
     * @param listener 如果你自行处理掉网络请求的异常，不要忘了回调失败的状态，以便于在fetchData的时候能收到状态的回调
     */
    protected abstract fun onLoadFromNetwork(callback: DoraCallback<M>, listener: OnLoadStateListener? = null)

    /**
     * 集合数据的API接口调用，Retrofit接口的方法返回retrofit.Call类型使用。
     *
     * @param callback 数据回调
     * @param listener 如果你自行处理掉网络请求的异常，不要忘了回调失败的状态，以便于在fetchListData的时候能收到状态的回调
     */
    protected abstract fun onLoadFromNetwork(callback: DoraListCallback<M>, listener: OnLoadStateListener? = null)

    /**
     * 非集合数据的API接口调用，Retrofit接口的方法返回io.reactivex.Observable类型使用。
     *
     * @param listener 如果你自行处理掉网络请求的异常，不要忘了回调失败的状态，以便于在fetchData的时候能收到状态的回调
     */
    protected abstract fun onLoadFromNetworkObservable(listener: OnLoadStateListener? = null) : Observable<M>

    /**
     * 集合数据的API接口调用，Retrofit接口的方法返回io.reactivex.Observable类型使用。
     *
     * @param listener 如果你自行处理掉网络请求的异常，不要忘了回调失败的状态，以便于在fetchListData的时候能收到状态的回调
     */
    protected abstract fun onLoadFromNetworkObservableList(listener: OnLoadStateListener? = null) : Observable<MutableList<M>>

    /**
     * 从仓库选择数据，处理数据来源的优先级。
     *
     * @param ds 数据的来源
     * @return 数据是否获取成功
     */
    protected abstract suspend fun selectData(ds: DataSource): Boolean

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

            /**
             * 内置SQLite数据库。
             */
            DATABASE,

            /**
             * 自定义的数据仓库。
             */
            CUSTOM
        }

        /**
         * 从缓存中加载数据。
         *
         * @param type
         * @return
         */
        suspend fun loadFromCache(type: CacheType): Boolean

        /**
         * 从服务器/网络加载数据。
         */
        suspend fun loadFromNetwork()
    }

    open fun fetchData(description: String?, listener: OnLoadStateListener?) : Flow<M> {
        return flow {
            flowData(description, listener)
        }
    }

    open fun fetchListData(description: String?, listener: OnLoadStateListener?) : Flow<MutableList<M>> {
        return flow {
            flowListData(description, listener)
        }
    }

    override suspend fun flowData(description: String?, listener: OnLoadStateListener?): StateFlow<M?> {
        if (description != null) {
            // 不能让null覆盖了默认类名
            this.description = description
        }
        this.listener = listener
        return dataFetcher.flowData(description)
    }

    override suspend fun flowListData(description: String?, listener: OnLoadStateListener?): StateFlow<MutableList<M>> {
        if (description != null) {
            // 不能让null覆盖了默认类名
            this.description = description
        }
        this.listener = listener
        return listDataFetcher.flowListData(description)
    }

    /**
     * @see IDataFetcher
     */
    override suspend fun clearData() {
        dataFetcher.clearData()
    }

    /**
     * @see IListDataFetcher
     */
    override suspend fun clearListData() {
        listDataFetcher.clearListData()
    }

    /**
     * @see flowData
     */
    override fun getFlowData(): StateFlow<M?> {
        return dataFetcher.getFlowData()
    }

    /**
     * @see flowListData
     */
    override fun getListFlowData(): StateFlow<MutableList<M>> {
        return listDataFetcher.getListFlowData()
    }

    /**
     * @see IListDataFetcher
     */
    override fun obtainPager(): IDataPager<M> {
        return listDataFetcher.obtainPager()
    }

    /**
     * 检测网络是否可用，非是否打开网络开关，而是是否能够收发数据包。
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
                ?: throw RuntimeException("@Repository is required.")
        isListMode = repository.isListMode
        isLogPrint = repository.isLogPrint
        val MClass: Class<M> = getGenericType(this) as Class<M>
        Log.d(TAG, "MClass:$MClass,isListMode:$isListMode")
        // 二选一实现CacheHolder和DataFetcher并使用
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

    /**
     * 拦截网络请求和缓存加载出来的数据，并做一些修改，非集合模式使用。
     */
    protected open fun onInterceptData(type: DataSource.Type, model: M) {}

    /**
     * 拦截网络请求和缓存加载出来的数据，并做一些修改，集合模式使用。
     */
    protected open fun onInterceptData(type: DataSource.Type, models: MutableList<M>) {}
}