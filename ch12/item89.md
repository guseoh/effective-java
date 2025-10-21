### ✅ 인스턴스 수를 통제해야 한다면 readResolve 보다는 열거 타입을 사용하라

싱글턴 클래스가 `Serializable`을 구현하면, 자바의 기본 **역직렬화 과정은 기존 싱글턴과 무관하게 새 인스턴스를 생성**하므로 싱글턴이 깨질 수 있습니다.
`readObject()`만으로는 이를 막을 수 없고 `readResolve()` 메서드를 정의해야만 해결할 수 있습니다.
`readResolve()` 메서드는 역직렬화 직후 호출되어 최종적으로**기존 싱글턴 인스턴스를 반환하도록 강제하는 역할**을 합니다. 덕분에 역직렬화 과정에서 잠시 생성되었던 새로운 객체는 참조를 잃고 곧바로
가비지 컬렉션의 대상이 되므로, 싱글턴 패턴을 안전하게 유지할 수 있습니다.

이렇게 `readResolve()` 메서드를 활용하여 역직렬화 시 새로 생성된 인스턴스를 무시하고 클래스 초기화 때 만들어진 기존 싱글턴 인스턴스만을 반환하도록 통제할 경우,
새로 생성되는 임시 객체는 결국 버려지게 되므로 실제 데이터를 가질 필요가 전혀 없습니다.
따라서 싱글턴의 상태를 나타내는 인스턴스 필드 중 직렬화에 가능한 모든 인스턴스 필드에 `transient` 키워드를 선언하여 직렬화 대상에서 제외해야 합니다.
만약 `transient` 키워드를 사용하지 않으면 `MutablePeriod` 공격과 유사한 방식으로 `readResolve()` 메서드가 수행되기 이전에 역직렬화된 객체의 참조를 악용할 여지가 남게 됩니다.
`transient` 선언은 이러한 잠재적 공격을 방지할 뿐만 아니라, 불필요한 직렬화 오버헤드를 줄이고 임시 인스턴스에 데이터가 잘못 로드되는 것을 막아 싱글턴 구현의 일관성과 효율성을 높이는 조치입니다.

**번외**  
[`readResolve`로 인스턴스 제어를 하는 클래스라면 가능한 한 모든 인스턴스 필드를
`transient`로 둬야 하는 자세한 이유](https://docs.oracle.com/en/java/javase/11/docs/specs/serialization/input.html)

### 문제 코드

```java

// transient 키워드를 사용하지 않은 싱글턴 패턴
public class Elvis implements Serializable {

    public static final Elvis INSTANCE = new Elvis();
    private String[] favoriteSongs = {"Hound Dog", "Heartbreak Hotel"};

    private Elvis() {
    }

    public void printFavorites() {
        System.out.println(Arrays.toString(favoriteSongs));
    }

    private Object readResolve() {
        return INSTANCE;
    }
}

// Elvis를 참조하는 클래스 - transient 키워드를 사용하지 않은 싱글턴 클래스 참조
public class ElvisStealer implements Serializable {
    static Elvis impersonator;
    private Elvis payload;

    private Object readResolve() {
        // resolve되기 전의 Elvis 인스턴스의 참조를 저장한다. 
        impersonator = payload;
        // favoriteSongs 필드에 맞는 타입의 객체를 반환한다. 
        return new String[]{"A Fool Such as I"};
    }

    private static final long serialVersionUID = 0;
}

public class ElvisImpersonator {
    // 진짜 Elvis 인스턴스로는 만들어질 수 없는 바이트 스트림
    private static final byte[] serializedForm = {
            -84, -19, 0, 5, 115, 114, 0, 20, 107, 114, 46, 115,
            101, 111, 107, 46, 105, 116, 101, 109, 56, 57, 46, 69,
            108, 118, 105, 115, 98, -14, -118, -33, -113, -3, -32,
            70, 2, 0, 1, 91, 0, 13, 102, 97, 118, 111, 114, 105, 116,
            101, 83, 111, 110, 103, 115, 116, 0, 19, 91, 76, 106, 97,
            118, 97, 47, 108, 97, 110, 103, 47, 83, 116, 114, 105, 110,
            103, 59, 120, 112, 117, 114, 0, 19, 91, 76, 106, 97, 118,
            97, 46, 108, 97, 110, 103, 46, 83, 116, 114, 105, 110, 103,
            59, -83, -46, 86, -25, -23, 29, 123, 71, 2, 0, 0, 120, 112,
            0, 0, 0, 2, 116, 0, 9, 72, 111, 117, 110, 100, 32, 68, 111,
            103, 116, 0, 16, 72, 101, 97, 114, 116, 98, 114, 101, 97, 107,
            32, 72, 111, 116, 101, 108
    };

    public static void main(String[] args) {
        // ElvisStealer.impersonator를 초기화한 다음,
        // 진짜 Elvis(즉 Elvis.INSTANCE)를 반환한다.
        Elvis elvis = (Elvis) deserialize(serializedForm);
        Elvis impersonator = ElvisStealer.impersonator;

        elvis.printFavorites();
        impersonator.printFavorites();
    }
}
```

1. 스트림을 읽어 Elvis를 새로 할당
2. `favoriteSongs` 필드를 복원하려는데, 스트림에는 평범한 `String[]`대신 `ElvisStealer` 객체가 들어 있고(유형은 스트림이 제공한 대체 객체로 맞춤),
   그 내부 필드 payload에는 역참조로 방금 만든 `Elvis`가 연결됩니다.
3. `ElvisStealer.readResolve()`가 `Elvis`보다 먼저 호출되어 `impersonator = payload`로 임시 `Elvis` 참조를 빼내고,
   반환값으로 `String[]`을 돌려 `favoriteSongs` 타입 제약을 만족시킵니다.
4. 이제야 `Elvis.readResolve()`가 호출되어 **최종 반환값은 `Elvis.INSTANCE`**가 되지만,
   이미 `ElvisStealer.impersonator`가 임시 `Elvis`를 붙잡았으므로 결과적으로 아래와 같은 인스턴스가 생성됩니다.
   elvis → Elvis.INSTANCE (정상 싱글턴)
   impersonator → 임시 Elvis (유출된 두 번째 객체)

해당 싱글턴 코드에서 공격자가 악용할 수 있는 지점은 바로 `favoriteSongs` 필드입니다.
공격자는 이 필드를 통해 임의의 객체 그래프를 직렬화 스트림에 주입할 수 있으며,
주입된 객체 그래프의 `readResolve()` 메서드가 싱글턴의 `readResolve()`보다 먼저 실행되면서 역직렬화 과정 중 잠시 생성된 임시 싱글턴 인스턴스(Elvis 객체)의 참조를 탈취할 수 있습니다.

그러나 `favoriteSongs` 필드를 **transient**로 선언하면, 이 필드가 역직렬화 대상에서 완전히 제외되므로,
외부에서 임의의 객체를 주입할 수 있는 공격 지점 자체가 사라집니다.
그 결과, `ElvisStealer`와 같은 공격 클래스가 역직렬화 과정에 끼어들어 싱글턴 인스턴스의 참조를 가로챌 여지가 원천적으로 차단됩니다.

### 문제 해결 코드 (MutablePeriod 공격 ) - 자세한 내용은 아이템 88 참조

```java
public enum Elvis {
    INSTANCE;
    private String[] favoriteSongs = {"Hound Dog", "Heartbreak Hotel"};

    public void printFavorites() {
        System.out.println(Arrays.toString(favoriteSongs));
    }
}
```

인스턴스를 한 개만 만들기 위해 `readResolve()`를 사용하는 방식이 결코 쓸모없는 활동은 아닙니다.
특히, 직렬화 가능한 인스턴스 통제 클래스를 작성해야 하는데 **컴파일 타임에는 어떤 인스턴스들이 존재하는지 알 수 없는 상황**이라면
열거 타입(enum)으로 표현하는 것이 불가능하기 때문입니다.
다만, 일반적으로 싱글턴을 구현할 때는 `enum` 타입의 사용이 권장되는데,
JVM이 `enum`을 특별 취급(직렬화/역직렬화 시 클래스 이름과 상수 이름으로 식별)하기 때문에 역직렬화가 새로운 객체를 만들지 않으며,
리플렉션(Reflection)을 통한 인스턴스 재생성 시도 역시 내부적으로 차단되어 안전한 싱글턴이 보장됩니다.

### readResolve() 메서드 접근성

readResolve() 메서드 접근성은 매우 중요합니다.

<table>
  <thead>
    <tr>
      <th>클래스 타입</th>
      <th>접근 제어자</th>
      <th>이유 및 주의사항</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>final 클래스</td>
      <td>private</td>
      <td>`final` 클래스라면 상속이 불가능하므로, `readResolve()`를 `private`으로 선언하는 것이 안전합니다. 이로써 외부 접근 및 악의적인 재정의 가능성을 완벽하게 차단할 수 있습니다.</td>
    </tr>
    <tr>
      <td rowspan="3">final이 아닌 클래스</td>
      <td>private</td>
      <td>하위 클래스에서 상속되지 않으므로, 하위 클래스가 자신만의 `readResolve()`를 구현하지 않으면 문제가 발생할 수 있습니다.</td>
    </tr>
    <tr>
      <td>package-private</td>
      <td>같은 패키지에 속한 하위 클래스에서만 접근 및 재정의하여 사용할 수 있습니다. 이는 패키지 내부에서만 싱글턴을 통제하려는 경우 적합합니다.</td>
    </tr>
    <tr>
      <td>protected 또는 public</td>
      <td>이를 재정의하지 않은 모든 하위 클래스에서 부모 클래스의 `readResolve()` 메서드가 상속되어 사용됩니다. 이 방식은 매우 위험합니다. <br/> 서브 클래스의 인스턴스를 역직렬화할 경우, 재정의되지 않은 부모 클래스의 `readResolve()`가 실행되어 부모 클래스의 싱글턴 인스턴스를 반환하게 됩니다. <br/> 그 결과, 서브 클래스의 인스턴스를 기대했던 곳에서 부모 클래스의 인스턴스를 받게 되어 `ClassCastException` 예외가 발생할 수 있습니다.</td>
    </tr>
  </tbody>
</table>
