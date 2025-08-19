package dora.cache

import io.reactivex.Flowable

class FlowableChain<T> {

    private var lastFlowable: Flowable<T> = Flowable.empty()
    private var lastListFlowable: Flowable<MutableList<T>> = Flowable.empty()

    /**
     * Add a [Flowable] to the chain and return the chain itself so calls can be
     * chained fluently.
     */
    fun add(next: Flowable<T>): FlowableChain<T> {
        lastFlowable = lastFlowable.concatWith(next)
        return this
    }

    /**
     * Add a list [Flowable] to the chain and return the chain for fluent calls.
     */
    fun addList(next: Flowable<MutableList<T>>): FlowableChain<T> {
        lastListFlowable = lastListFlowable.concatWith(next)
        return this
    }

    /**
     * Get the concatenated [Flowable] in the order items were added.
     */
    fun flowable(): Flowable<T> = lastFlowable

    /**
     * Get the concatenated list [Flowable].
     */
    fun listFlowable(): Flowable<MutableList<T>> = lastListFlowable

    /**
     * Clears the current chain and returns itself for further configuration.
     */
    fun reset(): FlowableChain<T> {
        lastFlowable = Flowable.empty()
        lastListFlowable = Flowable.empty()
        return this
    }
}
