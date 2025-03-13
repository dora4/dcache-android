package dora.cache.data.notify

interface IListDataSubscriber<M> {

    fun subscribe(publisher: IListDataPublisher<M>)

    fun relay(type: String, publisher: IListDataPublisher<M>)
}