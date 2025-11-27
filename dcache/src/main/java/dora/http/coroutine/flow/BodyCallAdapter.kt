package dora.http.coroutine.flow

import java.lang.reflect.Type
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.HttpException

class BodyCallAdapter<T>(
    private val responseType: Type
) : CallAdapter<T, Flow<T>> {

    override fun responseType(): Type =
        responseType

    @OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.InternalCoroutinesApi::class)
    override fun adapt(call: Call<T>): Flow<T> = flow {
        emit(suspendCancellableCoroutine { continuation ->
            call.registerCallback(continuation) { response ->
                continuation.resumeWith(kotlin.runCatching {
                    if (response.isSuccessful) {
                        response.body() ?: throw NullPointerException("Response body is null: $response")
                    } else {
                        throw HttpException(response)
                    }
                })
            }

            call.registerOnCancellation(continuation)
        })
    }
}