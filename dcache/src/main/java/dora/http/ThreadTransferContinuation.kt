package dora.http

import android.app.Activity
import kotlin.Result
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext

class ThreadTransferContinuation<T>(private val activity: Activity, private val continuation: Continuation<T>) : Continuation<T> {

    override val context: CoroutineContext = continuation.context

    override fun resumeWith(result: Result<T>) {
        activity.runOnUiThread {
            continuation.resumeWith(result)
        }
    }
}