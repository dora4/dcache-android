package dora.cache.data.notify

import androidx.annotation.CallSuper
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.ConcurrentHashMap

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

            private val map = ConcurrentHashMap<Class<*>, MutableList<*>>()

            override fun receive(modelType: Class<*>, liveData: MutableLiveData<MutableList<*>>) {
                map[modelType] = liveData.value as MutableList<*>
            }

            override fun getLastValue(modelType: Class<*>): MutableList<*>? {
                return map[modelType]
            }
        }
    }
}