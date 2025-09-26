### ✅ 이왕이면 제네릭 메서드로 만들라


## 1. 제네릭 메서드

**제네릭 메서드**는 선언 부에 적은 제네릭으로 리턴 타입, 파라미터의 타입이 정해지는 메서드다.
```java
public <타입파라미터, ...> 리턴타입 메서드명(매개변수, ...) {...}
public <T> Box<T> boxing(T t) {...}
```
<details>
    <summary>제네릭에 대한 예시 - 1</summary>
<div markdown="1">

```java
public class Student<T> {
    static T name;
}
```
</div>
</details>

- `static` 변수는 제네릭을 사용할 수 없다.
    - 왜냐하면 `Student` 클래스가 인스턴스 되기 전에 static은 메모리에 올라가는데 이때 `name` 의 타입인 T가 결정되지 않기 때문에 위와 같이 사용할 수 없다.  


<details>
    <summary>제네릭에 대한 예시 - 2</summary>
<div markdown="1">

```java
public class Student<T> {
  
    static T getName(T name) {   
        return name;
    }
}
```
</div>
</details>

- `static` 메서드에도 제네릭을 사용하면 에러가 발생한다.
    - 왜냐하면 static 변수와 마찬가지로 `Student` 클래스가 인스턴스화 되기 전에 메모리에 올라가는데 T의 타입이 정해지지 않았기 때문이다.

<details>
    <summary>제네릭에 대한 예시 - 3</summary>
<div markdown="1">

```java
public class Student<T> {

    static <T> T getOneStudent(T id) {
        return id;
    }
}
```
</div>
</details>

- 제네릭 메서드는  `static`이 가능하다.
- 제네릭 메서드는 호출 시에 매개 타입을 지정하기 때문애 static이 가능하다.

<details>
    <summary>Collection의 알고리즘 메서드(binarySearch, sort 등)는 모두 제네릭이다.</summary>
<div markdown="1">

- [Collection](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Collections.html)

```java
// sort 
@SuppressWarnings({"unchecked", "rawtypes"})
public static <T> void sort(List<T> list, Comparator<? super T> c) {
    list.sort(c);
}

// binarySearch 
public static <T> int binarySearch(List<? extends Comparable<? super T>> list, T key) {
    if (list instanceof RandomAccess || list.size()<BINARYSEARCH_THRESHOLD) 
        return Collections.indexedBinarySearch(list, key);
    else 
        return Collections.iteratorBinarySearch(list, key);
}
```
</div>
</details>

<br>

### 1.1 문제가 있는 메서드

<details>
    <summary>로 타입 사용 - 수용 불가 (아이템 26)</summary>
<div markdown="1">

```java
public static Set union(Set s1, Set s2) {
    Set result = new HashSet(s1);
    result.addAll(s2);
    return result;
}
```
</div>
</details>

- 컴파일은 되지만 경고가 발생한다.
- 경고를 없애려면 이 메서드를 타입 안전하게 만들어야 한다.
    - **제네릭 메서드로 수정**

<br>

### 1.2 제네릭 메서드 작성

1. 메서드 선언에서의 **세 집합(입력 2개, 반환 1개)의 원소 타입을 타입 매개변수**로 명시
2. 메서드 안에서도 **이 타입 매개변수만 사용하게 수정**하면 된다.

<details>
    <summary>(타입 매개변수들을 선언하는) 타입 매개변수 목록은 메서드의 제한자와 반환 타입 사이에 온다.</summary>
<div markdown="1">

```java
[제한자] <타입 매개변수 목록> [반환 타입] 메서드명(매개변수들)
```
</div>
</details>

```java
public static <E> Set<E> union(Set<E> s1, Set<E> s2) {
    Set<E> result = new HashSet<>(s1);
    result.addAll(s2);
    return result;
}
```

- 위 코드에서 매개 변수 목록은 `<E>` 이고 반환 타입은 `Set<E>` 이다.
- 한정적 와일드카드 타입(아이템 31)을 사용하여 유연성을 높일 수 있다.

<details>
    <summary>타입 파라미터의 명명 규칙은 제네릭 타입과 같다.</summary>
<div markdown="1">

- **타입 파라미터 명명 규칙**
    - 한문자로 이름 짓기
    - 대문자로 이름 짓기
- **자주 사용하는 타입 인자**
    - E (Element)
    - K (Key)
    - N (Number)
    - T (Type)
    - V (Value)
</div>
</details>

<br>

## 2. 제네릭 메서드 활용

- 불변 객체를 여러 타입으로 활용하는 경우
- 항등함수(identity function)를 담은 클래스를 만드는 경우
- 재귀적 타입 한정(recursive type bound)의 사용

<br>

### 2.1 불변 객체를 여러 타입으로 활용하는 경우

- 제네릭은 런타임에 타입 정보가 소거(아이템 28) 되므로 하나의 객체를 **어떤 타입으로든 매개변수화** 할 수 있다.
- 그러나 요청한 타입 매개 변수에 맞게 매번 그 객체의 타입을 바꿔주는 정적 팩터리를 만들어야 한다.

<details>
    <summary>이 패턴을 제네릭 싱글턴 팩터리 라 한다.</summary>
<div markdown="1">

- 제네릭 싱글턴 패턴은 **타입에 관계없이 하나의 인스턴스만 공유**하면서, 타입 안전성을 유지하는 기법이다.
    - 주로 불변(Immutable) 객체에서 사용한다.
- 제네릭 싱글턴 패턴 예시
    - Collections.reverseOrder 같은 함수 객체(아이템 42)
    - Collections.emptySet 같은 컬렉션

```java
// reverseOrder 
public static <T> Comparator<T> reverseOrder() {
    return (Comparator<T>) ReverseComparator.REVERSE_ORDER; 
}
    
// emptySet
public static final Set EMPTY_SET = new EmptySet<>();
    
public static final <T> Set<T> emptySet() { 
    return (Set<T>) EMPTY_SET; 
}
```
</div>
</details>

<br>

### 2.2 항등 함수(identity function)를 담은 클래스를 만드는 경우

- 항등 함수는 **입력 값 수정 없이 그대로 반환하는 함수** 이다.
- 자바 라이브러리의 Function.identity를 사용하면 된다. (여기서는 직접 작성)
- 항등함수 객체는 상태가 없으니 싱글톤으로 만드는 것이 좋다. 자바의 제네릭이 실체화된다면 항등함수를 타입별로 만들어야 하지만, 소거되므로 **제네릭 싱글턴**으로 충분하다.

<details>
    <summary>제네릭 싱글톤 팩터리 패턴</summary>
<div markdown="1">

```java
private static UnaryOperator<Object> IDENTITY_FN = (t) -> t;

@SuppressWarnings("unchecked") // 비검사 형변환 경고 방지 (컴파일 시 경고 없이 완료된다.)
public static <T> UnaryOperator<T> identityFunction() {
	return (UnaryOperator<T>) IDENTITY_FN;
}
```
</div>
</details>

- `IDENTITY_FN`을 `UnaryOperator<T>` 로 형변환하면 비검사 형변환 경고가 발생한다.
- T가 어떤 타입이든 `UnaryOperator<Object>`는 `UnaryOperator<T>`가 아니기 때문이다.
- 하지만 항등 함수는 입력 값을 수정 없이 그대로 반환하는 특별한 함수이므로, T가 어떤 타입이든 UnaryOperator<T>를 사용해도 타입 안전하다.

<details>
    <summary>제네릭 싱글턴을 사용하여 UnaryOperator과 UnaryOperator로 사용한 코드</summary>
<div markdown="1">

```java
public static void main(String[] args) {

  // UnaryOperator<String>
  String[] strings = {"삼배", "대마", "나일론"};
  UnaryOperator<String> sameString = identityFunction();
  for (String s : strings) {
        System.out.println(sameString.apply(s));
}

  // UnaryOperator<Number>
  Number[] numbers = {1, 2.0, 3L};
  UnaryOperator<Number> sameNumber = identityFunction();
  for (Number n : numbers) {
        System.out.println(sameNumber.apply(n));
  }
```
</div>
</details>

<br>

### 2.3 재귀적 타입 한정(recursive type bound)의 사용

- 드문 경우이지만, **재귀적 타입 한정**의 제네릭 사용
- `재귀적 타입 한정`은 **자기 자신**이 들어간 표현식을 사용해 타**입 파라미터의 허용범위를 한정**
- 주로 타입의 자연적 순서를 정하는Comparable 인터페이스(아이템 14)와 함께 쓰인다.

<details>
    <summary>예시</summary>
<div markdown="1">

```java
// Comparable 인터페이스(아이템 14)
public interface Comparable<T> {
    int compareTo(T o);
}

// String은  Comparable<String> 을 구현
public final class String implements Comparable<String> {
    @Override
    public int compareTo(String other) {
        return this.length() - other.length();
    }
}
```
</div>
</details>

- 타입 한정인 `<E extends Comparable<E>>` 는 **모든 타입 E는 자신과 비교할 수 있다.** 라고 읽을 수 있다.
    - 상호 비교 가능하다는 뜻을 아주 정확하게 표현한 것