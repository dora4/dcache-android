package dora.http

import dora.db.OrmTable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class DoraListCallback<T : OrmTable> : Callback<List<T>> {

    abstract fun onSuccess(data: List<T>)

    abstract fun onFailure(code: Int, msg: String?)

    protected open fun onInterceptNetworkData(data: List<T>) {}

    override fun onResponse(call: Call<List<T>>, response: Response<List<T>>) {
        if (response.isSuccessful) {
            val body = response.body()
            body?.let {
                if (it != null) {
                    onSuccess(it)
                } else {
                    onFailure(-1, "Empty Body")
                }
            }
        }
    }

    override fun onFailure(call: Call<List<T>>, t: Throwable) {
        onFailure(-1, t.message)
    }
}