package dora.http

import dora.db.OrmTable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class DoraListCallback<T : OrmTable> : Callback<ApiResult<List<T>>> {
    abstract fun onSuccess(data: List<T>)
    abstract fun onFailure(code: Int, msg: String?)
    protected open fun onInterceptNetworkData(data: List<T>) {}
    override fun onResponse(call: Call<ApiResult<List<T>>>, response: Response<ApiResult<List<T>>>) {
        val code = response.code()
        if (code == 200) {
            val body = response.body()
            if (body != null) {
                val data = body.data
                if (data != null) {
                    onSuccess(data)
                } else {
                    onFailure(1001, "空数据返回")
                }
            } else {
                onFailure(1002, "没有响应体")
            }
        } else {
            onFailure(code, "HTTP状态码：$code")
        }
    }

    override fun onFailure(call: Call<ApiResult<List<T>>>, t: Throwable) {
        onFailure(-1, t.message)
    }
}