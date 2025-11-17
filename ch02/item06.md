### ✅ 불필요한 객체 생성을 피하라

똑같은 기능의 객체를 매번 생성하기보다는 객체 하나를 재사용하는 편이 낫다. 재사용은 빠르고 세련됐으며, 불변 객체는 언제든 재사용할 수 있다.

<br>

### 문자열 객체 생성
```java
String s = new String("example");
```
- 실행될 때마다 **`String` 인스턴스를 새로 만든다**. (쓸때없는 행위)
    - 생성자에 넘겨진 `"example"`가 이 생성자를 만들어내려는 `String`과 기능적으로 완전히 똑같다.
- 반복문이나 빈번히 호출되는 메서드 안에 있다면 쓸때없는 `String` 인스턴스가 수백만 개 만들어질 수 있다.


```java
String s = "example";
```
- 새로운 인스턴스를 매번 만드는 대신 하나의 `String` 인스턴스를 사용
- 이 방식은 같은 JVM 안에서 똑같은 문자열 리터럴을 사용하는 모든 코드가 같은 객체를 재사용함이 보장된다.

<br>

### 정적 팩터리 메서드 사용
- 생성자 대신 정적 팩터리 메서드를 제공하는 불변 클래스에서는 정적 팩터리 메서드를 사용해 불필요한 객체 생성을 피할 수 있다.
    - `Boolean(String)` 생성자 대신 `Boolean.valueOf(String)` 팩터리 메서드를 사용하는 것이 좋다.
    - 생성자는 호출할 때마다 새로운 객체를 만들지만, 팩터리 메서드는 그렇지 않기 때문이다.
  ```java
  // 정적 팩터리 메서드
  Boolean b1 = Boolean.valueOf("true");
  Boolean b2 = Boolean.valueOf("true");

  System.out.println(b1 == b2); // true

  // 생성자
  Boolean b1 = new Boolean("true");
  Boolean b2 = new Boolean("true");
  
  System.out.println(b1 == b2); // false
  ```

<br>

### 비싼 객체
생성 비용이 비싼 객체도 더러 있다.

비싼 객체라는 말은 인스턴스를 생성하는데 드는 비용이 크다는 의미이다. 메모리, 디스크 사용량, 대역폭 등이 높을수록 생성 비용이 비싸다고 한다.

이런 '비싼 객체'가 반복해서 필요하다면 **캐싱**하여 재사용 하길 바란다.
하지만 자신이 만든 객체가 비싼 객체인지를 명확히 알 수 없다.

```java
static boolean isRomanNumeralSlow(String s) {
    return s.matches("^(?=.)M*(C[MD]|D?C{0,3})"
            + "(X[CL]|L?X{0,3})(I[XV]|V?I{0,3})$");
}
```
- 이 방식의 문제는 `String.matches` 메서드를 사용한다는 점이다.
  - `String.matches`는 정규 표현식으로 문자열 형태를 확인하는 가장 쉬운 방법이지만, 성능이 중요한 상황에서 반복해
    사용하기엔 적합하지 않다.
- 이 메서드가 내부에서 만드는 정규표현식용 `Pattern` 인스턴스는, 한 번 쓰고 곧바로 버려저서 가비지 컬렉션 대상이 된다.
    - `Pattern`은 유한 상태 머신(finite state machine)을 만들기 때문에 인스턴스 생성 비용이 높다.

```java
import java.util.regex.Pattern;

public class RomanNumerals {
  private static final Pattern ROMAN = Pattern.compile(
          "^(?=.)M*(C[MD]|D?C{0,3})"
                  + "(X[CL]|L?X{0,3})(I[XV]|V?I{0,3})$");

  static boolean isRomanNumeralFast(String s) {
    return ROMAN.matcher(s).matches();
  }
}
```
- 성능을 개선하려면 `Pattern` 인스턴스를 클래스 초기화(정적 초기화) 과정에서 직접 생성해 **캐싱**해두고, 나중에
  `isRomanNumeral` 메서드가 호출될 때마다 이 인스턴스를 재사용한다.
- 개선 전에는 존재조차 몰랐던 Pattern 인스턴스를 static final 필드로 끄집어내고 이름을 지어주어 코드의 의미가 훨씬 더 잘 드러난다.
    - 하지만 이 메서드를 한 번도 호출하지 않는다면 ROMAN 필드는 쓸데없이 초기화된 꼴이다.
    - 지연 초기화(lazy initialization)로 불필요한 초기화룰 없앨 수 있지만, 권장하지 않는 방식이다.

<br>

### 어댑터
- 객체가 불변이라면 재사용해도 안전함이 명백하다. 하지만 훨씬 덜 명확하거나, 심지어 직관에 반대되는 상황도 있다.
- **어댑터**는 실제 작업은 뒷단 객체에 위임하고, 자신은 제2의 인터페이스 역할을 해주는 객체다.
    - 즉, 뒷단 객체 외에는 관리할 상태가 없으므로 뒷단 객체 하나당 어댑터 하나씩만 만들어지면 충분하다.


```java
Map<String, Object> map = new HashMap<>();
map.put("Hello", "world");

Set<String> st1 = map.keySet();
Set<String> st2 = map.keySet();

assertThan(st1).isSameAs(st2); // True

st1.remove("Hello");
System.out.println(st1.size()); // 0
System.out.println(st2.size()); // 0
```
- `Map` 인터페이스의 `ketSet` 메서드는 `Map` 객체 안의 키 정보를 담은 `Set` 뷰를 반환한다.
- 반환된 `Set` 인스턴스가 일반적으로 가변이더라도 반환된 인스턴스들은 기능적으로 모두 똑같다.
    - 즉, 반환한 객체 중 하나를 수정하면 다른 모든 객체가 따라서 바뀐다.
    - 모두가 똑같은 `Map` 인스턴스를 대변하기 때문이다.
- 따라서 ketSet이 뷰 객체를 여러 개 만들 필요도 없고 이득도 없다.

### 오토박싱
- **오토박싱**은 프로그래머가 기본 타입과 박싱된 기본 타입을 섞어 쓸 때 자동으로 상호 변환해주는 기술이다.
    - 의미상으로는 별다를 것 없지만 성능에서는 그렇지 않다.
```java
private static long sum() {
    Long sum = 0L;
  for (long i = 0; i < Integer.MAX_VALUE; i++) {
    sum += i;
  }
  return sum;
}
```
- sum 변수를 `long`이 아닌 `Long`으로 선언해서 불필요한 `Long` 인스턴스가 약 2의 31승개나 만들어졌다.
    - `Long`으로 선언된 변수를 `long`으로 바꾸면 훨씬 더 빠른 프로그램이 된다.
> 박싱된 기본 타입보다는 기본 타입을 사용하고, 의도치 않은 오토박싱이 숨어들이 않도록 주의하자.

<br>

### 결론
"객체 생성은 비싸니 피해야 한다"로 오해하면 안 된다.

특히 요즘의 JVM에서는 별다른 일을 하지 않는 작은 객체를 생성하고 회수하는 일이 크게 부담되지 않는다.
프로그램의 명확성, 간결성, 기능을 위해서 객체를 추가로 생성하는 것이라면 일반적으로 좋은 일이다.

거꾸로, 단순히 객체 생성을 피하고자 자신만의 객체 풀을 만들지는 말자.
DB 커넥션 같은 경우 생성 비용이 워낙 비싸니 재사용하는 편이 좋다.
하지만 일반적으로는 자체 객체 풀은 코드를 헷갈리게 만들고 메모리 사용량을 늘리고 성능을 떨어뜨린다.

<br>

### +) 용어

**가비지 컬렉션**은 더 이상 프로그램에서 사용되지 않는 객체(쓸모없는 메모리)를 자동으로 정리하는 자바의 메모리 관리 기능이다.

**유한 상태 머신이란**
주어지는 모든 시간에서 처해 있을 수 있는 유한 개의 상태를 가지고 주어지는 입력에 따라 어떤 상태에서 다른 상태로 전환시키거나 출력이나 액션이
일어나게 하는 장치를 나타낸 모델이다. 쉽게 말해서 전이(Transition)를 나타낸다.
