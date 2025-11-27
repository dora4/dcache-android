package dora.http.coroutine

import kotlin.Result
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * A simple implementation of Continuation, often used as a placeholder or
 * default callback in coroutine-related operations.
 * 简体中文：一个简单的 Continuation 实现，用于在某些场景下作为占位或默认回调使用。
 *
 * @property context The coroutine context, defaults to EmptyCoroutineContext. 简体中文：协程上下文，默
 * 认为 EmptyCoroutineContext
 */
class ContextContinuation(override val context: CoroutineContext = EmptyCoroutineContext)
    : Continuation<Unit>  {

    override fun resumeWith(result: Result<Unit>) {
    }
}