package dora.http

import android.app.Activity
import androidx.fragment.app.Fragment
import retrofit2.Call
import kotlin.coroutines.resume
import kotlin.coroutines.startCoroutine
import kotlin.coroutines.suspendCoroutine

object DoraHttp {

    fun netScope(activity: Activity, block: suspend () -> Unit) {
        block.startCoroutine(ContextContinuation(DoraCoroutineContext(activity)))
    }

    fun netScope(fragment: Fragment, block: suspend () -> Unit) {
        block.startCoroutine(ContextContinuation(DoraCoroutineContext(fragment.requireActivity())))
    }

    fun Activity.net(block: suspend () -> Unit) {
        block.startCoroutine(ContextContinuation(DoraCoroutineContext(this)))
    }

    fun Fragment.net(block: suspend () -> Unit) {
        block.startCoroutine(ContextContinuation(DoraCoroutineContext(requireActivity())))
    }

    /**
     * 请求失败抛异常。
     */
    suspend fun <T> api(apiMethod: ()-> Call<T>) = suspendCoroutine<T> {
        val data = apiMethod()
        data.enqueue(object : DoraCallback<T>() {
            override fun onSuccess(data: T) {
                it.resume(data)
            }

            override fun onFailure(code: Int, msg: String?) {
                it.resumeWith(Result.failure(DoraHttpException(data.request(), msg)))
            }
        })
    }

    /**
     * 请求失败返回空。
     */
    suspend fun <T> result(apiMethod: ()-> Call<T>) = suspendCoroutine<T?> {
        val data = apiMethod()
        data.enqueue(object : DoraCallback<T?>() {
            override fun onSuccess(data: T?) {
                it.resume(data)
            }

            override fun onFailure(code: Int, msg: String?) {
                it.resumeWith(Result.success(null))
            }
        })
    }

    /**
     * 自己执行网络请求代码。
     */
    suspend fun <T> request(block: () -> T) = suspendCoroutine<T> {
        it.resume(block())
    }
}