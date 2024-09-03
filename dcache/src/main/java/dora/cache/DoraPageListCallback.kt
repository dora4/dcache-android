package dora.cache

import androidx.annotation.CallSuper
import dora.http.DoraListCallback
import retrofit2.Call
import retrofit2.Response

/**
 * 分页缓存专用。用于[dora.cache.repository.DoraPageDatabaseCacheRepository]和
 * [dora.cache.repository.DoraPageFlowDatabaseCacheRepository]，REST API返回分页列表数据的回调。
 * 与之对应的数据转换适配器为[dora.cache.data.adapter.PageListResultAdapter]。在repository的
 * onLoadFromNetwork()中会被用到。
 */
abstract class DoraPageListCallback<M> : DoraListCallback<M>() {

    /**
     * 总共数据的条数。
     */
    private var totalSize: Int = 0

    fun getTotalSize() : Int {
        return totalSize
    }

    @CallSuper
    open fun onSuccess(totalSize: Int, models: MutableList<M>) {
        this.totalSize = totalSize
    }

    final override fun onSuccess(models: MutableList<M>) {
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