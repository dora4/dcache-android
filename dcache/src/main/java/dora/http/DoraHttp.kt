package dora.http

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dora.cache.data.adapter.ResultAdapter
import dora.http.coroutine.ContextContinuation
import dora.http.coroutine.DoraCoroutineContext
import dora.http.exception.DoraHttpException
import dora.http.retrofit.ApiService
import dora.http.retrofit.RetrofitManager
import dora.http.rx.RxUtils
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import java.io.File
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.startCoroutine
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KClass

/**
 * Used to initiate network requests and includes some commonly used utility methods for network
 * requests.
 * 简体中文：用于发起网络请求，以及包含一些网络请求常用的工具方法。
 */
object DoraHttp {

    @JvmSynthetic
    operator fun <S : ApiService> DoraHttp.get(clazz: KClass<S>): S {
        return RetrofitManager.getService(clazz.java)
    }

    @JvmSynthetic
    fun net(block: suspend () -> Unit) {
        block.startCoroutine(ContextContinuation(DoraCoroutineContext()))
    }

    /**
     * Can be used within the net scope to convert the request result into a `[DoraCallback]`.
     * 简体中文：在net作用域下使用，可将请求结果[DoraCallback]进行转换。
     */
    suspend fun <M, R : dora.cache.data.adapter.Result<M>> callback(
        call: Call<M>, success: (model: M) -> Unit, failure: ((msg: String)
            -> Unit)? = null, realType: Class<R>? = null) = suspendCoroutine {
        if (realType != null) {
            call.enqueue(ResultAdapter<M, R>(object : DoraCallback<M>() {
                override fun onSuccess(model: M) {
                    success(model)
                    it.resume(model)
                }

                override fun onFailure(msg: String) {
                    failure?.invoke(msg)
                    it.resume(null)
                }
            }) as DoraCallback<M>)
        } else {
            call.enqueue(object : DoraCallback<M>() {
                override fun onSuccess(model: M) {
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
     * Can be used within the net scope to convert the request result into a `[DoraListCallback]`.
     * 简体中文：在net作用域下使用，可将请求结果[DoraListCallback]进行转换。
     */
    suspend fun <M> listCallback(call: Call<MutableList<M>>, success: (model: MutableList<M>)
        -> Unit, failure: ((msg: String) -> Unit)? = null) = suspendCoroutine<MutableList<M>> {
        call.enqueue(object : DoraListCallback<M>() {

            override fun onSuccess(models: MutableList<M>) {
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
     * Can be used within the net scope, where a request failure throws an exception, and the lock
     * is automatically released upon exiting the block. Unlike `request`, manual release is not
     * required.
     * 简体中文：在net作用域下使用，请求失败抛出异常，出块则自动释放锁，和request不同的是，无需手动释放。
     */
    suspend fun <M> api(apiMethod: ()-> Call<M>) = suspendCoroutine<M> {
        val data = apiMethod()
        data.enqueue(object : DoraCallback<M>() {
            override fun onSuccess(model: M) {
                it.resume(model)
            }

            override fun onFailure(msg: String) {
                it.resumeWith(Result.failure(DoraHttpException(data.request(), msg)))
            }
        })
    }

    /**
     * Can be used within the net scope, where a request failure throws an exception, and the lock
     * is automatically released upon exiting the block. Unlike `request`, manual release is not
     * required.
     * 简体中文：在net作用域下使用，请求失败抛出异常，出块则自动释放锁，和request不同的是，无需手动释放。
     */
    suspend inline fun <reified S : ApiService, M> api(clazz: KClass<S>, crossinline apiMethod: S.() -> Call<M>): M? {
        val service = DoraHttp[clazz]
        return suspendCoroutine<M?> {
            val data = service.apiMethod()
            data.enqueue(object : DoraCallback<M>() {
                override fun onSuccess(model: M) {
                    it.resume(model)
                }

                override fun onFailure(msg: String) {
                    it.resumeWith(Result.failure(DoraHttpException(data.request(), msg)))
                }
            })
        }
    }

    /**
     * The RxJava approach, used within the net scope, throws an exception on request failure, and
     * automatically releases the lock upon exiting the block. Unlike `request`, manual release is
     * not required.
     * 简体中文：RxJava的写法，在net作用域下使用，请求失败抛出异常，出块则自动释放锁，和request不同的是，无需手动释放。
     */
    suspend fun <M> rxApi(apiMethod: ()-> Observable<M>) = suspendCoroutine<M> {
        val data = apiMethod()
        RxUtils.doApiObserver(data, object : Observer<M> {
            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(model: M & Any) {
                it.resume(model)
            }

            override fun onError(e: Throwable) {
                it.resumeWith(Result.failure(DoraHttpException(e.toString())))
            }

            override fun onComplete() {
            }
        })
    }

    /**
     * The RxJava approach, used within the net scope, throws an exception on request failure, and
     * automatically releases the lock upon exiting the block. Unlike `request`, manual release is
     * not required.
     * 简体中文：RxJava的写法，在net作用域下使用，请求失败抛出异常，出块则自动释放锁，和request不同的是，无需手动释放。
     */
    suspend inline fun <reified S : ApiService, M> rxApi(clazz: KClass<S>, crossinline apiMethod: S.() -> Observable<M>): M? {
        val service = DoraHttp[clazz]
        return suspendCoroutine<M?> {
            val data = service.apiMethod()
            RxUtils.doApiObserver(data, object : Observer<M> {
                override fun onSubscribe(d: Disposable) {
                }

                override fun onNext(model: M & Any) {
                    it.resume(model)
                }

                override fun onError(e: Throwable) {
                    it.resumeWith(Result.failure(DoraHttpException(e.toString())))
                }

                override fun onComplete() {
                }
            })
        }
    }

    /**
     * Can be used within the net scope, where a request failure returns a null value, and the lock
     * is automatically released upon exiting the block. Unlike `request`, manual release is not
     * required.
     * 简体中文：在net作用域下使用，请求失败返回空值，出块则自动释放锁，和request不同的是，无需手动释放。
     */
    suspend fun <M> result(apiMethod: ()-> Call<M>) = suspendCoroutine<M?> {
        val data = apiMethod()
        data.enqueue(object : DoraCallback<M?>() {
            override fun onSuccess(model: M?) {
                it.resume(model)
            }

            override fun onFailure(msg: String) {
                it.resumeWith(Result.success(null))
            }
        })
    }

    /**
     * Can be used within the net scope, where a request failure returns a null value, and the lock
     * is automatically released upon exiting the block. Unlike `request`, manual release is not
     * required.
     * 简体中文：在net作用域下使用，请求失败返回空值，出块则自动释放锁，和request不同的是，无需手动释放。
     */
    suspend inline fun <reified S : ApiService, M> result(clazz: KClass<S>, crossinline apiMethod: S.() -> Call<M>): M? {
        val service = DoraHttp[clazz]
        return suspendCoroutine<M?> {
            val data = service.apiMethod()
            data.enqueue(object : DoraCallback<M>() {
                override fun onSuccess(model: M) {
                    it.resume(model)
                }

                override fun onFailure(msg: String) {
                    it.resumeWith(Result.success(null))
                }
            })
        }
    }

    /**
     * The RxJava approach, used within the net scope, returns a null value on request failure, and
     * automatically releases the lock upon exiting the block. Unlike `request`, manual release is
     * not required.
     * 简体中文：RxJava的写法，在net作用域下使用，请求失败返回空值，出块则自动释放锁，和request不同的是，无需手动释放。
     */
    suspend fun <M> rxResult(apiMethod: ()-> Observable<M>) = suspendCoroutine<M?> {
        val data = apiMethod()
        RxUtils.doApiObserver(data, object : Observer<M> {
            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(model: M & Any) {
                it.resume(model)
            }

            override fun onError(e: Throwable) {
                it.resumeWith(Result.success(null))
            }

            override fun onComplete() {
            }
        })
    }

    /**
     * The RxJava approach, used within the net scope, returns a null value on request failure, and
     * automatically releases the lock upon exiting the block. Unlike `request`, manual release is
     * not required.
     * 简体中文：RxJava的写法，在net作用域下使用，请求失败返回空值，出块则自动释放锁，和request不同的是，无需手动释放。
     */
    suspend inline fun <reified S : ApiService, M> rxResult(clazz: KClass<S>, crossinline apiMethod: S.() -> Observable<M>): M? {
        return suspendCoroutine<M?> {
            val service = DoraHttp[clazz]
            val data = service.apiMethod()
            RxUtils.doApiObserver(data, object : Observer<M> {
                override fun onSubscribe(d: Disposable) {
                }

                override fun onNext(model: M & Any) {
                    it.resume(model)
                }

                override fun onError(e: Throwable) {
                    it.resumeWith(Result.success(null))
                }

                override fun onComplete() {
                }
            })
        }
    }

    /**
     * Simulate the concept of locks in threads within the DoraHttp coroutine.
     * 简体中文：模拟DoraHttp协程中类似线程中锁的概念。
     */
    interface Lock<V> {
        fun releaseLock(returnVal : V)
    }

    /**
     * Net lock, used to unblock the `request` function within the net scope.
     * 简体中文：net锁，用于解除net作用域下request函数的阻塞。
     */
    class NetLock<V>(private val continuation: Continuation<V>) : Lock<V> {

        override fun releaseLock(returnVal: V) {
            continuation.resume(returnVal)
        }
    }

    /**
     * Execute the network request code by using it within the net scope. After completion (usually
     * in the `onSuccess` or `onError` callback), please call `lock.releaseLock()` to allow
     * subsequent code to execute. Additionally, you can specify the return result of the
     * higher-order function for the request; after releasing the lock, this can be assigned to a
     * variable as the result of the request function. This wraps the coroutine method in a
     * compatible way, eliminating the need to manually define coroutine methods for network data
     * requests.
     * 简体中文：自己执行网络请求代码，在net作用域下使用，执行完成（通常为onSuccess或onError的回调）后请调用
     * lock.releaseLock()，让后面的代码得以执行，另外可以指定request高阶函数的返回结果，释放锁后
     * 将可以作为request函数的执行结果赋值给变量。包装协程方法，以一种兼容的方式，无需手动定义协程方法
     * 去请求网络数据。
     */
    suspend fun <V> request(block: (lock: NetLock<V>) -> Unit) = suspendCoroutine<V> {
        try {
            val lock = NetLock(it)
            block(lock)
        } catch (e: Exception) {
            it.resumeWith(Result.failure(e))
        }
    }

    /**
     * You wrap a regular API interface into an interface that returns a Flow value.
     * 简体中文：你将一个普通的api接口包装成Flow返回值的接口。
     */
    suspend fun <T> flowResult(requestBlock: suspend () -> T,
                               loadingBlock: ((Boolean) -> Unit)? = null,
                               errorBlock: ((String) -> Unit)? = null,
    ) : Flow<T> {
        return flow {
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
     * Wrap a regular API interface into an interface that returns a Flow value.
     * 简体中文：将一个普通的api接口包装成Flow返回值的接口。
     */
    suspend fun <T> flowResult(lifecycle: Lifecycle,
                               lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
                               requestBlock: suspend () -> T,
                               loadingBlock: ((Boolean) -> Unit)? = null,
                               errorBlock: ((String) -> Unit)? = null,
    ) : Flow<T> {
        return flowResult(requestBlock, loadingBlock, errorBlock).flowWithLifecycle(lifecycle, lifecycleState)
    }

    /**
     * Directly initiate a Flow request. If you are using the internal framework's
     * `[dora.http.retrofit.RetrofitManager]`, you need to enable the Flow configuration option
     * `[dora.http.retrofit.RetrofitManager.Config.useFlow]` to `true`.
     * 简体中文：直接发起Flow请求，如果你使用框架内部的[dora.http.retrofit.RetrofitManager]的话，需要开启
     * [dora.http.retrofit.RetrofitManager]的flow配置选项
     * [dora.http.retrofit.RetrofitManager.Config.useFlow]为true。
     */
    suspend fun <T> flowRequest(requestBlock: () -> Flow<T>,
                                successBlock: ((T) -> Unit),
                                failureBlock: ((String) -> Unit)? = null,
                                loadingBlock: ((Boolean) -> Unit)? = null) {
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

    /**
     * Directly initiate a Flow request. If you are using the internal framework's
     * `[dora.http.retrofit.RetrofitManager]`, you need to enable the Flow configuration option
     * `[dora.http.retrofit.RetrofitManager.Config.useFlow]` to `true`.
     * 简体中文：直接发起Flow请求，如果你使用框架内部的[dora.http.retrofit.RetrofitManager]的话，需要开启
     * [dora.http.retrofit.RetrofitManager]的flow配置选项
     * [dora.http.retrofit.RetrofitManager.Config.useFlow]
     * 为true。
     */
    suspend fun <T> flowRequest(
                lifecycle: Lifecycle,
                lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
                requestBlock: () -> Flow<T>,
                successBlock: ((T) -> Unit),
                failureBlock: ((String) -> Unit)? = null,
                loadingBlock: ((Boolean) -> Unit)? = null
    ) {
       flowRequest(requestBlock = {requestBlock().flowWithLifecycle(lifecycle, lifecycleState)},
           successBlock, failureBlock, loadingBlock)
    }

    /**
     * Bind Flow to the lifecycle.
     * 简体中文：Flow与生命周期绑定。
     */
    inline fun <T> Flow<T>.observeWithLifecycle(
        activity: AppCompatActivity,
        activeState: Lifecycle.State = Lifecycle.State.STARTED,
        crossinline action: suspend ((value: T) -> Unit)
    ): Job {
        val job = activity.lifecycleScope.launch {
            flowWithLifecycle(activity.lifecycle, activeState).collect { value -> action(value) }
        }
        return job
    }

    /**
     * Bind Flow to the lifecycle.
     * 简体中文：Flow与生命周期绑定。
     */
    inline fun <T> Flow<T>.observeWithLifecycle(
        fragment: Fragment,
        activeState: Lifecycle.State = Lifecycle.State.STARTED,
        crossinline action: suspend ((value: T) -> Unit)
    ): Job {
        val job = fragment.viewLifecycleOwner.lifecycleScope.launch {
            flowWithLifecycle(fragment.viewLifecycleOwner.lifecycle, activeState).collect { value ->
                action(value)
            }
        }
        return job
    }

    /**
     * Conveniently create `MultipartBody.Part` objects.
     * 简体中文：便捷创建MultipartBody.Part对象。
     */
    fun createFilePart(file: File, partName: String = "file", mimeType: String = "*/*") : MultipartBody.Part {
        val requestFile: RequestBody = file
            .asRequestBody(
                mimeType.toMediaTypeOrNull()
            )
        return MultipartBody.Part.createFormData(
            partName,
            file.name,
            requestFile
        )
    }
}