### ✅ 로 타입은 사용하지 말라

## 1. 제네릭 클래스, 제네릭 인터페이스

- 클래스와 인터페이스 선언에 타입 매개변수(type parameter)가 쓰이면, 이를 **제네릭 클래스** 혹은 **제네릭 인터페이스**라 한다.

  ex) List는 원소의 타입을 나타내는 매개변수 E를 받은 List 이다.

- 제네릭 클래스와 제네릭 인터페이스를 통틀어 **제네릭 타입(generic type)** 이라 한다.
- 각각의 제네릭 타입은 일련의 **매개변수화 타입(parameterized type)** 을 정의한다.
    - 먼저 클래스(혹은 인터페이스) 이름이 나오고, 이어서 꺾쇠괄호 안에 실제 타입 매개변수들을 나열한다.

  ex) List는 원소의 타입인 String인 리스트를 뜻하는 매개변수화 타입이다. 여기서 String이 정규(formal) 타입 매개변수 E에 해당하는 실제(actual) 타입 매개변수이다.

- 제네릭을 하나 정의하면 **로 타입(raw type)** 도 함께 정의된다.
    - 로 타입이란 `제네릭 타입에서 타입 매개변수를 전혀 사용하지 않을 때`를 말한다.
    - 로 타입은 타입 선언에서 제네릭 타입 정보가 전부 지워진 것처럼 동작하는데, 이는 제네릭 도입 이전 코드와의 호환을 위한 것이다.

  ex) List<E>의 로 타입은 List 이다.

<br>

## 2. 왜 로 타입으로 사용하면 안될까?
<details>
    <summary>컬렉션의 로 타입 - 따라 하지 말 것!</summary>
<div markdown="1">

```java
class RawCollection {
    private final Collection stamps = null; // 편의상 null을 할당했다.

    void sample() {
        stamps.add(new Coin()); // "unchecked call" 경고를 내뱉는다.
    }

    private static class Coin {
        // ...
    }
}
```
</div>
</details>

- 제네릭을 지원하기 전 컬렉션을 위 코드와 같이 선언하였다.
- 실수로 도장(Stamp) 대신 동전(Coin)을 넣어도 아무 오류 없이 컴파일되고 실행된다. (컴파일러가 모호한 경고 메시지를 보여주긴 할 것)
- 이후 컬렉션에서 이 동전을 다시 꺼내기 전에는 오류를 알 수가 없다.

<details>
    <summary>반복자의 로 타입 - 따라 하지 말 것!</summary>
<div markdown="1">

```java
class RawIterator {
    private final Collection stamps = null; // 편의상 null을 할당

    void sample() {
        for (Iterator i = stamps.iterator(); i.hasNext(); ) {
            Stamp stamp = (Stamp) i.next(); // ClassCastException을 던진다.
            stamp.cancel();
        }
    }

    private static class Stamp {
        public void cancel() {
            // ...
        }
    }
}
```
</div>
</details>

- **오류는 가능한 한 발생 즉시, 이상적으로는 컴파일할 때 발견하는 것이 좋다.**
- `ClassCastException` 이 발생하면 stamps에 동전을 넣은 지점을 찾기 위해 코드 전체를 훑어봐야하는 비 효율적인 상황이 발생할 수 있다.

<br>

### 2.1 해결책
제네릭을 활용하여 위 코드의 주석 정보가 타입 선언 자체에 녹아들게 해보자.

```java
private final Collection<Stamp> stamps = ...;

stamps.add(new Coin(...));
```

- 위 코드로 변경 시 컴파일러는 stamps 에는 Stamp의 인스턴스만 넣어야 함을 인지하게 되고
- 이전 코드 처럼 stamps에 엉뚱한 Coin 인스턴스를 넣게 되면 컴파일 오류가 발생하며, 무엇이 잘못됐는지 정확히 알려준다.
- **컴파일러는 컬렉션에서 원소를 꺼내는 모든 곳에 보이지 않는 형변환을 추가하여 절대 실패하지 않음을 보장한다.**
- 로 타입을 쓰면 제네릭이 안겨주는 `안정성`과 `표현력`을 모두 잃게 된다. 따라서 절대로 사용하지 말아야 한다.
    - 로 타입은 애초에 이전 버전과의 호환성 때문에 존재할 뿐이다.
    - 이를 위해 타입을 지원하고 제네릭 구현에는 **소거 방식** 을 사용하기로 했다.

<details>
    <summary>제네릭의 타입소거(Generics Type Erasure)</summary>
<div markdown="1">

**소거**란 원소 타입을 컴파일 타임에만 검사하고 `런타임에는 해당 타입 정보를 알 수 없는 것`이다. 다른 말로는 컴파일 타임에만 타입에 대한 제약 조건을 적용하고, 런타임에는 타입에 대한 정보를 소거하는 프로세스이다.

```java
List<Object> ol = new ArrayList<Long>(); // 컴파일 에러
ol.add("타입이 달라 넣을 수 없다");
```

다음과 같은 상황에서 컴파일시에 타입 오류를 바로 알 수 있다. (리스트도 제네릭 타입으로 구현되어 있기 때문에)

Java 컴파일러는 타입소거를 아래와 같이 적용한다.

- 제네릭 타입(Example<T>) 에서는 해당하는 타입 파라미터 (T)나 Object로 변경해준다.
    - Object로 변경하는 경우는 unbounded 된 경우를 뜻하며, 이는 <E extends Comparable <E>>와 같이 bound를 해주지 않은 경우를 의미한다.
    - 따라서 이 소거 규칙에 대한 바이트코드는 제네릭을 적용할 수 있는 일반 클래스, 인터페이스, 메서드에만 해당된다.
- 타입 안정성 보존을 위해 필요하다면 type casting을 넣어준다.
- 확장된 제네릭 타입에서 다형성을 보존하기 위해 bridege method를 생성한다.
</div>
</details>

<br>

## 3. 로 타입 vs 제네릭 타입

- List 같은 로 타입은 사용해서는 안 되나, `List<Object>`처럼 임의 객체를 허용하는 매개변수화 타입은 괜찮다.
- 로 타입인 List와 매개변수화 타입인 `List<Object>`의 차이
    - `List(로 타입)`은 제네릭 타입에서 완전히 발을 뺀 것
    - `List<Object>`는 모든 타입을 허용한다는 의사를 컴파일러에 명확히 전달한 것
- 매개변수로 List를 받는 메서드에 List<String>을 넘길 수 있지만, `List<Object>`를 받는 메서드에는 넘길 수 없다.
    - 이는 제네릭의 하위 타입 규칙 때문이다
    - `List<String>`은 로 타입인 List의 하위 타입이지만,`List<Object>`의 하위 타입은 아니다.
    - `List<Object>`같은 매개변수화 타입을 사용할 때와 달리 List 같은 로 타입을 사용하면 타입 안정성을 잃게 된다.
    - 즉, **List<String>은 원시 타입 List 에는 넘길 수 있지만, 제네릭은 불공변이므로 `List<Object>` 에는 넘길 수 없다.**  
  

<details>
    <summary>런타임에 실패한다. - unsafeAdd 메서드가 로 타입(List)을 사용</summary>
<div markdown="1">

```java
class FailWithRawType {
    public static void main(String[] args) {
        List<String> strings = new ArrayList<>();
        unsafeAdd(strings, Integer.valueOf(42));
        String s = strings.get(0); // 컴파일러가 자동으로 형변환 코드를 넣어준다.
    }

    private static void unsafeAdd(List list, Object o) {
        list.add(o);
    }
}
```
</div>
</details>

- 위 코드는 string.get(0)의 결과를 형변환하려 할 때 ClassCastException을 던진다.
    - Integer를 String 으로 변환하려 시도한 것이기 때문이다.

<details>
    <summary>잘못된 예 - 모르는 타입의 원소도 받는 로 타입을 사용했다.</summary>
<div markdown="1">

```java
static int numElementsInCommon(Set s1, Set s2) {
    int result = 0;
    for (Object o1 : s1) {
        if (s2.contains(o1)) {
            result++;
        }
    }
    return result;
}
```
</div>
</details>

- 이 메서드는 동작은 하지만 로 타입을 사용해 안정하지 않다.
- 따라서 비한정적 와일드카드 타입(unbounded wildcard type)을 대신 사용하는게 좋다.
- **즉, 제네릭 타입을 쓰고 싶지만 실제 타입 매개변수가 무엇인지 신경 쓰고 싶지 않다면 물음표(?)를 사용하자.**
    - 예를 들어, 제네릭 타입인 Set의 비한정적 와일드카드 타입은 Set<?>이다.
    - 이는 어떤 타입이라도 담을 수 있는 가장 범용적인 매개변수화 Set 타입이다.

<details>
    <summary>비한정적 와일드카드 타입을 사용하라. - 타입 안전하며 유연하다.</summary>
<div markdown="1">

```java
static int numElementsInCommon(Set<?> s1, Set<?> s2) { ... }
```
</div>
</details>

- 로 타입 컬렉션에는 아무 원소나 넣을 수 있으니 타입 불변식을 훼손하기 쉽다.
- 반면, **Collection<?>에는 어떤 원소도 넣을 수 없다**. 다른 원소를 넣으려 하면 컴파일 할때 오류 메시지를 보게 된다.
    - 즉, 컬렉션의 타입 불변식을 훼손하지 못하게 막아준다.
    - (null 외의) 어떤 원소도 Collection<?>에 넣지 못하게 했으며 컬렉션에서 꺼낼 수 있는 객체의 타입도 전혀 알 수 없게 했다.

<br>

## 4. 로 타입을 쓰지 말라는 규칙의 예외

1. **class 리터털에는 로 타입을 써야 한다.**
    - 자바 명세는 class 리터럴에 매개변수화 타입을 사용하지 못하게 한다. (배열과 기본 타입은 허용)
    - ex) `List.class`, `String[].class`, `int.class`는 허용,`List<String>.class`, `List<?>.class`는 허용하지 않는다.
2. **instanceof 연산자는 비한정적 와일드카드 타입 이외의 매개변수화 타입에는 적용할 수 없다.**
    - 런타임에는 제네릭 타입 정보가 지워지기 때문이다. (타입 소거)
    - 로 타입이든 비한정적 와일드카드 타입이든 instanceof는 완전히 똑같이 동작한다.
    - 비한정적 와일드카드 타입의 꺾쇠괄호와 물음표는 아무런 역할 없이 코드만 지저분하게 하므로, 차라리 로 타입을 사용하는 편이 깔끔하다.
<details>
    <summary>로 타입을 써도 좋은 예 - instanceof 연산자</summary>
<div markdown="1">

```java
if (o instanceof Set) {
    Set<?> s = (Set<?>) o;
    // ...
}
```
</div>
</details>

- o의 타입인 Set 임을 확인한 다음 와일드카드 타입인 Set<?>로 형변환해야 한다.
    - 로타입은 Set이 아니다.
- 이는 검사 형변환(checked cast)이므로 컴파일러 경고가 뜨지 않는다.