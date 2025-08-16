### ✅ 추상 클래스보다는 인터페이스를 우선하라

## 0. 들어가기 전

자바가 제공하는 다중 구현 메커니즘은

- **인터페이스(Interface)**
- **추상 메서드(Abstract Class)**

이 두 가지 모두 여러 클래스에 공통된 기능을 정의하면서도, 각 클래스가 자신만의 구현을 가질 수 있도록 설계되었다.


### 0.1 인터페이스(interface)

인터페이스는 **클래스가 반드시 구현해야 할 메서드의 명세(Contract)**를 정의한다.

즉, “이 기능들을 제공해야 한다”는 규칙만 명시하며, 실제 구현 내용은 없다.

하지만 Java 8부터는 `default` 키워드를 사용하여 **디폴트 메서드(default method)**를 작성할 수 있게 되었다.

디폴트 메서드는 인터페이스 내부에서 메서드의 기본 동작을 구현하는 기능으로, 이를 구현한 클래스에서 필요 시 재정의(override) 할 수 있다. 이로써 인터페이스는 일정 부분 코드 재사용이 가능해졌고, 기존의 순수 추상 형태에서 확장성을 갖추게 되었다.
```java
interface MyInterface {
    void abstractMethod();

    default void defaultMethod() {
        System.out.println("인터페이스의 디폴트 메서드 동작");
    }
}
```

### 0.2 추상 클래스(Abstract Class)

추상 클래스는 `abstarct` 키워드로 선언하며, **추상 메서드**와 **일반 메서드**를 함께 가질 수 있다. 추상 메서드는 구현을 강제하고, 일반 메서드는 바로 사용할 수 있는 기본 기능을 제공한다.

또한 추상 클래스는 **필드(상태)**를 가질 수 있기 때문에, 공통 상태를 여러 하위 클래스에서 공유할 수 있다. 반면 인터페이스는 필드를 가질 수 있지만 `public static final` 상수만 가능하므로 상태 공유에는 적합하지 않다.
```java
abstract class MyAbstractClass {
    String name;

    abstract void abstractMethod();

    void concreteMethod() {
        System.out.println("추상 클래스의 일반 메서드 동작");
    }
}
```

### 0.3 두 메커니즘의 공통점과 차이점

- **공통점**: 두 메커니즘 모두 인스턴스 메서드를  구현 형태로 제공할 수 있다.
    - 추상 클래스 → 원래부터 가능
    - 인터페이스 → Java 8 부터 디폴트 메서드를 통해 가능
- **차이점**:
    1. 상속 구조
        - 추상 클래스는 단일 상속만 가능
        - 인터페이스는 다중 구현이 가능
    2. 상태(필드) 보유 여부
        - 추상 클래스는 인스턴스 변수(상태) 보유 가능
        - 인터페이스는 상수(`public static final`)만 보유 가능

## 1. 인터페이스의 장점

- 기존 클래스에도 손쉽게 새로운 인터페이스를 구현해 넣을 수 있다.
- 인터페이스는 믹스인(mixin) 정의에 안성맞춤이다.
- 인터페이스로는 계층 구조가 없는 타입 프레임워크를 만들 수 있다.
- 래퍼 클래스 관용규(아이템 18)와 함께 사용하면 인터페이스는 기능을 향상시키는 안전하고 강력한 수단이 된다.

### 1.1 기존 클래스에도 손쉽게 새로운 인터페이스를 구현해 넣을 수 있다.

기존 클래스에 새로운 기능을 부여하고 싶을 때, 인터페이스는 매우 유연하게 적용할 수 있다.

이미 작성된 클래스라도, **인터페이스가 요구하는 메서드만 구현**하고, 클래스 선언부에 `implements` 구문을 추가하면 끝이다.

이는 기존 계층 구조에 영향을 거의 주지 않으며, 필요한 기능만 자연스럽게 덧입힐 수 있는 장점이 있다.
<details>
    <summary>예시 코드</summary>
<div markdown="1">

```java
interface Printable {
    void print();
}

class Document {
    String title;
}

// 기존 클래스에 기능 추가
class Report extends Document implements Printable {
    @Override
    public void print() {
        System.out.println("리포트를 출력합니다.");
    }
}
```
- 이 경우, `Report` 클래스는 기존 `Document` 구조를 그대로 유지하면서도 **출력 기능**을 가진 클래스가 된다.
</div>
</details>


반면, **추상 클래스**를 기존 클래스 계층에 끼어 넣는 것은 쉽지 않다.

추상 클래스는 **단일 상속**만 가능하기 때문에, 두 클래스가 같은 추상 클래스를 확장하려면 **그 추상 클래스가 반드시 두 클래스의 공통 조상**이어야 한다.

문제는, 이 과정에서 모든 하위 클래스가 **상속하는 것이 적절하지 않은 추상 클래스**를 강제로 상속하게 되는 상황이 발생할 수 있다는 점이다.

이로 인해 클래스 계층 구조가 불필요하게 복잡해지고, 객체지향 설계의 원칙이 훼손될 위험이 있다.
<details>
    <summary>예시 코드</summary>
<div markdown="1">

```java
abstract class PrintableBase {
    abstract void print();
}

class Document {}
class Image {}

// 두 클래스 모두 PrintableBase를 상속받게 하려면?
class Report extends Document /*, PrintableBase 불가능*/ {}
class Picture extends Image /*, PrintableBase 불가능*/ {}
```
- 이 경우, `Document`와 `Image`는 서로 다른 계층에 속해 있으므로 `PrintableBase` 를 공통 조상으로 둘 수 없다.
- 결국, 추상 클래스를 통한 기능 추가는 계층 구조에 심각한 제약을 부여하게 된다.

</div>
</details>

### 1.2 인터페이스는 믹스인(mixin) 정의에 안성맞춤이다.

자바에서 인터페이스는 믹스인(mixin) 정의에 특히 적합하다. 믹스인이라는 개념은 **주된 기능을 가진 클래스에 선택적인 기능을 혼합(mixed in)하는 것**을 의미한다.

예를 들어, `Comparable` 인터페이스는 자신을 구현한 클래스의 인스턴스끼리 순서를 정할 수 있는 기능을 제공한다. 이는 클래스의 핵심 기능과 독립적으로 적용할 수 있는 부가 기능으로, 기존 클래스의 계층 구조를 변경하지 않고도 선택적으로 적용 가능하다.

Java 8 이후에는 디폴트 메서드를 통해 인터페이스 자체에 기본 구현을 제공할 수 있어, 코드 중복 없이 기능을 재사용할 수 있다는 장점도 있다.

반면, 추상 클래스는 **단일 상속**만 가능하므로  이미 다른 클래스를 상속한 상태에서는 새로운 기능을 덧씌우기 어렵고, 클래스 계층 구조상 적절한 위치에 삽입하기 어렵다. 이로 인해 추상 클래스를 통한 믹스인 구현은 계층 구조를 강제로 변경하게 되고, 설계의 유연성이 크게 떨어진다.

따라서 자바에서 주된 기능에 선택적 기능을 혼합하고자 할 때는 인터페이스가 가장 안전하고 유연한 수단이며, 다중 구현과 디폴트 메서드를 활용하면 여러 기능을 동시에 적용할 수도 있다.

### 1.3 인터페이스로는 계층 구조가 없는 타입 프레임워크를 만들 수 있다.

자바에서 인터페이스를 사용하면 계층 구조가 없는 타입 프레임워크를 손쉽게 만들 수 있다. 일반적으로 객체 지향 설계에서 타입을 계층적으로 정의하면 다양한 개념을 구조적으로 잘 표현할 수 있지만, 모든 개념을 엄격한 계층으로 구분하기 어려운 경우가 존재한다.

예를 들어, 노래는 부르는 기능과 곡을 작곡하는 기능을 각각 다른 타입으로 정의하고 싶을 때, 인터페이스를 사용하면 매우 유연하게 설계할 수 있다.
```java
interface Singer {
    void sing(String s);
}

interface Songwriter {
    void compose(int chartPosition);
}

interface SingerSongwriter extends Singer, Songwriter {
    void strum();
    void actSensitive();
}
```

위 코드에서 `Singer`와 `Songwriter` 인터페이스를 각각 구현하고, 필요한 경우 두 기능을 모두 가진 `SingerSongwriter` 인터페이스를 정의할 수 있다. 이렇게 하면 **기존 인터페이스를 확장하거나 새로운 메서드를 추가하더라도 유연하게 타입을 구성할 수 있으며, 클래스 계층 구조에 구속되지 않는다.**

반면, 같은 구조를 클래스로 구현하려고 하면 훨씬 복잡해진다. 추상 클래스는 단일 상속만 가능하기 때문에, 각 조합마다 별도의 클래스를 정의해야 하고, 속성이 n개라면 가능한 조합 수는 2^n개로 급격히 늘어나게 된다. 이를 조합 폭발(combinatorial explosion)이라고 한다.

예를 들어, Singer와 Songwriter를 클래스 기반으로 구현하면 다음과 같이 된다.
```java
abstract class Singer {
    abstract void sing(String s);
}

abstract class Songwriter {
    abstract void compose(int chartPosition);
}

abstract class SingerSongwriter {
    abstract void sing(String s);
    abstract void compose(int chartPosition);
    abstract void strum();
    abstract void actSensitive();
}
```

이 경우, **모든 조합마다 새로운 클래스를 정의**해야 하므로 계층 구조가 불필요하게 거대해지고, 공통 기능을 정의할 수 있는 타입이 없어 매개변수 타입만 다른 메서드들이 반복되는 비효율적인 구조가 된다.

반면, 인터페이스를 사용하며면 필요한 기능을 자유롭게 혼합(mixed in) 할 수 있으므로, 조합 폭발 문제를 피하고 가벼운 타입 프레임워크를 만들 수 있다.

즉, 인터페이스를 활용하면 다중 상속 없이도 여러 기능을 선택적으로 조합할 수 있으며, 클래스 계층 구조에 얽매이지 않고 유연한 설계를 가능하게 한다. 이는 특히 기능이 독립적이거나 조합 가능한 속성이 많은 경우에 인터페이스가 매우 강력한 이유이다.

### 1.4 래퍼 클래스 관용구(아이템 18)와 함께 사용하면 인터페이스는 기능을 향상시키는 안전하고 강력한 수단이 된다.

래퍼 클래스 관용구란, **기존 객체를 내부 필드로 감싸고 필요한 메서드를 위임하여 새로운 기능을 덧붙이는 패턴**을 말한다. 이를 통해 **기존 객체의 내부 구조를 변경하지 않고도 기능을 향상**시킬 수 있다.

반면, 타입을 추상 클래스로 정의하면 **그 타입에 기능을 추가하는 방법은 상속밖에 없다**. 즉, 기존 클래스를 확장하여 새로운 기능을 구현해야 하는데, **단일 상속 제약 때문에 이미 다른 클래스를 상속하고 있는 경우에는 확장이 어렵다**. 또한 상속으로 만든 클래스는 하위 클래스의 내부 구현에 의존하는 경우가 많아, 객체가 의도치 않게 깨지거나 계층 구조에 영향을 미칠 위험이 있다.

예를 들어, 어떤 컬렉션 타입을 추상 클래스로 정의했다고 가정하면,

- 기존 클래스에 기능을 추가하려면 반드시 그 추상 클래스를 상속해야 한다.
- 그러나 인터페이스를 사용하면 래퍼 클래스를 만들어 기존 객체를 감싼 후, 인터페이스가 요구하는 메서드를 위임하면서 새로운 기능을 덧붙일 수 있다.
- 이렇게 하면 기존 객체의 안정성을 유지하면서도 기능을 향상시킬 수 있고, 다중 구현을 통해 다양한 조합도 가능하다.

즉, 인터페이스는 래퍼 클래스 관용구와 결합할 때 상속 기반 확장의 한계를 극복하고, 기존 객체를 안전하게 감싸면서 기능을 확장할 수 있는 강력하고 유연한 수단이 된다.
<details>
    <summary>인터페이스 + 래퍼 클래스 관용구</summary>
<div markdown="1">

```java
// 1. 기능 인터페이스 정의
interface Printer {
    void print(String message);
}

// 2. 기존 클래스
class SimplePrinter implements Printer {

    @Override
    public void print(String message) {
        System.out.println("기본 출력: " + message);
    }
}

// 3. 래퍼 클래스 1: 대문자로 변환
class UpperCasePrinter implements Printer {
    private final Printer wrappedPrinter;

    public UpperCasePrinter(Printer printer) {
        this.wrappedPrinter = printer;
    }

    @Override
    public void print(String message) {
        wrappedPrinter.print(message.toUpperCase());
    }
}

// 4. 래퍼 클래스 2: 메시지 앞뒤에 꾸밈 추가
class DecoratedPrinter implements Printer {
    private final Printer wrappedPrinter;

    public DecoratedPrinter(Printer printer) {
        this.wrappedPrinter = printer;
    }

    @Override
    public void print(String message) {
        wrappedPrinter.print("<< " + message + " >>");
    }
}

// 5. 사용 예제
public class Main {
    public static void main(String[] args) {
    
        Printer printer = new SimplePrinter();
        printer.print("hello world");
        // 출력: 기본 출력: hello world

        // UpperCasePrinter만 적용
        Printer upperPrinter = new UpperCasePrinter(printer);
        upperPrinter.print("hello world");
        // 출력: 기본 출력: HELLO WORLD

        // UpperCasePrinter + DecoratedPrinter 적용
        Printer fancyPrinter = new DecoratedPrinter(upperPrinter);
        fancyPrinter.print("hello world");
        // 출력: 기본 출력: << HELLO WORLD >>
    }
}
```
- `SimplePrinter`는 원래 기능만 제공
- `UpperCasePrinter`는 메시지를 대문자로 변환
- `DecoratedPrinter`는 메시지 앞뒤를 꾸며줌
- **래퍼 클래스를 겹처서 적용할** 수 있기 때문에, 상속을 사용하지 않고도 **다중 기능 조합**이 가능
- 기존 클래스(`SimplePrinter`)는 수정하지 않아도 되고, 새로운 기능을 안전하게 추가할 수 있다.
</div>
</details>

## 2. 인터페이스의 디폴트 메서드(default method)

자바 8부터 인터페이스에 디폴트 메서드(default method)를 정의할 수 있다.

`default` 키워드를 사용하여 **구현부가 포함된 메서드**를 인터페이스에 추가할 수 있으며, 구현 클래스에 이를 재정의하지 않아도 바로 사용할 수 있다.

이는 구현 방법이 명확하고 공통적으로 필요한 메서드를 제공할 때 특히 유용하다.

디폴트 메서드의 주요 장점은 **기존 인터페이스의 변경이 구현 클래스의 수정을 강제하지 않는다는 점이다**.

원래라면 인터페이스에 메서드를 추가하면 모든 구현 클래스가 컴파일 에러를 피하기 위해 해당 메서드를 구현해야 하지만, 디폴트 메서드를 사용하면 새로운 기능을 바로 상속받게 된다.

<details>
    <summary>사용 예시</summary>
<div markdown="1">

```java
public interface MyInterface {
    default void greet() {
        System.out.println("Hello from default method!");
    }
}

public class MyClass implements MyInterface {
    // greet() 재정의 없이도 바로 사용 가능
}

public class Main {
    public static void main(String[] args) {
        MyClass obj = new MyClass();
        obj.greet(); // 출력: Hello from default method!
    }
}
```
</div>
</details>

### 2.1 문서화와 @implSpec 태그
디폴트 메서드를 제공할 때 반드시 `@implSpec` 자바독 태그를 사용해 구현 규약을 명확히 문서화활 것을 권장한다.

이는 해당 메서드가 어떤 방식으로 동작하는지, 재정의할 때 주의해야 할 점이 무엇인지를 상속받는 사람에게 알리기 위함이다.

<details>
    <summary>예시</summary>
<div markdown="1">

```java
/**
 * @implSpec
 * 이 메서드는 컬렉션의 각 요소를 순회하며
 * 지정된 액션을 수행합니다.
 */
default void forEach(Consumer<? super T> action) {
    Objects.requireNonNull(action);
    for (T t : this) {
        action.accept(t);
    }
}

```
</div>
</details>

### 2.2 디폴트 메서드의 제약

1. 디폴트 메서드도 계약(Constract)을 가진다.
    - 디폴트 메서드는 구현 코드가 포함되어 있지만, 여전히 인터페이스의 `규약`의 일부이므로 신중하게 설계해야 한다.
    - 재정의 가능성을 고려하여 **동작의 일관성**과 **호환성**을 보장해야 한다.
2. 상속 후 재정의 시 주의
    - 구현 클래스가 디폴트 메서드를 그대로 쓸 수도 있고, 재정의할 수도 있다.
    - 하지만 디폴트 메서드의 내부 구현에 변경이 생기면, 해당 구현을 상속받은 기존 클래스의 동작이 **의도치 않게 깨질 위험**이 있다.
        - 따라서 변경 가능성을 최소화하고, 문서로 해당 메서드의 기대 동작을 명확히 설명해야 한다.
3. 다중 상속 충돌 문제
    - 하나의 클래스가 **여러 인터페이스를 구현**하는데, 같은 시그니처의 디폴트 메서드가 존재하면 충돌이 발생한다.
    - 이 경우 구현 클래스에서 명시적으로 재정의해야 한다.
    ```java
    interface A { default void hello() { System.out.println("Hello from A"); } }
    interface B { default void hello() { System.out.println("Hello from B"); } }
    
    class C implements A, B {
        @Override
        public void hello() {
            A.super.hello(); // 또는 B.super.hello()
        }
    }
    ```

[Object 의 equals와 hashcode, toString 같은 메서드를 디폴트 메서드로 제공해서는 안된다.](https://stackoverflow.com/questions/24016962/java8-why-is-it-forbidden-to-define-a-default-method-for-a-method-from-java-lan?noredirect=1&lq=1)

## 3. 인터페이스와 추상 골격 구현(skeletal implementation) 클래스

자바에서는 **인터페이스**와 **추상 클래스**의 장점을 모두 취할 수 있는 방식으로 설계를 권장한다.

인터페이스는 타입을 정의하며 필요하다면 **디폴트 메서드**를 추가하여 공통 기능을 제공한다.

여기에 대한 **골격 구현 클래스(skeletal implementation)** 을 만들어 나머지 메서드를 기본적으로 구현해 둔다.

이렇게 하면 단순히 골격 구현 클래스를 상속하는 것만으로 해당 인터페이스를 구현하는 데 필요한 대부분의 코드가 완성된다. 이 구조는 [템플릿 메서드 패턴](https://steady-coding.tistory.com/384#google_vignette)과 유사한 효과를 제공한다.

### 3.1 네이밍 규칙

관례상 인터페이스 이름이 `Interface`라면 그 골격 구현 클래스의 이름은 `AbstractInterface` 로 한다.

예를 들어 `Collection` 인터페이스의 골격 구현 클래스는 `AbstractCollection`이며, `Map`의 경우 `AbstractMap`이라 한다.

### 3.2 골격 구현의 역할

골격 구현은 크게 두 가지 방식으로 제공된다.

1. 독립된 **추상 클래스** 형태 (`AbstractCollection`, `AbstractMap` 등)로 제공한다.
2. 인터페이스 내부의 **디폴트 메서드** 형태로 제공한다.

이 두 가지 방식 모두 인터페이스 구현 시 작성해야 할 코드를 크게 줄여준다.

### 3.3 활용 예시
<details>
    <summary>골격 구현을 사용해 완성한 구체 클래스</summary>
<div markdown="1">

```java
static List<Integer> intArrayAsList(int[] a) {
    Objects.requireNonNull(a);
    return new AbstractList<>() {
        @Override public Integer get(int i) {
            return a[i];  // 오토박싱(아이템 6)
        }
        @Override public Integer set(int i, Integer val) {
            int oldVal = a[i];
            a[i] = val;     // 오토언박싱
            return oldVal;  // 오토박싱
        }
        @Override public int size() {
            return a.length;
        }
    };
}
```
</div>
</details>

이 메서드는 `int[]` 배열을 받아 `Integer` 리스트로 변환해 주는 어댑터 역할을 한다. 다만 오토박싱/언박싱 과정 때문에 성능은 최적화되지 못한다.

이로써 억지로 메서드를 사용하는 상황을 방지하며, 컴파일 시점이 아닌 실행 시점에서 오류를 발생시키도록 한다.

### 3.4 장점과 한계

골격 구현 클래스는 추상 클래스처럼 **공통 구현을 제공**하면서도, 추상 클래스로 타입을 정의할 때 발생하는 **다중 상속 제약**에서는 벗어난다.

- 장점: 인터페이스 구현 클래스는 골격 구현을 확장하는 것만으로 구현을 거의 끝낼 수 있다.
- 한계: 이미 다른 클래스를 상속받고 있는 경우에는 골격 구현을 상속할 수 없다.

    ```java
    public class A extends B implements C, D
    ```

  위와 같은 경우 `AbstractC` 를 상속할 수 없으므로 `C`의 메서드를 직접 구현해야 한다.


그렇더라도 인터페이스가 제공하는 디폴트 메서드는 여전히 활용할 수 있다.

### 3.5 시뮬레이트한 다중 상속

골격 구현을 직접 상속하지 못할 때는 우회적으로 활용할 수 있다.

인터페이스를 구현한 클래스 내부에 골격 구현을 확장한 **private 내부 클래스**를 정의하고, 각 메서드 호출을 내부 클래스 인스턴스에 위임한다.

이 방식은 아이템 18의 래퍼 클래스 관용구와 비슷하며, `시뮬레이트한 다중 상속(simulated multiple inheritance)` 이라 한다.

### 3.6 골격 구현 작성 메뉴얼

1. **기반 메서드 선정**
    - 먼저, 인터페이스에서 다른 메서드들의 구현에 사용되는 **기반 메소드**를 선정한다.
    - 이 기반 메서드들은 인터페이스의 핵심 역할을 담당하며, 골격 구현에서는 추상 메서드로 선언된다.
    - 예를 들어, `Map.Entry` 인터페이스에서 `getkey()`와 `getValue()`가 기반 메서드가 된다.
2. **디폴트 메서드 제공**
    - 기반 메소드들만 있으면 직접 구현할 수 있는 메소드들이 있다면, 이들을 인터페이스에서 `디폴트 메소드(default method)`로 제공한다.
    - 이를 통해 인터페이스 구현 클래스는 최소한의 메서드만 작성하면 나머지 메서드를 그대로 상속받아 사용할 수 있게 된다.
    - 하지만, `equals`, `hashCode`, `toString` 같은 **Object 클래스의 메소드**는 디폴트 메소드로 제공할 수 없다.
    - 만약 인터페이스의 메소드 모두가 기반 메서드와 디폴트 메서드가 되면 골격 구현 클래스를 별도로 만들 이유는 없다.
3. **골격 구현 클래스 작성**
    - 여전히 구현되지 않은 메서드가 남아 있다면, 해당 인터페이스를 구현하는 골격 구현 클래스를 작성한다.
    - 골격 구현 클래스는 추상 클래스 형태로 만들어지며, 인터페이스에 남은 메서드를 구현한다.
        - 이 과정에서 필요하다면 **public가 아닌 필드나 메서드를 추가**해도 된다.

<details>
    <summary>Map.Entry 를 위한 골격 구현 클래스</summary>
<div markdown="1">

```java
public abstract class AbstractMapEntry<K,V> implements Map.Entry<K,V> {
    // 변경 가능한 엔트리는 이 메서드를 반드시 재정의해야 한다.
    @Override public V setValue(V value) {
        throw new UnsupportedOperationException();
    }

    // Map.Entry.equals의 일반 규약을 구현한다.
    @Override public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Map.Entry))
            return false;
        Map.Entry<?,?> e = (Map.Entry) o;
        return Objects.equals(e.getKey(), getKey())
                && Objects.equals(e.getValue(), getValue());
    }

    // Map.Entry.hashCode의 일반 규약을 구현한다.
    @Override public int hashCode() {
        return Objects.hashCode(getKey())
                ^ Objects.hashCode(getValue());
    }

    @Override public String toString() {
        return getKey() + "=" + getValue();
    }
}
```
</div>
</details>

위 코드에서 볼 수 있듯이, `equals`, `hashCode`, `toString`은 디폴트 메소드로 제공할 수 없으므로 골격 구현 클래스에 작성하였다.

또한 `setValue`는 기본적으로 예외를 던지도록 구현해, 필요 시 하위 클래스에서 재정의하도록 유도하였다.

골격 구현은 상속해서 사용하는 걸 가정하므로 아이템19에서 이야기한 설계 및 문서화 지침을 모두 따라야 한다.

## 4. 단순 구현(Simple Implementation)

골격 구현의 작은 변종으로 가장 단순한 구현이다. 그대로 써도 되고, 필요에 맞게 확장해도 된다.
<details>
    <summary>AbstractMap.SimpleEntry</summary>
<div markdown="1">

```java
public static class SimpleEntry<K, V>
        implements Entry<K, V>, Serializable {

    private static final long serialVersionUID = -8499721149061103585L;

    private final K key;
    private V value;

    /**
     * Creates an entry representing a mapping from the specified key to the specified value.
     * @param key   the key represented by this entry
     * @param value the value represented by this entry
     */
    public SimpleEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }
}
```
</div>
</details>

단순 구현도 골격 구현과 같이 상속을 위해 인터페이스를 구현한 건 맞지만 추상 클래스가 아니라는 점이 다르다.