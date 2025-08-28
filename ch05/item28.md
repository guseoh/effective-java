### ✅ 배열보다는 리스트를 사용하라

## 0. 들어가기 전

- `배열(Array`)과 `리스트(LIst)`의 메모리 할당 방법적인 차이의 접근 주제가 아니다.
- 해당 주제는 `배열(Array)`과 `리스트(List)` **공변성, 불공변성과 실체화**와 **타입 정보 소거**에 포커스를 두고 있다.
- [공변성과 불공변성](https://ko.wikipedia.org/wiki/%EA%B3%B5%EB%B3%80%EC%84%B1%EA%B3%BC_%EB%B0%98%EA%B3%B5%EB%B3%80%EC%84%B1_(%EC%BB%B4%ED%93%A8%ED%84%B0_%EA%B3%BC%ED%95%99))


## 1. 배열과 제네릭의 근본적인 차이

- 타입 시스템의 차이
- 런타임 vs 컴파일정보 타입 정보

### 1.1 타입 시스템의 차이

- **배열: 공변**
    - `Sub[]` 는 `Super[]` 의 하위 타입이다.
    - `Inreger[]`는 `Number[]`로 대입 가능하다는 소리이다.
- **제네릭: 불공변**
    - `List<Integer>`는 `List<Number>`의 하위 타입이 아니다.
    - 덕분에 타입 안전성이 더 강하게 보장된다.

```java
Number[] na=  new Integer[1]; // 에러 X (공변)
na[0] = 3.14;                 // 런타임 ArrayStoreException

List<Number> ln = new ArrayList<Integer>(); // 컴파일 에러 (불공변)
```

> 배열은 잘못된 값을 넣어도 **컴파일은 통과** 하고 **런타임에 터진다**. 리스트는 애초에 **컴파일 단계에서 막는다**.

<br>

### 1.2 런타임 vs 컴파일정보 타입 정보

- **배열은 실체화(reifiable)** 되어 있어, 런타임에 자신의 원소 타입을 **정확히 안다**. 잘못된 저장 시 `ArrayStoreException`을 던질 수 있다.
- **제네릭은 타입 소거(erasure)** 로 구현되어, 원소 타입을 컴파일에만 검사하며 런타임에는 알수조차 없다는 뜻이다.
    - 소거는 제네릭이 지원하기 전의 레거시 코드와 제네릭 타입을 함께 사용할 수 있게 해주는 메커니즘(아이템 26)


## 2. 제네릭 배열 생성을 허용하지 않는 이유

<details>
    <summary>제네릭 배열 생성을 허용하지 않는 이유 - 컴파일되지 않는다.</summary>
<div markdown="1">

```java
List<String>[] stringLists = new List<String>[1]; // (1)
List<Integer> intList = List.of(42);              // (2)
Object[] objects = stringLists;                   // (3)
objects[0] = intList;                             // (4)
String s = stringLists.get(0);                    // (5) 컴파일 에러가 난다!
```

1. `List<String>[] stringLists = new List<String>[1];`
    - 제네릭 배열을 생성하는 구문
    - **실제 자바에서는 이 코드 자체가 컴파일 에러가 난다.**
        - `java: generic array creation` 발생
    - 자바는 제네릭 배열 생성 자체를 차단하므로 (1)은 불가능하다.
    - 여기서는 (1)이 허용된다고 가정해보자.
2. `List<Integer> intList = List.of(42);`
    - 원소가 하나인 `List<Integer>`를 생성한다.
3. `Object[] objects = stringLists;`
    - (1)에서 생성한 `List<String>`의 배열을 `Object` 배열에 할당한다.
    - 배열은 공변이니 문제가 없다.
        - `List<String>[]`는 `Object[]`의 하위 타입으로 취급된다.
        - `stringLists`를 `Object[]` 변수에 대입하는 것이 허용된다.
    - 이로 인해 타입 안정성이 깨질 가능성이 생긴다.
4. `objects[0] = intList;`
    - `objects` 는 `Object[]` 타입이므로, 컴파일러가 `List<Integer>`를 넣는 것을 막지 않는다.
    - 그러나 실제 런타임에는 `stringLists` 는 `List<String>` 배열이라고 기대한다.
    - 이제 `stringLists[0]` 에는 `List<Integer>` 가 들어가게 된다.
    - 이 상태를 **힙 오염(Heap Pollution)** 이다.
        - 컴파일러가 믿는 타입(List<String>) 과 런타임 실제 타입(List<Integer>)가 불일치하는 상태이다.
5. `String s = stringLists.get(0);`  → 컴파일 에러
    - `stringLists`의 타입은 `List<String>[]`이므로 `stringLists[0]`의 타입은 `List<String>` 이다.
    - 그러나 **배열 자체가 제네릭 타입**이라, 컴파일러는 `stringLists[0]`의 실제 타입을 알 수 없다.
    - `List<String>`를 꺼내려 하지만, 런타임에는 `List<Integer>`일 수도 있기 때문에 컴파일러는 이 코드를 허용하지 않는다.
</div>
</details>

<br>

### 문제 1. 배열의 공변성
- 배열은 공변이기 때문에 `List<String>[]` → `Object[]` 변환이 허용된다.
- 이로 인해 `List<Integer>` 를 강제로 삽입할 수 있게 된다.

<br>

### 문제 2. 제네릭의 타입 소거(Type Erasure)
- 제네릭은 **런타임에 타입 정보가 사라진다**.
- `stringLists` 내부에 어떤 타입의 리스트가 들어있는지 **런타임에서는 알 수 없다.**
- 잘못된 타입을 꺼내 사용하면 `ClassCastException`이 발생할 위험이 있다.

<br>

### 문제 3. 힙 오염(Heap Pollution)
- `stringLists`가 `List<String>[]`라고 선언되어도,
- 내부 원소로 `List<Integer>` 같은 다른 타입의 리스트가 들어올 수 있다.
- **컴파일러가 안전하다고 믿는 것과 실제 힙 메모리에 있는 객체가 달라지는 현상** 이다.

## 3. 배열 대신 리스트 사용

생성자에서 컬렉션을 받은 Chooser 클래스를 예로 살펴보자.

<details>
    <summary>Chooser - 제네릭을 시급히 적용해야 한다</summary>
<div markdown="1">

```java
package Effective_java.item28;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Chooser {
    private final Object[] choiceArray;

    public Chooser(Collection choices) {
        choiceArray = choices.toArray();
    }

    public Object choose() {
        Random rnd = ThreadLocalRandom.current();
        return choiceArray[rnd.nextInt(choiceArray.length)];
    }
}
```
</div>
</details>

- 이 클래스를 사용하려면 `choose` 메서드를 호출할 때마다 반환된 `Object`를 원하는 타입으로 형변환해야 한다.
- 만약 타입이 다른 원소가 들어 있었다면 런타임에 형변환 오류가 날 것이다.

<details>
    <summary>Chooser를 제네릭으로 만들기 위한 첫 시도 - 컴파일되지 않는다.</summary>
<div markdown="1">

```java
package Effective_java.item28;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Chooser<T> {

    private final T[] choiceArray;

    public Chooser(Collection<T> choices) {
        choiceArray = choices.toArray();
    }

    public Object choose() {
        Random rnd = ThreadLocalRandom.current();
        return choiceArray[rnd.nextInt(choiceArray.length)];
    }
}

```
</div>
</details>

- 해당 코드는 T의 타입을 알 수 없다는 컴파일 경고가 발생한다.
- 형변환이 런타임에 안전한지 보장할 수 없다는 말이다.

<details>
    <summary>리스트 기반 Chooser - 타입 안전성 확보</summary>
<div markdown="1">

```java
package Effective_java.item28;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Chooser<T> {

    private final List<T> choiceArray;

    public Chooser(Collection<T> choices) {
        this.choiceArray = new ArrayList<>(choices);
    }

    public Object choose() {
        Random rnd = ThreadLocalRandom.current();
        return choiceArray.get(rnd.nextInt(choiceArray.size()));
    }
}
```
</div>
</details>

- 타입 안전성 체크가 가능하다.
- 경고나 컴파일 오류를 만나면 배열을 리스트로 대체하라.