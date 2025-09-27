### ✅ 적시에 방어적 복사본을 만들라

## 0. 들어가기 전
자바는 C, C++에 비해 메모리 관리 측면에서 안전하다. 자바로 작성한 클래스는 **불변식이 지켜진다**. 메모리 전체를 하나의 거대한 배열로 다루는 언어에서는 누릴 수 없는 강점이다.

하지만 다른 클래스로부터의 침범을 아무런 노력 없이 다 막을 수 있는 건 아니다. 프로그래머의 실수로 인해 **의도하지 않는 변경**이 일어날 수 있다. **외부에 의해 불변식이 깨질 수 있다**는 뜻이다.

일반적으로 객체를 생성할 때 수정자와 같은 장치가 없다면 욉에서 내부를 수정하는 것을 막아놨음을 의미한다. 하지만 **자기도 모르게 내부를 수정하도록 허락**하는 경우가 생긴다.

<br>

## 1. 공격 - 생성자 이용
```java
public final class Period {
    private final Date start;
    private final Date end;
    
    /**
     * @param start 시작 시각
     * @param end 종료 시각; 시작 시각보다 뒤여야 한다.
     * @throws IllegalArgumentException 시작 시각이 종료 시각보다 늦을 때 발생한다.
     * @throws NullPointerException start나 end가 null이면 발생한다.
    */
    public Period(Date start, Date end) {
        if(start.compareTo(end) > 0) 
            throw new IllegalArgumentException(
                start + "가 " + end + "보다 늦다.");
                
        this.start = start;
        this.end = end;
    }
    
    public Date start() {
        return start;
    }
    
    public Date end() {
        return end;
    }
    
    ...
}
```

‘**시작 시각이 종료 시각보다 늦을 수 없다는 불변식**’이 있는 위 클래스가 있다고 하자.

이 클래스는 [Date](https://d2.naver.com/helloworld/645609)가 가변이라는 사실을 이용한다면 어렵지 않게 불변식이 깨진다.

```java
Date start = new Date();
Date end = new Date();
Period p = new Period(start, end);
end.setYear(78); // p의 내부를 수정!!
```
외부 공격으로부터 Period 인스턴스의 내부를 보호하려면 **생성자에서 받은 가변 매개변수 각각을 방어적 복사(defensive copy) 해야 한다.**

```java
public Period(Date start, Date end) {

    this.start = new Date(start.getTime()); // defensive copy
    this.end = new Date(end.getTime());     // defensive copy
    
    if(this.start.compareTo(this.end) > 0)
        throw new IllegalArgumentException(
            this.start + "가 " + this.end + "보다 늦다.");
}
```

`Defensive Copy`는 인스턴스 내부 필드를 설정할 때, 파라미터로 온 값을 그대로 할당하는게 아닌 복사하는 방법이다.

매개변수로 받은 가변객체에 대해 **똑같은 타입의 새로운 객체(복사)한 후 멤버필드에 할당**하고 있다.

또한, 방어적 복사에 Date의 clone 메서드를 사용하지 않은 점에도 주목하자. **Date는 final이 아니므로 clone이 Date가 정의한게 아닐 수 있기 때문**이다.

즉, clone이 악의를 가진 하위 클래스의 인스턴스를 반환할 수도 있다.

<br>

## 2. 공격 - 접근자 이용

생성자를 수정하면 앞서의 공격은 막아낼 수 있지만, Period 인스턴스는 아직도 변경이 가능하다.

**접근자 메서드가 내부의 가변 정보를 직접 드러내기 때문**이다.

```java
Date start = new Date();
Date end = new Date();
Period p = new Period(start, end);
p.end().setYear(78); // p의 내부를 변경!!!
```

이를 방어하기 위해서는 단순히 **접근자가 가변 필드의 방어적 복사본을 반환**하면 된다.

```java
public Date start() {
    return new Date(start.getTime());
}

public Date end() {
    return new Date(end.getTime());
}
```

<br>

## 3. 방어적 복사
위 코드를 보면 **유효성 검사 보다 방어적 복사본을 만드는 코드가 먼저 위치**한다. 유효성 검사를 먼저하는게 당연하다 생각할 수 있지만 이유가 있다.  
  
```java
public Period(Date start, Date end){

    this.start = new Date(start.getTime()); // 방어적 복사 먼저
    this.end = new Date(end.getTime()); // 방어적 복사 먼저

    if(start.compareTo(end) > 0){ // 그 다음 유효성 검사
        throw new IllegalArgumentException(start +" 가 "+ end +" 보다 늦다.");
    }
}
```
유효성 검사를 먼저 할 경우 **멀티스레드 환경에서 불변식을 해치는 상황**이 발생할 수 있기 때문이다.

유효성 검사를 끝낸 직후 방어적 복사를 하기 전에 다른 스레드에서 파라미터로 들어온 가변객체의 값을 변경이라도 한다면 불변식이 깨져버린다.

### 3.1 방어적 복사의 사용 시기

매개변수를 방어적으로 복사하는 목적은 **외부로부터 들어오는 매개변수가 가변 객체**이고, 이를 **인스턴스 내부에서 갖게 될 때 인스턴스의 불변식을 지키기 위함**이다.

인스턴스 내에 멤버필드를 추가할 때에는 ‘**변경될 수 있는 멤버필드인가**’를 항상 고려해야 한다.

변경되도 상관 없다면 방어적 복사를 할 필요가 없지만, 불변식을 가져야 한다면 방어적 복사를 사용해야 한다. 멤버 필드의 불변성을 확신할 수 없을 때도 방어적 복사를 사용하는 것이 좋다.

하지만 **방어적 복사에는 성능 저하가 따르고, 또 항상 쓸 수 있는 것도 아니다**. (같은 패키지에 속하는 등의 이유로)

호출자가 컴포넌트 내부를 수정하지 않으리라 확신하면 방어적 복사를 생략할 수 있다. 이러한 상황이라도 호출자에서 해당 매개변수나 반환값을 수정하지 말아야 함은 명확히 **문서화**하는게 좋다. 다른 패키지에서 사용하는 경우도 마찬가지로 확실히 문서화해야 한다.

<br>

## 4. 핵심 정리

- 클래스가 클라이언트로부터 받는 혹은 클라이언트로 반환하는 구성요소가 가변이라면 그 요소는 반드시 방어적으로 복사해야 한다.
- 복사 비용이 너무 크거나, 잘못 수정할 일이 없음을 신뢰한다면 방어적 복사 대신 수정 책임이 클라이언트에 있음을 문서에 명시하도록 하자.