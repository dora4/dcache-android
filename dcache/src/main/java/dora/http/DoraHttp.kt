package dora.http

import android.app.Activity
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import dora.cache.data.adapter.ResultAdapter
import dora.cache.factory.CacheHolderFactory
import dora.cache.repository.BaseRepository
import dora.http.coroutine.ContextContinuation
import dora.http.coroutine.DoraCoroutineContext
import dora.http.exception.DoraHttpException
import dora.http.retrofit.ApiService
import dora.http.retrofit.RetrofitManager
import dora.http.rx.RxTransformer
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
    operator fun <T : ApiService> DoraHttp.get(clazz: KClass<T>): T {
        return RetrofitManager.getService(clazz.java)
    }

    fun netScope(activity: Activity, block: suspend () -> Unit) {
        block.startCoroutine(ContextContinuation(DoraCoroutineContext(activity)))
    }

    fun netScope(fragment: Fragment, block: suspend () -> Unit) {
        block.startCoroutine(ContextContinuation(DoraCoroutineContext(fragment.requireActivity())))
    }

    fun netScope(dialog: Dialog, block: suspend () -> Unit) {
        if (dialog.ownerActivity != null) {
            block.startCoroutine(ContextContinuation(DoraCoroutineContext(dialog.ownerActivity!!)))
        }
    }

    fun <M, F : CacheHolderFactory<M>> netScope(repository: BaseRepository<M, F>,
                                                block: suspend () -> Unit) {
        repository.viewModelScope.launch(DoraCoroutineContext(repository.context),
            CoroutineStart.DEFAULT) { block() }
    }

    fun Activity.net(block: suspend () -> Unit) {
        block.startCoroutine(ContextContinuation(DoraCoroutineContext(this)))
    }

    fun Fragment.net(block: suspend () -> Unit) {
        block.startCoroutine(ContextContinuation(DoraCoroutineContext(requireActivity())))
    }

    fun Dialog.net(block: suspend () -> Unit) {
        if (ownerActivity != null) {
            block.startCoroutine(ContextContinuation(DoraCoroutineContext(ownerActivity!!)))
        }
    }

    fun <M, F : CacheHolderFactory<M>> BaseRepository<M, F>.net(block: suspend () -> Unit) {
        viewModelScope.launch(DoraCoroutineContext(context), CoroutineStart.DEFAULT) { block() }
    }

    /**
     * Can be used within the net scope to convert the request result into a `[DoraCallback]`.
     * 简体中文：在net作用域下使用，可将请求结果[DoraCallback]进行转换。
     */
    suspend fun <T, R : dora.cache.data.adapter.Result<T>> callback(
        call: Call<T>, success: (model: T) -> Unit, failure: ((msg: String)
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
     * Can be used within the net scope to convert the request result into a `[DoraListCallback]`.
     * 简体中文：在net作用域下使用，可将请求结果[DoraListCallback]进行转换。
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
     * Can be used within the net scope, where a request failure throws an exception, and the lock
     * is automatically released upon exiting the block. Unlike `request`, manual release is not
     * required.
     * 简体中文：在net作用域下使用，请求失败抛出异常，出块则自动释放锁，和request不同的是，无需手动释放。
     */
    suspend fun <T> api(apiMethod: ()-> Call<T>) = suspendCoroutine<T> {
        val data = apiMethod()
        data.enqueue(object : DoraCallback<T>() {
            override fun onSuccess(model: T) {
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
    suspend inline fun <reified T : ApiService, M> api(clazz: KClass<T>, crossinline apiMethod: T.() -> Call<M>): M? {
        val service = DoraHttp[clazz]
        val data = service.apiMethod()
        return suspendCoroutine<M?> {
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
     * Can be used within the net scope, where a request failure returns a null value, and the lock
     * is automatically released upon exiting the block. Unlike `request`, manual release is not
     * required.
     * 简体中文：在net作用域下使用，请求失败返回空值，出块则自动释放锁，和request不同的是，无需手动释放。
     */
    suspend fun <T> result(apiMethod: ()-> Call<T>) = suspendCoroutine<T?> {
        val data = apiMethod()
        data.enqueue(object : DoraCallback<T?>() {
            override fun onSuccess(model: T?) {
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
    suspend inline fun <reified T : ApiService, M> result(clazz: KClass<T>, crossinline apiMethod: T.() -> Call<M>): M? {
        val service = DoraHttp[clazz]
        val data = service.apiMethod()
        return suspendCoroutine<M?> {
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
     * The RxJava approach, used within the net scope, returns a null value on request failure, and
     * automatically releases the lock upon exiting the block. Unlike `request`, manual release is
     * not required.
     * 简体中文：RxJava的写法，在net作用域下使用，请求失败返回空值，出块则自动释放锁，和request不同的是，无需手动释放。
     */
    inline fun <reified T : ApiService, M : Any> rxResult(crossinline apiMethod: T.()-> Observable<M>): M? {
        val service = DoraHttp[T::class]
        return runBlocking {
            suspendCoroutine<M?> {
                val data = service.apiMethod()
                RxTransformer.doApiObserver(data, object : Observer<M> {
                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onNext(t: M) {
                        it.resume(t)
                    }

                    override fun onError(e: Throwable) {
                        it.resumeWith(Result.success(null))
                    }

                    override fun onComplete() {
                    }
                })
            }
        }
    }

    /**
     * Simulate the concept of locks in threads within the DoraHttp coroutine.
     * 简体中文：模拟DoraHttp协程中类似线程中锁的概念。
     */
    interface Lock<T> {
        fun releaseLock(returnVal : T)
    }

    /**
     * Net lock, used to unblock the `request` function within the net scope.
     * 简体中文：net锁，用于解除net作用域下request函数的阻塞。
     */
    class NetLock<T>(private val continuation: Continuation<T>) : Lock<T> {

        override fun releaseLock(returnVal: T) {
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
    suspend fun <T> request(block: (lock: NetLock<T>) -> Unit) = suspendCoroutine<T> {
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
     * 简体中文：便捷创建创建MultipartBody.Part对象。
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