package dora.http.retrofit

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.HashMap

abstract class BaseRetrofitManager protected constructor() {

    private var config: Config
    private var retrofitMap: MutableMap<Class<*>, Retrofit> = HashMap()
    protected var urlMap: MutableMap<Class<*>, String> = HashMap()
    private var client: OkHttpClient
    private var clientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()

    init {
        client = createHttpClient()
        initBaseUrl(client)
        config = Config()
    }

    fun init(block: Config.() -> Unit) {
        block(config)
    }

    fun getConfig() : Config {
        return config
    }

    fun setClient(client: OkHttpClient) {
        this.client = client
    }

    protected abstract fun createHttpClient(): OkHttpClient

    protected abstract fun initBaseUrl(client: OkHttpClient)

    fun <T : ApiService> getService(clazz: Class<T>): T {
        val retrofit: Retrofit?
        if (retrofitMap.containsKey(clazz)) {
            retrofit = retrofitMap[clazz]
            return retrofit!!.create(clazz)
        } else {
            retrofit = Retrofit.Builder()
                    .baseUrl(Objects.requireNonNull(urlMap[clazz]))
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()
            retrofitMap[clazz] = retrofit
        }
        return retrofit.create(clazz)
    }

    inner class Config {

        /**
         * 请在子类的initBaseUrl()中进行注册，可以注册多个url地址。
         */
        fun registerBaseUrl(serviceClazz: Class<out ApiService>, baseUrl: String) = apply {
            urlMap[serviceClazz] = baseUrl
        }

        /**
         * 只配置一个。
         */
        fun okhttp(block: OkHttpClient.Builder.() -> OkHttpClient.Builder) = apply {
            clientBuilder = block(clientBuilder)
            client = clientBuilder.build()
        }
    }
}