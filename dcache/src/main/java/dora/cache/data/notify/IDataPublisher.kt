package dora.cache.data.notify

import androidx.annotation.CallSuper
import androidx.lifecycle.MutableLiveData

interface IDataPublisher<M> {

    fun setSubscriber(subscriber: IDataSubscriber<M>)

    @CallSuper
    fun send(type: String, data: M?)

    fun receive(type: String, liveData: MutableLiveData<M?>)

    fun getLiveData() : MutableLiveData<M?>

    companion object {
        @JvmStatic
        val DEFAULT = object : DataPublisher<Any>() {
            override fun receive(type: String, liveData: MutableLiveData<Any?>) {
            }
        }
    }
}