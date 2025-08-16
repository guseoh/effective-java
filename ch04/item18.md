### ✅ 상속보다는 컴포지션을 사용하라

## 1. 상속의 매력과 위험성

객체지향 프로그램에서 상속은 기존 클래스를 재사용하는 매우 손쉬운 방법이다. 부모 클래스의 메서드와 필드를 그대로 물려받을 수 있고, 필요한 기능만 추가하거나 수정하면 새로운 클래스를 빠르게 만들 수 있다. 그러나 이러한 편리함 뒤에는 함정이 숨어 있다.

상속은 **부모 클래스의 구현 세부사항에 의존**한다. 부모의 내부 동작이 바뀌면 자식 클래스의 동작도 의도치 않게 변할 수 있다. 이런 위험은 **상속이 캡슐화를 깨뜨린다는 사실**에서 비롯된다.

<br>

### 1.1. 캡슐화란 무엇인가?

캡슐화(encapsulation)는 객체지향 설계의 핵심 원칙 중 하나로, **클래스의 내부 구현을 외부에서 숨기고, 오직 공개된 인터페이스(메서드 시그니처)를 통해서만 접근하도록 하는 것**이다.

이렇게 하면 클래스 내부 로직을 변경해도 외부에 영향을 주지 않기 때문에, 코드의 독립성과 유지보수성이 높아진다.

예를 들어, 어떤 `List` 구현 클래스가 내부적으로 배열을 쓸지, 연결 리스트를 쓸지는 외부 코드가 몰라도 된다.

외부는 단지 `add()`, `remove()` 같은 메서드를 통해서만 데이터를 다루게 된다.

<br>

### 1.2 상속이 캡슐화를 깨뜨리는 이유

상속을 쓰면 자식 클래스는 부모 클래스의 **public, protected 메서드뿐 아니라 내부 동작 방식까지 사실상 알아야** 제대로 동작하는 코드를 작성할 수 있다.

문제는 부모의 메서드들이 서로를 어떻게 호출하는지, 어떤 순서로 호출하는지, 특정 메서드가 부수효과(side effect)를 가지는지 등을 알아야 한다는 점이다.

즉, **자식 클래스가 부모의 “구현 세부사항”을 의존해서 설계되는 순간, 부모 클래스의 내부 변경이 자식 클래스의 동작에 직접 영향을 미치게 된다.**

<details>
    <summary>예시 코드</summary>
<div markdown="1">

```java
class Animal {
    public void makeSound() {
        System.out.println("Animal is making a sound.");
    }
}

class Dog extends Animal {
    public void makeSound() {
        System.out.println("Dog is barking.");
    }
}

class Main {
    public static void main(String[] args) {
        Animal animal = new Dog();
        animal.makeSound(); // 출력: "Dog is barking."
    }
}
```
- `Dog`는 `Animal`을 상속 받는다. `Animal`이라는 클래스는 `makeSound`라는 소리르르내는 기능("Animal is making a sound.")이 존재한다.
- `Dog`는 `Animal` 클래스를 상속받고 `Dog` 클래스에서 **부모 메서드를 재정의(Override)**하면 기존의 부모 메서드 기능은 사라진다.
- 이런 경우 하나의 캡슐로 만들어놓은 **부모 클래스의 기능이 새로운 기능으로 대체**되었기 때문에 캡슐화가 깨졌다고 볼 수 있다.
</div>
</details>

## 2. 상속이 깨지기 쉬운 이유

- 우리에게 `HashSet`을 사용하는 프로그램이 존재한다.
- 이 `HashSet`은 처음 생성된 이후 원소가 몇 개 더해졌는지 알 수 있어야 한다.
- 따라서 변형된 `HashSet`을 만들어 추가된 원소의 수를 저장하는 변수와 접근자 메서드를 추가했다.
- 그 다음 `HashSet`에 원소를 추가하는 메서드인 `add`와 `addAll`을 재정의했다.
```java
public class InstrumentedHashSet<E> extends HashSet<E> {

    // 추가된 원소의 수
    private int addCount = 0;

    public InstrumentedHashSet() {
    }

    public InstrumentedHashSet(int initCap, float loadFactor) {
        super(initCap, loadFactor);
    }

    @Override public boolean add(E e) {
        addCount++;
        return super.add(e);
    }

    @Override public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();   // 1차 증가 
        return super.addAll(c); // 내부에서 add() 호출 -> 2차 증가
    }

    public int getAddCount() {
        return addCount;
    }
}
```
- 이 클래스는 잘 구현된 것처럼 보이지만 제대로 작동하지 않는다.  
- 이를 확인하기 위해 `addAll` 메서드로 원소 3개를 더한다고 해보자.

```java
InstrumentedHashSet<String> s = new InstrumentedHashSet<>();
s.addall(List.of("틱", "탁탁", "펑"));
```
여기서 우리는 `getAddCount()` 메서드가 `3`을 반환하리라 기대한다. `“틱”`, `“탁탁”`, `“펑”` 세 개의 원소를 추가했으니깐 당연해 보인다. 그러나 실제 결과는 `6`이다.

원인은 `HashSet`의 `addAll` 메서드 구현 방식에 있다. `HashSet.addAll()`은 내부적으로 반복문을 돌면서 각 요소를 `add()` 메서드로 추가한다. 그런데 `InstrumentedHashSet`은 `add()`를 오버라이딩하여 카운트를 증가시키기 때문에,

1. `addAll()` 메서드에서 요소 개수(`3`)만큼 `addCount`가 증가하고
2. 내부적으로 호출된 `add()`에서도 다시 `3`만큼 증가한다.

결국 총 `6`이 되어버리는 것이다. addAll로 추가한 원소 하나당 2씩 늘어났다.

추가적으로 하위 클래스가 깨지기 쉬운 이유가 더 있다.   
**상속 받은 하위 클래스는 상위 클래스의 변경에 취약하다.** 특히, 상위 클래스가 새로운 메서드를 추가하면, 하위 클래스가 그 메서드를 재정의하지 못해 보안 규칙이나 제약이 우회될 수 있다.

실제 사례로, 자바 컬렉션 프레임워크에 `Hashtable`과 `Vector`를 포함 시킬 때 이런 보안 취약점이 발견되어 수정한 적이 있다.

이상의 두 문제 모두 메서드 재정의가 원인이었다.  
따라서 클래스를 확장하더라도 `메서드를 재정의`하는 대신 새로운 메서드를 추가하면 괜찮으리라 생각할 수도 있다. 이 방식이 훨씬 안전한 것은 맞지만, 위험이 전혀 없는 것은 아니다.

만약 상위 클래스에 새 메서드가 추가됐는데, 하위 클래스에 추가한 메서드와 시그니처가 같고 반환 타입은 다르다면 해당 클래스는 컴파일조차 되지 않는다.

혹은 반환 타입마저 같다면 상위 클래스의 새 메서드를 재정의한 꼴이니 앞선 문제와 똑같은 상황에 부닥친다.

## 3. 컴포지션을 활용하여 문제 해결하기

기존 클래스를 확장하는 대신, 새로운 클래스를 만들고 **private 필드**로 기존 클래스의 인스턴스를 참조하도록 설계한다. **기존 클래스가 새로운 클래스의 구성 요소**로 쓰인다는 뜻에서 이러한 설계를 `컴포지션(composition)`이라 한다.

새 클래스의 메서드는 내부의 기존 클래스 메서드를 호출하여 결과를 반환하는데, 이를 **전달(forwarding)**이라 한다.

전달 역할만 수행하는 메서드를 **전달 메서드(forwarding method)**라고 부른다.

이 방식의 장점은 다음과 같다.
- 기존 클래스의 내부 구현 변경에 영향을 받지 않는다.
- 기존 클래스에 새로운 메서드가 추가되어도 동작에 영향이 없다.
- 원하는 기능을 안정적으로 덧붙일 수 있다.

<br>

### 3.1 예시: ForwardingSet과 InstrumentedSet

<details>
    <summary>ForwardingSet Code</summary>
<div markdown="1">

```java
public class ForwardingSet<E> extends Set<E> {
    private final Set<E> s;
  
    public ForwardingSet(Set<E> s) {
        this.s = s;
    }
    
    public void clear()                { s.clear(); }
    public boolean contains(Object o)  { return s.contains(o); }
    public boolean isEmpty()           { return s.isEmpty(); }
    public int size()                  { return s.size(); }
    public Iterator<E> iterator()      { return s.iterator(); }
    public boolean add(E e)            { return s.add(e); }
    public boolean containsAll(Collection<?> c)
                                       { return s.containsAll(c); }
    public boolean addAll(Collection<? extends E> c)
                                       { reutrn s.addAll(c); }
    public boolean retainAll(Collection<?> c)
                                       { return s.retainAll(c); }
    public Object[] toArray()          { return s.toArray(); }
    public <T> T[] toArray(T[] a)      { return s.toArray(a); }
    @Override public boolean equals(Object o)
                                       { return s.equals(o); }
    @Override public int hashCode()    { return s.hashCode(); }
    @Override public String toString() { return s.toString(); }
}
```
</div>
</details>

<details>
    <summary>InstrumentedSet Code</summary>
<div markdown="1">

```java
public class InstrumentedSet<E> extends ForwardingSet<E> {
    private int addCount = 0;
  
    public InstrumentedSet(Set<E> s) {
        super(s);
    }

    @Override public boolean add(E e) {
        addCount++;
        return super.add(e);
    }

    @Overrider public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return super.addAll(c);
    }

    public int getAddCount() {
        return addCount;
    }
}
```
</div>
</details>

`InstrumentedSet`은 특정 구현체(`HashSet`, `TreeSet` 등)에 직접 의존하지 않고, `Set` 인터페이스를 구현하여 설계된 클래스이다.

핵심은 원하는 `Set` 인스턴스를 생성자에서 받아 내부에 보관하고, 동작에 계측 기능을 덧붙이는 방식이다.

이렇게 하면 어떤 종류의 `Set`이든 동일한 계측 로직을 적용할 수 있으며, 기존 생성자 설정(초기 용량, 정렬 기준 등)도 그대로 유지할 수 있다.

```java
// 어떤 Set 구현체든 OK
Set<Instant> times = new InstrumentedSet<>(new TreeSet<>(cmp));
Set<String> names = new InstrumentedSet<>(new HashSet<>(INIT_CAPACITY));

// 특정 구간에서만 임시 계측도 가능
static void walk(Set<Dog> dogs) {
    InstrumentedSet<Dog> iDogs = new InstrumentedSet<>(dogs);
    // walk 동안의 add/remove만 카운트/로깅하고 범위를 벗어나면 끝
}
```

예를 들어, `new InstrumentedSet<>(new HashSet<>())` 처럼 일반적인 해시 기반 집합에도 적용할 수 있고, `new InstrumentedSet<>(new TreeSet<>(cmp))` 처럼 정렬 기반 집합에도 문제 없이 적용할 수 있다.

또한, 특정 범위나 조건에서만 임시로 계측하도록 감쌀 수도 있어 사용 범위가 매우 유연하다.

<br>

### 3.2 래퍼 클래스와 데코레이터 패턴

`InstrumentedSet` 처럼 기존 객체를 감싸(wrap)서 기능을 확장하는 구조를 **래퍼 클래스(Wrapper Class)**라고 부른다.

그리고 원래 기능을 유지한 채 부가 기능을 덧붙이는 설계는 **데코레이터 패턴(Decorator Pattern)**이라고 한다.

래퍼 클래스는 원본 객체의 메서드를 내부로 그대로 전달하면서, 호출 전후에 원하는 작업을 수행할 수 있다.

예를 들어, 요소가 추가되는 시점마다 카운트를 증가시키거나, 실행 로그를 남기는 등의 기능을 간단히 덧붙일 수 있다.

이 방식은 원본 코드 수정 없이 기능 확장이 가능하다는 점에서 재사용성과 확장성이 높다.

<br>

### 3.3 SELF 문제와 콜백 프레임워크의 제약

래퍼 클래스는 대부분의 경우 단점이 거의 없지만, 콜백을 사용하는 프레임워크에서는 주의가 필요하다.

콜백 구조에서는 자기 자신의 참조(this)를 외부 객체에 넘겨두었다가, 나중에 그 참조로 메서드를 호출하는 방식이 일반적이다.

문제는, 래퍼가 감싸고 있는 내부 객체는 자신이 래퍼에 의해 감싸져 있다는 사실을 모르기 때문에, 콜백 시 래퍼가 아닌 내부 원본 객체가 호출된다는 점이다.

이로 인해 래퍼에서 추가한 기능이 우회되어 동작하게 되는데, 이를 **SELF 문제**라고 부른다.

따라서 콜백 구조에서 래퍼를 사용할 때는, 반드시 래퍼 자신을 외부에 넘기거나, 콜백 경로에서 우회 호출이 발생하지 않도록 설계해야 한다.

### 3.4 전달 메서드의 성능 우려

일부 개발자들은 래퍼 클래스의 전달(위임) 방식이 성능과 메모리 사용에 영향을 줄 수 있다고 걱정한다.

그러나 실무 경험에 따르면 이러한 오버헤드는 대부분의 상황에서 무시할 수 있을 만큼 작다.

JVM 최적화(JIT 인라이닝)로 인해 메서드 호출 계층이 줄어드는 경우도 많으며, 래퍼 객체가 차지하는 메모리 역시 크지 않다.

오히려 유일한 단점은 전달 메서드를 일일이 작성해야 하는 번거로움인데, 이마자도 재사용 가능한 ‘전달 전용 클래스’를 만들어 두면 쉽게 해결할 수 있다.

실제로 구글의 Guava 라이브러리에는 모든 컬렉션 인터페이스에 대한 전달 클래스를 구현해 둔 예시가 있다.

## 4. 상속의 원칙과 is-a 관계

- 상속은 하위 클래스가 상위 클래스의 진정한 하위 타입일 때만 사용해야 한다.
- 두 클래스가 is-a 관계를 만족해야 하면, “B는 정말 A인가? 라는 질문에 확실히 “그렇다” 라고 답할 수 있어야 한다.
- 조건이 만족되지 않으면 상속 대신 컴포지션 사용
- 불필요하거나 위험한 기능을 외부로 노출하지 않고 원하는 동작만 선택적으로 제공

<br>

### 4.1 상속은 하위 클래스가 상위 클래스의 진정한 하위 타입일 때만 사용해야 한다.

상속은 단순히 코드 재사용을 위해 쓰는 기능이 아니다. 상속 구조가 올바르게 설계되려면 하위 클래스가 상위 클래스의 모든 특성과 동작을 자연스럽게 물려받아도 문제가 없는 경우여야 한다.

예를 들어, `사자`(Lion)는 `포유류`(Mammal)이고, `포유류`는 `동물`(Animal)이다. 이런 경우 `Lion extends Mammal`, `Mammal extends Animal` 같은 상속은 자엽스럽다. 왜냐하면 **사자는 포유류다라**는 관계가 사실이기 때문이다.

하지만, 단순히 기능이 비슷하다고 해서 상속하면 잘못된 경우가 생긴다. 예를 들어 Bird와 Airplane은 둘 다 날 수 있지만, **비행기가 새다**라고 할 수는 없다. 이 경우의 상속은 적절하지 않다.

<br>

### 4.2 두 클래스가 is-a 관계를 만족해야 하면, “B는 정말 A인가? 라는 질문에 확실히 “그렇다” 라고 답할 수 있어야 한다.

is-a 관계 검증은 **상속 설계 시 가장 중요한 테스트**이다. 여기서 “B는 정말 A인가?” 라는 질문은 **타입 계층 구조의 자연스러움**을 학인하는 방법이다.

예를 들어

- “고양이는 동물인가?” → Yes
- “스택은 리스트인가?” → 대부분의 경우 Yes
- “원은 타원인가?” → 수학적으로는 Yes 지만, 객체지향으로는 동작 제약 때문에 위험 No
- “비행기는 탈 것인가?” → Yes

<br>

### 4.3 조건이 만족되지 않으면 상속 대신 컴포지션 사용

예를 들어, `Car` 클래스 안에 `Engine` 객체를 두는 경우가 그렇다. `Car`는 `Engine`을 상속하지 않고, 단지 엔진을 내부에서 사용한다.

이 방식은 상속보다 유연하고, 상위 클래스의 변경이 하위 클래스에 영향을 주지 않게 할 수 있다.

- 상속: `class Car extends Engine { … }` → 잘못된 설계
- 컴포지션: 이렇게 하면 자동차가 엔진의 기능을 사용하되, 엔진의 내부 동작이나 불필요한 기능은 외부에 공개하지 않을 수 있다.
    ```java
    class Car {
        private Engine engine;
        public Car(Engine engine) { this.engine = engine; }
        public void start() { engine.start(); }
    }
    ```
<br>

### 4.4 불필요하거나 위험한 기능을 외부로 노출하지 않고 원하는 동작만 선택적으로 제공

상속을 하면 상위 클래스의 모든 public/protected 메서드가 하위 메서드에도 노출된다.

이게 문제인 이유는, 상위 클래스의 특정 메서드가 **하위 클래스의 문맥에서 사용하면 위험**할 수 있기 때문이다.

예를 들어, 위 코드인 `InstrumentedHashSet` 을 보면 `HashSet`의 `addAll` 메서드는 내부적으로 `add`를 여러 번 호출하는데, 하위 클래스에서 `add`를 오버라이드하면 예기치 못한 동작이 발생할 수 있다.

컴포지션을 쓰면 이런 위험을 줄일 수 있다. **필요한 메서드만 노출하고, 불필요하거나 위험한 메서드는 외부에서 호출할 수 없게 설계**하면 된다.

## 5. 잘못된 상속 사례

자파 표준 라이브러리에는 잘못된 상속 사례가 몇 가지 존재한다.

예를 들어, `Stack`은 LIFO 자료구조로 동작하는 스택이지만, `Vector`를 상속받아 불필요한 인덱스 접근 메서드까지 모두 노출한다.

또 다른 예로, `Properties`는 키와 값이 문자열만 가능하도록 설계되어야 하지만, 상위 클래스  `Hashtable`의 메서드를 그대로 상속받으면서 문자열이 아닌 객체도 저장할 수 있게 되어 불변식이 깨질 수 있다.

이런 설계는 혼란을 유발할 뿐 아니라, API의 일관성과 안정성을 해친다.

## 6. 컴포지션의 장점과 선택 기준

컴포지션을 사용하면 상위 클래스의 결함을 숨기고, 필요한 기능만 노출하는 새로운 API를 설계할 수 있다.

반면, 상속은 상위 클래스의 모든 기능과 그 결함까지 그대로 물려받게 된다.

따라서 상속을 사용하기 전에 다음과 같은 질문을 던져야 한다.

1. 상위 클래스의 API에 결함이 없는가?
2. 결함이 있다면 그 결함이 내 클래스에도 전파되어도 괜찮은가?
3. 상위 클래스와 하위 클래스가 정말로 is-a 관계를 만족하는가?

이 질문에 하나라도 “아니오”라는 답이 나온다면, 안전하고 유연한 선택은 컴포지션이다.