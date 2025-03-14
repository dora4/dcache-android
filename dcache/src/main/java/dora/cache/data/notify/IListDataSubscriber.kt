package dora.cache.data.notify

interface IListDataSubscriber<M> {

    fun subscribe(publisher: IListDataPublisher<M>)

    fun relay(modelType: Class<*>, publisher: IListDataPublisher<M>)
}