### ✅ int 상수 대신 열거 타입을 사용하라

## 1. 정수 열거 패턴(int enum Pattern)
- 열거 타입은 일정 개수의 상수 값을 정의한 다음, 그 외의 값은 허용하지 않는 타입
- 자바에서 열거 타입을 지원하기 전에는 정수 상수를 한 묶음 선언해서 사용

<details>
    <summary>정수 열거 패턴 - 상당히 취약하다!</summary>
<div markdown="1">

```java
public class Constant{
		public static final int APPLE_FUJI         = 0;
		public static final int APPLE_PIPPIN       = 1;
		public static final int APPLE_GRANNY_SMITH = 2;
		
		public static final int ORANGE_NAVEL  = 0;
		public static final int ORANGE_TEMPLE = 1;
		public static final int ORANGE_BLOOD  = 2;
}
```
</div>
</details>

위와 같이 상수만을 정의하고 클래스를 만들고 외부에서 바로 접근 가능하고, 변하지 않도록 `public static final` 키워드를 통해 정의한다. 이러한 구현 방식을 **정수 열거 패턴**이라고 한다.

#### 정수 열거 패턴의 단점
1. 타입 안전을 보장할 방법이 없다.
2. 문자열로 출력했을 대 때 의미를 알 수 없다.
3. 순회 방법이 까다롭다.

### 타입 안전을 보장할 방법이 없다.
- 상수를 타입으로 구분하지 못하기 때문에 `APPLE_, ORANGE_` 로 시작한다.
- 하지만 이 값을 받는 메서드나 클래스에서는 타입으로 값을 받기 때문에 `int` 타입으로 받게된다.
- 그 결과 오렌지를 건네야 할 메서드에 사과를 보내고 동등 연산자(==)로 비교하더라도 컴파일러는 아무런 경고 메시지를 출력하지 않는다.
    - 의도한 값을 받지 못했기 때문에 추후 로직에서 버그가 발생할 수 있다.

<details>
    <summary>Code</summary>
<div markdown="1">

```java
public class Apple {
    private final int type;

    public Apple(int type) {
        this.type = type;
    }

    public static void main(String[] args) {
        Apple firstApple = new Apple(Constant.APPLE_FUJI);
        Apple secodeApple = new Apple(30); // 컴파일 에러 x
        Apple thirdApple = new Apple(Constant.APPLE_GRANNY_SMITH);
    }
}

```
</div>
</details>

<br>

### 문자열로 출력했을 때 의미를 알 수 없다.
- 상수 값을 출력하거나 이 값을 파라미터로 받은 메서드에서 이를 디버깅하면 **값만 출력**된다.
- 이 상태에서는 ‘APPLE_FUJI’ 라는 의미는 알 수 없고 0 이라는 값만 알 수 있는 것이다.
- 0이 어떤 의미를 가지고 있는지도 함께 알려줄 수 있다면 디버깅 시 값의 의미를 파악하거나 출처를 알기 훨씬 쉬워질 것이다.

<br>

### 순회 방법이 까다롭다.
- 같은 정수 열거 그룹에 속한 모든 상수를 한 바퀴 순회하는 방법도 마땅치 않다.
    - 심지어 그 안에 상수가 몇 개인지도 알 수 없다.
- 만약 현재 사과의 종류를 출력하고 싶다면 개발자가 일일이 하드코딩하거나 리플렉션을 사용해야 한다.
<details>
    <summary>리플렉션 코드</summary>
<div markdown="1">

```java
Class<Constant> constant = Constant.class;

for (Field field : constant.getFields()) {
    if (field.getName().startsWith("APPLE")) {
        System.out.println(field.getName());
    }
}

/* 출력 */
APPLE_FUJI
APPLE_PIPPIN
APPLE_GRANNY_SMITH
```
</div>
</details>

<br>

### 문자열 열거 패턴(String Enum Pattern)
- 정수 대신 문자열 상수를 사용하는 변형 패턴도 있다.
    - 이 변형은 더 나쁘다.
- 상수의 의미를 출력할 수 있다는 점은 좋지만, 문자열 상수의 이름 대신 문자열 값 그대로 하드코딩하게 만들기 때문이다.
- 이렇게 하드코딩한 문자열에 오타가 있어도 컴파일러는 확인할 길이 없으니 자연스럽게 런타임 버그가 생긴다.

<br>

## 2. 열거 타입(Enum Type)
<details>
    <summary>AppleType Enum</summary>
<div markdown="1">

```java
package Effective_java.item34;

public enum AppleType {
    FUJI(0), PIPPIN(1), GRANNY_SMITH(2);

    private final int type;

    AppleType(int type) {
        this.type = type;
    }
}
```
</div>
</details>

- Enum 타입 자체는 클래스이며, 상수 하나당 인스턴스를 하나씩 만들어 `public static final` 필드로 공개하는 방식이다.
- 열거 타입은 밖에서 접근할 수 있는 생성자를 제공하지 않으므로 사실상 `final` 이다.
- 따라서 클라이언트가 **인스턴스를 직접 생성하거나 확장할 수 없으니** 열거 타입 선언으로 만들어진 인스턴스들은 딱 하나씩만 존재함이 보장된다.
    - 즉, 인스턴스가 통제된다.

#### 열거 패턴의 단점을 극복한 Enum 타입

1. 컴파일 타임에서의 타입 안전성을 제공한다.
2. 문자열로 출력했을 때 의미를 알 수 있다.
3. 순회 방법이 간단하다.
4. 메서드나 멤버 필드를 추가할 수 있다.

<br>

### 2.1 컴파일 타임에서의 타입 안정성을 제공한다.

- 이제 상수를 타입으로 구분할 수 있으므로 `Apple` 클래스의 생성자 메서드에서 `int`가 아닌 `AppleType` 타입으로 값을 받을 수 있다.
- 그러므로 `AppleType` 타입이 아닌 다른 타입이 들어올 경우 컴파일 에러가 발생하게 된다.

<details>
    <summary>Example Code</summary>
<div markdown="1">

```java
public class Apple {
    private final AppleType type; // int -> AppleType

    public Apple(AppleType type) {
        this.type = type;
    }

    public static void main(String[] args) {
        Apple firstApple = new Apple(AppleType.FUJI);
        Apple secodeApple = new Apple(30); // 컴파일 에러 o
        Apple thirdApple = new Apple(AppleType.GRANNY_SMITH);
    }
}
```
</div>
</details>

<br>

### 2.2 문자열로 출력했을 때 의미를 알 수 있다.

- Enum은 이름(name)과 값(value)을 함께 가지기 때문에, **단순 숫자보다 디버깅할 때 더 명확한 의미를 알 수 있다**.
- 즉, int 상수 대신 enum을 사용하면 출력했을 대 해당 값이 **무엇을 의미하는지** 알기 쉽다는 뜻

<details>
    <summary>int 상수 사용 시 문제점</summary>
<div markdown="1">

```java
public class Constant {
    public static final int APPLE_FUJI         = 0;
    public static final int APPLE_PIPPIN       = 1;
    public static final int APPLE_GRANNY_SMITH = 2;

    public static final int ORANGE_NAVEL  = 0;
    public static final int ORANGE_TEMPLE = 1;
    public static final int ORANGE_BLOOD  = 2;
}

public class Main{
    public static void main(String[] args) {
        int type = Constant.APPLE_GRANNY_SMITH;
        System.out.println(type);
    }
}

/* 출력 결과 */
```
</div>
</details>

<details>
    <summary>Enum 사용 시 장점</summary>
<div markdown="1">

```java
public enum AppleType {
    FUJI(0), PIPPIN(1), GRANNY_SMITH(2);

    private final int type;

    AppleType(int type) {
        this.type = type;
    }
}

public class Main2 {
    public static void main(String[] args) {
        AppleType appleType = AppleType.FUJI;

        System.out.println(appleType);
        System.out.println(appleType.toString());
    }
}
/* 출력 결과 */
FUJI
FUJI
```
</div>
</details>

<br>

### 2.3 순회 방법이 간단하다.

- Enum 타입은 **정의된 상수 값을 배열로 담아 반환하는 메서드**인 `values`를 제공한다.
  - 값들은 선언된 순서로 저장된다.
- 정의된 상수를 모두 출력하고 싶다면 `values` 메서드를 사용하면 된다.
- 위의 리플렉션을 사용했던 방식에 비해 매우 간단하다.
```java
for (AppleType a : AppleType.values()) { System.out.println(a); }
```

<br>

### 2.4 메서드나 멤버 필드를 추가할 수 있다.

enum은 클래스처럼 동작하므로, 필드(상태)와 메서드(행동)를 함께 가질 수 있다.

예를 들어 각 행성에는 질량과 반지름이 있고, 이 두 속성을 이용해 표면중력을 계산할 수 있다. 어떤 객체의 질량이 주어지면 그 객체가 행성 표면에 있을 때의 무게도 계산할 수 있다.

<details>
    <summary>데이터와 메서드를 갖는 열거 타입</summary>
<div markdown="1">

```java
package Effective_java.item34;

public enum Planet {

    MERCURY(3.303e+23, 2.4397e6),
    VENUS(4.869e+24, 6.0518e6),
    EARTH(5.976e+24, 6.37814e6),
    MARS(6.421e+23, 3.3972e6),
    JUPITER(1.9e+27, 7.1492e7),
    SATURN(5.688e+26, 6.0268e7),
    URANUS(8.686e+25, 2.5559e7),
    NEPTUNE(1.024e+26, 2.4746e7);

    private final double mass;
    private final double radius;
    private final double surfaceGravity;

    private static final double G = 6.67300E-11;

    Planet(double mass, double radius) {
        this.mass = mass;
        this.radius = radius;
        surfaceGravity = G * mass / (radius * radius);
    }

    private double mass() { return mass; }
    private double radius() { return radius;}
    private double surfaceGravity() { return surfaceGravity;}

    public double getSurfaceGravity(double mass) {
        return mass * surfaceGravity;
    }
}
```
</div>
</details>

- 열거 타입 상수 각각을 특정 데이터와 연결지으려면 생성자에서 데이터를 받아 인스턴스 필드에 저장하면 된다.
- 열거 타입은 근본적으로 불변이라 모든 필드는 `final`이어야 한다.
  - 필드를 `public` 으로 선언해도 되지만, `private` 으로 두고 별도의 `public` 접근자 메서드를 두는 게 낫다. (아이템 16)
> `Planet` 상수들은 서로 다른 데이터와 연결되는 데 그쳤지만, 상수마다 동작이 달라져야 하는 상황도 있을 것이다.

<br>

## 3. 상수별 메서드 구현 (constant-specific method implement)

<details>
    <summary>값에 따라 분기하는 열거 타입 - 이대로 만족하는가?</summary>
<div markdown="1">

```java
package Effective_java.item34;

public enum Operation {
    PLUS, MINUS, TIMES, DIVIDE;

    public double apply(double x, double y) {
        switch (this) {
            case PLUS: return x + y;
            case MINUS: return x - y;
            case TIMES: return x * y;
            case DIVIDE: return x / y;
        }
        throw new AssertionError("알 수 없는 연산: " + this);
    }
}
```
</div>
</details>

- 해당 코드는 동작은 하지만 깨지기 쉬운 코드이다. (유지보수가 어렵고, 코드 변경 시 다른 부분에서 쉽게 버그가 발생하는 코드)
- 예를 들어, 새로운 상수를 추가하면 해당 case 문도 추가해야 한다.  

<details>
    <summary>상수별 메서드 구현을 활용한 메서드 타입</summary>
<div markdown="1">

```java
package Effective_java.item34;

public enum Operation2 {
    PLUS { public double apply(double x, double y) { return x + y; } },
    MINUS { public double apply(double x, double y) { return x - y; } },
    TIMES { public double apply(double x, double y) { return x * y; } },
    DIVIDE { public double apply(double x, double y) { return x / y; } };

    public abstract double apply(double x, double y);
}

```
</div>
</details>

- 열거 타입에는 상수 이름을 입력받아 그 이름에 해당하는 상수를 반환해주는 `valueOf(String)` 메서드가 자동 생성된다.
- 열거 타입의 `toString` 메서드를 재정의하려거든, **toString이 반환하는 문자열을 해당 열거 타입 상수로 변환** 해주는 `fromString` 메서드도 함께 고려해보자.

<details>
    <summary>열거 타입용 fromStirng 메서드 구현하기</summary>
<div markdown="1">

```java
public enum Operation {

  // (...)

  private static final Map<String, Operation> stringToEnum = 
		     Stream.of(values())
                           .collect(toMap(Object::toString, e -> e));

  // 지정한 문자열에 해당하는 Operation 을 (존재한다면) 반환한다.
  public static Optional<Operation> fromString(String symbol) {
      return Optional.ofNullable(stringToEnum.get(symbol));
  }

}
```
</div>
</details>

- `Operation` 상수가 `stringToEnum`에 추가되는 시점은 열거 타입 상수 생성 후 정적 필드가 초기화될 때다.
- 열거 타입 상수는 **생성자에서 자신의 인스턴스를 맵에 추가할 수 없다.**
  - 열거 타입의 정적 필드 중 열거 타입의 생성자에서 접근할 수 있는 것은 상수 변수 뿐이다. (아이템 24)

> **열거 타입 초기화 순서**
> 1. 열거 상수 인스턴스 생성 (`Operation.PLUS` 등 상수들이 가장 먼서 생성)
> 2. 정적 필드 초기화 (상수 생성이 끝난 뒤 `stringToEnum` 같은 `static final` 필드가 초기화됨)
> 3. 클래스 로딩 완료 (이 시점 이후에야 `Operation` 을 사용할 수 있음.)

#### 상수별 메서드 구현에는 열거 타입 상수끼리 코드를 공유하기 어렵다는 단점이 있다.

<br>

## 4. 전략 열거 타입 패턴

<details>
    <summary>값에 따라 분기하여 코드를 공유하는 열거 타입 - 좋은 방법인가?</summary>
<div markdown="1">

```java
public enum PayrollDay {

    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;

    private static final int MINUTES_PER_SHIFT = 8 * 60;

    int pay(int minutesWorked, int payRate) {
        int basePay = minutesWorked * payRate;

        int overtimePay;

        switch (this) {
            case SATURDAY:
            case SUNDAY:
                overtimePay = basePay / 2;
                break;
            default:
                overtimePay = minutesWorked <= MINUTES_PER_SHIFT ? 0 : (minutesWorked - MINUTES_PER_SHIFT) * payRate / 2;
        }

        return basePay + overtimePay;
    }

}
```
</div>
</details>

- 휴가와 같은 새로운 값을 열거 타입에 추가하려면 그 값을 처리하는 case 문을 잊지 말고 쌍으로 넣어줘야 한다.

<details>
    <summary>전략 열거 타입 패턴</summary>
<div markdown="1">

```java
public enum PayrollDay {

    MONDAY(PayType.WEEKDAY), TUESDAY(PayType.WEEKDAY), WEDNESDAY(PayType.WEEKDAY), THURSDAY(PayType.WEEKDAY),
    FRIDAY(PayType.WEEKDAY), SATURDAY(PayType.WEEKEND), SUNDAY(PayType.WEEKEND);

    private final PayType payType;

    PayrollDay(PayType payType) {
        this.payType = payType;
    }

    int pay(int minutesWorked, int payRate) {
        return payType.pay(minutesWorked, payRate);
    }

    // 전략 열거 타입
    enum PayType {
        WEEKDAY {
            @Override
            int overtimePay(int minutesWorked, int payRate) {
                return minutesWorked <= MINUTES_PER_SHIFT ? 0 : (minutesWorked - MINUTES_PER_SHIFT) * payRate / 2;
            }
        },
        WEEKEND {
            @Override
            int overtimePay(int minutesWorked, int payRate) {
                return minutesWorked * payRate / 2;
            }
        };

        abstract int overtimePay(int minutesWorked, int payRate);

        private static final int MINUTES_PER_SHIFT = 8 * 60;

        int pay(int minutesWorked, int payRate) {
            int basePay = minutesWorked * payRate;
            return basePay + this.overtimePay(minutesWorked, payRate);
        }
    }
}
```
</div>
</details>

- 새로운 상수를 추가할 때 잔업수당 ‘전략’을 선택하도록 하는 것이다.
- 추가하려는 메서드가 의미상 열거 타입에 속하는 경우 위와 같이 전략 열거 타입 패턴을 사용한다.
- 그렇지 않은 경우에는 `switch`를 적용해서 간단하게 만든다.

<details>
    <summary>switch 문을 이용해 원래 열거 타입에 없는 기능을 수행한다.</summary>
<div markdown="1">

```java
public static Operation inverse(Operation op) {
    switch (op) {
        case PLUS:
            return Operation.MINUS;
        case MINUS:
            return Operation.PLUS;
        case TIMES:
            return Operation.DIVIDE;
        case DIVIDE:
            return Operation.TIMES;

        default:
            throw new AssertionError("알 수 없는 연산: " + op);
    }
}
```
</div>
</details>

- 기존 열거 타입에 상수별 동작을 혼합해 넣을 때는 switch 문이 좋은 선택이 될 수 있다.

<br>

## 5. 열거 타입을 언제 쓰란 말인가?

- 필요한 원소를 컴파일타임에 다 알 수 있는 상수 집합이라면 항상 열거 타입을 사용하자.
  - ex) 태양계 행성, 한 주의 요일, 체스 말 등
- 열거 타입에 정의된 상수 개수가 영원히 고정 불변일 필요는 없다.