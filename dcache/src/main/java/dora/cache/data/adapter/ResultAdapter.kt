package dora.cache.data.adapter

import dora.http.DoraCallback

/**
 * To implement a REST API interface that adapts the model data returned by
 * [dora.cache.data.adapter.Result] into a [dora.http.DoraCallback] object, which is required
 * by [dora.cache.repository.BaseRepository] in its onLoadFromNetwork() method.
 * 简体中文：将实现[dora.cache.data.adapter.Result]的REST API接口返回的model数据适配成框架需要的
 * [dora.http.DoraCallback]对象，用于[dora.cache.repository.BaseRepository]的onLoadFromNetwork()中。
 *
 * @see ListResultAdapter
 */
open class ResultAdapter<M, R : Result<M>>(private val callback: DoraCallback<M>) : DoraCallback<R>() {

    /**
     * Adapt the successful callback of [dora.http.DoraCallback].
     * 适配[dora.http.DoraCallback]成功的回调。
     */
    override fun onSuccess(model: R) {
        model.getRealModel()?.let { callback.onSuccess(it) }
    }

    /**
     * Adapt the failure callback of [dora.http.DoraCallback].
     * 适配[dora.http.DoraCallback]失败的回调。
     */
    override fun onFailure(msg: String) {
        callback.onFailure(msg)
    }
}