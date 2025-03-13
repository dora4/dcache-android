package dora.cache.data.notify

import androidx.annotation.CallSuper
import androidx.lifecycle.MutableLiveData

interface IListDataPublisher<M> {

    fun setSubscriber(subscriber: IListDataSubscriber<M>)

    @CallSuper
    fun send(type: String, data: MutableList<M>)

    fun receive(type: String, liveData: MutableLiveData<MutableList<M>>)

    fun getListLiveData() : MutableLiveData<MutableList<M>>

    companion object {
        @JvmStatic
        val DEFAULT = object : ListDataPublisher<Any>() {
            override fun receive(type: String, liveData: MutableLiveData<MutableList<Any>>) {
            }
        }
    }
}