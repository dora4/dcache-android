package dora.http.coroutine.flow

import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellableContinuation
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal fun Call<*>.registerOnCancellation(continuation: CancellableContinuation<*>) {
    continuation.invokeOnCancellation {
        try {
            cancel()
        } catch (e: Exception) {
            // Ignore cancel exception
        }
    }
}

internal fun <T> Call<T>.registerCallback(continuation: CancellableContinuation<*>, success: (response: Response<T>) -> Unit) {
    enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            success(response)
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            continuation.resumeWithException(t)
        }
    })
}