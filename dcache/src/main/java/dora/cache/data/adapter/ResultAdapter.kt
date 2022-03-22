package dora.cache.data.adapter

import dora.http.DoraCallback

class ResultAdapter<M, R : Result<M>>(val callback: DoraCallback<M>) : DoraCallback<R>() {

    override fun onSuccess(model: R) {
        callback.onSuccess(model.getRealModel())
    }
    override fun onFailure(code: Int, msg: String?) {
        callback.onFailure(code, msg)
    }
}