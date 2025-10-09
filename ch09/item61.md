### ✅ 박싱된 기본 타입보다는 기본 타입을 사용하라

Java에서 오토박싱(auto-boxing)과 오토언박싱(auto-unboxing)을 지원한다고 해서 두 타입 간의 차이가 사라지는 것은 아닙니다.
기본 타입과 래퍼 클래스는 분명한 차이를 가지고 있으며, 어떤 타입을 사용하는지 상당히 중요합니다.

### 기본 타입과 래퍼 클래스의 차이점

1. 기본 타입은 값만 가지고 있으나, 래퍼 클래스는 값에 더해 식별성이란 속성을 갖는다.
   쉽게 이야기하면 래퍼 클래스는 값이 같아도 인스턴스가 다르면 다르다고 식별할 수 있습니다.
2. 기본 타입의 값은 언제나 유효하나, 래퍼 클래스는 유효하지 않은 값 `null`을 가질 수 있습니다.
3. 기본 타입이 래퍼 클래스보다 시간과 메모리 사용면에서 더 효휼적입니다.

### 래퍼 클래스 사용 시 문제- 객체 참조 비교

```java
Comprator<Integer> naturalOrder = (i, j) -> (i < j) ? -1 : (i == j ? 0 : 1);
```

해당 코드에는 별 문제가 없어 보이겠지만 `naturalOrder.compare(new Integer(42), new Integer(42))`의 값을 출력하면 0이 될 것 같지만 실제로는 1을 출력됩니다.

그 이유는 비교식(`i<j`)에서는 오토박싱 되어서 정상적으로 동작하지만 두번째 비교식인 `i == j`에서는 값이 아닌 **객체 참조 값을 비교**되기 때문입니다.  
두 객체는 서로 다른 인스턴스이므로 `false` 값이 나와서 최종적으로 1이 출력됩니다.

즉, 래퍼 클래스에서 `==` 연산자를 사용하면 값이 아닌 참조 값을 비교하게 되어서 오류가 발생합니다.

해당 문제를 해결하기 위해서 오토 박싱으로 기본 타입으로 저장한 다음 비교해서 해결할 수 있습니다.

### 래퍼 클래스 사용 시 문제 - 자동 언박싱의 함정

아래 코드는 아무런 문제가 없어 보이지만, 컴파일 에러가 아닌 실행 시 예외가 발생합니다.

```java
static Integer i;

public static void main(String[] args) {
    if (i == 42) {
        System.out.println("믿을 수 없군!");
    }
}
```

해당 코드는 `i==42`를 검사할 때 `NullPointException` 예외를 발생시킵니다. 그 이유는 `i`가 `int`가 아닌 `Integer`이며, `i`의 초깃값이 `null`이기 때문입니다.  
즉, `i==42`는 `Integer`와 `int`를 비교하는 구문으로 **기본 타입과 래퍼 타입이 혼합된 연산에서는 거의 예외 없이 래퍼 타입이 자동으로 언박싱**됩니다.
그런데 `null` 참조를 언박싱하려 하면 `NullPointException`예외가 발생하게 됩니다.

### 래퍼 클래스 사용 시 문제 - 성능 문제

```java

@Test
void before() {
    Long sum = 0L;

    long start = System.currentTimeMillis();
    for (long i = 0; i <= Integer.MAX_VALUE; i++) {
        sum += i;
    }

    long end = System.currentTimeMillis();
    long result = end - start;

    System.out.println("실행 시간: " + result + " ms (" + (result / 1000.0) + " 초)"); // 실행 시간: 6290 ms (6.29 초)
}


@Test
void after() {
    long sum = 0L;

    long start = System.currentTimeMillis();
    for (long i = 0; i <= Integer.MAX_VALUE; i++) {
        sum += i;
    }

    long end = System.currentTimeMillis();
    long result = end - start;

    System.out.println("실행 시간: " + result + " ms (" + (result / 1000.0) + " 초)"); // 실행 시간: 555 ms (0.555 초)
}
```

오토 박싱, 오토 언박싱이 계속 일어나면서 성능 저하가 발생합니다.

### 그럼 래퍼 클래스는 언제 써야 하는가??

1. 컬렉션의 원소, 키, 값으로 사용(제네릭 타입 파라미터에 사용)
2. 리플렉션을 통해 메소드를 호출할 때도 래퍼 클래스를 사용