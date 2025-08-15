### ✅ 변경 가능성을 최소화하라

불변 클래스
불변 클래스는 한번 생성되면 그 내부의 상태를 절대 변경할 수 없는 클래스를 말합니다.
즉 인스턴스에 저장된 정보는 객체가 소멸될 때까지 항상 동일하게 유지합니다.

대표적인 예시로는 자바의 String 클래스, 기본 타입의 값을 감싸는 박싱된 클래스들(`Integer`, `Double` 등등) 그리고 `BigInteger`, `BigDecimal` 등이 있습니다.
불변 클래스는 가변 클래스보다 예상치 못한 값 변경으로부터 안전하기 때문에 오류가 발생할 여지가 적습니다.

불변 클래스를 만드는 핵심 규칙

1. 객체의 상태를 변경하는 메소드를 제공 ❌ ex) Setter
2. 클래스를 확장할 수 없도록 `final`로 설정
3. 모든 필드를 final로 선언
4. 모든 필드를 private로 선언하여 참조 타입이면 내부에서 변경하는 걸 방지할 수 있습니다.
5. 자신 외에는 내부의 가변 컴포넌트(객체)에 접근할 수 없도록 합니다.

### 불변 클래스 필드를 public 선언 시 문제 상황

```java
public class RoleTest {

    public final List<String> publicList = new ArrayList<>();
    private final List<String> privateList = new ArrayList<>();

    public List<String> getPrivateList() {
        return privateList;
    }
}

@Test
void test() {

    RoleTest publicList = new RoleTest();
    RoleTest privateList = new RoleTest();

    publicList.publicList.add("추가");
    privateList.privateList.add("컴파일 오류 발생");

    privateList.getPrivateList().add("컴파일 오류 미 발생");

}    
```

### 가변 객체에 접근 상황

```java
public final class Period {
    private final Date start;
    private final Date end;

    public Period(Date start, Date end) {
        this.start = start;
        this.end = end;
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }
}

@Test
void test() {
    Date start = new Date();
    Date end = new Date();

    // 변경 전
    System.out.println(period.getEnd());

    Period period = new Period(start, end);

    // get 메서드로 반환된 원본 Date 객체를 외부에서 수정 가능
    period.getEnd().setYear(123);

    // 변경 후
    System.out.println(period.getEnd());
}

// 개선 코드
public final class Period {
    private final Date start;
    private final Date end;

    public Period(Date start, Date end) {
        this.start = new Date(start.getTime());
        this.end = new Date(end.getTime());
    }

    public Date getStart() {
        return new Date(start.getTime());
    }

    public Date getEnd() {
        return new Date(end.getTime());
    }
}
```

생성자와 `getter` 메서드를 통해 [방어적 복사](https://tecoble.techcourse.co.kr/post/2021-04-26-defensive-copy-vs-unmodifiable/)를 적용해여
외부에서 원본 객체에 직접 접근하는 것을 막을 수 있습니다.

```java
public final class Complex {
    private final double re; // 실수
    private final double im; // 허수: 제곱했을 때 음수가 되는 수

    public Complex(double re, double im) {
        this.re = re;
        this.im = im;
    }

    public double imaginaryPary() {
        return im;
    }

    public double realPart() {
        return re;
    }

    public Complex plus(Complex c) {
        return new Complex(re + c.re, im + c.im);
    }

    public Complex minus(Complex c) {
        return new Complex(re - c.re, im - c.im);
    }

    public Complex times(Complex c) {
        return new Complex(re * c.re - im * c.im,
                re * c.im - im * c.re);
    }

    public Complex divideBy(Complex c) {
        double tmp = c.re * c.re + c.im * c.im;
        return new Complex((re * c.re + im * c.im) / tmp,
                (im * c.re - re * c.im) / tmp);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Complex)) {
            return false;
        }
        Complex c = (Complex) o;
        return Double.compare(c.re, re) == 0 &&
                Double.compare(c.im, im) == 0;
    }

    @Override
    public int hashCode() {
        return 31 * Double.hashCode(re) + Double.hashCode(im);
    }

    @Override
    public String toString() {
        return "(" re + " + " + im + "i)";
    }

}
```

메소드들을 보면 새로운 인스턴스를 변환하는 모습을 볼 수 있습니다. 이는 피연산자를 직접 수정하지 않는 함수형 프로그래밍 패턴을 이용한 방식이며, 불변 객체를 메소드을 생성할 때 핵심을 잘 지키고 있습니다.
이러한 불변성을 강조하기 위해 메소드 이름에도 신경 쓴 점을 주목해 볼 수 있습니다. add 동사 대신 plus 같은 전치사를 사용하여 해당 메소드가 객체의 값을 변경하지 않고 새로운 객체를 반환한다는 사실을 메소드
명으로 전달하고 있습니다.

자바의 BigInteger와 BigDecimal 클래스도 불변 객체입니다. 앞서 말한 내용을 모르고 사람들이 잘못 사용해 오류가 발생하는 일이 자주 있습니다.

````java
public class Test {

    public static void main(String[] args) {
        BigInteger total = new BigInteger("100");
        total.add(new BigInteger("50"));

        System.out.println(total); // 결과 값으로 150이 아닌 100이 나옴
    }
}
````

어떻게 보면 `BigInteger`와 `BigDecimal` 클래스는 `plus`, `minus와` 같은 전치사를 사용하여 새로운 객체를 반환한다는 이상적인 메소드 명명 규칙을 안 지키고 있습니다.
해당 규칙을 안 지켰다고 무조건 틀렸다고는 볼 수는 없습니다. 이러한 규칙보다는 일관성을 더 우선순위로 보면 `add`, `subtract`와 같은 메소드 명이 더 좋은 메소드 명일수도 있습니다.

### 불변 객체의 장점

불변 객체는 생성되고 그 상태나 값이 항상 일정하다는 점이 근본이 여러 장점이 발생합니다.

- **재사용과 효율성:** 불변 클래스는 동일한 인스턴스 값을 중복해서 만들지 않는 정적 팩터리 메서드를 제공할 수 있습니다.
  예를 들어, `Integer.valueOf(10)` 메서드는 10이라는 값을 가진 `Integer` 인스턴스가 이미 존재하면 새로 생성하지 않고 기존 객체를 재활용합니다.
- **스레드 안정성:** 불변 객체는 값이 변하지 않기 때문에 멀티스레드 환경에서 동기화(synchronization) 없이도 안전하게 사용할 수 있습니다.
  여러 스레드가 동시에 같은 객체에 접근하더라도 객체의 상태가 변경될 염려가 없어 안심하고 공유할 수 있습니다.
- **방어적 복사 불필요:** 불변 객체는 값이 항상 일정하므로 가변 객체처럼 clone() 메서드나 복사 생성자를 통해 방어적 복사를 할 필요가 없습니다.
  이는 불필요한 객체 생성 비용을 줄이고 코드를 더 간결하게 만듭니다.
  String 클래스에 존재하는 복사 생성자`(new String(original))`는 자바 초창기에 만들어진 것으로,
  불변 객체의 특성을 제대로 활용하지 못하는 불필요한 기능이므로 사용하지 않는 것이 좋습니다.

**불변 객체의 내부 데이터 공유**
불변 객체는 상태가 변하지 않기 때문에, 불변 객체끼리 내부 데이터를 안전하게 공유할 수 있다는 큰 장점이 있습니다.

`BigInteger` 클래스가 이 원칙을 잘 보여주는 예시입니다. `BigInteger`는 값의 부호(sign)를 나타내는 int 변수와 크기(magnitude)를 나타내는 int 배열을 사용합니다.
negate 메서드는 기존 BigInteger와 부호만 다르고 크기는 같은 새로운 BigInteger를 생성합니다.
이때 int 배열은 가변(mutable) 객체이지만, 원본 인스턴스가 가리키는 배열을 복사하지 않고 그대로 공유해도 안전합니다.
원본 인스턴스 자체가 불변이므로 배열의 내용이 변경될 염려가 없기 때문입니다.

```java
public final class Example {
    private final int sign;
    private final int[] magnitude; // 가변 객체

    public Example(int sign, int[] magnitude) {
        this.sign = sign;
        this.magnitude = Arrays.copyOf(magnitude, magnitude.length);
    }

    public int getSign() {
        return sign;
    }

    // 기존 배열을 그대로 공유 및 새로운 인스턴스 생성
    public Example negate() {
        return new Example(-sign, this.magnitude);
    }

    @Override
    public String toString() {
        return "Sign: " + sign + ", Magnitude: " + Arrays.toString(magnitude);
    }

    public int[] getMagnitude() {
        return magnitude;
    }
}

public class Main {
    public static void main(String[] args) {
        int[] originalArray = {1, 2, 3};
        Example original = new Example(1, originalArray);

        System.out.println("원본 객체: " + original) // 출력: 원본 객체: Sign: 1, Magnitude: [1, 2, 3]

        Example negated = original.negate();

        System.out.println("새로운 객체: " + negated); // 출력: 새로운 객체: Sign: -1, Magnitude: [1, 2, 3]

        // 원본과 새로운 객체가 동일한 배열 인스턴스를 공유하고 있는지 확인
        System.out.println("두 객체가 같은 배열을 공유하는가? " +
                (original.getMagnitude() == negated.getMagnitude()));
    }
}
```

### 불변 객체의 단점: 성능과 리소스 낭비

불변 객체는 상태가 변하지 않아 안전하다는 큰 장점이 있지만 **값이 다를 때마다 새로운 객체를 생성**해야 한다는 점 때문에 불변 객체를 많이 생성해야 되는 상황도 나올 수 있습니다.

예를 들어, `BigInteger`객체에서 단 한 개의 비트를 변경해야 한다고 가정해 봅시다. `BigInteger`는 불변 클래스이므로 `flipBit(0)`와 같은 메서드는 새로운 인스턴스를 생성하여
반환합니다. 원본과 단 한 비트만 다른 아주 큰 비트짜리 새로운 인스턴스를 만드는 연산은 많은 리소스를 소모하게 됩니다.

```java
BigInteger original = new BigInteger("1000000000000000");
BigInteger modified = original.flipBit(0);
```

이러한 문제에 대응하기 위해 2가지 대처 방법이 있습니다.

1. **가변 동반 클래스**  
   일반적으로 불변 클래스에 단점을 보완하기 위해 가변 동반 클래스가 존재합니다. 이 가변 클래스는 다단계 연산을 효율적으로 처리하고 
   최종적으로 불변 객체로 변환하는 역할을 합니다. 가장 대표적인 예시가 바로 `String`(불변 객체)과 `StringBuilder`(가변 동반 클래스) 입니다.

```java
String str = "Hello";
str +=" World"; // 새로운 객체 생성
        System.out.

println(str);

StringBuilder sb = new StringBuilder("Hello");
mutableString.

append(" World"); // 기존 객체에 문자열 추가
System.out.

println(sb.toString()); 
```

2. **가변 클래스**  
   `BigInteger`와 같은 불변 객체는 `flipBit` 메소드를 실행할 때 새로운 객체를 생성합니다. 이 과정에서 O(n) 시간 복잡도가 발생합니다.
   반면 `BitSet`(가변 클래스)의 `flipBit`은 기존 객체 데이터를 수정하기 때문에 새로운 객체를 만들 필요가 없습니다. 덕분에 O(1)으로 매우 빠르게 작업을 처리할 수 있습니다.


### 불변 클래스 생성 시 설계 방법
불변 클래스를 설계할 때 `final` 클래스로 선언하거나 생성자를 `private` 또는 `package-private`으로 만드는 것 외에 정적 팩터리를 사용하는 방법이 있습니다.

불변 클래스는 값이 변하지 않기 때문에 동일한 값을 가진 인스턴스를 중복 생성할 필요가 없습니다. 
정적 팩터리는 바로 이 점을 활용하여 자주 사용되는 인스턴스를 캐싱(caching)하여 재사용합니다. 이러한 대표적인 예시가 `Integer.valueOf(int i)` 메서드입니다.
```java
@IntrinsicCandidate
    public static Integer valueOf(int i) {
        if (i >= IntegerCache.low && i <= IntegerCache.high)
            return IntegerCache.cache[i + (-IntegerCache.low)];
        return new Integer(i);
    }
```
이처럼 정적 팩터리를 사용하면 여러 클라이언트가 같은 인스턴스를 공유하게 되어 메모리 사용량이 줄어들고 가비지 컬렉션(GC) 비용도 절감됩니다.
