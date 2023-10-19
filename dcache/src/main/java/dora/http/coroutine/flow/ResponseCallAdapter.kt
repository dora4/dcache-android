package dora.http.coroutine.flow

import java.lang.reflect.Type
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.HttpException
import retrofit2.Response

internal class ResponseCallAdapter<T>(
    private val responseType: Type
) : CallAdapter<T, Flow<Response<T>>> {

    override fun responseType(): Type =
        responseType

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun adapt(call: Call<T>): Flow<Response<T>> = flow {
        emit(suspendCancellableCoroutine { continuation ->
            call.registerCallback(continuation) { response ->
                continuation.resumeWith(kotlin.runCatching {
                    if (response.isSuccessful) {
                        response
                    } else {
                        throw HttpException(response)
                    }
                })
            }

            call.registerOnCancellation(continuation)
        })
    }
}