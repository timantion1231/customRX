package org.tima.core;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.tima.schedulers.Scheduler;

public class Observable<T> {
    private final Consumer<Observer<? super T>> onSubscribe;

    protected Observable(Consumer<Observer<? super T>> onSubscribe) {
        this.onSubscribe = onSubscribe;
    }

    public static <T> Observable<T> create(Consumer<Observer<? super T>> onSubscribe) {
        return new Observable<>(onSubscribe);
    }

    public Disposable subscribe(Observer<? super T> observer) {
        Subscription subscription = new Subscription();
        try {
            onSubscribe.accept(new SafeObserver<>(observer, subscription));
        } catch (Throwable t) {
            observer.onError(t);
        }
        return subscription;
    }

    public <R> Observable<R> map(Function<? super T, ? extends R> mapper) {
        return new Observable<>(observer -> {
            Observable.this.subscribe(new Observer<T>() {
                @Override
                public void onNext(T item) {
                    try {
                        R result = mapper.apply(item);
                        observer.onNext(result);
                    } catch (Throwable t) {
                        observer.onError(t);
                    }
                }

                @Override
                public void onError(Throwable t) {
                    observer.onError(t);
                }

                @Override
                public void onComplete() {
                    observer.onComplete();
                }
            });
        });
    }

    public Observable<T> filter(Predicate<? super T> predicate) {
        return new Observable<>(observer -> {
            Observable.this.subscribe(new Observer<T>() {
                @Override
                public void onNext(T item) {
                    try {
                        if (predicate.test(item)) {
                            observer.onNext(item);
                        }
                    } catch (Throwable t) {
                        observer.onError(t);
                    }
                }

                @Override
                public void onError(Throwable t) {
                    observer.onError(t);
                }

                @Override
                public void onComplete() {
                    observer.onComplete();
                }
            });
        });
    }

    public <R> Observable<R> flatMap(Function<? super T, ? extends Observable<? extends R>> mapper) {
        return new Observable<>(observer -> {
            Observable.this.subscribe(new Observer<T>() {
                @Override
                public void onNext(T item) {
                    try {
                        Observable<? extends R> observable = mapper.apply(item);
                        observable.subscribe(new Observer<R>() {
                            @Override
                            public void onNext(R r) {
                                observer.onNext(r);
                            }

                            @Override
                            public void onError(Throwable t) {
                                observer.onError(t);
                            }

                            @Override
                            public void onComplete() {
                            }
                        });
                    } catch (Throwable t) {
                        observer.onError(t);
                    }
                }

                @Override
                public void onError(Throwable t) {
                    observer.onError(t);
                }

                @Override
                public void onComplete() {
                    observer.onComplete();
                }
            });
        });
    }

    public Observable<T> subscribeOn(Scheduler scheduler) {
        return new Observable<>(observer -> {
            scheduler.execute(() -> {
                try {
                    onSubscribe.accept(observer);
                } catch (Throwable t) {
                    observer.onError(t);
                }
            });
        });
    }

    public Observable<T> observeOn(Scheduler scheduler) {
        return new Observable<>(observer -> {
            Observable.this.subscribe(new Observer<T>() {
                @Override
                public void onNext(T item) {
                    scheduler.execute(() -> observer.onNext(item));
                }

                @Override
                public void onError(Throwable t) {
                    scheduler.execute(() -> observer.onError(t));
                }

                @Override
                public void onComplete() {
                    scheduler.execute(() -> observer.onComplete());
                }
            });
        });
    }

    private static class Subscription implements Disposable {
        private volatile boolean disposed = false;

        @Override
        public void dispose() {
            disposed = true;
        }

        public boolean isDisposed() {
            return disposed;
        }
    }

    private static class SafeObserver<T> implements Observer<T> {
        private final Observer<? super T> actual;
        private final Subscription subscription;

        SafeObserver(Observer<? super T> actual, Subscription subscription) {
            this.actual = actual;
            this.subscription = subscription;
        }

        @Override
        public void onNext(T item) {
            if (!subscription.isDisposed()) {
                actual.onNext(item);
            }
        }

        @Override
        public void onError(Throwable t) {
            if (!subscription.isDisposed()) {
                actual.onError(t);
            }
        }

        @Override
        public void onComplete() {
            if (!subscription.isDisposed()) {
                actual.onComplete();
            }
        }
    }
}