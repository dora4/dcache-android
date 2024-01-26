package dora.http

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import dora.cache.data.adapter.ResultAdapter
import dora.cache.factory.CacheHolderFactory
import dora.cache.repository.BaseRepository
import dora.http.coroutine.ContextContinuation
import dora.http.coroutine.DoraCoroutineContext
import dora.http.exception.DoraHttpException
import dora.http.rx.RxTransformer
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
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

    fun <M, F : CacheHolderFactory<M>> netScope(repository: BaseRepository<M, F>, block: suspend () -> Unit) {
        repository.viewModelScope.launch(DoraCoroutineContext(repository.context), CoroutineStart.DEFAULT) {
            block()
        }
    }

    fun Activity.net(block: suspend () -> Unit) {
        block.startCoroutine(ContextContinuation(DoraCoroutineContext(this)))
    }

    fun Fragment.net(block: suspend () -> Unit) {
        block.startCoroutine(ContextContinuation(DoraCoroutineContext(requireActivity())))
    }

    fun <M, F : CacheHolderFactory<M>> BaseRepository<M, F>.net(block: suspend () -> Unit) {
        viewModelScope.launch(DoraCoroutineContext(context), CoroutineStart.DEFAULT) {
            block()
        }
    }

    /**
     * 在net作用域下使用，可将请求结果[DoraCallback]进行转换。
     */
    suspend fun <T, R : dora.cache.data.adapter.Result<T>> callback(call: Call<T>, success: (model: T) -> Unit, failure: ((msg: String)
            -> Unit)? = null, realType: Class<R>? = null) = suspendCoroutine {
        if (realType != null) {
            call.enqueue(ResultAdapter<T, R>(object : DoraCallback<T>() {
                override fun onSuccess(model: T) {
                    success(model)
                    it.resume(model)
                }

                override fun onFailure(msg: String) {
                    failure?.invoke(msg)
                    it.resume(null)
                }
            }) as DoraCallback<T>)
        } else {
            call.enqueue(object : DoraCallback<T>() {
                override fun onSuccess(model: T) {
                    success(model)
                    it.resume(model)
                }

                override fun onFailure(msg: String) {
                    failure?.invoke(msg)
                    it.resume(null)
                }
            })
        }
    }

    /**
     * 在net作用域下使用，可将请求结果[DoraListCallback]进行转换。
     */
    suspend fun <T> listCallback(call: Call<MutableList<T>>, success: (model: MutableList<T>)
        -> Unit, failure: ((msg: String) -> Unit)? = null) = suspendCoroutine<MutableList<T>> {
        call.enqueue(object : DoraListCallback<T>() {

            override fun onSuccess(models: MutableList<T>) {
                success(models)
                it.resume(models)
            }

            override fun onFailure(msg: String) {
                failure?.invoke(msg)
                it.resume(arrayListOf())
            }
        })
    }

    /**
     * 在net作用域下使用，请求失败抛异常，出块则自动释放锁，和request不同的是，无需手动释放。
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
     * RxJava的写法，在net作用域下使用，请求失败抛异常，出块则自动释放锁，和request不同的是，无需手动释放。
     */
    suspend fun <T : Any> rxApi(apiMethod: ()-> Observable<T>) = suspendCoroutine<T> {
        val data = apiMethod()
        RxTransformer.doApiObserver(data, object : Observer<T> {
            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(t: T) {
                it.resume(t)
            }

            override fun onError(e: Throwable) {
                it.resumeWith(Result.failure(DoraHttpException(e.toString())))
            }

            override fun onComplete() {
            }
        })
    }

    /**
     * 在net作用域下使用，请求失败返回空值，出块则自动释放锁，和request不同的是，无需手动释放。
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
     * RxJava的写法，在net作用域下使用，请求失败返回空值，出块则自动释放锁，和request不同的是，无需手动释放。
     */
    suspend fun <T : Any> rxResult(apiMethod: ()-> Observable<T>) = suspendCoroutine<T?> {
        val data = apiMethod()
        RxTransformer.doApiObserver(data, object : Observer<T> {
            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(t: T) {
                it.resume(t)
            }

            override fun onError(e: Throwable) {
                it.resumeWith(Result.success(null))
            }

            override fun onComplete() {
            }
        })
    }

    /**
     * 模拟DoraHttp协程中类似线程中锁的概念。
     */
    interface Lock<T> {
        fun releaseLock(returnVal : T)
    }

    /**
     * net锁，用于解除net作用域下request函数的阻塞。
     */
    class NetLock<T>(private val continuation: Continuation<T>) : Lock<T> {

        override fun releaseLock(returnVal: T) {
            continuation.resume(returnVal)
        }
    }

    /**
     * 自己执行网络请求代码，在net作用域下使用，执行完成（通常为onSuccess或onError的回调）后请调用
     * lock.releaseLock()，让后面的代码得以执行，另外可以指定request高阶函数的返回结果，释放锁后
     * 将可以作为request函数的执行结果赋值给变量。包装协程方法，以一种兼容的方式，无需手动定义协程方法
     * 去请求网络数据。
     */
    suspend fun <T> request(block: (lock: NetLock<T>) -> Unit) = suspendCoroutine<T> {
        try {
            val lock = NetLock(it)
            block(lock)
        } catch (e: Exception) {
            it.resumeWith(Result.failure(e))
        }
    }

    /**
     * 将一个普通的api接口包装成Flow返回值的接口。
     */
    suspend fun <T> flowResult(requestBlock: suspend () -> T,
                               loadingBlock: ((Boolean) -> Unit)? = null,
                               errorBlock: ((String) -> Unit)? = null,
    ) : Flow<T> {
        return flow {
            // 设置超时时间为10秒
            val response = withTimeout(10 * 1000) {
                requestBlock()
            }
            emit(response)
        }
            .flowOn(Dispatchers.IO)
            .onStart {
                loadingBlock?.invoke(true)
            }
            .catch { e ->
                errorBlock?.invoke(e.toString())
            }
            .onCompletion {
                loadingBlock?.invoke(false)
            }
    }

    /**
     * 直接发起Flow请求，如果你使用框架内部的[dora.http.retrofit.RetrofitManager]的话，需要开启
     * [dora.http.retrofit.RetrofitManager]的flow配置选项[dora.http.retrofit.RetrofitManager.Config.useFlow]
     * 为true。
     */
    suspend fun <T> flowRequest(requestBlock: () -> Flow<T>,
                                successBlock: ((T) -> Unit),
                                failureBlock: ((String) -> Unit)? = null,
                                loadingBlock: ((Boolean) -> Unit)? = null
    ) {
        requestBlock()
            .flowOn(Dispatchers.IO)
            .onStart {
                loadingBlock?.invoke(true)
            }
            .catch { e ->
                failureBlock?.invoke(e.toString())
            }
            .onCompletion {
                loadingBlock?.invoke(false)
            }.collect {
                successBlock(it)
            }
    }
}