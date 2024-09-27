package dora.http

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Callback for REST API returning non-list data.
 * 简体中文：REST API返回非列表数据的回调。
 */
abstract class DoraCallback<M> : Callback<M> {

    abstract fun onSuccess(model: M)

    abstract fun onFailure(msg: String)

    override fun onResponse(call: Call<M>, response: Response<M>) {
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

    override fun onFailure(call: Call<M>, t: Throwable) {
        onFailure(t.toString())
    }

    companion object {
        const val EMPTY_BODY = "Empty Body"
    }
}