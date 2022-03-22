package dora.http.retrofit

import okhttp3.OkHttpClient

/**
 * 由于无法调用init(block: Config.() -> Unit)，仅供Java使用的RetrofitManager，功能等同于RetrofitManager。
 */
class JRetrofitManager : RetrofitManager() {

    override fun initBaseUrl(client: OkHttpClient) {
    }

    override fun createHttpClient(): OkHttpClient {
        return OkHttpClient()
    }
}