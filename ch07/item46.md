### ✅ 스트림에서는 부작용 없는 함수를 사용하라

## 0. 들어가기전

스트림이 제공하는 표현력, 속도, (상황에 따라서는) 병렬성을 얻으려면 API는 말할 것도 없고 이 패러다임까지 함께 받아들어야 한다.

스트림 패러다임의 핵심은 계산을 일련의 변환(transformation)으로 재구성하는 부분이다.

이때 각 변환 단계는 가능한 한 이전 단계의 결과를 받아 처리하는 **순수 함수**여야 한다.

순수함수는 오직 **입력만이 결과에 영향을 주는 함수**를 말한다. 다른 가변 상태를 참조하지 않고, 함수 스스로도 다른 상태를 변경하지 않는다.

그러므로 스트림 연산에 건네는 함수 객체는 모두 부작용(side effect)이 없어야 한다.

<br>

## 1. 짧고 명확

<details>
    <summary>스트림 패러다임을 이해하지 못한 채 API만 사용했다 - 따라 하지 말 것!</summary>
<div markdown="1">

```java
import java.util.Map;
import java.util.stream.Stream;

Map<String, Long> freq = new HashMap<>();
try (Stream<String> words = new Scanner(file).tokens()) {
    words.forEach(word -> {
        freq.merge(word.toLowerCase(), 1L, Long::sum)
    }
}
```
</div>
</details>

- 스트림 API를 사용했지만 내부에서 상태(`freq` 맵)를 변경하고 있다.
    - 즉, 반복적(Imperative) 방식을 사용한 코드다.

위 로직은 두 가지 문제가 존재한다.  
1. forEach에서 외부(freq)에 영향을 주는 부작용 있는 로직이 포함되어 있다.
2. 코드가 간결하지 않다.

<details>
    <summary>스트림을 제대로 활용해 빈도표를 초기화한다.</summary>
<div markdown="1">

```java
Map<String, Long> freq;
try (Stream<String> words = new Scanner(file).tokens()) {
    freq = words
           .collect(groupingBy(String::toLowerCase, counting)));
}
```
</div>
</details>

- 짧고 명확하다.
- forEach 연산은 스트림 계산 결과를 보고할 때만 사용하고, 계산할 때는 쓰지 말자.
    - 종단 연산 중 기능이 가장 적고 ‘덜’ 스트림 같기 때문

<details>
    <summary>종단 연산</summary>
<div markdown="1">

- 스트림 파이프라인의 **마지막에 호출**되어 **실제 처리를 수행**하고 **결과를 반환**하거나 부작용을 발생시키는 연산
- 특징
    1. 종단 연산이 호출될 때까지 스트림은 지연(Lazy) 상태이다.
    2. 종단 연산 이후 스트림은 사용 불가하다.
    3. 결과를 생성하거나 외부 상태를 변경한다.

| **함수**              | **내용**                                                                 |
|-----------------------|---------------------------------------------------------------------------|
| `void forEach()`      | 요소를 하나씩 소비해가며 지정된 작업 수행 (병렬 스트림에서 순서 보장 X)   |
| `void forEachOrdered()` | 요소를 하나씩 소비해가며 지정된 작업 수행 (병렬 스트림에서 순서 보장 O) |
| `long count()`        | 요소 개수 반환                                                            |
| `Optional max()`      | 요소 중 최대 값을 참조하는 `Optional` 반환                                |
| `Optional min()`      | 요소 중 최소 값을 참조하는 `Optional` 반환                                |
| `Optional findFirst()`| 첫번째 요소를 참조하는 `Optional` 반환                                    |
| `Optional findAny()`  | 첫번째 요소를 참조하는 `Optional` 반환 (병렬 스트림에서는 첫번째 요소 보장 X) |
| `boolean allMatch()`  | 모든 요소가 특정 조건을 만족하는지 여부를 반환                            |
| `boolean anyMatch()`  | 하나의 요소라도 특정 조건을 만족하는지 여부를 반환                        |
| `boolean noneMatch()` | 모든 요소가 특정 조건을 불만족하는지 여부를 반환                          |
| `reduce()`            | 요소를 하나씩 빼며 지정된 연산 처리 후 결과를 반환                        |
| `collect()`           | 요소들을 컬렉션으로 반환     
</div>
</details>

<details>
    <summary>중간 연산</summary>
<div markdown="1">

- 스트림 파이프라인에서 **종단 연산 전에 호출**되며, **새로운 스트림을 반환**하는 연산
- 특징
    1. 스트림을 변환하가나 필터링할 수 있다.
    2. 새로운 스트림을 반환한다. (기존 스트림은 변경되지 않는다.)
    3. 종단 연산이 호출될 때까지 아무 작업도 수행하지 않는다.
    4. 여러 중간 연산을 연쇄(chain) 가능하다.

| **함수**              | **내용**                                                   |
|-----------------------|-----------------------------------------------------------|
| `Stream<T> distinct()` | 요소의 중복 제거                                          |
| `Stream<T> filter()`   | 요소에 대한 필터링 조건 추가                              |
| `Stream<T> limit()`    | 요소 개수 제한                                            |
| `Stream<T> skip()`     | 처음 `n`개의 요소 건너뛰기                                |
| `Stream<T> sorted()`   | 요소 정렬                                                 |
| `Stream<T> peek()`     | 요소에 대한 작업 수행 (단, `forEach`와는 다르게 요소를 소비하지 않음) |
</div>
</details>

<br>

## 2. 수집기(collector)

`java.util.stream.Collectors` 클래스는 메서드를 39개 가지고 있고, 그중에는 타입 매개변수가 5개나 되는 것도 있다.

수집기가 생성하는 객체는 일반적으로 컬렉션으로, “collector”라는 이름을 사용한다.

수집기를 사용하면 스트림의 원소를 쉽게 컬렉션으로 모을 수 있다. 수집기는 총 세 가지로, `toList()`, `toSet()`, `toCollection(collectionFactory)`이다.

<details>
    <summary>스트림 파이프라인 예시</summary>
<div markdown="1">

```java
List<String> topTen = freq.keySet().stream()
	.sorted(comparing(freq::get).reversed())
	.limit(10)
	.collect(toList());
```
</div>
</details>

<br>

## 3. 스트림이 제공하는 API
> [API Doc](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/stream/Collectors.html)

1. **toMap & 복잡한 형태의 toMap**
    ```java
    toMap(Function<? super T,? extends K> keyMapper, Function<? super T,? extends U> valueMapper)
    ```
    - 스트림 원소를 키에 매핑하는 함수와 값에 매핑하는 함수를 인수로 받는다.

    ```java
    private static final Map<String, Operation> stringToEnum =
        Stream.of(values()).collect(toMap(Object::toString, e -> e));
    ```
    - 스트림의 각 원소가 고유한 키에 매핑되어 있을 때 적합하다.
    - 스트림 원소 다수가 같은 키를 사용한다면 파이프라인이 IllegalStateException을 던진다.
   
2. **인수 3개를 받는 toMap**
    ```java
    private static final Map<String, Operation> stringToEnum =
        Stream.of(values())
            .collect(toMap(Object::toString, e -> e, (existing, replacement) -> existing));
    ```
    - **병합(merge) 함수**를 통해 충돌을 제어할 수 있다.
        - `BinaryOperation<U>` 형태를 가지며, U는 해당 맵의 value 타입
        - 같은 키를 공유하는 값들은 병합 함수를 사용해 기존 값에 합쳐진다.
        - `toMap`이나 `groupingBy`는 이러한 방식으로 충돌을 다룬다.
    ```java
    Map<Artist, Album> topHist = albums.collect(
        toMap(Album::artist, a->a, maxBy(comparing(Album::sales))));
    ```
    - “**어떤 키**" 와 “**그 키에 연관된 원소들 중 하나**”를 골라 연관 짓는 맵을 만들 때 유용하다.
    - 코드를 말로 풀어보자면 “앨범 스트림을 맵으로 바꾸는데, 이 맵은 각 음악가와 그 음악가의 베스트 앨범을 짝지은 것이다.”
    - 인수가 3개인 toMap은 충돌이 나면 마지막 값을 취하는(last-write-wins) 수집기를 만들 때도 유용하다.
3. **인수 4개를 받는 toMap**
    ```java
    private static final Map<Operation, String> operationToString =
        Stream.of(Operation.values())
            .collect(toMap(Function.identity(), Object::toString, (a, b) -> a, EnumMap::new));
    ```
    - `toMap`은 일반적으로 `HashMap`을 사용하는데, 4번째 인수로 `EnumMap`이나 `TreeMap`처럼 특정 맵 구현체를 직접 지정할 수도 있다.
4. **toMap 변종 (toConcurrentMap)**
    ```java
    public static <T, K, U> 
    Collector<T, ?, ConcurrentMap<K, U>> 
    toConcurrentMap(Function<? super T, ? extends K> keyMapper,
                    Function<? super T, ? extends U> valueMapper)
    
    ```
    - 위 세 가지 `toMap`에는 변종이 있다.
    - 그중 `toConcurrentmap`은 병렬 실행된 후 결과로 `ConcurrentHashMap` 인스턴스를 생성한다.
    - 위 코드는 **병렬 스트림(parallel stream)** 에서 동시성 문제 없이 안전하게 결과를 모아준다.
5. **groupingBy**
    ```java
    word.collect(groupingBy(word -> alphabetize(word));
    ```
    - 이 메서드는 입력으로 분류 함수를 받고 출력으로는 원소들을 카테고리별로 모아 놓은 맵을 담은 수집기를 반환한다.
        - 분류 함수는 입력받은 원소가 속하는 카테고리를 반환한다.
    - `groupingBy` 가 반환하는 수집기가 리스트 외의 값을 갖는 맵을 생성하게 하려면, 분류 함수와 함께 다운스트림(downstream) 수집기도 명시해야 한다.
    - 다운스트림 수집기의 역할은 해당 카테고리의 모든 원소를 담은 스트림으로부터 값을 생성하는 일이다.
6. **groupingBy 다른 자료형으로 반환**
   - 다운스트림을 사용하는 가장 간단한 방법은 `toSet()`을 넘기는 것이다.
       - 그러면 groupingBy는 원소들의 리스트가 아닌 집합(Set)을 값을 갖는 맵을 만들어낸다.
   - `toSet()` 대신 `tocollection(collectionFactory)`를 건네는 방법도 있다.
       - 리스트나 집합 대신 컬렉션을 값으로 갖는 맵을 생성한다.
       - 원하는 컬렉션 타입을 선택할 수 있다는 유연성도 얻는다.
   - counting()을 건네는 방법도 있다.
    ```java
    Map<String, Long> freq = words.collect(groupingBy(String::toLowerCase, counting()));
    ```
    - 각 카테고리(키)를 해당 카테고리에 속하는 원소의 개수(값)와 매핑한 맵을 얻는다.
   
7. **groupingBy 세 번재 버전**
    ```java
    public static <T, K> 
    Collector<T, ?, Map<K, List<T>>> 
    groupingBy(Function<? super T, ? extends K> classifier)
    
    List<String> names = Arrays.asList("apple", "banana", "cherry");
    
    Map<Character, List<String>> groupedByNameLength = names.stream()
            .collect(groupingBy(name -> name.charAt(0)));
    
    System.out.println(groupedByNameLength);
    
    // {a=[apple, ape], b=[banana, bat], c=[cherry]}
    ```
    - `toMap`처럼 `mapFactory`를 지정할 수 있다.
    - 해당 메서드는 점층적 인수 목록 패턴(telescoping argument list pattern, Item 2)에 어긋난다.
      - `mapFactory` 매개변수가 downStream 매개변수보다 앞에 놓인다.
    - 이 버전의 `groupingBy` 을 사용하면 맵과 그 안에 담긴 컬렉션의 타입을 모두 지정할 수도 있다.
      - ex) `TreeSet`인 `TreeMap`을 반환하는 수집기를 만들 수 있다.
8. **partitioningBy**
    ```java
    Map<Boolean, List<Product>> mapPartitioned = productList.stream()
        .collect(Collectors.partitioningBy(p -> p.getAmount() > 15));
    ```
    - 분류 함수 자리에 프레디케이트(predicate)를 받고 Boolean인 맵을 반환한다.
    - 많이 사용하지 않는다.
9. **joining & minBy/maxBy**
   - ‘수집’과는 관련이 없다.
   - **minBy/maxBy**
       - 인수로 받은 비교자를 이용해 스트림에서 가장 작은/큰 원소를 반환한다.
   - **joining**
       ```java
       String listToString = productList.stream()
           .map(Product::getName)
           .collect(Collectors.joining(", ", "<", ">"));
       // <potatoes, orange, lemon, bread, sugar>
       ```
       - 문자열 등의 CharSequence 인스턴스 스트림에만 적용 가능하다.
       - 매개변수가 없을 경우, 단순히 원소들을 연결(concatenate)하는 수집기 반환한다.

<br>

## 4. 핵심 정리

- 스트림 파이프라인 프로그래밍의 핵심은 부작용 없는 함수 객체에 있다.
- 스트림뿐 아니라 스트림 관련 객체에 건네지는 모든 함수 객체가 부작용이 없어야 한다.