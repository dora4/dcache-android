package dora.http.rx

import android.annotation.SuppressLint
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.schedulers.Schedulers
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer

/**
 * RxJava transformer that provides utility methods for executing Rx functions.
 * 简体中文：RxJava变换器，提供执行Rx函数的工具方法。
 */
object RxTransformer {

    @SuppressLint("CheckResult")
    fun <M> doApiConsumer(observable: Observable<M>, consumer: Consumer<M>) {
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorResumeNext(Observable.empty())
            .subscribe(consumer)
    }

    fun <M> doApiObserver(observable: Observable<M>, observer: Observer<M>) {
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorResumeNext(Observable.empty())
            .subscribe(observer)
    }
}