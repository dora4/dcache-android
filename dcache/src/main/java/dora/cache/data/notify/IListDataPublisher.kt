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

            private val map = HashMap<String, MutableList<*>>()

            override fun receive(type: String, liveData: MutableLiveData<MutableList<Any>>) {
                if (map.contains(type)) {
                    map.remove(type)
                }
                map[type] = liveData.value as MutableList<*>
            }

            fun getValue(type: String) : MutableList<*>? {
                return map[type]
            }
        }
    }
}