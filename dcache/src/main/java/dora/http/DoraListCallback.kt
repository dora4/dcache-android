package dora.http

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class DoraListCallback<M> : Callback<List<M>> {

    abstract fun onSuccess(models: List<M>)

    abstract fun onFailure(code: Int, msg: String?)

    override fun onResponse(call: Call<List<M>>, response: Response<List<M>>) {
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                onSuccess(body)
            } else {
                onFailure(-1, "Empty Body")
            }
        }
    }

    override fun onFailure(call: Call<List<M>>, t: Throwable) {
        onFailure(-1, t.message)
    }
}