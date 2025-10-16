### ✅ 과도한 동기화는 피하라

### Synchronization (동기화)의 개념

- 여러 쓰레드가 공유 자원에 접근할 때, 한 쓰레드가 자원을 사용 중이면 다른 쓰레드가 접근하지 못하도록 차단
- Java에서는 `synchronized` 키워드를 사용해 메소드 및 블록 구현
- Java의 모든 객체는 하나의 모니터 락(Monitor Lock)을 가지고 있으며, 이 모니터 락이 잠금 및 해체를 이용해 다른 쓰레드에 접근을 차단

### 과도한 동기화의 위험성

과도한 동기화는 **성능 저하**, **교착상태**, **데이터 훼손**, 심지어 **예측할 수 없는 동작을 유발**할 수 있으므로,
응답 불가와 안전 실패를 방지하기 위해서는 **락을 획득한** 동기화 메소드나 동기화 블록 내부에서 절대로 **통제권이 벗어난 코드(재정의할 수 있는 메소드나 콜백 함수 포함)를 실행**해서는 안 됩니다.

#### 성능 저하 예시

```java

class Item79Resource {

    void externalAction() {
        // 통제권이 벗어난 외부 코드 (시간이 오래 걸릴 수도 있고, 예외가 발생할 수도 있음)
        System.out.println("Performing external action...");
        try {
            Thread.sleep(2000); // 모종의 이유로 지연 발생 (코드는 억지로 지연시킴)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class Item79Tests {

    private final Item79Resource resource = new Item79Resource();

    @Test
    synchronized void testBadSynchronized() {
        System.out.println("락 획득");

        // 통제권이 벗어난 코드
        resource.externalAction(); // 이곳에서 지연 발생, 다른 스레드들이 락을 기다리며 멈춤

        System.out.println("testBadSynchronized 종료");
    }

}

```

`testBadSynchronized` 메소드는 `synchronized` 키워드를 사용하여 해당 객체의 모니터 락(Monitor Lock)을 점유한 상태에서 실행됩니다.
특히 내부 코드인 `resource.externalAction()`은 통제권이 벗어난 외부 메소드이며, 해당 메소드 내부에 Thread.sleep(2000)으로 대기 시켰습니다.

이로 인해 아무 작업도 수행하지 않고 2초 동안 락을 점유한 상태로 머물게 되었습니다.  
결과적으로 이 2초의 대기 시간 동안 다른 모든 스레드들은 해당 락을 얻지 못하고 BLOCKED 상태로 강제 대기해야 하므로,
[응답성 문제](https://madeprogame.tistory.com/391)와 성능 저하를 유발하게 됩니다.

### 문제 코드

<details>
    <summary>사전 코드</summary>
<div markdown="1">

```java
public interface SetObserver<E> {
    void added(ObservableSet<E> set, E element);
}
```

```java
// item 18에서 사용한 재사용 구현
public class ForwardingSet<E> implements Set<E> {
    private final Set<E> s;

    public ForwardingSet(Set<E> s) {
        this.s = s;
    }

    public void clear() {
        s.clear();
    }

    public boolean contains(Object o) {
        return s.contains(o);
    }

    public boolean isEmpty() {
        return s.isEmpty();
    }

    public int size() {
        return s.size();
    }

    public Iterator<E> iterator() {
        return s.iterator();
    }

    public boolean add(E e) {
        return s.add(e);
    }

    public boolean remove(Object o) {
        return s.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return s.containsAll(c);
    }

    public boolean addAll(Collection<? extends E> c) {
        return s.addAll(c);
    }

    public boolean removeAll(Collection<?> c) {
        return s.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return s.retainAll(c);
    }

    public Object[] toArray() {
        return s.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return s.toArray(a);
    }

    @Override
    public boolean equals(Object o) {
        return s.equals(o);
    }

    @Override
    public int hashCode() {
        return s.hashCode();
    }

    @Override
    public String toString() {
        return s.toString();
    }
}
```

</div>
</details>

메인 코드

`ObservableSet`이라는 클래스는 `Set`에 원소가 추가될 때마다 등록된 `SetObserver`(책에서 관찰자라고 표현함)에게 알림을 보내는 기능을 제공합니다.    
단! 원소가 제거될 때 알려주는 기능은 생략

```java
public class ObservableSet<E> extends ForwardingSet<E> {

    private final List<SetObserver<E>> observers = new ArrayList<>();

    public ObservableSet(Set<E> set) {
        super(set);
    }

    public void addObserver(SetObserver<E> observer) {
        synchronized (observers) {
            observers.add(observer);
        }
    }

    public boolean removeObserver(SetObserver<E> observer) {
        synchronized (observers) {
            return observers.remove(observer);
        }
    }

    // 실제로 문제가 되는 코드
    private void notifyElementAdded(E element) {
        synchronized (observers) {
            for (SetObserver<E> observer : observers)
                observer.added(this, element); // 외계인 코드
        }
    }

    @Override
    public boolean add(E element) {
        boolean added = super.add(element);

        if (added) {
            notifyElementAdded(element); // 최초 문제 발생
        }

        return added;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean result = false;

        for (E element : c) result |= add(element);  // notifyElementAdded를 호출, |= (복합 대입 연산자)

        return result;
    }
}
```

#### 오류 유발 코드

```java

@Test
void testBadSynchronized() {

    ObservableSet<Integer> set = new ObservableSet<>(new HashSet<>());

//        set.addObserver((s, e) -> System.out.println("s = " + s + " e = " + e));

    set.addObserver(new SetObserver<Integer>() {

        @Override
        public void added(ObservableSet<Integer> set, Integer element) {
            System.out.println("s = " + set + " e = " + element);

            if (element == 23) {
                set.removeObserver(this); // 다른 쓰레드에서 동시 접근
            }
        }
    });

    for (int i = 0; i < 100; i++) {
        set.add(i);
    }
}

/**
 * 아래와 같은 오류 발생
 * s = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23] e = 23
 * java.util.ConcurrentModificationException
 */
```

### 문제 이유 - 통제권이 벗어난 코드

`notifyElementAdded()`메소드는 `observers`리스트의 **락(Lock)** 을 획득하고 유지하는 상태에서,
인터페이스의 메소드인 `observer.added(this, element)`를 호출합니다.

`observer.added()`메소드는 **클라이언트(코드를 사용하는 주체)** 가 `SetObserver` 인터페이스를 구현하며 정의한 코드입니다.
즉, `ObservableSet`클래스 입장에서는 이 코드가 무슨 일을 할지 **예측하거나 통제할 수 없습니다.**

이러한 외부 코드를 **외계인 메소드(Alien Method)** 라고 부릅니다.
락을 쥔 채로 외계인 메소드를 호출하는 것은 **응답 불가(교착상태)** 와 **안전 실패(ConcurrentModificationException 예외 발생)** 를 유발하는 주 원인입니다.

### 1. 해결 방법 - 외계인 메소드를 동기화 블록 바깥으로 이동

동기화 영역(락)에서 통제권이 벗어난 코드(외부 코드 or 외계인 메소드)를 완전히 분리하는 것입니다. 즉, 리스트를 복사한 후 락을 해제하고 순회합니다.

```java
private void notifyElementAdded(E element) {
    List<SetObserver<E>> snapshot;

    synchronized (observers) {
        snapshot = new ArrayList<>(observers);
    }

    // 락 해제 후 외부 코드 호출
    for (SetObserver<E> observer : snapshot) {
        observer.added(this, element);
    }
}
```

### 2. 해결 방법 - CopyOnWriteArrayList 동시성 컬렉션 사용

해당 컬렉션은 쓰기 시점에 내부 배열을 복사하므로, 락 없이도 안전한 반복이 가능합니다.

```java
private final List<SetObserver<E>> observers = new CopyOnWriteArrayList<>();

public void addObserver(SetObserver<E> observer) {
    observers.add(observer);
}

public boolean removeObserver(SetObserver<E> observer) {
    return observers.remove(observer);
}

private void notifyElementAdded(E element) {
    for (SetObserver<E> observer : observers)
        observer.added(this, element);
}
```

두 방법 모두 외부 코드 `observer.added()`가 내부 상태를 변경하더라도 `ConcurrentModificationException`이 발생하지 않습니다.

### 적절한 해결 선택 방법

> 기본 원칙: 동기화 영역에서는 가능한 한 일을 적헤 하는 것 입니다.

과도한 동기화는 병렬성을 저해하고, 가상 머신의 코드 최적화까지 제한하는 숨겨진 비용을 발생시킵니다.

가변 클래스를 설계할 때 스레드 안전성 확보를 위해 다음 두 가지 전략 중 하나를 선택해야 합니다.

1. 외부 동기화 위임 - 동기화하지 않은 클래스(비동기 클래스)로 만들기
   클래스 자체가 동기화를 책임지지 않고, 쓰레드 안전성 확보를 클래스 외부로 위임하는 전략입니다.
    - **원칙**
        - 클래스를 안전하지 않은 비동기 상태로 설계하고, **동기화**의 책임과 구현을 **전적으로 클래스 외부의 호출자(외부 코드)** 에게 위임합니다.
    - **사용 상황 및 해결 방법**
        - `ArrayList`, `HashMap` 같은 일반 컬렉션 클래스를 사용할 때, 필요에 따라
          클라이언트가 `Collections.synchronizedList()` 등을 통해 동기화를 적용하거나, 직접 **외부에서 락(lock)을 제어**한다.
    - **이점**
        - 클래스 내부의 불필요한 락 오버헤드를 방지하고, 클라이언트가 락의 범위를 최소화하여 **동시성**을 극대화할 수 있습니다.


2. 내부 동기화 수행 - 쓰레드 안전한 클래스로 만들기
    - **원칙**
        - 클래스 내부에서 동기화를 수행하여 스레드 안전하게 만듭니다.
    - **사용 상황**
        - 외부 동기화보다 **동시성(Concurrency)** 을 월등히 개선할 수 있을 때만 사용해야 합니다.
    - **해결 방법 (통제권이 벗어난 코드 문제 해결)**
        - 적절한 동기화 코드 분리
        - 동시성 컬렉션 사용
    - **성능/확장성 향상 방법**
        - 비차단 동시성 제어
        - 락 분할
        - 락 스트라이핑 