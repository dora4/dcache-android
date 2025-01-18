package dora.http.coroutine

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor

class DoraCoroutineContext : AbstractCoroutineContextElement
        (ContinuationInterceptor), ContinuationInterceptor {

    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
        return ThreadSwitchContinuation(continuation.context.fold(continuation) { con, element ->
            if (element != this && element is ContinuationInterceptor) {
                element.interceptContinuation(con)
            } else con
        })
    }
}