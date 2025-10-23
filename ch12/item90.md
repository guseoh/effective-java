## ✅ 가능한 한 실패 원자적으로 만들라

## 1. 직렬화된 프록시 패턴
먼저, **바깥 클래스의 논리적 상태를 정밀하게 표현하는 중첩 클래스를 설계해 private static으로 선언**하다. 이 중첩 클래스가 바로 바깥 클래스의 직렬화 프록시다.

중첩 클래스의 생성자는 단 하나여야 하며, 바깥 클래스를 매개변수로 받아야 한다.   
이 생성자는 단순히 인수로 넘어온 인스턴스의 데이터를 복사한다. **일관성 검사나 방어적 복사도 필요없다.**

설계상, 직렬화 프록시의 기본 직렬화 형태는 바깥 클래스의 직렬화 형태로 쓰기에 이상적이다. 그리고 바깥 클래스와 직렬화 프록시 모두 `Serializable`을 구현한다고 선언해야 한다.

<details>
    <summary>Period 클래스</summary>
<div markdown="1">

```java
class Period implements Serializable {
    private final Date start;
    private final Date end;
  
    public Period(Date start, Date end) {
        this.start = start;
        this.end = end;
    }
  
    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 2123123123L; // 아무 값이나 상관 없음.
        private final Date start;
        private final Date end;
  
        public SerializationProxy(Period p) {
            this.start = p.start;
            this.end = p.end;
        }

        private Object readResolve() {
            return new Period(start, end);
        }
    }
.
    private Object writeReplace() {
        return new SerializationProxy(this); // SerializationProxy 인스턴스 반환
    }
  
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("프록시가 필요합니다.");
    }
}
```
</div>
</details>

```java
private Object writeReplace() {
    return new SerializationProxy(this); 
}
```

- `writeReplace()`
    - 범용적이니 직렬화 프록시를 사용하는 모든 클래스에 그대로 복사해 쓰면 된다.
    - 자바의 직렬화 시스템이 바깥 클래스의 인스턴스 대신 `SerializationProxy` 의 인스턴스를 반환하게 하는 역할을 한다.
    - 덕분에 직렬화된 시스템은 결코 바깥 클래스의 직렬화된 인스턴스를 생성해낼 수 없다.

<br>

```java
private void readObject(ObjectInputStream stream) throws InvalidObjectException {
      throw new InvalidObjectException("프록시가 필요합니다.");
  }
```
- `readObject()`
    - 공격자가 불변식을 훼손하는 공격을 시도해도. 이 메서드를 바깥 클래스에 추가하면 가볍게 막을 수 있다.

<br>

```java
private Object readResolve() {
    return new Period(start, end);
}
```

- `readResolve()`
    - 바깥 클래스와 논리적으로 동일한 인스턴스를 반환한다.
    - 역직렬화 시에 직렬화 시스템이 직렬화 프록시를 다시 바깥 클래스의 인스턴스로 변환하게 해준다.
- 이 메서드는 공개된 API만을 사용해 바깥 클래스의 인스턴스를 생성하는 기능을 제공하는 패턴이다.
    - 직렬화는 생성자를 이용하지 않고도 인스턴스를 생성하는 기능을 제공한다.
    - 이 패턴은 직렬화의 이런 언어도단적 특성을 상당 부분 제거한다.
    - 즉, 일반 인스턴스를 만들 때와 똑같은 생성자, 정적 팩터리, 혹은 다른 메서드를 사용해 역직렬화된 인스턴스를 생성하는 것이다.
- 따라서 **역직렬화된 인스턴스가 해당 클래스의 불변식을 만족하는지 검사할 또 다른 수단을 강구하지 않아도 된다.**

<br>

## 2. 방어적 복사 vs 프록시 패턴
방어적 복사처럼, 직렬화 프록시 패턴은 가짜 바이트 스트림 공격과 내부 필드 탈취 공격을 프록시 수준에서 차단해준다.  
직렬화 프록시는 `Period`의 필드를 `final`로 선언해도 되므로 `Period` 클래스를 진정한 불변(아이템 17)으로 만들 수도 있다.  
어떤 필드가 기만적인 직렬화 공격의 목표과 될지 고민하지 않아도 되며, 역직렬화할 때 유효성 검사를 수행하지 않아도 된다.

직렬화 프록시 패턴은 **역직렬화한 인스턴스와 원래의 직렬화된 인스턴스의 클래스가 달라도 정상 작동**한다.

대표적이 사례인 `EnumSet`을 보자. (아이템 36)
```java
private static class SerializationProxy <E extends Enum<E>> implements Serializable
{ 
    // EnumSet의 원소 타입
    private final Class<E> elementType;
  
    // EnumSet 내부 원소
    private final Enum<?>[] elements;
  
    SerializationProxy(EnumSet<E> set) {
        elementType = set.elementType;
        elements = set.toArray(ZERO_LENGTH_ENUM_ARRAY);
    }
  
    // 새롭게 원소의 크기에 맞는 EnumSet 생성
    private Object readResolve() {
        EnumSet<E> result = EnumSet.noneOf(elementType);
        for (Enum<?> e : elements)
            result.add((E)e);
        return result;
    }
  
    private static final long serialVersionUID = 362491234563181265L;
}
```

- `EnumSet` 클래스는 `public` 생성자 없이 정적 팩터리들만 제공한다.
- 클라이언트 입장에서는 `EnumSet` 인스턴스를 반환하는 걸로 보이지만,
    - 현재의 OpenJDK를 보면 열거 타입의 크기에 따라 두 하위 클래스 중 하나의 인스턴스를 반환한다.
    - 열거 타입의 원소가 64개 이하면 `RegularEnumSet`을 사용, 그보다 크면 `JumboEnumSet`을 사용하는 것이다.
- 처음에 64개의 열거 타입을 가진 `EnumSet`을 직렬화하고, 5개의 원소를 추가했다 가정해보자.
    - 처음 직렬화된 것은 `RegularEnumSet` 인스턴스다. (64 개)
    - 하지만 역직렬화는 `JumboEnumSet` 인스턴스로 하면 좋을 것이다. (64+5 개)

<br>

## 3. 직렬화 프록시 패턴의 한계
1. 클라이언트가 멋대로 확장할 수 있는 클래스(아이템 19)에는 적용할 수 없다.
2. 객체 그래프 순환이 있는 클래스에도 적용할 수 없다.
    - 순환 구조에서는 객체 간의 참조 관계를 프록시 수준에서 완벽히 복원할 방법이 없다.

이런 객체의 메서드를 직렬화 프록시의 `readResolve` 안에서 호출하려 하면 `ClassCastException`이 발생할 것이다. 직렬화 프록시만 가졌을 뿐 실제 객체는 아직 만들어진 것이 아니기 때문이다.

그리고 직렬화 프록시 패턴이 주는 강력함과 안전성에도 대가는 따른다. 속도가 느려진다.


<br>

## 4. 핵심 정리

- 제3자가 확장할 수 없는 클래스라면 가능한 한 직렬화 프록시 패턴을 사용하자.
- 이 패턴이 중요한 불변식을 안정적으로 직렬화해주는 가장 쉬운 방법일 것이다.