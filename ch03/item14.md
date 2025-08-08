### ✅ Comparable을 구현할지 고려하라

## 1. Comparable
Comparable 인터페이스에는 compareTo 메서드가 있다.

`compareTo`의 성격은 두 가지만 빼면 Object의 `equals`와 같다.
- 단순 동치성 비교에 더해 순서까지 비교할 수 있다.
- 제네릭하다.

`Comparable` 을 구현했다는 것은 그 클래스의 인스턴스들에는 자연적인 순서가 있음을 뜻한다. 검색, 극단값 계산, 자동 정렬되는 컬렉션 관리도 쉽게 할 수 있다.

그러므로 **알파벳, 숫자, 연대 같이 순서가 명확한 값 클래스**를 작성한다면 반드시 `comparable` 인터페이스를 구현하자.

### 1.1 단순 동치성 비교에 더해 순서까지 비교할 수 있다.

- `equals()`는 오직 "같다/다르다"로 두 객체가 논리적인 동일한 값이냐만 판단한다.
    ```java
    a.eqauls(b); // true or false
    ```

- `compareTo()`는 객체 사이의 "순서"를 정의할 수 있다.
    ```java
    a.compareTo(b); // 음수 or 0 or 양수
    ```

```java
String a = "apple";
String b = "banana";

System.out.println(a.compareTo(b)); // 음수 -> "apple"이 "banana"보다 앞에 있다.
```
`compareTo`는 `equals()` 보다 더 정렬 친화적인 메서드로 `TreeSet`, `TreeMap`, `Arrays.sort()` 같은 정렬 기반
컬렉션 및 메서드는 내부적으로 `compareTo()`를 사용한다.

### 1.2 제네릭하다 (타입 안전)

- equals(Object o)는 모든 타입을 허용한다.
  ```java
  public boolean equals(Object obj);
  ```
- `compareTo(T o)`는 제네릭이 적용된 타입만 허용한다.
  ```java
  public interface Comparable<T> {
        int compareTo(T o);
  }
  ```
  즉, `compareTo`의 매개변수는 **동일 타입 T** 만 허용한다. 

## 2. compareTo 메서드의 일반 규약
- 두 객체의 참조 순서를 바꿔 비교해도 예상한 결과가 나와야 한다.
- 첫 번째가 두 번째보다 크고 두 번째가 세 번째보다 크면, 첫 번째는 세 번째보다 커야 한다.(즉, 추이성을 보장해야 한다.)
- 크기가 같은 객체들끼리는 어떤 객체와 비교하더라도 항상 같아야 한다.
- (필수 X) `compareTo` 메서드로 수행한 동치성 테스트 결과가 `equals`의 결과와 같아야 한다.

(필수 X)를 제외한 세 규약은 `compareTo` 메서드로 수행하는 동치성 검사도 `equals` 규약과 똑같이 반사성, 대칭성, 추이성을 충족해야 함을 뜻한다.

### 2.1 Comparable 주의사항

기존 클래스를 확장한 구체 클래스에서 새로운 값 컴포넌트를 추가했다면 `compareTo` 규약을 지킬 수 없다.
```java
public class Point implements Comparable<Point> {
protected int x, y;

    public int compareTo(Point other) {
        int result = Integer.compare(this.x, other.x);
        return (result != 0) ? result : Integer.compare(this.y, other.y);
    }
 }
 
public class ColorPoint extends Point {
  private String color;


  @Override
  public int compareTo(Point other) {
      
      ...

      // 문제가 되는 부분
      if (other instanceof ColorPoint cp) {
          return this.color.compareTo(cp.color);
      }
  
      return 0;
  }

}
```
`ColorPoint`는 `color` 까지 고려하고 싶어 `compareTo` 를 재정의 하였다.

하지만 `new ColorPoint(1, 2, "red");`, `new Point(1, 2);` 이 두 객체는 `compareTo` 상으로는 같다고 나오지만, 실제로는 color가 다르므로
진짜 동등하지 않다. `equals` 나 다른 맥락에서는 다르게 취급될 수 있다.

`Comparable` 을 구현한 클래스를 확장해 값 컴포넌트를 추가하고 싶다면, 확장하는 대신 독립된 클래스를 만들고,
이 클래스에 원래 클래스의 인스턴스를 가리키는 필드를 두자.

그런 다음 내부 인스턴스를 반환하는 '뷰' 메서드를 제공하면 된다.

이렇게 하면 바깥 클래스에 `compareTo` 메서드를 구현해 넣을 수 있다.

### 2.2 compareTo의 마지막 규약

`compareTo` 메서드로 두 객체를 비교했을 때 0을 반환한다면, 즉 두 객체가 동치라고 판단된다면, `equals` 메서드 역시
true를 반환하도록 구현하는 것이 바람직 하다. 이는 필수 조건은 아니지만, `compareTo`와 `equals`의 결과가 다를 경우
문제를 일으킬 수 있다. 

Java의 정렬된 컬렉션들, 예를 들어 `TreeSet`이나 `TreeMap`은 요소의 동치성을 판단할 때 `equals`가 아니라
`compareTo`의 결과를 기준으로 사용한다. 

따라서 이 둘의 결과가 불일치하면, 논리적으로 같은 객체가 중보긍로 저장되거나, 동일한 키를 다른 키로 인식하는 등
예상과 다른 동작이 발생할 수 있다.

이러한 이유로 `Comparable` 인터페이스를 구현하는 클래스는 `compareTo`와 `equals`가 일관되게 동작하도록 작성하는 것이 좋다.
`compareTo`로 동치라고 판단되는 객체라면, `equals` 역시 true를 반환해야 컬렉션의 동작이 올바르게 유지된다.

## 3. compareTo 메소드 작성 요령

- `Comparable`은 타입을 인수로 받는 제네릭 인터페이스이므로 타입을 확인하거나 형변환할 필요가 없다.
  - 인수의 타입이 잘못됐다면 컴파일 자체가 되지 않는다.
- `compareTo` 메서드는 각 필드가 동치인지를 비교하는 게 아니라 그 순서를 비교한다.
  - 객체 참조 필드를 비교하려면 `compareTo` 메서드를 재귀적으로 호출한다.
- `Comparable`을 구현하지 않은 필드나 표준이 아닌 순서로 비교해야 한다면 비교자(Comparator)를 대신 사용한다.
  - 비교자는 직접 만들거나 자바가 제공하는 것 중 골라 쓰면 된다.
- `compareTo()` 에서는 관계 연산자 `< `와 `>` 를 사용하는 이전 방식은 거추장스럽고 오류를 유발하므로 박싱 기본 타입 클래스들의 정적 메서드 `compare()`을 사용하자.
- 핵심 필드부터 비교해 나가서 순서가 먼저 결절되면 곧장 반환하자.

## 4. Comparator 인터페이스

일반적으로 `Comparable.compareTo`를 사용하는 메서드는 `Comparator`의 `compare` 메서드를 사용할 수 있도록 구현되어있다.
(ex: Collections.sort 또는 Arrays.sort) 따라서 미리 구현되어 있는 `compareTo` 방식이 아닌 새로운 방식으로 순서를 지정하고
싶다면 `Comparator`를 구현하면 된다.

자바 8에는 `Comparator` 인터페이스가 일련의 비교자 생성 메서드를 이용하여 비교자를 생성할 수 있게 되었다.

이 방식은 간결하지만, 약간의 성능 저하가 뒤따른다. 아래는 예시이다.
```java
public static void main(String[] args) {
    String arr[] = { "banana", "apple", "google", "aws" };

    Arrays.sort(arr, new Comparator<String>() {
        public int compare(String o1, String o2) {
            return o2.compareTo(o1); // o1을 o2와 비교한 것이 아닌 o2를 o1과 비교
        }
    });

    System.out.printf("%s\n", Arrays.toString(arr)); // [google, banana, aws, apple]

}
```
`Arrays.sort`에 새롭게 구현한 `Comparator` 구현체를 사용하였다. `o2.compareTo(o1)`을 사용하여 사전순이 아닌 역사전순으로 비교하도록 하였다.


```java
import java.util.Comparator;

private static final Comparator<PhoneNumber> COMPARATOR = 
    Comparator.comparingInt((PhoneNumber pn) -> pn.areaCode)
            .thenComparingInt(pn -> pn.prefix)
            .thenComparingInt(pn -> pn.lineNum);

@Override
public int compareTo(PhoneNumber pb){
  return COMPARATOR.compare(this, pn);
}
```

이 코드는 `PhoneNumber` 객체를 비교하기 위해 미리 하나의 `Comparator`를 만들어 두고, `compareTo` 메서드에서 그 비교자를 재사용하는 방식이다.
클래스가 초기화될 때 `comparingInt` 와 `thenComparingInt` 라는 두 가지 비교자 생성 메서드를 조합해 최종 비교자를 만든다

`comparingInt`는 객체 참조를 `int` 타입 키로 매핑하는 키 추출 함수를 인수로 받아, 그 키를 기준으로 순서를 정하는 
비교자를 반환하는 정적 메서드이다.

`thenComparingInt`는 `Comparator`의 인스턴스 메서드로, 현재 비교 결과가 같을 때 사용할 추가적인 `int` 키 추출 함수를 받아 새로운 비교자를 반환한다. 
이 메서드는 원하는 만큼 연속해서 호출할 수 있으며, 제네릭 타입을 명시하지 않아도 **자바의 타입 추론**이 자동으로 처리해준다. 이 코드에서는 `prefix` 와 `lineNum` 순으로 추가 비교가 이루어진다.

이렇게 작성하면 `compareTo` 메서드 안에서 복잡한 비교 로직을 직접 구현할 필요 없이, 미리 정의된 비교자를 호출하는 것만으로 정렬 기준을
일관되고 간결하게 유지할 수 있다.

### 4.1 기본 타입

`Comparator`는 `long`과 `double` 타입을 위한 `comparingLong`, `thenComparingLong`, `comparingDouble`, `thenComparingDouble` 메서드를 제공한다.

`short`, `byte`, `char`와 같이 더 작은 정수 타입은 내부적으로 `int`로 변환해 `int`용 메서드를 사용하면 되고,
`float` 타입은 `double`용 메서드를 사용해 비교를 수행하면 된다.

### 4.2 객체 참조 타입

`Comparator`는 객체 참조 타입을 비교하기 위한 메서드로 `comparing`과 `thenComparing`을 제공한다.

`comparing`은 정적 메서드로 두 가지 형태가 다중 정의되어 있다. 첫 번째 형태는 키 추출 함수를 받아 해당 키의 자연 순서를 기준으로 비교자를 생성한다.
두 번째 형태는 키 추출 함수와 함께 키 비교자도 받아, 추출한 키를 지정한 방식으로 비교할 수 있도록 한다.
```java
// 1. 키 추출 함수만 받는 형태
Comparator<Person> c = Comparator.comparing(Person::getName);

// 2. 키 추출 함수와 키 비교자 둘 다 받는 형태
Comparator<Person> c = Comparator.comparing(Person::getName, String.CASE_INSENSITIVE_ORDER);
```

`thenComparing`은 인스턴스 메서드로 세 가지 형태가 다중정의되어 있다. 첫 번째는 다른 비교자를 직접 전달하는 방식이고,
두 번째는 키 추출 함수만 전달해 자연 순서로 비교하는 방식, 세 번째는 키 추출 함수와 키 비교자를 함께 전달해 원하는 비교 방식으로 처리하는 방식이다.
```java
// 1. 다른 비교자 전달
c = c.thenComparing(Comparator.comparing(Person::getAge));

// 2. 키 추출 함수만 전달
c = c.thenComparing(Person::getAge);

// 3. 키 추출 함수와 키 비교자 둘 다 전달
c = c.thenComparing(Person::getAge, Comparator.reverseOrder());
```

즉, `comparing` 계열 메서드는 첫 비교 기준을 만들 때, `thenComparing` 계열 메서드는 추가 비교 기준을 불일 때 사용하며,
두 경우 키 추출 함수만 주거나 키 추출 함수와 비교자를 함께 줄 수 있도록 오버로딩되어 있다.

## 5. Comparable vs Comparator 정리

1. Comparable
   - 패키지: `java.lang`
   - 비교 로직 위치: 클래스 내부
   - 메서드: `compareTo(T o)`
   - 특징
     - 객체 자체가 "기본 정렬 기준"을 가진다.
     - 한 클래스에 정렬 기준 1개만 정의 가능
     - 자연 순서(Natural Ordering) 제공
   - 예시:
     ```java
      public class Person implements Comparable<Person> {
          private String name;
          @Override
          public int compareTo(Person other) {
                return this.name.compareTo(other.name); // 이름 오름차순
          }
      }
      ```

2. Comparator
   - 패키지: `java.util`
   - 비교 로직 위치: 별도 클래스 (혹은 람다)
   - 메서드: `compare(T o1, T o2)`
   - 특징
     - 객체 외부에서 비교 로직 정의
     - 여러 정렬 기준 자유롭게 생성 가능
     - 원본 클래스 수정 불필요
     - 예시:
       ```java
        Comparator<Person> byNameDesc =
             (p1, p2) -> p2.getName().compareTo(p1.getName());
        list.sort(byNameDesc);
       ```
[구체적인 동작](https://st-lab.tistory.com/243)