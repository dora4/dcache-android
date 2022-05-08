package dora.http.retrofit

import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.HashMap

object RetrofitManager {

    private var retrofitMap: MutableMap<Class<*>, Retrofit> = HashMap()
    private var urlMap: MutableMap<Class<*>, String> = HashMap()
    private var config: Config = Config()

    fun getConfig() : Config {
        return config
    }

    fun initConfig(block: Config.() -> Unit) {
        block(config)
    }

    fun <T : ApiService> getService(clazz: Class<T>): T {
        val retrofit: Retrofit?
        if (retrofitMap.containsKey(clazz)) {
            retrofit = retrofitMap[clazz]
            return retrofit!!.create(clazz)
        } else {
            retrofit = Retrofit.Builder()
                    .baseUrl(Objects.requireNonNull(urlMap[clazz]))
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                    .client(getConfig().getClient())
                    .build()
            retrofitMap[clazz] = retrofit
        }
        return retrofit.create(clazz)
    }


    class Config {

        private var client = OkHttpClient()
        val builder = OkHttpClient.Builder()

        fun setClient(client: OkHttpClient) : Config {
            this.client = client
            return this
        }

        fun getClient() : OkHttpClient {
            return client
        }

        fun okhttp(block: OkHttpClient.Builder.() -> OkHttpClient) {
            client = block(builder)
        }

        /**
         * 请在子类的initBaseUrl()中进行注册，可以注册多个url地址。
         */
        fun registerBaseUrl(serviceClazz: Class<out ApiService>, baseUrl: String) = apply {
            urlMap[serviceClazz] = baseUrl
        }
    }
}