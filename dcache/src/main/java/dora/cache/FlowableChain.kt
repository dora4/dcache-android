package dora.cache

import io.reactivex.Flowable

class FlowableChain<T> {

    private var lastFlowable: Flowable<T> = Flowable.empty()
    private var lastListFlowable: Flowable<MutableList<T>> = Flowable.empty()

    fun add(next: Flowable<T>): Flowable<T> {
        lastFlowable = lastFlowable.concatWith(next)
        return lastFlowable
    }

    fun addList(next: Flowable<MutableList<T>>): Flowable<MutableList<T>> {
        lastListFlowable = lastListFlowable.concatWith(next)
        return lastListFlowable
    }

    fun reset() {
        lastFlowable = Flowable.empty()
        lastListFlowable = Flowable.empty()
    }
}
