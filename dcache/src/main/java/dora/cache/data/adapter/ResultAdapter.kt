package dora.cache.data.adapter

import dora.http.DoraCallback

/**
 * 将实现[dora.cache.data.adapter.Result]的api接口返回的model数据适配成框架需要的[dora.http.DoraCallback]
 * 对象。
 */
class ResultAdapter<M, R : Result<M>>(val callback: DoraCallback<M>) : DoraCallback<R>() {

    /**
     * 适配[dora.http.DoraCallback]成功的回调。
     */
    override fun onSuccess(model: R) {
        model.getRealModel()?.let { callback.onSuccess(it) }
    }

    /**
     * 适配[dora.http.DoraCallback]失败的回调。
     */
    override fun onFailure(msg: String) {
        callback.onFailure(msg)
    }
}