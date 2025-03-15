package dora.cache.data.notify

import androidx.annotation.CallSuper
import androidx.lifecycle.MutableLiveData

interface IListDataPublisher<M> {

    fun setSubscriber(subscriber: IListDataSubscriber<M>)

    @CallSuper
    fun <T> send(modelType: Class<T>, data: MutableList<T>)

    fun receive(modelType: Class<*>, liveData: MutableLiveData<MutableList<*>>)

    fun getListLiveData(modelType: Class<*>) : MutableLiveData<MutableList<*>>?

    fun getLastValue(modelType: Class<*>) : MutableList<*>?

    companion object {
        
        @JvmStatic
        val DEFAULT = object : ListDataPublisher<Any>() {

            override fun receive(modelType: Class<*>, liveData: MutableLiveData<MutableList<*>>) {
                liveDataMap[modelType] = liveData
            }
        }
    }
}