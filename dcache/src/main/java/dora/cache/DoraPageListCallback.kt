package dora.cache

import dora.http.DoraListCallback
import retrofit2.Call
import retrofit2.Response

/**
 * REST API返回列表数据的回调。
 */
abstract class DoraPageListCallback<M> : DoraListCallback<M>() {

    private var totalSize: Int = 0

    fun getTotalSize() : Int {
        return totalSize
    }

    open fun onSuccess(totalSize: Int, models: MutableList<M>) {
        this.totalSize = totalSize
    }

    override fun onResponse(call: Call<MutableList<M>>, response: Response<MutableList<M>>) {
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                onSuccess(body)
            } else {
                onFailure(EMPTY_BODY)
            }
        } else {
            onFailure("${response.code()}:${response.message()}")
        }
    }
}