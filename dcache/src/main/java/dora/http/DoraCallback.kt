package dora.http

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class DoraCallback<T> : Callback<T> {

    abstract fun onSuccess(data: T)

    abstract fun onFailure(code: Int, msg: String?)

    protected open fun onInterceptNetworkData(data: T) {}

    override fun onResponse(call: Call<T>, response: Response<T>) {
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                onSuccess(body)
            } else {
                onFailure(-1, "Empty Body")
            }
        }
    }

    override fun onFailure(call: Call<T>, t: Throwable) {
        onFailure(-1, t.message)
    }
}