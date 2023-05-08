package dora.http

import android.app.Activity
import androidx.fragment.app.Fragment
import dora.http.coroutine.ContextContinuation
import dora.http.coroutine.DoraCoroutineContext
import dora.http.exception.DoraHttpException
import dora.http.rx.RxTransformer
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import retrofit2.Call
import kotlin.coroutines.Continuation
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

            override fun onFailure(msg: String) {
                it.resumeWith(Result.failure(DoraHttpException(data.request(), msg)))
            }
        })
    }


    /**
     * 请求失败抛异常。
     */
    suspend fun <T> rxApi(apiMethod: ()-> Observable<T>) = suspendCoroutine<T> {
        val data = apiMethod()
        RxTransformer.doApi(data, object : Observer<T> {
            override fun onSubscribe(d: Disposable?) {
            }

            override fun onNext(t: T) {
                it.resume(t)
            }

            override fun onError(e: Throwable?) {
                it.resumeWith(Result.failure(DoraHttpException(e.toString())))
            }

            override fun onComplete() {
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

            override fun onFailure(msg: String) {
                it.resumeWith(Result.success(null))
            }
        })
    }

    /**
     * 请求失败返回空。
     */
    suspend fun <T> rxResult(apiMethod: ()-> Observable<T>) = suspendCoroutine<T?> {
        val data = apiMethod()
        RxTransformer.doApi(data, object : Observer<T> {
            override fun onSubscribe(d: Disposable?) {
            }

            override fun onNext(t: T) {
                it.resume(t)
            }

            override fun onError(e: Throwable?) {
                it.resumeWith(Result.success(null))
            }

            override fun onComplete() {
            }
        })
    }

    /**
     * 自己执行网络请求代码，执行完成请调用continuation.resume()。
     */
    suspend fun <T> request(block: (continuation: Continuation<T>) -> T) = suspendCoroutine<T> {
        try {
            block(it)
        } catch (e: Exception) {
            it.resumeWith(Result.failure(e))
        }
    }
}