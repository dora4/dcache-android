package dora.http.coroutine

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor

class DoraCoroutineContext : AbstractCoroutineContextElement
        (ContinuationInterceptor), ContinuationInterceptor {

    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
        return ThreadSwitchContinuation(continuation.context.fold(continuation) { continuation, element ->
            if (element != this && element is ContinuationInterceptor) {
                element.interceptContinuation(continuation)
            } else continuation
        })
    }
}