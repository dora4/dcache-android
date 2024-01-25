package dora.cache.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import androidx.lifecycle.ViewModel
import dora.cache.data.fetcher.IDataFetcher
import dora.cache.data.fetcher.IFlowDataFetcher
import dora.cache.data.fetcher.IListDataFetcher
import dora.cache.data.fetcher.IListFlowDataFetcher
import dora.cache.data.fetcher.OnLoadStateListener
import dora.cache.data.fetcher.OnLoadStateListenerImpl
import dora.cache.data.page.IDataPager
import dora.cache.holder.DatabaseCacheHolder
import dora.http.DoraCallback
import dora.http.DoraListCallback
import io.reactivex.Observable
import kotlinx.coroutines.flow.StateFlow
import java.lang.reflect.ParameterizedType

/**
 * 数据仓库，缓存和加载流程处理基类。一个[BaseFlowRepository]要么用于非集合数据，要么用于集合数据。如果要用于
 * 非集合数据，请在实现类配置[Repository]注解，如果为集合数据，请在实现类配置[ListRepository]注解。必须配置其中
 * 一个注解。
 */
abstract class BaseFlowRepository<M>(val context: Context) : ViewModel(), IFlowDataFetcher<M>,
    IListFlowDataFetcher<M> {

    /**
     * 非集合数据获取接口。
     */
    protected lateinit var dataFetcher: IFlowDataFetcher<M>

    /**
     * 集合数据获取接口。
     */
    protected lateinit var listDataFetcher: IListFlowDataFetcher<M>

    /**
     * 非集合数据缓存接口。
     */
    protected lateinit var databaseCacheHolder: DatabaseCacheHolder<M>

    /**
     * 集合数据缓存接口。
     */
    protected lateinit var listDatabaseCacheHolder: DatabaseCacheHolder<MutableList<M>>

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

    protected abstract fun createDataFetcher(): IFlowDataFetcher<M>

    protected abstract fun createListDataFetcher(): IListFlowDataFetcher<M>

    protected abstract fun createCacheHolder(clazz: Class<M>): DatabaseCacheHolder<M>

    protected abstract fun createListCacheHolder(clazz: Class<M>): DatabaseCacheHolder<MutableList<M>>

    /**
     * 手动放入缓存数据，仅listMode为true时使用，注意只会追加到缓存里面去，请调用接口将新数据也更新到服务端，以致
     * 于下次请求api接口时也会有这部分数据。
     */
    fun addData(data: M, listener: OnSyncListener<M>?) {
        if (isListMode) {
            addData(arrayListOf(data), listener)
            listener?.onSyncData(true, arrayListOf(data))
        }
    }

    /**
     * 手动放入一堆缓存数据，仅listMode为true时使用，注意只会追加到缓存里面去，请调用接口将新数据也更新到服务端，
     * 以致于下次请求api接口时也会有这部分数据。
     */
    fun addData(data: MutableList<M>, listener: OnSyncListener<M>?) {
        if (data.size == 0) return
        if (isListMode) {
            getListFlowData().value.let {
                it.addAll(data)
                listDatabaseCacheHolder.addNewCache(data)
                listener?.onSyncData(data.size == 1, data)
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
    protected abstract fun selectData(ds: DataSource): Boolean

    /**
     * 手动添加数据，也需要同步数据给后端。
     */
    interface OnSyncListener<M> {

        /**
         * 在此回调中调用REST API同步数据给后端，isSingle是否为单条数据。
         */
        fun onSyncData(isSingle: Boolean, data: MutableList<M>)
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
        fun loadFromCache(type: CacheType): Boolean

        /**
         * 从服务器/网络加载数据。
         */
        fun loadFromNetwork()
    }

    /**
     * 抓取非集合数据，返回给stateflow，以便于展示在UI上。抓取成功后会一直在livedata中，可以通过[.geFlowData()]
     * 拿到。
     */
    override fun fetchData(description: String?, listener: OnLoadStateListener?): StateFlow<M?> {
        if (description != null) {
            // 不能让null覆盖了默认类名
            this.description = description
        }
        this.listener = listener
        return dataFetcher.fetchData(description)
    }

    /**
     * 抓取集合数据，返回给stateflow，以便于展示在UI上。抓取成功后会一直在stateflow中，可以通过[.getListFlowData()]
     * 拿到。
     */
    override fun fetchListData(description: String?, listener: OnLoadStateListener?): StateFlow<MutableList<M>> {
        if (description != null) {
            // 不能让null覆盖了默认类名
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
    override fun getFlowData(): StateFlow<M?> {
        return dataFetcher.getFlowData()
    }

    /**
     * @see fetchListData
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
        val MClass: Class<M> = getGenericType(this) as Class<M>
        Log.d(TAG, "MClass:$MClass,isListMode:$isListMode")
        // 二选一实现CacheHolder和DataFetcher并使用
        if (isListMode) {
            listDatabaseCacheHolder = createListCacheHolder(MClass)
            listDatabaseCacheHolder.init()
            listDataFetcher = createListDataFetcher()
        } else {
            databaseCacheHolder = createCacheHolder(MClass)
            databaseCacheHolder.init()
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