### ✅ equals는 일반 규약을 지켜 재정의하라

## 0. 들어가기 전
***

Java에서 두 객체를 비교할 때는 `동일성`과 `동등성` 이라는 두 가지 개념으로 구분한다.

이 두 개념은 비교 대상이 같은 객체인가 또는 같은 값을 가지고 있는가를 판단하는 기준에 따라 달라진다.



### 0.1 동일성(Identity)
***
`동일성`은 두 객체가 **정확히 같은 메모리 주소를 참조**하고 있는지, 즉 같은 인스턴스인지를 판단한다.

이는 `==`연산자틀 통해 비교하며, 객체의 내부 상태와는 무관하게 **참조 값**이 같은가 만을 따진다.

```java
String a = new String("hello");
String b = new String("hello");
String c = a;

System.out.println(a == b); // fasle
System.out.println(a == c); // true
```
`a`와 `b`는 같은 문자열을 담고 있지만, `new` 키워드를 통해 각각 생성된 서로 다른 객체이므로
동일하지 않다.

하지만 `c`는 생성시에 새로운 인스턴스를 생성하는 것이 아니라, 기존 객체인 `a`를 대입받아 `a`와 `c`는
같은 메모리 주소에 위치한 같은 인스턴스를 바라보게 된다.

변수는 stack 영역에 생성되는데, 이 stack 영역에 있는 변수는 heap 영역에 있는 객체를 가리키게 된다.

이때 두 변수가 동일한 객체를 가리키게 되므로 두 변수는 동일하다고 할 수 있다.

<details>
    <summary>String은 동일성 비교(==)를 해도 true가 나올 수 있다</summary>
<div markdown="1">

String을 생성하는 방법은 2가지가 있는데 `new` 연산자와 `리터럴("")`를 이용해서 생성할 수 있다.

리터럴 방식은 `hello`처럼 리터럴로 문자열을 생성하면, JVM은 해당 문자열을 `String constant Pool`에 저장한다.
같은 문자열 리터럴이 이미 존재한다면, 기존 객체의 참조를 재사용한다.

생성자 방식인 `new String("hello")`는 항상 새로운 객체를 Heap에 생성한다. 리터럴과 동일한 문자열이라도
다른 메모리 주소를 가지므로 `==`은 `false`가 된다.

```java
public boolean equals(Object anObject) {
  if (this == anObject) {
    return true; // 동일성 검사 (같은 참조인지)
  }
  if (anObject instanceof String) { // 타입 검사
    String anotherString = (String) anObject;
    int n = value.length;
    if (n == anotherString.value.length) {
      char v1[] = value;
      char v2[] = anotherString.value;
      int i = 0;
      while (n-- != 0) {
        if (v1[i] != v2[i])
          return false; // 문자 하나라도 다르면 false
        i++;
      }
      return true; // 모든 문자가 같으면 true
    }
  }
  return false; // 타입이 다르거나 길이가 다르면 false
}
```

String 클래스는 위와 같이 `equals()`를 재정의하여 인자로 전달된 String의 문자열을 비교하고 있다.

`==` 키워드를 통해 두 객체의 동일성 여부를 판단하고, 두 객체가 동일하지 않다면 String 인지 판단, 문자 배열로 바꾼 뒤
문자 하나 하나가 같은지 비교한다.

이때, 모든 조건을 통과한다면 두 String 객체의 내용이 같은 것이므로 동등하다고 판별한다.

</div>
</details>

### 0.2 동등성(Equality)
***

`동등성`은 두 객체가 논리적으로 같은 값을 가지는지를 비교하는 개념이다. 즉, 서로 다른 객체라도 그 안의 데이터(상태)가 같다면 동등하다고 판단할 수 있다.
```java
String n1 = new String("guseoh");
String n2 = new String("guseoh");

System.out.println(n1 == n2);       // false (동일성)
System.out.println(n1.equals(n2));  // true (동등성)
```
두 개의 인스턴스 `n1`과 `n2`가 있다.` n1`과 `n2`는 서로 다른 인스턴스지만 값은 같다.

이 경우 `동일` 하진 않지만, `동등`하다는 것을 알 수 있다.

이런 이유는 String 의 equals 는 인스턴스의 값이 같은지 확인하도록 재정의 되어있기 때문이다.

그러면 Java의 상위 클래스인 Object 클래스의 equals 메서드는 어떻게 되어있을까?

```java
public class Object {
    ...

  public boolean equals(Object obj) {
    return (this == obj);
  }
}
```
Object 클래스의 equals 메서드는 == 연산자를 사용하여 두 인스턴스의 동일성을 비교한다.

즉, Java 에서는 기본적으로 equals 메서드가 두 인스턴스의 동일성을 비교한다고 생각할 수 있다.

하지만 위의 String 경우처럼 `동등성`을 비교해야할 경우가 생기게 될 수 있다.

이런 경우 equals 메서드를 재정의해서 사용하면된다.

하지만 논리적으로 같은지 확인하는 로직을 잘못 구현한다면 예의치 않은 결과를 초래할 수 있다.

## 1. 재정의하지 않는 경우
***
- 각 인스턴스가 본질적으로 고유한 경우
- 인스턴스의 논리적 동치성(logical equality)을 검사할 일이 없는 경우
- 상위 클래스에서 재정의한 equals가 하위 클래스에도 딱 맞는 경우
- 클래스가 `private`, `package-private` 이고, equals 메서드를 호출할 일이 없는 경우

### 1.1 각 인스턴스가 본질적으로 고유한 경우
***

`Integer`나 `String`처럼 값을 표현하는 게 아니라 **동작하는 객체를 표현하는 클래스**가 여기 해당한다.

어떤 객체가 존재 자체로 유일성을 가진다면, 논리적 동등성 비교는 의미가 없다.

Thread가 좋은 예시이다. 운영체제(OS) 관점에서 각 Thread는 독립적인 실행 단위이며, 하나하나가 고유한 자원이다.

자바의 `Thread` 클래스 역시 이 개념을 반영하여 고유하게 다뤄지며, equals()를 재정의 하지 않는다.

`Thread a = new Thread();` 와 `Thread b = new Thread();`는 내부 상태가 같더라도 서로 다른 스레드로 간주된다.

### 1.2 인스턴스의 논리적 동치성(logical equality)을 검사할 일이 없는 경우
***
`논리적 동치성(logical equality)`은 객체의 참조 주소가 같은지를 검사하지 않고, 객체의 값(value)이 같은 경우를 나타내는 것이다.

예를 들어 두 개의 Person 객체가 있을 때, 두 인스턴스의 정보(이름, 주민번호 등)가 모두 같으면 논리적으로 같은 객체라고 할 수 있다.
```java
class Person {
  private int 주민번호;
  private String 이름;

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Person)) {
      return false;
    }
    Person person = (Person) o;
    return this.주민번호 == person.주민번호;
  }
}

```
위 코드는 주민번호, 이름을 가진 Person 이라는 클래스이다.

우리나라는 주민번호가 같은 경우 같은 사람으로 취급한다.

따라서 이 경우에는 주민번호가 같은 사람은 논리적으로 같은 사람으로 취급할 수 있다.

즉, Person 인스턴스의 equals는 주민번호가 같으면 **동일한 인스턴스라는 논리적 의미**를 구현하게 된다.

### 1.3 상위 클래스에서 재정의한 equals가 하위 클래스에도 딱 맞는 경우
***
자바에서 클래스가 상속 구조를 가지고 있을 때, 상위 클래스의 `equals()` 메서드가 논리적으로 하위 클래스에도 잘 작동한다면,
하위 클래스에서 굳이 `equals()`를 재정의(override) 하지 않아도 된다.

자바의 `Set` 구현체들은 아래와 같은 구조를 가진다.
```java
Set<E>
  ↑
AbstractSet<E>
  ↑
HashSet<E>, LinkedHashSet<E>, TreeSet<E> ...
```
이 중 `AbstractSet` 클래스는 `equals()` 메서드를 직접 구현하고 있으며, 이는 모든 하위 `Set` 구현체들에게 그대로 상속된다.

```java
// AbstractSet의 equals() 구현
public boolean equals(Object o) {
  if (o == this) return true;

  if (!(o instanceof Set)) return false;
  Collection<?> c = (Collection<?>) o;

  if (c.size() != size()) return false;

  try {
    return containsAll(c);
  } catch (ClassCastException | NullPointerException unused) {
    return false;
  }
}

// -----------------------------------------------------------------------
HashSet<Integer> set1 = new HashSet<>();
LinkedHashSet<Integer> set2 = new LinkedHashSet<>();
TreeSet<Integer> set3 = new TreeSet<>();

int num = 1;
set1.add(num); set2.add(num); set3.add(num);

System.out.println(set1.equals(set2)); // true
        System.out.println(set2.equals(set3)); // true
        System.out.println(set1.equals(set3)); // true

```
`AbstractSet` 의 `equals()` 구현 로직을 보면 두 Set이 동일한 요소들로 구성되어 있으면 순서나 구현체와 관계없이 equals()는 true를 반환한다.

즉, `HashSet`, `LinkedHashSet`, `TreeSet` 등은 내부 동작 방식은 다르지만 **요소만 같으면 논리적으로 같은 Set**으로 취급된다.

세 개의 Set은 동일한 요소(1)만 포함하므로, equals() 비교 결과는 모두 `true` 이다.

### 1.4 클래스가 private, package-private 이고, equals 메서드를 호출할 일이 없는 경우
***
클래스가 `private` 혹은 `package-private` 이라는 것은, **특정 클래스나 패키지의 내부에서만 사용할 클래스라는** 의미이다.

일반적으로 이런 클래스는 내부 구현용으로 사용되며, 외부 객체와 비교하거나 컬렉션(Set, Map 등)에 넣는 일이 없다.

따라서 `equals()`를 사용할 이유가 없고, 사용 자체가 잘못된 상황일 수 있다.

그리고 equals가 실수로라도 호출되는 걸 막고 싶다면 아래처럼 구현하자.
```java
@Override public boolean eqauls(Object o){
  throw new AssertionError(); // 호출 금지!
}
```
`AssertionError`는 개발 중 실수로 호출된 코드를 즉시 발견하기 위해 사용한다.

이 방식은 개발자에게 경고를 하며, "이 메서드는 절대 호출하면 안 된다"는 의도를 명확히 드러낸다.

## 2. 정의해야 하는 경우
***
`객체 식별성`(object identity: 두 객체가 물리적으로 같은가)이 아니라 동치성을 확인해야 하는데, 상위 클래스의 equals가
논리적 동치성을 비교하도록 재정의되지 않았을 때다.

주로 값 클래스가 여기 해당한다.

`값 클래스`는 Integer와 String 처럼 값을 표현하는 클래스를 말한다. 두 값 객체를 equals로 비교하는 프로그래머는
객체가 같은지가 아니라 `값`이 같은지를 알고 싶어 할 것이다.

예를 들어, 두 값의 객체를 equals로 비교했을 때, 참조 동등성이 아니라 논리적 동치성을 확인하도록 재정의하면 값 비교는 물론이고
Map의 key와 Set의 원소로 사용할 수 있다.

<details>
    <summary>equals를 재정의하면 Map의 key와 Set의 원소로 사용할 수 있다.</summary>
<div markdown="1">

해당 말은 **객체를 `값` 중심으로 비교하고, `중복 없이` 저장하거나 조회할 수 있다**는 뜻이다.

Java 에서 `Map`과 `Set` 같은 컬렉션은 객체를 비교할 때 단순한 **참조(주소)** 비교가 아니라 **논리적 동등성(equals) + 해시값(hashCode)** 기준으로 비교한다.

#### 1. Map의 key로 사용한다는 것은?
```java
Map<Person, String> map = new HashMap<>();
map.put(new Person("홍길동", 1234), "서울");

System.out.println(map.get(new Person("홍길동", 1234))); // null?
```
- `equals()`와 `hashCode()`를 재정의하지 않으면, `new Person(...)` 객체는 다른 주소이기 때문에 찾지 못한다.
- 하지만 **논리적으로 동등한 객체**임을 `equals()`로 정의하고, 같은 해시값을 `hashCode()`로 반환하면,
  새로운 객체로도 동일한 key를 정확히 조회할 수 있다.

#### 2. Set의 원소로 사용한다는 것은?
```java
Set<Person> set = new HashSet<>();
set.add(new Person("홍길동", 1234));
set.add(new Person("홍길동", 1234)); // 같은 값인데 중복 저장?

System.out.println(set.size()); // 2? 아니면 1?
```
- `equals()`를 재정의하지 않으면, `HashSet`은 서로 다른 객체라고 보고 두 개를 모두 저장한다. -> `size == 2`
- 하지만 `equals()`와 `hashCode()`를 올바르게 구현하면, 논리적으로 같은 객체는 하나만 저장된다. -> `size == 1`

#### 3. 결론
- `equals()`와 `hashCode()`를 논리적으로 동등한 값 기준으로 재정의하면,
  - `Map`에서 동일한 key 객체로 get 가능하고.
  - `Set`에서 중복 없이 원소를 저장할 수 있다.
- 즉, 값 객체(Value Object)로서 활용할 수 있는 조건이 갖춰진 것이다.
</div>
</details>

그러나, 값 클래스라 해도 같은 인스턴스가 둘 이상 만들어지지 않는 `인스턴스 통제 클래스`라면 재정의하지 않아도 된다.
(어차피 논리적으로 같은 인스턴스가 2개 이상 만들어지지 않기 때문)

## 3. Object 클래스의 equals 규약
***
> equals 메서드는 동치관계(equivalence relation)를 구현하며, 다음을 만족한다.
- 반사성(reflexivity): 자기 자신과 비교하면 항상 true 여야 한다.
- 대칭성(symmetry): 역으로 비교해도 항상 같은 결과가 나와야 한다.
- 추이성(transitivity): 첫 번째와 두 번째가 같고, 두 번째와 세 번째가 같으면, 첫 번째와 세 번째도 같아야 한다.
- 일관성(consistency): 항상 같은 결과를 반환해야 한다.
- null-아님: 비교 대상이 null이 아니다.

<details>
    <summary>Object 명세에서 말하는 동치관계란?</summary>
<div markdown="1">

쉽게 말해, 집합을 서로 같은 원소들로 이뤄진 부분집합으로 나누는 연산이다. 이 부분집합을 동치류(동치 클래스)라 한다.

equals 메서드가 쓸모 있으려면 모든 원소가 같은 동치류에 속한 어떤 원소와도 서로 교환할 수 있어야 한다.

</div>
</details> 

### 3.1 반사성
> 반사성: 자기 자신과 비교하면 항상 true 여야 한다.

객체는 자기 자신과 같아야 한다는 기본적인 규약이다.

이 요건을 어긴 클래스의 인스턴스를 컬렉션에 넣은 다음 `contains` 메서드를 호출하면 방금 넣은 인스턴스가 없다고 답할 것이다.
```java
class Weird {
    @Override
    public boolean equals(Object obj) {
        return false; // 반사성 위반!
    }
}

public class Test {
    public static void main(String[] args) {
        Weird w = new Weird();
        List<Weird> list = new ArrayList<>();
        list.add(w); 

        System.out.println(list.contains(w)); // false?!
    }
}
```
이 코드는 `list`에 `w`를 넣었는데 `list.contains(w)`를 호출하면 내부적으로 `w.equals(w)`를 호출한다.

그런데 equals()가 false를 반환하도록 구현되어 있으므로, 자기 자신도 포함되지 않는다고 판단하게 된다.

즉, `contains()`는 내부적으로 `equals()`를 이용해 객체를 비교하기 때문에 `equals()`가 반사성을 지키지 않으면
정상적으로 포함 여부를 판단할 수 없게 되는 것이다.



앞서 1.3에서 살펴봤던 `AbstractSet`의 equals 구현체 메서드에서 `containsAll` 이라는 메서드가 사용된다.
```java
public abstract class AbstractCollection<E> implements Collection<E> {

    /**
     * 주어진 Collection의 모든 요소가 이 컬렉션에 포함되는지 확인
     */
    public boolean containsAll(Collection<?> c) {
        for (Object e : c) {
            if (!contains(e))  // 각 요소에 대해 contains 메서드 호출
                return false;
        }
        return true;
    }

    /**
     * 주어진 요소가 이 컬렉션에 존재하는지 확인
     */
    public boolean contains(Object o) {
        Iterator<E> it = iterator();

        if (o == null) {
            while (it.hasNext()) {
                if (it.next() == null)
                    return true;
            }
        } else {
            while (it.hasNext()) {
                if (o.equals(it.next()))  // 이 부분에서 equals 메서드가 호출됨
                    return true;
            }
        }
        return false;
    }

    ...
}
```
`AbstractCollection` 에서는 대상이 있는지 확인하는 로직에서 결국 `equals` 가 사용된다.

즉, `AbstractCollection` 의 `contains`, `containsAll` 메서드는 내부적으로 `equals()`에 의존하여 동등성을 비교한다.

### 3.2 대칭성
***
> 대칭성: 역으로 비교해도 항상 같은 결과가 나와야 한다

두 객체는 서로에 대한 동치 여부에 똑같이 답해야 한다는 뜻이다.

`equals` 를 재정의 하면 로직이 복잡해지면 자칫하면 어길 수 있다.

```java
public final class CaseInsensitiveString {
  private final String s;

  public CaseInsensitiveString(String s) {
    this.s = Objects.requireNonNull(s);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof CaseInsensitiveString)
      return s.equalsIgnoreCase(((CaseInsensitiveString) o).s);
    if (o instanceof String) // 이 부분이 문제
      return s.equalsIgnoreCase((String) o);
    return false;
  }
}
```
위 코드는 문자열을 대소문자 구분 없이 비교하기 위해 `equalsIgnoreCase`를 사용한다. 그리고 자기 타입이 아닌
`String` 객체 까지도 비교 대상으로 허용하고 있다.

```java
// 대칭성 위반 사례
CaseInsensitiveString cis = new CaseInsensitiveString("Hello");
String str = "hello";

System.out.println(cis.equals(str)); // true
System.out.println(str.equals(cis)); // false
```

`cis.equals(str)`는 `CaseInsensitiveString` 내부에서 String에 대해서도 비교를 허용하므로 `true` 이다.

`str.equals(cis)`에서 String 클래스는 `CaseInsensitiveString`을 전혀 모르므로 `false` 이다.

즉, `CaseInsensitiveString` 이 다른 클래스(String) 과의 비교를 허용한 결과, 한 쪽에서는 같다고 판단하지만 다른 쪽에서는
다르다고 판단하는 `비대칭적인 상황`이 발생하게 된다.

```java
// 올바른 구현 방법
@Override public boolean equals(Object o){
    return o instanceof CaseInsensitiveString && ((CaseInsensitiveString) o).s.equalsIgnoreCase(s);
}
```
이렇게 수정하면 오직 동일한 타입인 `CaseInsensitiveString` 끼리만 비교하게 되므로, 대칭성을 포함한 모든 `equals` 규약을 지킬 수 있다.

### 3.3 추이성
***
> 추이성: 첫 번째 객체와 두 번째 객체가 같고, 두 번째 객체와 세 번째 객체가 같다면, 첫 번째 객체와 세 번째 객체도 같아야 한다.

상위 클래스에 없는 새로운 필드를 하위 클래스에 추가하는 2차원에서의 점을 표현하는 클래스를 예로 보자.
```java
public class Point {
    private final int x;
    private final int y;
    
    ...
  
    @Override public boolean equals(Object o){
        if (!(o instanceof Point)) return false;
        Point p = (Point) o;
        return p.x == x && p.y = y;
    }
}
class ColorPoint extends Point {
    private final Color color;
    
    ...

  @Override
  public boolean equals(Object o) {
        if (!(o instanceof ColorPoint)) return false;
        
        return super.equals(o) && ((ColorPoint) o).color == color;
  }
}

//----------------------------------------------------------------------
Point p = new Point(1, 2);                       // true       
ColorPoint cp = new ColorPoint(1, 2, Color.RED); // false
```

`ColorPoint` 클래스에 equals 메서드를 재정의하지 않으면 `Point`의 구현이 상속되어 색상 정보를 무시한 채 비교를 수행한다.

재정의한 equals 메서드는 일반 `Point`를 `ColorPoint`에 비교한 결과와 그 둘을 바꿔 비교한 결과가 다를 수 있다.

`Point`의 `equals`는 색상을 무시하고, `ColorPoint`의 `equals`는 입력 매개변수의 클래스 종류가 다르다며 매번 false만 반환할 것 이다.

`ColorPoint.equals`가 `Point`와 비교할 때 색상을 무시하다록 하면 어떻게 될까?
```java
@Override
public boolean equals(Object o) {
      if (!(o instanceof Point)) return false;
      
      // o가 Point면 색상을 무시하고 비교한다.
      if (!(o instanceof ColorPoint)) return o.equals(this);
      
      return super.equals(o) && ((ColorPoint) o).color == color;
}

//---------------------------------------------------------------
ColorPoint p1 = new ColorPoint(1,2, Color.RED);
Point p2 = new Point(1,2);
ColorPoint p3 = new ColorPoint(1,2, Color.BLUE);

System.out.println(p1.equals(p2)); // true (좌표 같음)
System.out.println(p2.equals(p3)); // true (좌표 같음)
System.out.println(p1.equals(p3)); // false (좌표 같음, 색상 다름)
```

위 처럼 p1, p2는 동등하고 p2, p3도 동등하지만 p1, p3은 동등하지 않다.

추이성에 명백히 위배된다. p1과 p2, p2와 p3 비교에서는 색상을 무시했지만, p1과 p3 비교에서는 색상까지 고려헸기 때문이다.

사실 이 현상은 모든 객체 지향 언어의 `동치관계`에서 나타나는 근본적인 문제이다.

구체 클래스를 확장해 새로운 값을 추가하면서 equals 규약을 만족시킬 방법은 존재하지 않는다.

이 말은 얼핏, equals 안의 `instanceof` 검사를 `getClass` 검사로 바꾸면 규약도 지키고 값도 추가하면서 구체 클래스를 상속할 수 있다는 뜻을 들린다.
```java
@Override
public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Point p = (Point) o;
    return x == p.x && y == p.y;
}
```
이렇게 구현할 경우 `Point`와 `ColorPoint`는 절대 서로를 같다고 판단하지 않는다. 즉, 대칭성과 추이성을 지킬 수 있을 것처럼 보인다.

하지만 `ColorPoint`는 본질적으로 `Point`로 `ColorPoint` 객체는 어디에서든 `Point`로 취급될 수 있어야 한다.

그런데 equals를 `getClass()` 기반으로 정의하면, `ColorPoint`는 더 이상 `Point` 로서 논리적으로 대등한 존재가 아니게 된다.

즉, 위 equals는 `리스코프 치환 원칙`(Liskov substitution principle)을 위배한 코드이다.

<details>
    <summary>리스코프 치환 원칙(Liskov substitution principle)</summary>
<div markdown="1">

`리스코프 치환 원칙`은 **서브 타입은 언제나 기반 타입으로 교체**할 수 있어야 한다는 것을 뜻한다.

교체할 수 있다는 말은, 자식 클래스는 최소한 자신의 부모 클래스에서 가능한 행위는 수행이 보장되어야 한다는 의미이다.

즉, 부모 클래스의 인스턴스를 사용하는 위치에서 자식 클래스의 인스턴스를 대신 사용했을 때 코드가 원래 의도대로 작동해야 한다는 의미이다.

결국 리스코프 치환 원칙은, `다형성`의 특징을 이용하기 위해 상위 클래스 타입으로 객체를 선언하여 하위 클래스의 인스턴스를 받으면, 업캐스팅된 상태에서
부모의 메서드를 사용해도 동작이 의도대도만 흘러가도록 구성하면 되는 것이다.

핵심은 상속(Inheritance)이다.
</div>
</details>

equals의 추이성을 해결하는 방법은 2가지가 있다.

첫 번째 해결책으로는 "상속 대신 컴포지션(Composition) 사용" 하는 방법이 있다.

`ColorPoint`는 `Point`의 좌표를 기반으로 하면서 색상(color) 이라는 추가 정보를 가진 객체이다.
이때 상속을 사용하면 `equals` 규약을 지키기 매우 어려워지지만, **컴포지션을 사용하면 equals 구현의 책임을 독립적으로 관리할 수 있다.**
```java
public class ColorPoint {
    private final Point point; // Point를 내부 필드로 두는 방식
    private final Color color;
    
    ...

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ColorPoint)) return false;
        ColorPoint cp = (ColorPoint) o;
        return point.equals(cp.point) && color.equals(cp.color);
    }

}
```

`Point`와 `ColorPoint` 간의 비교는 허용하지 않으므로 대치성, 추이성 등 equals 규약을 지킬 수 있다.

`Point` 클래스는 자기 역할만 수행하고, `ColorPoint`는 이를 포함해서 확장하는 방식으로 책임 분리 가능하다.

두 번째 해결책은 "추상 클래스의 필드 확장" 이다.

추상 클래스의 하위 클래스에서라면 equals 규약을 지키면서도 값을 추가할 수 있다.

추상 클래스는 직접 인스턴스화되지 않으므로 equals 비교 대상은 언제나 하위 클래스 인스터스 간에만 이루어진다.

따라서 `instanceof` 검사로 `equals` 구현 시에도 대칭성 문제가 발생하지 않는다.

예를 들어, 아무런 값을 갖지 않는 추상 클래스인 Shape를 위에 두고, 확장하여 radius 필드를 추가한 Circle 클래스와,
length와 width 필드를 추가한 Rectangle 클래스를 만들 수 있다.


### 3.4 일관성
***
> 일관성: 항상 같은 결과를 반환해야 한다.

클래스가 불변이든 가변이든 equals의 판단에 신뢰할 수 없는 자원이 끼어들게 해서는 안 된다.

이와 관련하여 `java.net.URL` 클래스가 이 규약을 위반하는 대표적인 사례라고 소개한다.

URL 클래스의 equals는 매핑된 호스트 IP 주소를 비교한다. 호스트 이름을 IP 주소로 바꾸려면 네트워크를 통해야 하는데 그 결과가 항상 같다고 보장할 수 없다.

즉, equals는 JVM 메모리 상의 정보로만 동등성을 비교해야 한다.

### 3.5 null-아님
***
> null-아님: 비교 대상이 null이 아니다.

`null-아님`은 이름처럼 모든 객체가 null과 같지 않아야 한다는 뜻이다.

```java
// 명시적 null 검사 - 필요 없다!
@Override public boolean equals(Object o){
    if (o == null) return false;
    ...
}

// 묵시적 null 검사
@Override public boolean equals(Object o) {
  if (!(o instanceof Point)) return false;
  
  Point p = (Point) o;
  return this.x == p.x && this.y == p.y;
}
```

명시적 null 검사는 묵시적 null 검사의 타입 확인 단계에서 false를 반환하기 때문에 null 검사를 명시적으로 하지 않아도 된다.

## 4. 정리
***
1. `==` 연산자를 사용해 입력이 자기 자신의 참조인지 확인한다.
   - 자기 자신이면 `true`를 반환한다.
   - 이는 단순 성능 최적화용, 비교 작업이 복잡할 경우에 추천한다.
2. `instanceof` 연산자로 입력이 올바튼 타입인지 확인한다.
    - 올바른 타입은 equals가 정의한 클래스인 것이 보통이지만, 그 클래스가 구현한 특정 인터페이스가 될 수도 있다.
    - `Set`, `List`, `Map`, `Map.Entry` 등의 컬렉션 인터페이스들이 여기 해당한다.
3. 입력을 올바튼 타입으로 형변환한다.
4. 입력 객체와 자기 자신의 대응되는 '핵심' 필드들이 모두 일치하는지 하나씩 검사한다.
   - `equals()`는 객체의 의미적 동등성을 비교한다.
   - 따라서 두 객체가 같은 의미인지 판단하려면, 그 의미를 구성하는 핵심 필드만 비교해야 한다.
   - 이 핵심 필드는 일반적으로
     - 도메인 상의 고유값(ex: ID, 주민번호 등)
     - PK 역할을 하는 필드
     - 비즈니스적으로 **두 객체가 동일하다고 판단하는 기준 필드**

### 4.1 번외
***

`float`와 `double`을 제외한 기본 타입 필드는 `==` 연산자로 비교하고, 참조 타입 필드는 각각의 equals 메서드로,
`float`와 `double` 필드는 각각 정적 메서드인 `compare` 메서드로 비교한다.

`float`와 `double` 필드를 특별 취급하는 이유는 특수한 부동소수 값을 다뤄야 하기 때문이다.

### 4.2 주의사항
***
- `equals`를 재정의 할땐 `hashCode`도 반드시 재정의하자.
- 너무 복잡하게 해결하려 들지 말자.
- `Object` 외의 타입을 매개변수로 받는 equals 메서드는 선언하지 말자.
