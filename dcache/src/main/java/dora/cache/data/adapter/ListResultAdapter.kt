package dora.cache.data.adapter

import dora.http.DoraCallback
import dora.http.DoraListCallback

/**
 * 将实现[dora.cache.data.adapter.Result]的REST API接口返回的model数据适配成框架需要的
 * [dora.http.DoraListCallback]对象，用于[dora.cache.repository.BaseRepository]的onLoadFromNetwork()中。
 *
 * @see ResultAdapter
 */
open class ListResultAdapter<M, R : Result<M>>(val callback: DoraListCallback<M>) : DoraCallback<R>() {

    /**
     * 适配[dora.http.DoraListCallback]成功的回调。
     */
    override fun onSuccess(model: R) {
        callback.onSuccess(if (model.getRealModel() != null &&
                (model.getRealModel() is MutableList<*>)) model.getRealModel() as MutableList<M>
        else arrayListOf())
    }

    /**
     * 适配[dora.http.DoraListCallback]失败的回调。
     */
    override fun onFailure(msg: String) {
        callback.onFailure(msg)
    }
}