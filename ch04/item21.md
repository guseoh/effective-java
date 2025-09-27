### ✅ 인터페이스는 구현하는 쪽을 생각해 설계해라

자바 8 이전에는 인터페이스의 모든 메서드가 추상 메서드여야 했기 때문에 기존에 존재하던 인터페이스에 새로운 메서드를 추가할 수 없었습니다.

예를 들어, 자바 컬렉션 프레임워크의 `List` 인터페이스에 `sort()` 메서드를 추가하려면, `List`를 구현하는 수많은 기존 클래스들을 모두 수정해야 하는 심각한 문제가 발생했을 것입니다.

이러한 문제를 해결하기 위해 자바 8에서는 `default` 메서드이 도입되었습니다.

- 디폴트 메서드(Default Method): `default` 키워드를 사용하여 인터페이스에 메서드 바디(구현)를 포함할 수 있게 되었습니다.
  이는 기존 구현 클래스를 변경하지 않고도 인터페이스에 새로운 기능을 추가할 수 있는 길을 열어주었습니다.
  이로써 인터페이스는 더 이상 '규격'의 역할에만 국한되지 않고, '기본 구현'을 제공하는 역할까지 수행할 수 있게 되어 추상 클래스와의 경계가 모호해졌습니다.

### 디폴트 메서드의 한계와 주의점

자바 8에서 디폴트 메소드를 제공한다고 해서 모든 상황에서 불변식을 해치지 않는 완벽한 디폴트 메소드를 작성하기는 힘듭니다.

인터페이스는 다양한 클래스에서 사용될 수 있기 때문에 인터페이스가 특정 클래스의 **내부 상태와 충돌**을 일으킬 수 있는 가능성이 존재합니다.

`Collection` 인터페이스에 추가된 `removeIf` 메서드가 바로 그런 예시입니다.

```java
default boolean removeIf(Predicate<? super E> filter) {
    Objects.requireNonNull(filter);
    boolean removed = false;
    final Iterator<E> each = iterator();
    while (each.hasNext()) {
        if (filter.test(each.next())) {
            each.remove();
            removed = true;
        }
    }
    return removed;
}
```

해당 메소드는 반복문을 통해 각 원소를 `Predicate`에 넣어 조건이 `true`를 반환하면 `Iterator`의 `remove()` 메서드를 호출하여 해당 원소를 제거합니다.

이러한 디폴트 메서드의 설계가 문제가 될 수 있는 대표적인 예시가 바로 `org.apache.commons.collection4.collection.SynchronizedCollection`입니다.

### SynchronizedCollection에서 removeIf의 충돌 - 컴파일에 성공하더라도 런타임 오류 발생

`org.apache.commons.collection4.collection.SynchronizedCollection`은 여러 스레드가 동시에 안전하게 컬렉션에 접근할 수 있도록
각 메서드 호출에 대해 동기화(synchronization)를 적용하는 래퍼(wrapper) 클래스입니다.

이 클래스가 대표적인 예시가 되는 이유는 `removeIf` 디폴트 메서드의 동작 방식과 충돌하기 때문입니다.
`SynchronizedCollection`은 `add()`, `remove()`, `iterator()`와 같은 개별 메서드에만 락을 걸어 보호합니다.

하지만 `removeIf`는 `iterator()`를 얻어 반복문을 통해 여러 번의 `hasNext()`, `next()`, `remove()` 호출을 수행합니다.
이 과정 전체가 하나의 락으로 보호되지 않습니다. 따라서 `removeIf`가 실행되는 동안 다른 스레드가 `SynchronizedCollection`의 `add()` 메서드를 호출해
컬렉션의 **구조를 변경**할 수 있고, 이로 인해 `ConcurrentModificationException`이 발생합니다.

### 여러 스레드에서 `removeIf` 호출 예제

**build.gradle**

```java
implementation 'org.apache.commons:commons-collections4:4.4'
```

```java

@SpringBootTest
class ApplicationTests {

    private Collection<String> synchronizedCollection;

    // 초기 데이터 추가
    @BeforeEach
    void setUp() {
        synchronizedCollection = CollectionUtils.synchronizedCollection(new ArrayList<>());
        for (int i = 0; i < 1000; i++) {
            synchronizedCollection.add("Element-" + i);
        }
    }

    @Test
    void SingleThreadSynchronizedTest() {
        // 플래그 생성
        AtomicBoolean exceptionOccurred = new AtomicBoolean(false);

        // 2개의 스레드가 완료되기를 기다리기 위한 래치
        CountDownLatch latch = new CountDownLatch(2);

        // 스레드 풀 2개 생성
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        // 스레드 1: removeIf 실행
        executorService.submit(() -> {
            try {
                // removeIf는 디폴트 메서드로, 내부적으로 iterator를 사용해 요소를 제거한다.
                synchronizedCollection.removeIf(s -> s.startsWith("Element-1"));
            } catch (ConcurrentModificationException e) {
                // 예상한 예외가 발생하면 플래그를 true로 설정
                exceptionOccurred.set(true);
            } finally {
                latch.countDown(); // 작업 완료를 알림
            }
        });

        // 스레드 2: removeIf가 실행되는 동안 원소 추가
        executorService.submit(() -> {
            try {
                // removeIf가 실행되는 동안 컬렉션을 수정하여 충돌을 유발
                for (int i = 0; i < 100; i++) {
                    synchronizedCollection.add("New-Element-" + i);
                }
            } catch (Exception e) {
                // 이 스레드에서는 예외가 발생하지 않아야 함
                fail("원소 추가 스레드에서 예외 발생: " + e.getClass().getSimpleName());
            } finally {
                latch.countDown(); // 작업 완료를 알림
            }
        });

        try {
            // 모든 스레드의 작업이 완료될 때까지 대기
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // 만약 발생하더라도 테스트 실패로 처리
            fail("CountDownLatch 대기 중 인터럽트 발생: " + e.getMessage());
        }

        // 스레드 풀 종료
        executorService.shutdown();

        // 예외가 발생했는지 최종적으로 확인
        assertTrue(exceptionOccurred.get(), "ConcurrentModificationException이 발생하지 않았습니다. 테스트 실패.");
    }

}
```

- **스레드 1 (removeIf 호출):** synchronizedCollection의 removeIf 메서드를 호출하여 "Element-1"로 시작하는 원소를 제거
- **스레드 2 (add 호출)**: removeIf가 실행되는 동안 synchronizedCollection에 **새로운 원소를 추가하여 컬렉션의 구조를 변경**합니다.

`removeIf`는 `iterator()`를 통해 순회 중인 상태에서 컬렉션의 구조가 변경되었음을 감지하고, `ConcurrentModificationException`을 발생

디폴트 메서드는 컴파일러에 성공하더라도 기존 구현체에 런타임 오류를 일으킬 수 있습니다.