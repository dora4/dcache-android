package dora.cache.data.notify

import androidx.annotation.CallSuper
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.ConcurrentHashMap

interface IDataPublisher<M> {

    fun setSubscriber(subscriber: IDataSubscriber<M>)

    @CallSuper
    fun <T> send(modelType: Class<T>, data: T?)

    fun receive(modelType: Class<*>, liveData: MutableLiveData<*>)

    fun getLiveData(modelType: Class<*>) : MutableLiveData<*>?

    fun getLastValue(modelType: Class<*>) : Any?

    companion object {

        @JvmStatic
        val DEFAULT = object : DataPublisher<Any>() {

            private val map = ConcurrentHashMap<Class<*>, Any?>()

            override fun receive(modelType: Class<*>, liveData: MutableLiveData<*>) {
                map[modelType] = liveData.value
            }

            override fun getLastValue(modelType: Class<*>): Any? {
                return map[modelType]
            }
        }
    }
}