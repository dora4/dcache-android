package dora.http.retrofit

import okhttp3.OkHttpClient

object DoraRetrofitManager : BaseRetrofitManager() {

    override fun initBaseUrl(client: OkHttpClient) {
    }

    override fun createHttpClient(): OkHttpClient {
        return OkHttpClient()
    }
}