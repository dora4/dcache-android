package dora.http

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * REST API返回列表数据的回调。
 */
abstract class DoraListCallback<M> : Callback<MutableList<M>> {

    abstract fun onSuccess(models: MutableList<M>)

    abstract fun onFailure(msg: String)

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

    override fun onFailure(call: Call<MutableList<M>>, t: Throwable) {
        onFailure(t.toString())
    }

    companion object {
        const val EMPTY_BODY = "Empty Body"
    }
}