package dora.http

import android.app.Activity
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor

class DoraCoroutineContext(private val activity: Activity) : AbstractCoroutineContextElement
        (ContinuationInterceptor), ContinuationInterceptor {

    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
        return ThreadTransferContinuation(activity, continuation.context.fold(continuation) {
            continuation, element ->
            if (element != this && element is ContinuationInterceptor) {
                element.interceptContinuation(continuation)
            } else continuation
        })
    }
}