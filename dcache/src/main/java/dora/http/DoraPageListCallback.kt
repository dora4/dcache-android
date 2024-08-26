package dora.http

import retrofit2.Call
import retrofit2.Response

/**
 * REST API返回列表数据的回调。
 */
abstract class DoraPageListCallback<M> : DoraListCallback<M>() {

    abstract fun totalSize() : Int

    final override fun onSuccess(models: MutableList<M>) {
    }

    abstract fun onSuccess(totalSize: Int, models: MutableList<M>)

    override fun onResponse(call: Call<MutableList<M>>, response: Response<MutableList<M>>) {
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                onSuccess(totalSize(), body)
            } else {
                onFailure(EMPTY_BODY)
            }
        } else {
            onFailure("${response.code()}:${response.message()}")
        }
    }
}