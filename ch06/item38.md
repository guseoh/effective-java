### ✅ 확장할 수 있는 열거 타입이 필요하면 인터페이스를 사용하라

## 0. 들어가기 전
열거 타입은 거의 모든 상황에서 타입 안전 열거 패턴 보다 우수하다. 단, 예외적으로 타입 안전 열거 패턴은 확장할 수 있으나 열거 타입은 그럴 수 없다.

즉, 타입 안전 열거 패턴은 열거한 값들을 그대로 가져온 다음 값을 더 추가하여 다른 목적으로 쓸 수 있는 반면, 열거 타입은 그렇게 할 수 없다는 뜻이다.

<details>
    <summary>열거 타입</summary>
<div markdown="1">

```java
// 열거 타입은 확장 불가능하다.
enum Operation {
    PLUS, MINUS, TIMES, DIVIDE
}
```
</div>
</details>

<details>
    <summary>타입 안전 열거 타입</summary>
<div markdown="1">

```java
public class Operation {
  public static final Operation PLUS = new Operation("+");
  public static final Operation MINUS = new Operation("-");
  public static final Operation TIMES = new Operation("*");
  public static final Operation DIVIDE = new Operation("/");

  private final String symbol;

  protected Operation(String symbol) {
    this.symbol = symbol;
  }
}

// 타입 안전 열거 패턴은 확장가능 
public class ExtendedOperation extends Operation {
  public static final ExtendedOperation EXP = new ExtendedOperation("^");
  public static final ExtendedOperation REMAINDER = new ExtendedOperation("%");

 protected ExtendedOperation(String symbol) {
  super(symbol);
  }
}
```
</div>
</details>

<br>

### 사실 대부분 상황에서 열거 타입을 확장하는건 좋지 않은 생각이다.
<details>
    <summary>Enum은 상수 집합인데, 확장한 enum의 요소는 상위 enum의 요소로 취급하지만, 그 반대는 성립하지 않는 건 이상하다.</summary>
<div markdown="1">

### 1. Enum의 기본 규칙

1. 자바에서 Enum은 클래스처럼 동작하지만, 다른 클래스처럼 상속할 수 없다.

    ```java
    enum Color { RED, GREEN, BLUE }
    ```

    - `Color`는 최종 클래스로 취급되어, **extends를 통한 상속이 불가**하다.
2. 모든 Enum은 `java.lang.Enum`을 상속한다.
3. Enum 내부의 요소(`RED`, `GREEN` 등)는 상위 Enum 클래스 타입으로 취급될 수 있다.

### 2. “확장한 Enum의 요소는 상위 Enum의 요소로 취급”

- 자바는 Enum 상속을 직접 허용하지 않지만, 인터페이스를 통해 Enum 요소를 상위 타입처럼 다룰 수 있다.

    ```java
    interface Shape { }
    
    enum BasicShape implements Shape {
        CIRCLE, SQUARE
    }
    
    enum ExtendedShape implements Shape {
        TRIANGLE, RECTANGLE
    }
    ```

- `shape` 인터페이스를 사용하면, BasicShape와 ExtendsShape의 모든 요소를 Shape 타입으로 취급 가능
- 즉, **상위 타입 인터페이스를 통해 “상위 Enum 타입” 처럼 다룰 수 있다.**

### 3. 반대는 성립하지 않는다.

- 상위 Enum(Shape) 타입만으로 하위 Enum(ExtendShape) 요소를 자동으로 알 수는 없다.
- 이유:
    1. Enum 요소는 **고정된 집합**이므로 런타임에 추가되는 것이 불가하다.
    2. 하위 Enum 요소는 **상위 Enum이 모르는 별개의 타입**이다.
- 따라서 상위 타입으로는 **모든 하위 Enum 요소를 포함하지 못한다.**
</div>
</details>

- 확장된 타입과 상위 타입들의 모든 상수를 순회할 방법도 마땅치 않다.
- 확장성을 높이려면 고려할 요소가 늘어나 설계와 구현아 더 복잡해진다.

<br>

### 확장할 수 있는 열거 타입이 알맞는 상황은?

- **연산 코드 (operation code)** 의 각 원소는 특정 기계가 수행하는 연산을 뜻한다.
- 이따금 API가 제공하는 기본 연산 외에 사용자 확장 연산을 추가할 수 있도록 열어줘야 할 때가 있다.

<br>

## 1. 인터페이스를 통해 해결

- 연산 코드용 인터페이스를 정의하고 열거 타입이 이 인터페이스를 구현하게 하면 된다.
- 이때 열거 타입이 **인터페이스의 표준 구현체 역할**을 한다.

<details>
    <summary>연산 코드용 인터페이스</summary>
<div markdown="1">

```java
public interface Operation {
  double apply(double x, double y);
}
```
</div>
</details>

<details>
    <summary>인터페이스를 이용해 확장 가능 열거 타입을 흉내 냈다.</summary>
<div markdown="1">

```java
public enum BasicOperation implements Operaion {
  PLUS("+") {
    public double apply(double x, double y) {return x + y;}
  },
  MINUS("-") {
    public double apply(double x, double y) {return x - y;}
  },
  TIMES("*") {
    public double apply(double x. double y) {return x * y;}
  },
  DIVIDE("/") { 
    public double apply(double x, double y) {return x / y;}
  };
  private final String symbol;

  BasicOperation(String symbol) {
    this.symbol = symbol;
  }

  @Override 
  public String toString() {
    return symbol;
  }
}
```
</div>
</details>

- 열거 타입인 `BasicOperation` 은 확장할 수 없지만 인터페이스인 `Operation`은 확장할 수 있고, 이 인터페이스를 연산의 타입으로 사용하면 된다.
- 이렇게 하면 `Operation` 을 구현한 또 다른 열거 타입을 정의해 기본 타입인 `BasicOperation` 을 대체할 수 있다.

<details>
    <summary>확장 가능 열거 타입</summary>
<div markdown="1">

```java
public enum ExtendedOperation implements Operation {
  EXP("^") {
    public double apply(double x, double y) {
      return Math.pow(x, y);
    }
  },
  REMAINDER("%") {
    public double apply(double x, double y) {
      return x % y;
    }
  };

  private final String symbol;

  ExtendedOperation(String symbol) {
    this.symbol = symbol;
  }

  @Override
  public String toString() {
    return symbol;
  }
}
```
</div>
</details>

- 기존 Operation 인터페이스를 사용하는 곳에서 기존 연산(BasicOperation )과 확장한 연산 (ExtendedOperation)을 모두 사용할 수 있다.

<br>

## 2. 타입 수준에서의 확장된 열거 타입
> 개별 인스턴스 수준에서뿐 아니라, 타입 수준에서도 기본 열거 타입 대신 확장된 열거 타입을 사용할 수 있다.

1. Class 리터럴을 넘겨서 사용하는 것
```java
public static void main(String[] args) {
  double x = Double.parseDouble(args[0]);
  double y = Double.parseDouble(args[1]);
  test(ExtendedOperation.class, x, y);
}

private static <T extends Enum<T> & Operation> void test(Class<T> opEnumType, double x, double y) {
  for (Operation op : opEnumType.getEnumConstants())
    System.out.printf("%f %s %f = %f%n", x, op, y, op.apply(x, y));
}
```

- main 메서드는 test 메서드에 `ExtendedOperation` 의 class 리터럴을 넘겨 확장된 연산들이 무엇인지 알려준다.
    - 여기서 class 리터럴은 **한정적 타입 토큰 역할(아이템 33)**을 한다.
    - **<T extends Enum<T> & Operation>**: Class 객체가 열거 타입인 동시에 `Operation` 의 하위 타입
    - 열거 타입이어야 원소를 순회할 수 있고, `Operation` 이어야 원소가 연산을 수행할 수 있기 때문이다.

2. 한정적 와일드카드(아이템 31)인 Collection<? extends Operation>을 넘기는 방법
```java
public static void main(String[] args) {
  double x = Double.parseDouble(args[0]);
  double y = Double.parseDoulbe(args[1]);
  test(Arrays.asList(ExtendedOperation.values()), x, y);
}

  private static void test(Collection<? extends Operation> operations, double x, double y) {
    for (Operation operation : operations) {
      System.out.printf("%f %s %f = %f%n", x, operation, y, operation.apply(x, y));
    }
  }
}
```
- 이 코드는 덜 복잡하고 test 메서드가 여러 구현 타입의 연산을 조합해 호출할 수 있게 되었다.
- 반면, 특정 연산에서 EnumSet(아이템 36)과 EnumMap(아이템 37)을 사용하지 못한다.

<br>

## 3. 인터페이스를 이용한 확장 가능한 열거 타입의 문제점

- 열거 타입끼리 구현을 상속할 수 없다는 것이다.
- 아무 상태에도 의존하지 않는 경우에는 **디폴트 구현**(아이템 20)을 이용해 인터페이스에 추가하는 방법이 있다.
    - 반면, `Operation` 예는 연산 기호를 저장하고 찾는 로직이 `BasicOperation`과 `ExtendedOperation` 모두에 들어가야만 한다.
    - 이 경우에는 중복량이 적으니 문제되지 않지만, 공유하는 기능이 많다면 그 부분을 별도의 도우미 클래스나 정적 도우미 메서드로 분리하는 방식으로 코드 중복을 없앨 수 있을 것이다.