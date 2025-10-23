## ✅ readObject 메서드는 방어적으로 작성하라

## 1. 불변 클래스 직렬화
아이템 50에서 불변인 날짜 범위 클래스를 만드는 데 가변인 `Date` 필드를 이용했다.

불변식을 지키고 유지하기 위해 생성자와 접근자에 `Date` 객체를 방어적으로 복사하느라 코드가 상당히 길어졌다.

```java
// 방어적 복사를 사용하는 불변 클래스
public final class Period {
    private final Date start;
    private final Date end;
  
    /**
     * @param  start 시작 시각
     * @param  end 종료 시각; 시작 시각보다 뒤여야 한다.
     * @throws IllegalArgumentException 시작 시각이 종료 시각보다 늦을 때 발생한다.
     * @throws NullPointerException start나 end가 null이면 발생한다.
     */
    public Period(Date start, Date end) {
        this.start = new Date(start.getTime()); // 새로운 객체로 방어적 복사
        this.end = new Date(end.getTime());
        if (this.start.compareTo(this.end) > 0) {
            throw new IllegalArgumentException(start + " after " + end);
        }
    }
  
    public Date start() { return new Date(start.getTime()); }
    public Date end() { return new Date(end.getTime()); }
    public String toString() { return start + " - " + end; }
    
    // ... 나머지 코드는 생략
}
```

- 이 클래스를 직렬화하기로 결정했다고 해보자.
- `Period` 객체의 물리적 표현이 논리적 표현과 부합하므로 기본 직렬화 형태(아이템 87)를 사용해도 나쁘지 않다.
    - 그러니 `implements Serializable`을 추가하면 된다.
    - 하지만 이렇게 해서는 클래스의 주요한 불변식을 보장하지 못하게 된다.
- 문제는 `readObject` 메서드가 실질적인 또 다른 `public` 생성자이기 때문이다.
    - 따라서 다른 생성자와 같이 주의를 기울여야 한다.
    - 보통의 생성자처럼 `readObject` 메서드에서도 유효성 검사해야 하고(아이템 49), 필요하다면 매개변수를 방어적으로 복사해야 한다.(아이템 50)

<br>

## 2. readObject 
- 매개변수로 바이트 스트림을 받는 생성자
- 보통의 경우 바이트 스트림은 정상적으로 생성된 인스턴스를 직렬화해 만들어진다.
- 하지만 불변식을 깨뜨릴 의도로 임의 생성한 바이트 스트림을 건네면 문제가 생긴다.
    - 정상적인 생성자로는 만들어낼 수 없는 객체를 생성해낼 수 있기 때문
<details>
    <summary>허용되지 않는 Period 인스턴스를 생성할 수 있다.</summary>
<div markdown="1">

```java
public class BogusPeriod {
    // 진짜 Period 인스턴스에서는 만들어질 수 없는 비정상적 바이트 스트림
    private static final byte[] serializedForm = {
        (byte)0xac, (byte)0xed, 0x00, 0x05, 0x73, 0x72, 0x00, 0x06,
        0x50, 0x65, 0x72, 0x69, 0x6f, 0x64, 0x40, 0x7e, (byte)0xf8,
        0x2b, 0x4f, 0x46, (byte)0xc0, (byte)0xf4, 0x02, 0x00, 0x02,
        0x4c, 0x00, 0x03, 0x65, 0x6e, 0x64, 0x74, 0x00, 0x10, 0x4c,
        0x6a, 0x61, 0x76, 0x61, 0x2f, 0x75, 0x74, 0x69, 0x6c, 0x2f,
        0x44, 0x61, 0x74, 0x65, 0x3b, 0x4c, 0x00, 0x05, 0x73, 0x74,
        0x61, 0x72, 0x74, 0x71, 0x00, 0x7e, 0x00, 0x01, 0x78, 0x70,
        0x73, 0x72, 0x00, 0x0e, 0x6a, 0x61, 0x76, 0x61, 0x2e, 0x75,
        0x74, 0x69, 0x6c, 0x2e, 0x44, 0x61, 0x74, 0x65, 0x68, 0x6a,
        (byte)0x81, 0x01, 0x4b, 0x59, 0x74, 0x19, 0x03, 0x00, 0x00,
        0x78, 0x70, 0x77, 0x08, 0x00, 0x00, 0x00, 0x66, (byte)0xdf,
        0x6e, 0x1e, 0x00, 0x78, 0x73, 0x71, 0x00, 0x7e, 0x00, 0x03,
        0x77, 0x08, 0x00, 0x00, 0x00, (byte)0xd5, 0x17, 0x69, 0x22,
        0x00, 0x78
    };
  
    public static void main(String[] args) {
        Period p = (Period) deserialize(serializedForm);
        System.out.println(p);
    }
      
    static Object deserialize(byte[] sf) {
        try {
            return new ObjectInputStream(new ByteArrayInputStream(sf)).readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
```
</div>
</details>

- Period 클래스 선언에 `implements Serializable` 만 추가했다고 하자.
- 이 프로그램은 종료 시각이 시작 시각보다 앞서는 Period 인스턴스를 만들 수 있다.
- Period를 직렬화할 수 있도록 선언한 것만으로 클래스의 불변식을 깨뜨리는 객체를 만들 수 있다는 것을 확인했다.

<br>

## 3. 해결 방법

### 3.1 유효성 검사
```java
private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    s.defaultReadObject();
    
    // 불변식을 만족하는지 검사한다.
    if (start.compareTo(end) > 0) {.
        throw new InvalidObjectException(start + " 가 " + end + " 보다 늦다.");
    }
}
```

- `Period`의 `readObject` 메서드가 `defaultReadObject`를 호출한 다음 역직렬화된 객체가 유효한지 검사해야 한다.
    - 유효성 검사가 실패하면 `InvalidObjectException`를 던지게 하여 잘못된 역직렬화가 일어나는 것을 막을 수 있다.

<br>

### 3.2 방어적 복사

- 하지만 정상 `Period` 인스턴스에서 시작된 바이트 스트림 끝에 `private Date` 필드로의 참조를 추가하면 가변 `Period` 인스턴스를 만들어낼 수 있다.
    - 공격자는 `ObjectInputStream` 에서 `Period` 인스턴스를 읽은 후
    - 스트림 끝에 추가된 ‘악의적인 객체 참조’를 읽어 `Period` 객체의 내부 정보를 얻을 수 있다.
    - 이제 `Date` 인스턴스들은 수정할 수 있으니, `Period` 인스턴스는 더는 불변이 아니게 된 것이다.
<details>
    <summary> 가변 공격의 예</summary>
<div markdown="1">

```java
public class MutablePeriod {
   public final Period period;

   public final Date start;

   public final Date end;

   public MutablePeriod() {
       try {
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         ObjectOutputStream out = new ObjectOutputStream(bos);

         // 불변식을 유지하는 Period 를 직렬화.
         out.writeObject(new Period(new Date(), new Date()));

         /*
          * 악의적인 '이전 객체 참조', 내부 필드로의 참조를 추가.
          */
         byte[] ref = { 0x71, 0, 0x7e, 0, 5 }; // 악의적인 참조
         bos.write(ref); // 시작 필드
         ref[4] = 4; // 악의적인 참조
         bos.write(ref); // 종료 필드

         // 역직렬화 과정에서 Period 객체의 Date 참조를 훔친다.
         ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
         period = (Period) in.readObject();
         start = (Date) in.readObject();
         end = (Date) in.readObject();
       } catch (IOException | ClassNotFoundException e) {
           throw new AssertionError(e);
       }
   }
```
</div>
</details>

<details>
    <summary>공격 코드</summary>
<div markdown="1">

```java
public static void main(String[] args) {
    MutablePeriod mp = new MutablePeriod();
    Period mutablePeriod = mp.period; // 불변 객체로 생성한 Period
    Date pEnd = mp.end; // MutablePeriod 클래스의 end 필드
    
    pEnd.setYear(78); // MutablePeriod 의 end 를 바꿨는데 ?
    System.out.println(mutablePeriod.end()); // Period 의 값이 바뀐다.
    
    pEnd.setYear(69);
    System.out.println(mutablePeriod.end());
}
```
</div>
</details>

- `Period` 인스턴스는 불변식을 유지한 채 생성되었지만, 내부 값을 수정할 수 있다.
- 이처럼 변경할 수 있는 `Period` 인스턴스를 획득한 공격자는 이 인스턴스가 불변이라고 가정하는 클래스에 넘겨 엄청난 보안 문제를 일으킬 수 있다.
- 이 문제의 근원은 `Period`의 `readObject` 메서드가 **방어적 복사를 충분히 하지 않은** 데 있다.
    - 객체를 역직렬화할 때는 클라이언트가 소유해서는 안 되는 객체 참조를 갖는 필드를 모두 반드시 방어적으로 복사해야 한다.
    - 따라서 `readObject` 에서는 불변 클래스 안의 모든 private 가변 요소를 방어적으로 복사해야 한다.
<details>
    <summary>방어적 복사와 유효성 검사를 수행하는 readObject 메서드</summary>
<div markdown="1">

```java
private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    s.defaultReadObject();
    
    // 가변 요소들을 방어적으로 복사한다.
    start = new Date(start.getTime());
    end   = new Date(end.getTime());
    
    // 불변식을 만족하는지 검사한다.
    if (start.compareTo(end) > 0) {
        throw new InvalidObjectException(start + "가 " + end + "보다 늦다.");
    }
}
```
</div>
</details>

- **방어적 복사를 유효성 검사보다 앞서 수행**하며, `Date`의 `clone` 메서드는 사용하지 않았음에 주목하자.
- **`final` 필드는 방어적 복사가 불가능**하니 주의하자.
    - 이 `readObject` 메서드를 사용하려면 `start`와 `end` 필드에서 `final` 한정자를 제거해야 한다.

<br>

## 4. 기본 readObject 사용 여부 판단

- `transienet` 필드를 제외한 모든 필드의 값을 매개변수로 받아 **유효성 검사 없이** 필드에 대입하는 public 생성자를 추가해도 괜찮은가?
    - 아니요 → 커스텀 `readObject` 메서드를 만들어 모든 유효성 검사와 방어적 복사를 수행 혹은 직렬화 프록시 패턴을 사용하는 방법도 있다.
    - 예 → 사용 가능
<details>
    <summary>transient</summary>
<div markdown="1">

- **직렬화 시 제외해야 할 필드**라는 뜻
- 즉, `transient` 로 선언된 필드는
    - **직렬화할 때 바이트 스트림으로 저장되지 않으며,**
    - **역직렬화 시에도 값이 복원되지 않는다.**
</div>
</details>

<br>

## 5. 핵심 정리
- `readObject` 메서드를 작성할 때는 언제나 `public` 생성자를 작성하는 자세로 임해야 한다.
- `readObject`는 어떤 바이트 스트림이 넘어오더라도 유효한 인스턴스를 만들어내야 한다.
    - 바이트 스트림이 진짜 직렬화된 인스턴스라고 가정해서는 안 된다.

### readObject 메서드를 작성하는 지침 요약
- `private`이어야 하는 객체 참조 필드는 각 필드가 가리키는 객체를 방어적으로 복사하라.
    - 불변 클래스 내의 가변 요소가 여기에 속한다.
- 모든 불변식을 검사하여 어긋나는 게 발견되면 `InvalidObjectException`을 던진다.
    - 방어적 복사 다음에는 반드시 불변식 검사가 뒤따라야 한다.
- 역직렬화한 후 객체 그래프 전체의 유효성을 검사해야 한다면 `ObjectInputValidation` 인터페이스를 사용하라
- 직접적이든 간접적이든, 재정의할 수 있는 메서드는 호출하지 말자.