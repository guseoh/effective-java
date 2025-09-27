### ✅ 람다보다는 메소드 참조를 사용하라

메소드 참조는 람다 표현식을 더욱 간결하게 표현할 수 있도록 도와주는 문법입니다.
불필요한 코드를 생략하고 기존 메소드를 재활용하여 코드를 더 간결하게 만들어 줍니다.

#### 메소드 참조 기본 문법

메소드 참조는 이중 콜론(::) 기호를 사용하여 클래스나 객체가 가진 메소드를 참조

- 클래스명::메소드명
- 객체명::메소드명

#### 메소드 참조 유형

크게 5가지 유형으로 나눌 수 있습니다.

1. 정적 메소드 참조
   Integer::parseInt (람다식: s -> Integer.parseInt(s))

2. 한정적 인스턴스
   이미 생성된 특정 객체의 인스턴스 메소드를 참조합니다. 여기서 '한정적'이라는 의미는 특정 객체(인스턴스)에 대한 참조가 이미 정해져 있다는 것입니다.
   Instant.now()::isAfter (람다식: Instant then = instant.now(); \n t -> then.isAfter(t);

3. 특정 객체(비한정적)의 인스턴스 메소드 참조
   한정적 인스턴스 유형과 반대로 특정 객체를 지정하지 않고 해당 객체가 생성된 후에 호출될 인스턴스 메소드를 참조할 때 사용합니다.
   System.out::println (람다식: s -> System.out.println(s))

4. 특정 타입의 인스턴스 메소드 참조
   String::length (람다식: s -> s.length())

5. 생성자 참조
   ArrayList::new (람다식: () -> new ArrayList())

#### 주의할 점

- 항상 메소드 참조가 코드의 가독성이나 간결하지 않을 수 있습니다.
- 람다가 할 수 없는 일이라면 메소드 참조로도 할 수 없습니다. 단! 애매한 상황 존재

<details>
   <summary>람다 문법이 코드 가독성이나 간결한 코드인 예시</summary>
<div markdown="1"> 

```java
service.execute(GoshThisClassNameIsHumongous::action);

service.execute(() -> action());
```

</div>
</details>

### 애매한 상황 - 제네릭 함수 타입

제네릭 함수 타입과 관련해서는 메서드 참조만 가능한 특수한 경우가 있습니다.

```java
interface G1 {
    <E extends Exception> object m() throws e;
}

interface G1 {
    <F extends Exception> String m() throws e;
}

interface G extends G1, G2 {
}

// 함수형 인터페이스 G를 제네릭 함수 타입으로 표현
<F extends Exception> ()-> String throws F
```

이 경우 람다식으로는 불가능하지만, 메서드 참조로는 표현이 가능합니다.
즉, 메서드 참조는 특정한 제네릭 함수 타입 모호성을 해결할 수 있는 수단이 됩니다.

<details>
    <summary>람다식이 제네릭 함수 타입을 다루지 못하는 이유</summary>
<div markdown="1">

자바의 타입 시스템에서 람다는 **익명 클래스의 인스턴스와 유사하게 취급**됩니다. 익명 클래스를 만들 때처럼,
**람다식은 자신이 어떤 함수형 인터페이스의 구현체인지 명확**해야 합니다.

예를 들어, `() -> "hello"` 라는 람다식이 있다고 가정해 봅시다.
이 람다식은 `Supplier<String>`이 될 수도 있고, `Callable<String>`이 될 수도 있습니다.
컴파일러는 이 람다식이 사용되는 **맥락(context)** 을 보고 어떤 타입인지 추론합니다.

하지만 메서드에 제네릭 예외가 포함되면 상황이 복잡해집니다.

```java

@FunctionalInterface
interface MyFunction<T, E extends Exception> {
    T apply(T t) throws E;
}
```

만약 이 인터페이스를 람다식으로 구현하려 한다면 다음과 같이 작성할 수 있습니다. `MyFunction<String, IOException> func = s -> s.toUpperCase();`

여기서 `IOException`은 구체적인 타입입니다. 람다식은 `throws E`와 같은 제네릭 타입 변수 자체를 명시적으로 다루는 문법을 지원하지 않습니다.

반면, 메서드 참조는 이미 존재하는 **메서드의 시그니처(이름, 매개변수, 반환 타입, 그리고 던지는 예외)** 를 그대로 가져옵니다.
`MyFunction` 인터페이스를 구현하는 메서드가 있다고 가정해 봅시다.

```java
class MyUtil {
    public static <T, E extends Exception> T process(T t) throws E {
        // ...
        return t;
    }
}
```

이 메서드를 메소드 참조로 사용하면 MyUtil::process가 됩니다.
이 순간, 컴파일러는 `process` 메서드의 **모든 시그니처 정보를 그대로 가져와** `MyFunction` 인터페이스와 일치하는지 확인합니다.
즉, `throws E` 같은 제네릭 예외 정보까지 완벽하게 추론할 수 있습니다.

따라서 람다 표현식이 타입 추론에 의존하는 반면, 메서드 참조는 이미 완성된 메서드의 시그니처를 참조하기 때문에 제네릭 함수 타입의 복잡성을 해결할 수 있습니다.
</div>
</details>





