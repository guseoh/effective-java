### ✅ 표준 함수형 인터페이스를 사용하라

## 0. 들어가기 전
자바가 람다를 지원하면서 API를 작성하는 모법 사례도 크게 바꾸었다.

상위 클래스의 기본 메서드를 재정의해 원하는 동작을 구현하는 `템플릿 메서드 패턴` 에서 함수 객체를 받는 정적 팩터리나 생성자를 제공하는 방식으로 바뀌게 되었다. 일반화 해보자면 **함수 객체를 매개변수로 받는 생성자와 메서드를 더 많이 만들어야 한다는 말**이다.

중요한 점은 매개변수의 `함수 객체 타입`을 올바르게 선택해야 한다.

`LinkedHashMap` 을 이용하여 캐시를 구현한다고 생각해보자.

<br>

### removeEldestEntry 구현 - Basic
```java
import java.util.LinkedHashMap;
import java.util.Map;

public class MyCache<K, V> extends LinkedHashMap<K, V> {

    private final int limitSize;

    public MyCache(int limitSize) {
        this.limitSize = limitSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > this.limitSize;
    }
}

public class Main {

    public static void main(String[] args) {
        MyCache<String, Integer> cache = new MyCache<>(3);
        cache.put("1", 1);
        cache.put("2", 2);
        cache.put("3", 3);
        cache.put("4", 4);
        System.out.println(String.join(", ", cache.keySet()));
    }
}

/----------------------------------------/
2, 3, 4
```

- `protected` 메서드인 `removeEldestEntry`를 재정의하면 캐시로 사용할 수 있다.
- `removeEldestEntry` 선언을 보면 `Map.Enrty<K, V>`를 받아 `boolean`을 번환해야 할 것 같지만, 꼭 그렇지는 않다.
    - `removeEldestEntry` 가 인스턴스 메서드라 가능한 방식
    - 하지만 생성자에 넘기는 함수 객체는 이 맵의 인스턴스 메서드가 아니다.
    - 팩터리나 생성자를 호출할 때는 맵의 인스턴스가 존재하지 않기 때문이다.

<br>

### removeEldestEntry 구현 - 함수형 인터페이스
```java
@FunctionalInterface
interface EldestEntryRemovalFunction<K, V> {
	  boolean remove(Map<K, V> map, Map.Entry<K, V> eldest);
}
```

- 굳이 불필요하게 함수형 인터페이스를 만들 필요는 없다.
- 표준 인터페이스인 `BiPredicate<Map<K, V>`, `Map.Entry<K, V>`를 사용할 수 있기 때문이다.

<br>

## 1. 표준 함수형 인터페이스

java.util.function 패키지에는 총 43개의 인터페이스가 담겨 있다.

이 기본 인터페이스들은 모두 참조 타입용이다.

| 인터페이스        | 함수 시그니처          | ****예****                  |
|-------------------|------------------------|---------------------|
| UnaryOperator<T>  | T apply(T t)           | String::toLowerCase |
| BinaryOperator<T> | T apply(T t1, T t2)    | BigInteger::add     |
| Predicate<T>      | boolean test(T t)      | Collection::isEmpty |
| Function<T, R>    | R apply(T t)           | Arrays::asList      |
| Supplier<T>       | T get()                | Instant::now        |
| Consumer<T>       | void accept(T t)       | System.out::println |

1. **Operator 인터페이스**
    - 인수가 1개인 `UnaryOperator`와 인수가 2개인 `BinaryOperator`로 나뉜다.
    - 반환값과 인수의 타입이 같은 함수
    <details>
    <summary>예시</summary>
    <div markdown="1">

    |  | 인터페이스 이름        | 함수 시그니처                                |
    |---|-----------------------|---------------------------------------------|
    | 1 | `BinaryOperator<T>`   | T apply(T, T)                               |
    | 2 | `UnaryOperator<T>`    | T apply(T)                                  |
    | 3 | `DoubleBinaryOperator`| double applyAsDouble(double, double)        |
    | 4 | `DoubleUnaryOperator` | double applyAsDouble(double)                 |
    | 5 | `IntBinaryOperator`   | int applyAsInt(int, int)                    |
    | 6 | `IntUnaryOperator`    | int applyAsInt(int)                         |
    | 7 | `LongBinaryOperator`  | long applyAsLong(long, long)                |
    | 8 | `LongUnaryOperator`   | long applyAsLong(long)                      |

    </div>
    </details>

2. **Predicate 인터페이스**
   - 인수 1개를 받아 boolean을 반환하는 함수
   <details>
    <summary>예시</summary>
    <div markdown="1">
    
    |  | 인터페이스 이름       | 함수 시그니처                  |
    |---|----------------------|--------------------------------|
    | 1 | `Predicate<T>`       | boolean test(T t)              |
    | 2 | `BiPredicate<T, U>`  | boolean test(T t, U u)         |
    | 3 | `DoublePredicate`    | boolean test(double value)     |
    | 4 | `IntPredicate`       | boolean test(int value)        |
    | 5 | `LongPredicate`      | boolean test(long value)       |

    </div>
    </details>

3. **Function 인터페이스**
    - 인수와 반환 타입이 다른 함수를 뜻한다.
   - 기본 타입을 반환하는 변형이 총 9가지 더 존재한다.
   - Function 인터페이스의 변형은 입력과 결과의 타입이 항상 다르다.
   - 예를 들어 `long`을 받아 `int`를 반환하면 `LongToIntFunction`이 되는 식
   <details>
    <summary>예시</summary>
    <div markdown="1">

   |  | 인터페이스 이름             | 함수 시그니처                          |
   |---|----------------------------|----------------------------------------|
   | 1 | `Function<T, R>`           | R apply(T t)                           |
   | 2 | `BiFunction<T, U, R>`      | R apply(T t, U u)                      |
   | 3 | `DoubleFunction<R>`        | R apply(double value)                  |
   | 4 | `IntFunction<R>`           | R apply(int value)                     |
   | 5 | `LongFunction<R>`          | R apply(long value)                    |
   | 6 | `DoubleToIntFunction`      | int applyAsInt(double value)           |
   | 7 | `DoubleToLongFunction`     | long applyAsLong(double value)         |
   | 8 | `IntToDoubleFunction`      | double applyAsDouble(int value)        |
   | 9 | `IntToLongFunction`        | long applyAsLong(int value)            |
   | 10 | `LongToDoubleFunction`    | double applyAsDouble(long value)       |
   | 11 | `LongToIntFunction`       | int applyAsInt(long value)             |
   | 12 | `ToDoubleBiFunction<T,U>` | double applyAsDouble(T t, U u)         |
   | 13 | `ToDoubleFunction<T>`     | double applyAsDouble(T t)              |
   | 14 | `ToIntBiFunction<T,U>`    | int applyAsInt(T t, U u)               |
   | 15 | `ToIntFunction<T>`        | int applyAsInt(T t)                    |
   | 16 | `ToLongBiFunction<T,U>`   | long applyAsLong(T t, U u)             |
   | 17 | `ToLongFunction<T>`       | long applyAsLong(T t)                  |

    </div>
    </details>

4. **Supplier 인터페이스**
    - 인수를 받지 않고 값을 반환(제공)하는 함수
   - BooleanSupplier 인터페이스는 boolean을 반환하도록 하는 Supplier의 변형이다.
   <details>
    <summary>예시</summary>
    <div markdown="1">

   |  | 인터페이스 이름 | 함수 시그니처 |
   | --- | --- | --- |
   | 1 | `Supplier<T>` | T get() |
   | 2 | `BooleanSupplier` | boolean getAsBoolean() |
   | 3 | `DoubleSupplier` | double getAsDouble() |
   | 4 | `IntSupplier` | int getAsInt() |
   | 5 | `LongSupplier` | long getAsLong() |
    </div>
    </details>

5. **Consumer 인터페이스**
    - 인수를 하나 받고 반환값은 없는(특히 인수를 소비하는) 함수
   <details>
    <summary>예시</summary>
    <div markdown="1">

   |  | 인터페이스 이름 | 함수 시그니처 |
   | --- | --- | --- |
   | 1 | `Consumer<T>` | void accept(T t) |
   | 2 | `BiConsumer<T,U>` | void accept(T t, U u) |
   | 3 | `DoubleConsumer` | void accept(double value) |
   | 4 | `IntConsumer` | void accept(int value) |
   | 5 | `LongConsumer` | void accept(long value) |
   | 6 | `ObjDoubleConsumer<T>` | void accept(T t, double value) |
   | 7 | `ObjIntConsumer<T>` | void accept(T t, int value) |
   | 8 | `ObjLongConsumer<T>` | void accept(T t, long value) |
    </div>
    </details>
    
> 💡 주의사항
>> 표준 함수형 인터페이스 대부분은 기본 타입만 지원한다.  
그렇다고 기본 함수형 인터페이스에 박싱된 기본 타입을 넣어 사용하지는 말자.  
동작은 하지만, 계산량이 많을 때 성능이 처참히 느려질 수 있다.

<br>

## 2. 함수형 인터페이스 작성

대부분 상황에서는 직접 작성하는 것보다 표준 함수형 인터페이스를 사용하는 편이 낫다.

그렇지만 함수형 인터페이스를 직접 작성해야만 할 때도 있다.

표준 인터페이스 중 필요한 용도에 맞는게 없다면 직접 작성 해야 한다.

아래 조건 중 하나 이상을 만족하는 경우, 전용 함수형 인터페이스를 구현해야 하는 것 아닌지 고민해야 한다.

- **자주 쓰이며, 이름 자체가 용도를 명확히 설명해준다.**
- **반드시 따라야 하는 규칙이 있다.**
- **유용한 디폴트 메서드를 제공할 수 있다.**

전용 함수형 인터페이스를 작성하기로 했다면, **자신이 작성하는 게 다른 것도 아닌 ‘인터페이스’임을 명심**해야 한다. 즉, 아주 주의해서 설계해야 한다.(Item 21)

<br>

### 2.1 @FunctionalInterface 애너테이션

함수형 인터페이스에 @**FunctionalInterface** 애너테이션을 사용하는 이유는 프로그래머의 의도를 명시하기 위한 것으로 크게 세 가지 목적이 있다.

1. 해당 클래스의 코드나 설명 문서를 읽을 이에게 그 인터페이스가 람다용으로 설계된 것임을 알려준다.
2. 해당 인터페이스가 추상 메서드를 오직 하나만 가지고 있어야 컴파일되도록 해준다.
3. 유지보수 과정에서 누군가 실수로 메서드를 추가하지 못하게 막아준다. (2의 결과이기도 하다)

<br>

### 2.2 서로 다른 함수형 인터페이스를 같은 위치의 인수로 받는 메서드를 오버로딩하면 안된다.

```java
public interface ExecutorService extends Executor {
  // ...
  
  <T> Future<T> submit(Callable<T> task);
  Future<?> submit(Runnable task);

  // ...
}
```

- `submit` 메서드는 `Callable<T>`를 받는 것과 `Runnable`을 받는 것을 다중 정의했다.
- 올바른 메서드를 알려주기 위해 형변환해야 할 때가 생긴다.  (Item 52)
- 이런 문제를 피하는 쉬운 방법은 서로 다른 함수형 인터페이스를 같은 위치의 인수로 사용하는 다중정의로 피하는 것이다.