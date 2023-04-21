package dora.http.retrofit

import dora.http.exception.DoraHttpException
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.collections.HashMap

/**
 * Retrofit管理器，kotlin调用initConfig进行初始化，java调用getConfig进行初始化。通过配置RetrofitManager来管理全局
 * 的URL映射。
 */
object RetrofitManager {

    /**
     * 用于保存所有API服务对应的Retrofit对象。
     */
    private var retrofitMap: MutableMap<Class<*>, Retrofit> = HashMap()

    /**
     * 用于保存所有API服务映射的URL地址。
     */
    private var urlMap: MutableMap<Class<*>, String> = HashMap()

    /**
     * 用于保存全局的配置信息。
     */
    private var config: Config = Config()

    /**
     * java调用它初始化全局配置。
     */
    fun getConfig() : Config {
        return config
    }

    /**
     * kotlin调用它初始化全局配置。
     */
    fun initConfig(block: Config.() -> Unit) {
        block(config)
    }

    /**
     * 检测API服务是否可用。
     */
    fun <T : ApiService> checkService(clazz: Class<T>) : Boolean {
        return urlMap.containsKey(clazz)
    }

    /**
     * 移除API服务对象。
     */
    fun <T : ApiService> removeService(clazz: Class<T>) {
        if (retrofitMap.containsKey(clazz)) {
            retrofitMap.remove(clazz)
        }
        if (urlMap.containsKey(clazz)) {
            urlMap.remove(clazz)
        }
    }

    /**
     * 获取API服务对象。
     */
    fun <T : ApiService> getService(clazz: Class<T>): T {
        val retrofit: Retrofit
        if (retrofitMap.containsKey(clazz)) {
            retrofit = retrofitMap[clazz]!!
            return retrofit.create(clazz)
        } else {
            if (urlMap.containsKey(clazz)) {
                val url = urlMap[clazz] ?: ""
                val builder = Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(getConfig().getClient())
                if (config.isUseRxJava()) {
                    builder.addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                }
                retrofit = builder.build()
                retrofitMap[clazz] = retrofit
            } else {
                throw DoraHttpException("ApiService is not registered.")
            }
        }
        return retrofit.create(clazz)
    }

    class Config {

        private var client = OkHttpClient()

        /**
         * 低版本可能需要启用rxjava。
         */
        private var useRxJava: Boolean = false

        val builder = OkHttpClient.Builder()

        fun setClient(client: OkHttpClient) : Config {
            this.client = client
            return this
        }

        fun rxJava(useRxJava: Boolean) : Config {
            this.useRxJava = useRxJava
            return this
        }

        fun getClient() : OkHttpClient {
            return client
        }

        fun isUseRxJava() : Boolean {
            return useRxJava
        }

        fun okhttp(block: OkHttpClient.Builder.() -> OkHttpClient) {
            client = block(builder)
        }

        /**
         * 将API服务和baseUrl绑定起来，没有则添加baseUrl，有则替换baseUrl。可以映射多个url地址。
         */
        fun mappingBaseUrl(serviceClazz: Class<out ApiService>, baseUrl: String) = apply {
            if (!urlMap.containsKey(serviceClazz)) {
                registerBaseUrl(serviceClazz, baseUrl)
            } else {
                replaceBaseUrl(serviceClazz, baseUrl)
            }
        }

        /**
         * @see mappingBaseUrl
         */
        private fun registerBaseUrl(serviceClazz: Class<out ApiService>, baseUrl: String) = apply {
            urlMap[serviceClazz] = baseUrl
        }

        /**
         * @see mappingBaseUrl
         */
        private fun replaceBaseUrl(serviceClazz: Class<out ApiService>, baseUrl: String) = apply {
            urlMap.remove(serviceClazz)
            urlMap[serviceClazz] = baseUrl
        }
    }
}