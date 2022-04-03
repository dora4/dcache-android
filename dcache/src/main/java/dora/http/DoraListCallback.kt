package dora.http

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class DoraListCallback<M> : Callback<MutableList<M>> {

    abstract fun onSuccess(models: MutableList<M>)

    abstract fun onFailure(code: Int, msg: String?)

    override fun onResponse(call: Call<MutableList<M>>, response: Response<MutableList<M>>) {
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                onSuccess(body)
            } else {
                onFailure(-1, "Empty Body")
            }
        }
    }

    override fun onFailure(call: Call<MutableList<M>>, t: Throwable) {
        onFailure(-1, t.message)
    }
}