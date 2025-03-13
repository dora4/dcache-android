package dora.cache.data.notify

import androidx.annotation.CallSuper
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.ConcurrentHashMap

interface IDataPublisher<M> {

    fun setSubscriber(subscriber: IDataSubscriber<M>)

    @CallSuper
    fun send(type: String, data: M?)

    fun receive(type: String, liveData: MutableLiveData<M?>)

    fun getLiveData() : MutableLiveData<M?>

    companion object {
        @JvmStatic
        val DEFAULT = object : DataPublisher<Any>() {
            private val map = HashMap<String, Any?>()
            override fun receive(type: String, liveData: MutableLiveData<Any?>) {
                if (map.contains(type)) {
                    map.remove(type)
                }
                map[type] = liveData.value
            }

            fun getValue(type: String) : Any? {
                return map[type]
            }
        }
    }
}