package dora.rx;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class RxTransformer {

    public static <T> void doApi(Observable<T> observable, Consumer<T> consumer) {
        observable.observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(Observable.<T>empty())
                .subscribe(consumer);
    }

    public static <T> void doApi(Observable<T> observable, Observer<T> observer) {
        observable.observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(Observable.<T>empty())
                .subscribe(observer);
    }
}
