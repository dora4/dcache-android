package dora.http.coroutine

import android.os.Handler
import android.os.Looper
import kotlin.Result
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext

class ThreadSwitchContinuation<T>(private val continuation:
    Continuation<T>) : Continuation<T> {

    override val context: CoroutineContext = continuation.context

    override fun resumeWith(result: Result<T>) {
        Handler(Looper.getMainLooper()).post {
            continuation.resumeWith(result)
        }
    }
}