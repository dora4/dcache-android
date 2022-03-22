package dora.cache.data.adapter

import dora.http.DoraCallback
import dora.http.DoraListCallback

class ListResultAdapter<M, R : Result<M>>(val callback: DoraListCallback<M>) : DoraCallback<R>() {

    override fun onSuccess(model: R) {
        callback.onSuccess(model.getRealModel() as List<M>)
    }
    override fun onFailure(code: Int, msg: String?) {
        callback.onFailure(code, msg)
    }
}