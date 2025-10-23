## ✅ 가능한 한 실패 원자적으로 만들라

## 0. 들어가기 전

한 메서드를 여러 스레드가 동시에 호출할 때 그 메서드가 어떻게 동작하느냐는 해당 클래스와 이를 사용하는 클라이언트 사이의 중요한 계약과 같다.

API 문서에서 아무런 언급도 없으면 그 클래스 사용자는 나름의 가정을 해야만 한다. 만약 그 가정이 틀리면 클라이언트 프로그램은 동기화를 충분히 하지 못하거나(아이템 78) 지나치게 한(아이템 79) 상태일 것이며, 두 경우 모두 심각한 오류로 이어질 수 있다.

<br>

## 1. API 문서에서 synchronized 한정자
API 문서에 `synchronized` 한정자가 보이는 메서드는 스레드 안전하다는 이야기가 있지만 **`synchronized` 한정자 만으로 스레드 안전성을 판단하는 것은 위험하다.**

javadoc이 기본 옵션에서 생성한 API 문서에는 `synchronized` 한정자가 포함되지 않는다.

**메서드 선언에 `synchronized` 한정자를 선언할지는 구현 이슈일 뿐 API에 속하지 않는다.**
(참고로 API 문서는 어떻게 동작하는지에 대한 설명을 제공하는 것에 초점을 맞추며, 어떻게 구현되었는지에 대한 세부 사항을 숨긴다.)


<br>

## 2. 스레드 안전성

- 스레드 안전성 수준 문서화는 개발자에게 스레드 환경에서의 안전성과 동기화 방법을 알려준는 역할을 한다.
- 다중 스레드 문제를 사전에 방지하고 코드의 안전성을 보장하는 데 중요하다.
- **멀리스레드 환경에서도 API를 안전하게 사용하게 하려면 클래스가 지원하는 스레드 안전성 수준을 정확히 명시해야 한다.**

### 스레드 안전성 수준

- `불변(immutable)`
    - 이 클래스의 인스턴스는 마치 상수와 같아서 외부 동기화도 필요 없다.
    - String, Long, BigInteger(아이템 7)가 대표적이다.
- `무조건적 스레드 안전(unconditionally thread-safe)`
    - 이 클래스의 인스턴스는 수정될 수 있으나, 내부에서 충실히 동기화하여 별도의 외부 동기화 없이 동시에 사용해도 안전하다.
    - AtomicLong, ConcurrentHashMap
- `조건부 스레드 안전(conditionally thread-safe)`
    - 무조건적 스레드 안전과 같으나, 일부 메서드는 동시에 사용하려면 외부 동기화가 필요하다.
    - Collections.synchronized 래퍼 메서드가 반환한 컬렉션들이 속한다.
    <details>
    <summary>Collections.synchronized 메서드</summary>
    <div markdown="1">

    - Java의 Collections.synchronized 메서드는 기본 컬렉선 객체를 스레드 안전한 버전으로 래핑하는데 사용된다.
    - 이러한 래핑된 컬렉션은 내부 동기화를 통해 멀티 스레드 환경에서 안전성을 보장하지만, 일부 동작에 대해서는 아니다.
      - ex) Iterator, Spliterator, Stream
      </div>
    </details>

- 어떤 순서로 호출할 때 외부 동기화가 필요한지, 그 순서로 호출하려면 어떤 락 혹은 락들을 얻어야 하는지 알려줘야 한다.
- 일반적으로 인스턴스 자체를 락으로 얻지만 예외도 있다.
- `스레드 안전하지 않음(not thread-safe)`
    - 이 클래스의 인스턴스는 수정될 수 있다.
    - 동시에 사용하려면 각각의 메서드 호출을 클라이언트가 선택한 외부 동기화 메커니즘으로 감싸야 한다.
    - ArrayList, HashMap 같은 기본 컬렉션이 속한다.
- `스레드 적대적(thread-hostile)`
    - 이 클래스는 모든 메서드 호출을 외부 동기화로 감싸더라도 멀티스레드 환경에서 안전하지 않다.
    - 이 수준의 클래스는 일반적으로 정적 데이터를 아무 동기화 없이 수정한다.
    - 스레드 적대적으로 밝혀진 클래스나 메서드는 일반적으로 문제를 고쳐 재배포하거나 사용 자제(deprecated) API로 지정한다.
    - generateSerialNumber 메서드에서 내부 동기화를 생략하면 스레드 적대적이게 된다.

<br>

### 스레드 안전성 애너테이션

1. `@Immutable` - 불변(immutable)
2. `@ThreadSafe` - 무조건적 스레드 안전(unconditionally thread-safe), 조건부 스레드 안전(conditionally thread-safe)
3. `@NotThreadSafe` - 스레드 안전하지 않음(not thread-safe)

<br>

### 문서화 tip

- 보통 클래스의 문서화 주석에 작성하며, 독특한 특성의 메서드라면 해당 메서드의 주석에 기재하도록 하자.
- 열거 타입은 굳이 불변이라고 쓰지 않아도 된다.
- 반환 타입만으로 정확히 알 수 없는 정적 팩터리라면 자신이 반환하는 객체의 스레드 안전성을 반드시 문서화해야 한다.

<br>

## 3. 비공개 Lock 객체

클래스가 외부에서 사용할 수 있는 락을 제공하면 클라이언트에서 일련의 메서드 호출을 원자적으로 수행할 수 있다. 하지만 이 유연성에는 대가가 따른다.
내부에서 처리하는 고성능 동시성 제어 메커니즘과 혼용할 수 없게 되는 것이다.
그래서 `ConcurrentHashMap` 같은 동시성 컬렉션과는 함께 사용하지 못한다.

또한, 클라이언트가 공개된 락을 오래 쥐고 놓지 않는 **서비스 거부 공격(denial-of-service attack)** 을 수행할 수도 있다.
<details>
    <summary>서비스 거부 공격(denial-of-service attack)</summary>
<div markdown="1">

```java
public class SharedResource {
    public final Object lock = new Object(); // 공개된 락

    public void doSomething() {
        synchronized(lock) {
            // 어떤 작업 수행
        }
    }
}

public class MaliciousClient {
    public void denialOfServiceAttack(SharedResource resource) {
        synchronized(resource.lock) {
            while(true) { 
                // 무한 루프로 락을 영원히 쥐고 있음
            }
        }
    }
}
```

한 스레드(`MaliciousClient`)가 `resource.lock`을 `synchronized`로 획득한 뒤 **무한루프**로 빠져 락을 계속 점유한다.

그 결과 같은 락을 필요로 하는 다른 스레드들은 영원히 블록되어 **서비스 거부 상태(denial-of-service attack, DoS)** 상태가 된다.
</div>
</details>

```java
// 비공개 락 객체 관용구 - 서비스 거부 공격을 막아준다.
private final Object lock = new Object();

public void foo() {
    synchronized(lock) {
        ...
    }
}
```

비공개 락 객체는 클래스 바깥에서는 볼 수 없으니 클라이언트가 그 객체의 동기화에 관여할 수 없다.

<br>

### 락 필드는 항상 final로 선언하라.

- 앞의 코드에서 lock 필드를 `final`로 선언했다.
- `final`로 선언하여 락 객체가 교체되는 일을 방지한다. (아이템 15, 78)
- `private`로 선언해 클래스 내부에서만 접근할 수 있다.

<br>

### 언제 사용할가?

1. **무조건적 스레드 안전 클래스**
    - 조건부 스레드 안전 클래스에서는 특정 호출 순서에 필요한 락이 무엇인지를 클라이언트에게 알려줘야 하므로 이 관용구를 사용할 수 없다.
2. **상속용으로 설계한 클래스 (아이템 19)**
    - 상속용 클래스에서 자신의 인스턴스를 락으로 사용한다면, 하위 클래스는 아주 쉽게,
        - 의도치 않게 기반 클래스의 동작을 방해할 수 있다.(반대도 마찬가지)
    - 같은 락을 다른 목적으로 사용하게 되어 하위 클래스와 기반 클래스는 ‘서로가 서로를 훼방놓는’ 상태에 빠진다.
    - ex) Thread 클래스
        - Thread 클래스 메서드 중 일부는 synchronized로 동기화 되어 있는데, 하위 클래스에서 이러한 메서드로 확장하거나 오버라이드할 때 동기화 문제가 발생할 수 있다.
        - 따라서 Thread 확장하는 것은 권장되지 않으며, Runnable 인터페이스를 구현하는 것이 더 안전하고 유연한 방법이다.
    <details>
    <summary>부모 클래스와 자식 클래스가 동일한 락을 사용할 경우 문제 상황</summary>
    <div markdown="1">
    
    #### 나쁜 예 - this를 모니터로 사용한 상속용 클래스
       
    ```java
    public class SuperClass {
        // 상속용이라 설계자가 this로 동기화를 썼다고 가정
        public void criticalOperation() {
            synchronized (this) {
                System.out.println(Thread.currentThread().getName() + " - Super: 작업 시작");
                try { Thread.sleep(2000); } catch (InterruptedException e) {}
                System.out.println(Thread.currentThread().getName() + " - Super: 작업 종료");
         }
       }
    }
   
    public class SubClass extends SuperClass {
        // 하위 클래스에서 같은 인스턴스를 락으로 잡음 — 문제가 된다
        public void interferingOperation() {
            synchronized (this) {
                System.out.println(Thread.currentThread().getName() + " - Sub: 락 확보(길게 유지)");
                try { Thread.sleep(10000); } catch (InterruptedException e) {}
                System.out.println(Thread.currentThread().getName() + " - Sub: 락 해제");
         }
      }
    }
    
    public class Main {
      public static void main(String[] args) {
        SubClass instance = new SubClass();

        // 1) 하위 클래스에서 락을 오래 잡는 스레드
        new Thread(() -> instance.interferingOperation(), "T-sub").start();

        // 2) 기반 클래스의 임계영역을 호출하는 스레드 (블록됨)
        new Thread(() -> instance.criticalOperation(), "T-super").start();
      }
    }
     ```

    - `T-sub`가 `this` 락을 확보하고 오래 유지 → `T-super`는`criticalOperation()` 안으로 진입하지 못하고 블록된다.
      이건 의도치 않는 상호 간섭이며 서비스 중단/응답 지연으로 이어질 수 있다.
    - 위 예제는 `synchronized(this)` 를 기반 클래스로 사용했고 하위 클래스가 동일한 락을 사용했기 때문에 발생한다.

    <br>

    #### 문제가 되는 이유
    
    - 상속 관계에서 `this`는 동일 인스턴스를 가리키며, 기반 클래스와 하위 클래스가 동일한 모니터를 공유하게 된다.
      - 하위 클래스가 의도적으로 그 모니터를 길게 점유하면 기반 클래스의 동작이 막힌다.
      - API 설계자(기반 클래스 개발자)는 하위 클래스가 어떻게 동작할지 통제할 수 없으므로 외부에 락을 노출하면 취약점이 된다.
    
    <br>
    
    #### 해결책 - 락을 숨긴다.(private lock)
    
    ```java
    public class SafeSuper {
        private final Object lock = new Object();
        public void criticalOperation() {
            synchronized (lock) {
                System.out.println(Thread.currentThread().getName() + " - SafeSuper: 작업 시작");
                try { Thread.sleep(2000); } catch (InterruptedException e) {}
                System.out.println(Thread.currentThread().getName() + " - SafeSuper: 작업 종료");
            }
        }
    }
    ```
   - 기반 클래스는 **외부에 노출되지 않는(private final) 락 객체**를 사용한다.
   - 필요하면 `final`로 선언하고 `this`를 락으로 쓰지 않는다.
   - 하위 클래스는 `lock` 객체에 접근할 수 없으니 같은 모니터를 잡아 기반 클래스의 동작을 방해할 수 없다.
     
   </div>
   </details>

<br>

## 4. 핵심 정리

- 모든 클래스가 자신의 스레드 안전성 정보를 명확히 문서화해야 한다.
    - 정확한 언어로 설명하거나 스레드 안전성 애너테이션을 사용할 수 있다.
    - `synchronized` 한정자는 문서화와 관련이 없다.
- 조건부 스레드 안전 클래스는 메서드를 어떤 순서로 호출할 때 외부 동기화가 요구되고, 그때 어떤 락을 얻어야 하는지도 알려줘야 한다.
- 무조건적 스레드 안전 클래스를 작성할 때는 `synchronized` 메서드가 아닌 비공개 락 객체를 사용하자.
    - 클라이언트나 하위 클래스에서 동기화 메커니즘을 깨뜨리는 걸 예방할 수 있다.
    - 필요하다면 더 정교한 동시성을 제어 메커니즘으로 재구현할 여지가 생긴다.