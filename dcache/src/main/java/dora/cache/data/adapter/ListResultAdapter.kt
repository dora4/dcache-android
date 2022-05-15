package dora.cache.data.adapter

import dora.http.DoraCallback
import dora.http.DoraListCallback

class ListResultAdapter<M, R : Result<M>>(val callback: DoraListCallback<M>) : DoraCallback<R>() {

    override fun onSuccess(model: R) {
        callback.onSuccess(if (model.getRealModel() != null &&
                (model.getRealModel() is MutableList<*>)) model.getRealModel() as MutableList<M> else arrayListOf())
    }
    override fun onFailure(msg: String) {
        callback.onFailure(msg)
    }
}