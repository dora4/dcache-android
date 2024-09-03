package dora.http.retrofit

import android.annotation.SuppressLint
import dora.http.coroutine.flow.FlowCallAdapterFactory
import dora.http.exception.DoraHttpException
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.collections.HashMap

/**
 * Retrofit管理器，kotlin调用initConfig进行初始化，java调用getConfig进行初始化。通过配置RetrofitManager来管理全局
 * 的URL映射。测试http请求地址：http://httpbin.org。
 */
object RetrofitManager {

    /**
     * 用于保存所有API服务对应的Retrofit对象。
     */
    private val retrofitMap: MutableMap<Class<*>, Retrofit> = HashMap()

    /**
     * 用于保存所有API服务映射的URL地址。
     */
    private val urlMap: MutableMap<Class<*>, String> = HashMap()

    /**
     * 用于保存全局的配置信息。
     */
    private val config: Config = Config()

    /**
     * java或kotlin调用它初始化全局配置。
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
                if (config.isUseFlow()) {
                    builder.addCallAdapterFactory(FlowCallAdapterFactory())
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

        /**
         * 发送请求的客户端。
         */
        private var client = OkHttpClient()

        /**
         * 是否启用Observable请求，兼容rxjava低版本，高版本不启用也不存在问题。
         */
        private var useRxJava: Boolean = false

        /**
         * 是否启用Flow接口请求。
         */
        private var useFlow: Boolean = false

        /**
         * 是否信任所有https证书。
         */
        private var useHttps: Boolean = false

        /**
         * builder保持public方便获取后直接修改内容。
         */
        val builder = OkHttpClient.Builder()

        fun setClient(client: OkHttpClient) : Config {
            this.client = client
            return this
        }

        /**
         * 启用Observable请求，兼容rxjava低版本，高版本不启用也不存在问题。
         */
        fun rxJava(useRxJava: Boolean) : Config {
            this.useRxJava = useRxJava
            return this
        }

        /**
         * 启用Flow接口请求。
         */
        fun flow(useFlow: Boolean) : Config {
            this.useFlow = useFlow
            return this
        }

        /**
         * 信任所有https证书。
         */
        fun https(useHttps: Boolean) : Config {
            this.useHttps = useHttps
            return this
        }

        fun getClient() : OkHttpClient {
            return client
        }

        fun isUseRxJava() : Boolean {
            return useRxJava
        }

        fun isUseFlow() : Boolean {
            return useFlow
        }

        fun isUseHttps() : Boolean {
            return useHttps
        }

        fun okhttp(block: OkHttpClient.Builder.() -> OkHttpClient) {
            if (useHttps) {
                val trustAllCerts = arrayOf<TrustManager>(
                    @SuppressLint("CustomX509TrustManager")
                    object : X509TrustManager {
                        @SuppressLint("TrustAllX509TrustManager")
                        override fun checkClientTrusted(
                            chain: Array<X509Certificate>,
                            authType: String
                        ) {
                        }

                        @SuppressLint("TrustAllX509TrustManager")
                        override fun checkServerTrusted(
                            chain: Array<X509Certificate>,
                            authType: String
                        ) {
                        }

                        override fun getAcceptedIssuers(): Array<X509Certificate> {
                            return arrayOf()
                        }
                    }
                )
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, SecureRandom())
                builder.sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            }
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