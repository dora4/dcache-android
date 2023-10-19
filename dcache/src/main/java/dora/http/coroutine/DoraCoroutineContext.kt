package dora.http.coroutine

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor

class DoraCoroutineContext(private val context: Context) : AbstractCoroutineContextElement
        (ContinuationInterceptor), ContinuationInterceptor {

    private fun convertActivity(context: Context) : Activity? {
        if (context is Activity) {
            return context
        } else if (context is ContextWrapper) {
            if (context.baseContext is Activity) {
                return context.baseContext as Activity
            }
        }
        return null
    }

    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
        val activity = convertActivity(context)
        return if (activity != null) {
            ThreadSwitchContinuation(activity, continuation.context.fold(continuation) { continuation, element ->
                if (element != this && element is ContinuationInterceptor) {
                    element.interceptContinuation(continuation)
                } else continuation
            })
        } else continuation
    }
}