package dora.http.coroutine.flow

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlinx.coroutines.flow.Flow
import retrofit2.CallAdapter
import retrofit2.Response
import retrofit2.Retrofit

/**
 * A Retrofit CallAdapter.Factory that adapts API return types into Kotlin Flow.
 * 简体中文：Retrofit 的 CallAdapter.Factory，用于将接口返回类型适配为 Kotlin Flow。
 *
 * Supports the following return types:
 *  - Flow<Foo>
 *  - Flow<Response<Foo>>
 * 简体中文：支持如下两种返回形式：
 *  - Flow<Foo>
 *  - Flow<Response<Foo>>
 *
 * If the API method returns a Flow type, this factory will provide a proper CallAdapter.
 * 简体中文：若接口返回类型匹配 Flow，则本工厂会提供相应的 CallAdapter。
 */
class FlowCallAdapterFactory : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (Flow::class.java != getRawType(returnType)) {
            return null
        }

        if (returnType !is ParameterizedType) {
            throw IllegalStateException(
                "Flow return type must be parameterized as Flow<Foo>"
            )
        }

        val responseType = getParameterUpperBound(0, returnType)
        val rawDeferredType = getRawType(responseType)

        return if (rawDeferredType == Response::class.java) {
            if (responseType !is ParameterizedType) {
                throw IllegalStateException(
                    "Response must be parameterized as Response<Foo> or Response<out Foo>"
                )
            }
            ResponseCallAdapter<Any>(getParameterUpperBound(0, responseType))
        } else {
            BodyCallAdapter<Any>(responseType)
        }
    }
}