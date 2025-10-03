### ✅ 옵셔널 반환은 신중히 하라

### 메서드가 특정 조건에서 값을 반환할 수 없을 때

#### 자바 8 이전 방식의 단점

1. 예외를 던지는 방법
    - **용도 오용:** 예외는 '진짜 예외적인 상황', 즉 프로그램이 정상적인 흐름을 벗어났을 때만 사용해야 합니다.
      값을 반환할 수 없는 상황을 예외로 처리하는 것은 예외 처리 메커니즘의 목적에 맞지 않습니다.
    - **성능 비용:** 예외를 생성할 때 스택 추적(Stack Trace) 전체를 기록 및 수집해야 하므로, 이는 상당한 비용을 수반합니다. 빈번하게 발생할 수 있는 '값 없음' 상황에 예외를 사용하면 성능
      저하를 초래할 수 있습니다.

2. null을 반환하는 방식
    - **별도의 null 처리 코드 작성:** `null`을 반환할 가능성이 있는 메서드를 호출하는 쪽에서는 항상 반환 값에 대한 별도의 `null` 처리 코드를 추가해야 합니다.
    - **`NullPointerException` 위험:** 이 처리를 잊거나 누락하면 런타임에 흔히 발생하는 `NullPointerException` 오류를 유발하여 프로그램의 안정성을 떨어뜨립니다. 즉, "
      이 메서드가 `null`을 반환할 수도 있다"는 사실이 API 자체에 명시적으로 드러나지 않는 문제가 있습니다.

#### 자바 8부터 생긴 해법 - `Optional<T>`

자바 8부터 도입된 `Optional<T>`는 기존 방식의 단점들을 해소하고 '값 없음'을 명시적으로 처리하는 방법을 제공합니다.
본질적으로 `Optional<T>`는 `T` 타입 참조를 최대 하나 담을 수 있는 **불변(Immutable) 컬렉션**과 같습니다.  
따라서, `null`이 아닌 `T` 타입 참조를 담고 있는 상태는 **'비지 않았다'** 고 하며, 이 경우에 **'값 있음'** 을 의미합니다.
반대로, 아무것도 담고 있지 않은 상태는 **'비었다'** 고 하며 **'값 없음'** 을 나타냅니다.  
이처럼 `Optional<T>`는 값이 존재할 수도 있고 존재하지 않을 수도 있는 상황을 명확하게 표현할 수 있게 해줍니다.  
추가적으로 자바 9에서는 `Optional`에 `steam()` 메소드가 추가되어 값이 있을 때만 그 값을 스트림(Stream)으로 변환할 수 있습니다.

#### Optional<T> 적용 예시

```java
// Optional<T> 사용 전 - 인수로 들어온 컬렉션에서 최댓값 반환하는 메소드
public static <E extends Comparable<E>> E max(Collection<E> c) {
    if (c.isEmpty())
        throw new IllegalArgumentException("빈 컬렉션");

    E result = null;
    for (E e : c)
        if (result == null || e.compareTo(result) > 0)
            result = Objects.requireNonNull(e);

    return result;
}

// Optional<T> 사용 후
public static <E extends Comparable<E>>
Optional<E> max(Collection<E> c) {
    if (c.isEmpty())
        return Optional.empty();

    E result = null;
    for (E e : c)
        if (result == null || e.compareTo(result) > 0)
            result = Objects.requireNonNull(e);

    return Optional.of(result);
}
```

`Optional`을 사용할 때 `Optional.of(value)` 메서드에 `null`을 인수로 전달하면 `NullPointerException`이 발생한다는 점을 특히 주의해야 합니다.
만약 `null` 값도 허용하는 옵셔널을 생성하고 싶다면 `Optional.ofNullable(value)`을 사용해야 합니다.  
더불어, `Optional<T>`을 반환하도록 선언된 메서드에서 절대 `null`을 반환해서는 안 됩니다.  
이는 `Optional`을 도입한 주된 목적인 무시하는 행위이며, 기존의 `NullPointerException` 위험을 그대로 가져오는 행위이기 때문입니다.

### 그럼 null을 반환하거나 예외를 던지는 대신 옵셔널 반환을 선택해야 하는 기준은 무엇일까요??

`Optional<T>`는 반환 값이 **선택적인(optional) 경우에 사용**해야 합니다.
즉, 메서드를 정상적으로 실행했지만, 결과가 없을 수도 있음이 예상되거나 용인될 수 있는 상황에서 사용합니다.

실제로 스트림(Stream) API의 종단 연산 중 상당수가 이러한 목적으로 `Optional`을 반환합니다.

#### Optional의 장점

1. 값이 없을 경우 반환할 기본 값을 정해둘 수 있습니다. - 기본 값 설정

```text
String lastWordInLexicon = max(words).orElse("단어 없음...");
```

2. 값이 없을 경우 원하는 예외를 명확하게 던지도록 지정할 수 있어, 예외 처리의 의미를 명확히 합니다. - 값 없을 때 예외 지정

```text
Toy myToy = max(toys).orElseThrow(TemperTantrumException::new);
```

3. 지연 연산으로 기본 값 비용 절감  
   만약 기본 값을 설정하는 비용이 커서 부담이 될 때가 있다면, `Supplier<T>`를 인수로 받는 `orElseGet`을 사용합니다.
   이 메서드는 값이 처음 필요할 때만 기본 값을 생성하도록 하여 초기 설정 비용을 낮출 수 있습니다.

- **orElse:** 값이 있든 없든, 인수로 전달된 객체는 항상 미리 생성됩니다. (즉시 평가)
- **orElseGet:** 값이 있을 때는 `Supplier` 람다식을 호출하지 않습니다. (지연 연산)

4. 다양한 용도에 사용할 수 메소드 제공 ex) `filter`, `map`, `flatMap`, `ifPresent`

`Optional`을 사용할 때 주의할 점도 존재합니다. 항상 값이 채워져 있다고 확신할 수 있는 경우에 한해서는 `get()`을 사용해 즉시 값을 꺼낼 수 있습니다.
하지만 만약 값이 없는데 `get()`을 호출하면 `NoSuchElementException`이 발생하므로 사용에 주의해야 합니다.

### Optional을 사용하면 안되는 상황

1. 컬렉션, 스트림, 배열, 옵셔널 같은 컨테이너 타입은 옵셔널로 감싸면 안됩니다. ex) `Optional<List<T>>`
2. 박싱된 기본 타입을 담은 `Optional`, 예를 들어 `Optional<Integer>`를 반환하는 일은 피해야 하는데, 이는 이러한 옵셔널이 기본 타입 자체보다 무겁고 성능 저하를 일으킬 수 있기
   때문이며, 대신 `OptionalInt`, `OptionalLong`, `OptionalDouble`과 같은 전용 옵셔널을 사용하는 것이 권장됩니다.
3. `Optional`을 컬렉션의 키, 값, 원소나 배열의 원소로 사용하는 상황에서 거의 적절하지 않습니다.
4. `Optional`은 본래 메서드의 반환 타입으로 '값 없음'을 명시적으로 처리하기 위해 설계되었기 때문에 반환 타입 외 사용하면 좋지 않습니다.
    - **피해야 되는 상황:** 메서드 인자 `(someMethod(Optional<String> name))`나 클래스 필드 `(private Optional<User> user;)`로 사용하는 것은
      일반적으로 권장되지 않습니다.
    - **이유:** 인자로 사용할 경우 호출할 때마다 불필요하게 `Optional` 객체를 생성하는 오버헤드가 발생하며, 필드로 사용할 경우 메모리 소비가 증가하고 특히 객체 직렬화 시 문제가 발생할 수 있기
      때문입니다.
 
