package dora.http.retrofit

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.HashMap

abstract class BaseRetrofitManager protected constructor() {

    protected var urlMap: MutableMap<Class<*>, String> = HashMap()
    protected var retrofitMap: MutableMap<Class<*>, Retrofit> = HashMap()
    var client: OkHttpClient

    init {
        client = createHttpClient()
        initBaseUrl(client)
    }

    protected abstract fun initBaseUrl(client: OkHttpClient)

    /**
     * 请在子类的initBaseUrl()中进行注册，可以注册多个url地址。
     */
    fun registerBaseUrl(serviceClazz: Class<out ApiService>, baseUrl: String) = apply {
        urlMap[serviceClazz] = baseUrl
    }

    protected abstract fun createHttpClient(): OkHttpClient

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
}