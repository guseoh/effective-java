### ✅ 커스텀 직렬화 형태를 고려해보자

### 커스텀 직렬화 사용 이유

일정상의 제약으로 인해 현재 버전(v1)에서는 핵심 API 기능만 구현하고, 다음 릴리스(v2)에서 해당 구현을 완전히 개선하거나 대체하려는 상황이 발생할 수 있습니다.

문제는 이때 클래스에 `Serializable` 인터페이스를 구현하고 **기본 직렬화 형태를 사용**하게 되면 다음 버전(v2)에서 버리거나 개선하려 했던 **v1의 현재 구현에 영구히 발이 묶이는 결과**를
초래한다는 점입니다. 왜냐하면 한 번 객체가 직렬화되어 외부에 저장되는 순간, 이 객체로부터 생성된 **바이트 스트림 인코딩 자체가 사실상의 공개 API처럼 취급**되기 때문입니다.

결국에 다음 버전(v2)은 이전 버전(v1)에서 만들어진 직렬화 데이터를 문제없이 읽고 해석할 수 있도록 호환성을 유지해야 합니다.
그러나 **기본 직렬화 형태는 클래스의 물리적인 내부 구조(필드 구성)** 에 강력하게 의존하므로,
이 구조를 변경하는 순간 호환성이 깨지게 됩니다. 따라서 다음 버전(v2)에서 자유롭게 개선하려 했던 클래스가 v1의 비효율적이거나 **임시적인 구조에 영구히 발이 묶이는 결과**로 이어집니다.

실제로 이러한 문제점은 `BigInteger`와 같은 자바 표준 라이브러리 클래스 중 일부에서도 발견된 바 있습니다.
그러므로 직렬화를 구현할지 결정할 때는 이러한 문제점과 기본 직렬화 형태의 간편함을 신중하게 비교해야 합니다.
객체의 물리적 표현(내부 구조)과 논리적 표현(데이터의 의미)이 동일하다면 기본 직렬화 형태를 사용해도 무방합니다.

하지만 두 표현의 차이가 크거나, 객체의 논리적인 상태만을 외부에 노출하고 싶다면 반드시 커스텀 직렬화 형태를 고려하는 것이 좋습니다.
예를 들어, `writeReplace/readResolve` 메서드를 사용하거나 직렬화 프록시 패턴을 활용하는 것이 효과적인 대안이 될 수 있습니다. -- 이건 보류 일단

### 물리적 표현(내부 구조)과 논리적 표현(데이터의 의미) 차이점

<details>
    <summary>코드 예시</summary>
<div markdown="1">

```java
public class Name implements Serializable {

    /**
     * 성 
     * Notnull
     * @serial
     */
    private final Stirng lastName;

    /**
     * 이름 
     * Notnull
     * @serial
     */
    private final String firstName;

    /**
     * 중간이름 - 중간 이름이 없다면 null
     * @serial
     */
    private final String middleName;

    // ...
}
```

</div>
</details>

| 구분 |                                                     물리적 표현                                                     |                   논리적 표현                   |
|:--:|:--------------------------------------------------------------------------------------------------------------:|:------------------------------------------:|
| 정의 |                                클래스의 멤버 변수(필드) 그 자체이며, 객체가 메모리상에서 실제로 구성되는 내부 구조                                | 물리적 표현을 토대로 하여 성립하는, 추상적이고 유효한 데이터의 의미입니다. |
| 특징 |                                          기본 직렬화가 바이트 스트림에 그대로 기록하는 대상                                          |      실제 필드랑 상관없이 나타내고자 하는 비즈니스/추상적 개념      |
| 예시 | `private final String lastName` <br/> `private final String firstName` <br/> `private final String middleName` |     위 세 필드가 모여서 이루는 **한 사람의 이름**이라는 개념     |

### 기본 직렬화의 나쁜 예시 및 이유

해당 코드에 클래스는 기본 직렬화 형태에 적합하지 않은 클래스입니다.

```java
public final class StringList implements Serializable {
    private int size = 0;
    private Entry head = null;

    private static class Entry implements Serializable {
        String data;
        Entry next;
        Entry previous;
    }
    // ... 생략
}
```

**적합하지 않은 이유**

1. `StringList.Entry`와 같은 **내부 구현 클래스까지 공개 API**의 일부가 되어버리기 때문에 향후 버전에서 `StringList`의 내부 표현 방식을 변경하더라도 이전
   버전과의 호환성을 위해 연결 리스트 처리와 관련된 기존 코드를 영구적으로 제거할 수 없게 됩니다. ex) 연결 리스트 -> 배열로 변경
2. **불필요한 오버헤드가 발생**합니다. 기본 직렬화에 필요한 데이터는 `data`, `size` 필드 뿐입니다. 하지만 `StringList`가 `Entry` 이중 연결 리스트 구조로
   구현되어 있기 때문에, `Entry` 객체의 구조적 오버헤드와 모든 연결 포인터까지 불필요하게 바이트 스트림에 포함시켜 데이터 크기를 비대하게 만들고 성능을 저하시키게 됩니다.
3. `StringList`를 직렬화하기 시작할 때 객체들이 어떻게 연결되어 있는지 미리 알 수 없습니다. 그래서 실시간으로 모든 객체를 탐색해야 되기 때문에 시간이 오래 걸릴 수 있습니다.
4. StringList의 원소의 개수를 1,000개에서 1,800개 정도로 늘리면 직렬화 과정에서 **StackOverflowError**가 발생할 수 있습니다. 조금 더 생각하면 이러한 예외 상황은 단순히 원소
   개수에만 의존하지 않고, JVM 설정이나 **운영 환경(OS)** 에 할당된 스택 메모리 크기 등 다양한 요소에 따라 발생 시점이 달라질 수 있습니다.

### 커스텀 직렬화 예시

`StringList` 클래스에 커스텀 직렬화를 적용함으로써 기존 기본 직렬화의 문제점들을 효과적으로 해결하고 다음과 같은 이점을 얻을 수 있습니다.

```java
public final class StringList implements Serializable {
    private transient int size = 0; // 직렬화 대상에서 제외
    private transient Entry head = null;

    // 이번에는 직렬화 하지 않는다.
    private static class Entry {
        String data;
        Entry next;
        Entry previous;
    }

    // 지정한 문자열을 이 리스트에 추가
    public final void add(String s) {
        // ...
    }

    /**
     * 이 {@code StringList} 인스턴스를 직렬화한다.
     *
     * @serialData 이 리스트의 크기(포함된 문자열의 개수)를 기록한 후
     * ({@code int}), 이어서 모든 원소를(각각은 {@code String})
     * 순서대로 기록한다.
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject(); // transient가 붙지 않은 필드를 기본 방식으로 직렬화합니다. 이 경우 모든 필드에 transient를 붙였으므로, 실제로 직렬화되는 필드는 없습니다.
        stream.writeInt(size); // 리스트의 논리적 크기를 먼저 기록합니다. 역직렬화할 때 몇 개의 문자열을 읽어야 하는지 알려주는 핵심 정보

        /**
         * 모든 원소를 순서대로 기록
         * 이중 연결 리스트 구조(물리적 표현)를 순회하되, 
         * 구조 정보(next, previous)는 버리고 
         * 실제 데이터(e.data)**인 문자열만 순서대로 스트림에 기록
         */
        for (Entry e = head; e != null; e = e.next) {
           stream.writeObject(e.data);
        }
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject(); // 위와 동일
        int numElements = stream.readInt(); // 전에 writeObject가 기록한 리스트의 크기를 읽어옴

        // 모든 원소를 읽어 이 리스트에 삽입한다.
        for (int i = 0; i < numElements; i++) {
            /**
             * 읽어온 문자열을 현재 버전의 add 메서드를 사용하여 객체에 추가합니다. 
             * add 메서드는 현재 클래스의 내부 구현 방식(v2의 Entry 구조, 또는 배열 구조 등)에 맞춰 노드를 생성하고 연결하므로, 
             * 스트림에 기록된 v1의 구조와는 완전히 독립적으로 객체를 재구성할 수 있습니다.
             */
            add((String) stream.readObject());
        }
    }

    // ... 나머지 코드는 생략
}
```

1. 내부 구조 노출 차단 및 캡슐화 유지 (문제 1, 2 해결)
    - `transient` 키워드를 사용하여 `size`나 `head`와 같은 물리적인 내부 구현 필드를 직렬화 대상에서 제외시켰습니다.
      이로써 외부로의 구현 상세 노출을 막아 캡슐화를 유지하고, 향후 버전에서 내부 구조를 자유롭게 변경할 수 있게 됩니다.

2. 논리적 표현만 기록하여 효율성 확보 (문제 2 해결)
    - `writeObject` 메서드를 사용하여 객체의 논리적 표현인 **"문자열들의 순차적인 목록"** 만 바이트 스트림에 기록합니다.
      복잡한 `Entry` 객체의 구조적 오버헤드와 불필요한 연결 포인터 정보가 제외되면서, 직렬화 데이터 크기가 최소화되어 성능이 향상됩니다.

3. 논리적 재구성으로 유연성 확보 (문제 1, 3 해결)
    - `readObject` 메서드는 스트림에서 읽어온 논리적 데이터(문자열)를 현재 버전의 `add()` 메서드를 통해 재삽입합니다. 이 과정은 현재 내부 구현 방식에
      맞춰 객체를 새롭게 재구성하므로, 과거 버전의 물리적 구조에 묶이지 않고 미래 버전에서 연결 리스트를 배열 등으로 변경하더라도 호환성이 유지됩니다.

4. StackOverflowError 위험 방지 (문제 4 해결)
    - **readObject**와 writeObject 모두 재귀 호출 대신 **반복문 (for 루프)** 을 사용하여 모든 원소를 순회하고 처리하도록 구현되었습니다.
      이로써 객체 그래프의 깊이가 JVM 스택 깊이로 연결되는 것을 근본적으로 막아, 원소 개수가 많을 때 발생하던 `StackOverflowError` 위험을 효과적으로 제거합니다.

### defaultReadObject() / defaultReadObject 메소드 정의 및 주의 사항

`defaultReadObject`와 `defaultWriteObject`는 메서드는 `transient`가 붙지 않은 필드를 기본 방식으로 직렬화/역직렬화합니다.
따라서 모든 필드에 `transient`를 붙인 경우 `defaultWriteObject`와 `defaultReadObject` 호출을 생략해도 된다고 생각할 수 있지만,
자바 직렬화 명세는 이 작업을 무조건 하도록 요구합니다. 왜냐하면 향후 버전에서 `transient`가 아닌 새로운 필드가 추가되더라도
이전 버전의 직렬화된 데이터와 상호 호환성을 유지하기 위한 필수적인 조치이기 때문입니다.

결론적으로, 필드에 `transient` 키워드를 생략하고 기본 직렬화 대상이 되도록 하려면,
해당 필드가 객체의 논리적 상태와 무관하지 않다고 확신할 때만 신중하게 결정해야 합니다.

#### 주의 사항

1. 직렬화 메서드의 동기화 적용 (쓰레드 안전성 확보)

기본 직렬화 사용 여부와 관계없이, 객체의 전체 상태를 읽는 모든 메서드에 적용해야 하는 동기화 메커니즘을 직렬화/역직렬화 메서드에도 동일하게 적용해야 합니다.
직렬화는 객체의 전체 상태를 한 번에 읽어 바이트 스트림에 기록하는 작업입니다. 만약 다른 쓰레드가 동시에 객체 상태를 수정(쓰기)하는 작업을 한다면, 직렬화된 데이터가 일관성 없는(손상된) 상태를 포함하게 될 수
있습니다.

```java

private synchronized void writeObject(ObjectOutputStream s) throws IOException {
    s.defaultWriteObject();
}

```

2. UID 부여

어떤 직렬화 형태를 쓰든 모든 `Serializable` 관련 클래스에 직접 `serialVersionUID`를 부여해야 합니다.
JDK가 계산하는 기본값에 의존하면, 사소한 구현 변경에도 UID가 바뀌어 예상치 못한 `InvalidClassException` 이 발생할 수 있습니다.

> private static final long serialVersionUID = 무작위로 고른 long 값;

3. 웬만하면 UID 값 수정 x

일단 한 번 배포된 클래스의 `serialVersionUID`는 구버전으로 직렬화된 인스턴스들과의 호환성을 깨뜨리지 않도록 절대로 수정해서는 안 됩니다.
만약 이 값을 수정하게 되면, 기존 버전의 직렬화된 인스턴스를 역직렬화할 때 `InvalidClassException` 예외가 발생하여 데이터를 읽을 수 없게 되기 때문입니다.
따라서 이 값을 변경해야 하는 경우는 오직 새 버전의 클래스가 이전 버전의 데이터와 호환되지 않음을 의도적으로 선언하고자 할 때뿐입니다.

#### 추가 정보

기본 직렬화를 사용한다면 `transient` 필드들은 역직렬화될 때 기본값으로 초기화 됨

