### ✅ 익명 클래스보다는 람다를 사용하라

## 0. 들어가기 전

예전에는 자바에서 함수 타입을 표현할 때 추상 메서드를 하나만 담은 인터페이스를 사용했다.

이런 인터페이스의 인스턴스를 `함수 객체(function object)`라고 하여, 특정 함수나 동작을 나타내는 데 썼다.

JDK 1.1이 등장하면서 함수 객체를 만드는 주요 수단은 익명 클래스(아이템 24)가 되었다.
<details>
    <summary>익명 클래스의 인스턴스를 함수 객체로 사용 - 낡은 기법이다!</summary>
<div markdown="1">

```java
public class Ex42_1 {
    public static void main(String[] args) {
        List<String> words = Arrays.asList("Hello", "World", "Java");

        Collections.sort(words, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return Integer.compare(o1.length(), o2.length());
            }
        });
    }
}
```
</div>
</details>

- [전략 패턴](https://refactoring.guru/ko/design-patterns/strategy)처럼, 과거 객체 지향 디자인 패턴에는 익명 클래스면 충분했다.
- comparator 인터페이스가 정렬을 담당하는 추상 전략
- 문자열을 정렬하는 구체적인 전략을 익명 클래스로 구현

<br>

## 1. 람다식
- 자바 8에 와서 추상 메서드 하나짜리 인터페이스는 특별한 의미를 인정받아 특별한 대우를 받게 되었다.
- **함수형 인터페이스라 부르는 인터페이스들의 인스턴스**를 `람다식(lambda expression)` 을 사용해 만들 수 있게 되었다.
- 함수나 익명 클래스와 개념은 비슷하나 코드는 훨씬 간결하다.
```java
// 1. 람다식을 함수 객체로 사용 - 익명 클래스 대체
Collections.sort(words, (s1, s2) -> Integer.compare(s1.length(), s2.length()));

// 2. 람다 자리에 비교자 생성 메서드 사용
Collections.sort(words, comparingInt(String::length));

// 3. List 인터페이스 sort 메서드 사용
words.sort(comparingInt(String::length));
```

1. **람다식을 함수 객체로 사용 - 익명 클래스 대체**
    - 람다, 매개변수(s1, s2), 반환값의 타입은 각각 `(Comparator<String>)`, `String`, `int`지만 코드에는 없다.
    - 컴파일러가 문맥을 살펴 타입을 추론해주기 때문이다.
    - 타입을 명시해야 코드가 더 명확할 때만 제외하고는, 람다의 모든 매개변수 타입은 생략하자.
2. **람다 자리에 비교자 생성 메서드 사용**
    - `Comparator.comparingInt(ToIntFunction<? super T> keyExtractor)`
    - 정렬 기준이 되는 `int` 값을 뽑아주는 함수를 받아 `Comparator`를 생성한다.
3. **List 인터페이스 sort 메서드 사용**
    - `Collections.sort` 대신 `List` 인터페이스의 기본 메서드를 사용한다.

<details>
    <summary>상수별 클래스 몸체와 데이터를 사용한 열거 타입</summary>
<div markdown="1">

```java
public enum Operation {
    PLUS("+")   {public double apply(double x, double y) {return x + y;}},
    MINUS("-")  {public double apply(double x, double y) {return x - y;}},
    TIMES("*")  {public double apply(double x, double y) {return x * y;}},
    DIVIDE("/") {public double apply(double x, double y) {return x / y;}};

    private final String symbol;

    Operation(String symbol) {
        this.symbol = symbol;
    }

    @Override public String toString() {
        return symbol;
    }
    
    public abstract double apply(double x, double y);
}
```
</div>
</details>

- 아이템 34에서는 상수별 클래스 몸체를 구현하는 방식보다는 열거 타입에서 인스턴스 필드를 두는 편이 낫다고 했다.
- **람다를 이용하면 열거 타입의 인스턴스 필드를 이용하는 방식**으로 상수별로 다르게 동작하는 코드를 쉽게 구현할 수 있다
- 각 열거 타입 상수의 동작을 람다로 구현해 생성자에 넘기고, 생성자는 이 람다를 인스턴스 필드로 저장해둔다.

<br>

### 함수 객체(람다)를 인스턴스 필드에 저장해 상수별 동작을 구현한 열거 타입

```java
enum Operation {
    PLUS("+", (x, y) -> x + y),
    MINUS("-", (x, y) -> x - y),
    TIMES("*", (x, y) -> x * y),
    DIVIDE("/", (x, y) -> x / y);

    private final String symbol;
    private final DoubleBinaryOperator op;

    Operation(String symbol, DoubleBinaryOperator op) {
        this.symbol = symbol;
        this.op = op;
    }

    public double apply(double x, double y) {
        return op.applyAsDouble(x, y);
    };
    @Override public String toString() { return symbol; }
}
```

<details>
    <summary>DoubleBinaryOperator</summary>
<div markdown="1">

- `java.util.function` 패키지가 제공하는 다양한 함수 인터페이스(Item 44)중 하나이다.
- `double` 타입 인수 2개를 받아 `double` 타입 결과를 돌려준다.

```java
@FunctionalInterface
public interface DoubleBinaryOperator {
    /**
     * Applies this operator to the given operands.
     *
     * @param left the first operand
     * @param right the second operand
     * @return the operator result
     */
    double applyAsDouble(double left, double right);
}
```

[DoubleBinaryOperator](https://docs.oracle.com/javase/8/docs/api/java/util/function/DoubleBinaryOperator.html)
</div>
</details>

<br>

## 2. 주의 사항

1. **간결하게 사용될 때만 사용하라.**
    - 람다는 이름이 없고 문서화도 못한다.
    - 따라서 코드 자체를 동작이 명확히 설명되지 않거나, 코드 줄 수가 많아지면 사용하지 말아야 한다.
    - 한 줄일 때가 가장 좋고 길어야 세 줄 안에 끝내는게 좋다.
2. **인스턴스 멤버에 접근할 수 없다.**
    - 열거 타입 생성자에 넘겨지는 인수들의 타입은 컴파일 타입에 추론된다.
    - 인스턴스 멤버는 런타임에 만들어지므로, 생성자 안의 람다는 열거 타입 인스터스 멤버 접근이 불가능하다.
    - 인스턴스 필드나 메서드를 사용해야 한다면 상수별 클래스 몸체를 사용하라.
3. **함수형 인터페이스에서만 사용하라.**
    - 람다는 함수형 인터페이스의 인스턴스를 만들 때만 사용된다.
    - 추상 클래스의 인스턴스를 만들 때는 사용할 수 없으니, 익명 클래스를 써야 한다.
   <details>
    <summary>함수형 인터페이스(Functional Interface)</summary>
    <div markdown="1">

   - `1개의 추상 메서드를 갖는 인터페이스`를 말한다.
   - Java 8부터 인터페이스는 기본 구현체를 포함한 디폴트 메서드를 포함할 수 있다.
   - 여러 개의 디폴트 메서드가 있더라도 **추상 메서드가 오직 하나**면 함수형 인터페이스이다.
   - `@FunctionalInterface` 어노테이션을 사용하여, 해당 인터페이스가 함수형 인터페이스 조건에 맞는지 검사해준다.
     - 없어도 동작하고 사용하는데는 문제가 없지만, 인터페이스 검증과 유지보수를 위해 붙이는게 좋다.
     ```java
     @FunctionalInterface
     interface CustomInterface<T> {
        
     // abstract method 오직 하나
      T myCall();
        
      // default method 는 존재해도 상관없음
      default void printDefault() {
          System.out.println("Hello Default");
        }
        
      // static method 는 존재해도 상관없음
      static void printStatic() {
          System.out.println("Hello Static");
        }
     }
        ```
       </div>
       </details>

4. **람다는 자신을 참조할 수 없다.**
    - 람다에서의 `this` 키워드는 바깥 인스턴스를 가리킨다.
    - 함수 객체가 자신을 참조해야 한다면 반드시 익명 클래스를 써야 한다.
5. **직렬화를 할 수 없다.**
    - 익명 클래스와 마찬가지로 직렬화 형태가 구현별(혹은 가상머신별)로 다를 수 있다.
    - 람다와 익명 클래스를 직렬화하는 일은 극히 삼가야 한다.
    - 직렬화해야만 하는 함수 객체가 있다면 **private 정적 중첩 클래스(Item 24)** 의 인스턴스를 사용하자.