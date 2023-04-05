package dora.http.rx

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.schedulers.Schedulers
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer

/**
 * RxJava转换器。
 */
object RxTransformer {
    fun <T> doApi(observable: Observable<T>, consumer: Consumer<T>?) {
        observable.observeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorResumeNext(Observable.empty())
            .subscribe(consumer)
    }

    fun <T> doApi(observable: Observable<T>, observer: Observer<T>?) {
        observable.observeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorResumeNext(Observable.empty())
            .subscribe(observer)
    }
}