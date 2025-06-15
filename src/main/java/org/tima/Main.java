package org.tima;

import org.tima.core.Disposable;
import org.tima.core.Observable;
import org.tima.core.Observer;
import org.tima.schedulers.ComputationScheduler;
import org.tima.schedulers.IOThreadScheduler;
import org.tima.schedulers.SingleThreadScheduler;

public class Main {
    public static void main(String[] args) {
        Observable<Integer> numbers = Observable.<Integer>create(observer -> {
            System.out.println("Генерация чисел на потоке: " + Thread.currentThread().getName());
            for (int i = 1; i <= 5; i++) {
                observer.onNext(i);
            }
            observer.onComplete();
        }).subscribeOn(new SingleThreadScheduler());

        Observable<Integer> squaredEvenNumbers = numbers
                .map(x -> x * x)
                .filter(x -> x % 2 == 0);

        Disposable disposable = squaredEvenNumbers.observeOn(new IOThreadScheduler())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onNext(Integer item) {
                        System.out.println("Получено: " + item + " на потоке: " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onError(Throwable t) {
                        System.err.println("Ошибка: " + t.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("Четные квадраты завершены на потоке: " + Thread.currentThread().getName());
                    }
                });

        Observable<String> flatMapped = numbers.flatMap(num ->
                Observable.<String>create(observer -> {
                    observer.onNext("Элемент " + num);
                    observer.onNext("Квадрат " + (num * num));
                    observer.onComplete();
                }).subscribeOn(new ComputationScheduler())
        );

        flatMapped.observeOn(new IOThreadScheduler()).subscribe(new Observer<String>() {
            @Override
            public void onNext(String item) {
                System.out.println("FlatMap: " + item + " на потоке: " + Thread.currentThread().getName());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Ошибка в flatMap: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("FlatMap завершен на потоке: " + Thread.currentThread().getName());
            }
        });

        Observable<Integer> infinite = Observable.create(observer -> {
            int i = 0;
            while (true) {
                observer.onNext(i);
                if (i >= 3) {
                    break;
                }
                i++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    observer.onError(e);
                    break;
                }
            }
            observer.onComplete();
        });

        final Disposable[] infiniteDisposable = new Disposable[1];
        infiniteDisposable[0] = infinite.subscribe(new Observer<Integer>() {
            int count = 0;
            @Override
            public void onNext(Integer item) {
                System.out.println("Бесконечный поток: " + item);
                count++;
                if (count > 3 && infiniteDisposable[0] != null) {
                    infiniteDisposable[0].dispose();
                }
            }
            @Override
            public void onError(Throwable t) {
                System.err.println("Ошибка в бесконечном потоке: " + t.getMessage());
            }
            @Override
            public void onComplete() {
                System.out.println("Бесконечный поток завершен");
            }
        });

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}