package dora.cache.data.notify

interface IDataSubscriber<M> {

    fun subscribe(publisher: IDataPublisher<M>)

    fun relay(modelType: Class<*>, publisher: IDataPublisher<M>)
}