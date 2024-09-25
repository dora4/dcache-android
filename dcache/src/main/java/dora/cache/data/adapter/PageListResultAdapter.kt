package dora.cache.data.adapter

import dora.cache.DoraPageListCallback
import dora.http.DoraCallback

/**
 * To implement a REST API interface that adapts the model data returned by
 * [dora.cache.data.adapter.PageResult] into a [dora.http.DoraCallback] object, which is required
 * by [dora.cache.repository.DoraPageDatabaseCacheRepository] or
 * [dora.cache.repository.DoraPageFlowDatabaseCacheRepository] in their onLoadFromNetwork() method.
 * 简体中文：将实现[dora.cache.data.adapter.PageResult]的REST API接口返回的model数据适配成框架需要的
 * [dora.http.DoraCallback]对象，用于[dora.cache.repository.DoraPageDatabaseCacheRepository]
 * 或[dora.cache.repository.DoraPageFlowDatabaseCacheRepository]的onLoadFromNetwork()中。
 *
 * @see ResultAdapter
 * @see ListResultAdapter
 */
open class PageListResultAdapter<M, R : PageResult<M>>(val callback: DoraPageListCallback<M>)
    : DoraCallback<R>() {

    /**
     * Adapt the successful callback of [dora.cache.DoraPageListCallback].
     * 简体中文：适配[dora.cache.DoraPageListCallback]成功的回调。
     */
    override fun onSuccess(model: R) {
        val totalSize = model.getTotalSize()
        callback.onSuccess(totalSize, if (model.getRealModel() != null &&
                (model.getRealModel() is MutableList<*>)) model.getRealModel() as MutableList<M>
        else arrayListOf())
    }

    /**
     * Adapt the failure callback of [dora.cache.DoraPageListCallback].
     * 简体中文：适配[dora.cache.DoraPageListCallback]失败的回调。
     */
    override fun onFailure(msg: String) {
        callback.onFailure(msg)
    }
}