package dora.http.retrofit

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

abstract class BaseRetrofitManager protected constructor() {
    var urlMap: MutableMap<Class<*>, String>
    var retrofitMap: MutableMap<Class<*>, Retrofit?>
    protected abstract fun initBaseUrl()
    protected fun registerBaseUrl(serviceClazz: Class<out ApiService?>, baseUrl: String) {
        urlMap[serviceClazz] = baseUrl
    }

    protected abstract fun createHttpClient(): OkHttpClient?

    /**
     * 建议在单例的子类写一些静态方法getService，调用此方法。
     * <pre>
     * public static <T> T getService(Class<T> clazz) {
     * if (retrofitManager == null) {
     * synchronized (RetrofitManager.class) {
     * if (retrofitManager == null) retrofitManager = new RetrofitManager();
     * }
     * }
     * return retrofitManager._getService(clazz);
     * }
    </T></T></pre> *
     *
     * @param clazz
     * @param <T>
     * @return
    </T> */
    protected fun <T> _getService(clazz: Class<T>): T {
        val retrofit: Retrofit?
        if (retrofitMap.containsKey(clazz)) {
            retrofit = retrofitMap[clazz]
            return retrofit!!.create(clazz)
        } else {
            retrofit = Retrofit.Builder()
                    .baseUrl(Objects.requireNonNull(urlMap[clazz]))
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(createHttpClient())
                    .build()
            retrofitMap[clazz] = retrofit
        }
        return retrofit.create(clazz)
    }

    init {
        urlMap = HashMap()
        retrofitMap = HashMap()
        initBaseUrl()
    }
}