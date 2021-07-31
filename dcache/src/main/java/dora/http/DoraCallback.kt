package dora.http

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class DoraCallback<M> : Callback<M> {

    abstract fun onSuccess(model: M)

    abstract fun onFailure(code: Int, msg: String?)

    protected open fun onInterceptNetworkData(data: M) {}

    override fun onResponse(call: Call<M>, response: Response<M>) {
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                onSuccess(body)
            } else {
                onFailure(-1, "Empty Body")
            }
        }
    }

    override fun onFailure(call: Call<M>, t: Throwable) {
        onFailure(-1, t.message)
    }
}