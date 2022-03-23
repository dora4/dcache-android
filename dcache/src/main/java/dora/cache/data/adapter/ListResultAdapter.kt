package dora.cache.data.adapter

import dora.http.DoraCallback
import dora.http.DoraListCallback

class ListResultAdapter<M, R : Result<M>>(val callback: DoraListCallback<M>) : DoraCallback<R>() {

    override fun onSuccess(model: R) {
        callback.onSuccess(if (model.getRealModel() != null &&
                (model.getRealModel() is List<*>)) model.getRealModel() as List<M> else arrayListOf())
    }
    override fun onFailure(code: Int, msg: String?) {
        callback.onFailure(code, msg)
    }
}