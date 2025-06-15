# CustomRX

## Архитектура системы

CustomRX реализует основные компоненты реактивного программирования, включая паттерн "Наблюдатель" (Observer pattern), управление потоками (Schedulers) и операторы преобразования данных.

### Основные компоненты

- **Observable** — источник данных, поддерживает подписку и цепочку операторов (`map`, `filter`, `flatMap`).
- **Observer** — получает события: `onNext(T item)`, `onError(Throwable t)`, `onComplete()`.
- **Disposable** — позволяет отменять подписку и прекращать получение событий.
- **Scheduler** — интерфейс для управления потоками выполнения.

### Операторы

- **map(Function)** — преобразует элементы потока.
- **filter(Predicate)** — фильтрует элементы по условию.
- **flatMap(Function)** — преобразует элементы в новые Observable и объединяет их.
- **subscribeOn(Scheduler)** — задаёт поток для подписки.
- **observeOn(Scheduler)** — задаёт поток для обработки событий.

## Schedulers: принципы работы и области применения

- **IOThreadScheduler**  
  Использует `CachedThreadPool`. Предназначен для операций ввода-вывода, где требуется много коротких задач (например, сетевые запросы, файловые операции).

- **ComputationScheduler**  
  Использует `FixedThreadPool` с количеством потоков, равным числу ядер процессора. Подходит для вычислительных задач, не связанных с вводом-выводом.

- **SingleThreadScheduler**  
  Использует один поток. Гарантирует последовательное выполнение задач, подходит для задач, требующих строгого порядка.

## Процесс тестирования

- Для всех компонентов написаны юнит-тесты (см. папку `src/test/java`).
- Проверяется корректность обработки ошибок, отмены подписки, работы операторов и Schedulers.
- Тесты покрывают сценарии:
  - Создание и подписка на Observable.
  - Работа операторов `map`, `filter`, `flatMap`.
  - Управление потоками через Schedulers.
  - Обработка ошибок и отмена подписки через Disposable.

## Примеры использования

```java
Observable<Integer> numbers = Observable.<Integer>create(observer -> {
    for (int i = 1; i <= 5; i++) observer.onNext(i);
    observer.onComplete();
}).subscribeOn(new SingleThreadScheduler());

Observable<Integer> squaredEvenNumbers = numbers
    .map(x -> x * x)
    .filter(x -> x % 2 == 0);

squaredEvenNumbers.observeOn(new IOThreadScheduler())
    .subscribe(new Observer<Integer>() {
        public void onNext(Integer item) { System.out.println(item); }
        public void onError(Throwable t) { t.printStackTrace(); }
        public void onComplete() { System.out.println("Done"); }
    });
```
Или просто запустите через Main
## Описание архитектуры и реализованных операторов

- **Observable** реализует цепочку операторов, поддерживает асинхронность и отмену подписки.
- **Schedulers** позволяют гибко управлять потоками выполнения.
- Операторы реализованы с учётом потокобезопасности и корректной обработки ошибок.

