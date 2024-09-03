package dora.http.rx

import android.annotation.SuppressLint
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.schedulers.Schedulers
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer

/**
 * RxJava变换器，提供执行Rx函数的工具方法。
 */
object RxTransformer {

    @SuppressLint("CheckResult")
    fun <T> doApiConsumer(observable: Observable<T>, consumer: Consumer<T>) {
        observable.observeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorResumeNext(Observable.empty())
            .subscribe(consumer)
    }

    fun <T> doApiObserver(observable: Observable<T>, observer: Observer<T>) {
        observable.observeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorResumeNext(Observable.empty())
            .subscribe(observer)
    }
}