package org.tima;

import org.junit.Test;
import static org.junit.Assert.*;
import org.tima.core.Observable;
import org.tima.core.Observer;
import org.tima.core.Disposable;

public class ObservableTest {
    @Test
    public void testCreateAndSubscribe() {
        Observable<String> observable = Observable.create(observer -> {
            observer.onNext("Hello");
            observer.onComplete();
        });

        final String[] result = {null};
        observable.subscribe(new Observer<String>() {
            @Override
            public void onNext(String item) {
                result[0] = item;
            }

            @Override
            public void onError(Throwable t) {
                fail("Ошибка произошла");
            }

            @Override
            public void onComplete() {
                assertEquals("Hello", result[0]);
            }
        });
    }

    @Test
    public void testDisposable() {
        Observable<String> observable = Observable.create(observer -> {
            observer.onNext("First");
            observer.onNext("Second");
            observer.onComplete();
        });

        final String[] result = {null};
        Disposable[] disposable = new Disposable[1];
        disposable[0] = observable.subscribe(new Observer<String>() {
            @Override
            public void onNext(String item) {
                result[0] = item;
                disposable[0].dispose();
            }

            @Override
            public void onError(Throwable t) {
                fail("Ошибка произошла");
            }

            @Override
            public void onComplete() {
                fail("Не должно завершиться");
            }
        });

        assertEquals("First", result[0]);
    }
}