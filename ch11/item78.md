## ✅ 공유 중인 가변 데이터는 동기화에 사용하라

<br>

## 0. 스레드와 동시성 프로그래밍의 어려움
스레드는 **여러 작업을 동시에 수행할 수 있게 해주는 도구**이다.
예를 들어, 한 스레드가 파일을 읽는 동안 다른 스레드가 네트워크 요청을 처리하거나, 사용자 입력을 감시할 수도 있다.

이처럼 스레드를 활용하면 프로그램의 응답성이 높아지고, 여러 CPU 코어를 활용해 성능을 향상시킬 수도 있다.

하지만 스레드가 주는 이점 뒤에는 **복잡성**과 **위험성**이 존재한다. 동시성 프로그래밍은 단일 스레드 프로그래밍보다 어렵다.
그 이유는 “**여러 스레드가 동시에 같은 데이터를 다룬다**”는 사실에 있다.

<br>

### 실행 순서가 예측 불가능하다.

- 단일 스레드 프로그램에서는 코드가 위에서 아래로 순차적으로 실행되므로, 언제 어떤 코드가 실행되는지 쉽게 추론할 수 있다.
- 하지만 여러 스레드가 동시에 실행될 때는
    - 각 스레드가 언제 CPU를 점유할지, 어떤 순서로 코드가 실행될지 예측할 수 없다.
- ex) 두 스레드가 동시에 `count++` 를 실행하면,
    - 한 스레드가 값을 읽는 순간 다른 스레드가 이미 값을 변경했을 수도 있다.
    - 이런 상황은 코드가 한 줄이라도 결과가 다르게 나올 수 있게 만든다.
- 즉, **코드가 논리적으로 옳더라도 실행 순서에 따라 결과가 달라질 수 있다.**

<br>

### 공유 자원이 충돌한다. (Race Condition)

- 여러 스레드가 **같은 변수를 동시에 수정**하면, 그 변수의 값이 꼬이는 현상이 발생한다.
    - 이를 `레이스 컨디션(Race Condition)` 이라고 한다.
- ex) 은행 계좌를 여러 스레드가 동시에 수정한다면
  ```java
   balance = balance - 100;
  ```
  - 두 스레드가 동시에 이 연산을 수행하면 결과적으로 100원만 빠져야 하는데 200원이 빠질 수도 있다.
  - 이런 문제는 실행 순서에 따라 불규칙하게 나타나며, **특정 환경에서만 드러나기 때문에 테스트 중엔 잘 보이지도 않는다.**

<br>

### 재현이 어렵다 (Heisenbug)

- 동시성 버그는 **재현하기 가장 어려운 종류의 버그**이다.
- 프로그램을 여러 번 실행해도 문제가 발생했다가 사라졌다 하며, 디버깅 중에는 오히려 사라지기도 한다.
- 즉, **문제가 관찰하려는 행위 자체가 프로그램의 타이밍을 바꿔버리기 때문**이다.
    - 로그를 찍거나 디버거를 붙이는 순간 실행 순서가 달라지고, 버그가 숨는 경우가 많다.

<br>

### 메모리 일관성 문제 (Visibility Issue)

- 스레드는 **각자 CPU 캐시를 통해 데이터를 읽고 쓴다**는 점도 문제이다.
- 한 스레드가 변수 값을 변경해도, 다른 스레드가 그 변경을 즉시 보지 못할 수도 있다.
- ex) 한 스레드가 `stop = true` 로 바꿔도 다른 스레드는 여전히 `stop = false`라고 인식할 수 있다.
- 이는 가시성(visibility) 문제로 `synchronized`나 `volatile` 같은 키워드로 해결해야 한다.

<br>

### 교착 상태와 경쟁 상태

- 스레드가 서로의 자원을 기다리며 **무한 대기**에 빠질 수도 있다.
    - 이것은 `교착 상태(deadlock)`이다.
- ex) 스레드 A는 자원 1을 잡고 지원 2를 기다리고, 스레드 B는 자원 2를 잡고 자원 1을 기다리면 둘 다 영원히 멈춰 있게 된다.
- 한 스레드가 계속 CPU를 점유해서 다른 스레드가 실행 기회를 얻지 못하는 경우
    - 이것을 `기아(starvation)` 라고 한다.
- 이러한 문제들이 단일 스레드 환경에서는 존재하지 않던 복잡성이다.

<br>

### 동기화의 비용과 복잡성

- 이런 문제를 막기 위해서는 **동기화(synchronization)** 가 필요하지만, 동기화는 또 다른 문제를 야기한다.
    - 락을 많이 걸면 병목이 생겨 성능이 떨어진다.
    - 락의 순서를 잘못 설계하면 교착 상태 발생
    - 락을 최소화하면 안전성 저하
- 즉, **동기화는 안전성과 성능 사이의 균형 문제**를 생성한다.

<br>

## 1. 동기화

`synchronized` 키워드는 **해당 메서드나 블록을 한 번에 한 스레드씩 수행하도록 보장**한다. 많은 프로그래머가 동기화를 배타적 실행, 즉 한 스레드가 변경하는 중이라서 상태가 일관되지 않은 순간의 객체를 다른 스레드가 보지 못하게 막는 용도로만 생각한다.

한 객체가 일관된 상태를 가지고 생성되고, 이 객체에 접근하는 메서드는 스레드가 락(lock)을 건다. 락을 건 메서드는 객체의 상태를 확인하고 필요하면 수정한다. 일관된 상태에서 다른 일관된 상태로 변화시키는 것이다. 그래서 **동기화를 제대로 사용하면 항상 일관된 상태를 볼 수 있다.**

동기화에는 중요한 기능이 하나 더 있다. 동기화 없이는 한 스레드가 만든 변화를 다른 스레드에서 확인하지 못할 수 있다. 동기화는 일관성 깨진 상태를 볼 수 없게 하는 것은 물론, **동기화된 메서드나 블록에 들어간 스레드가 같은 락의 보호하에 수행된 모든 이전 수정의 최종 결과를 보게 해준다.**

자바는 `long`과 `double`를 제외한 모든 변수의 읽기와 쓰기는 원자적이다.
하지만 원자적 데이터를 쓸 때도 동기화는 해야 한다. 자바 언어는 스레드가 필드를 읽을 때 항상 ‘수정이 완전히 반영된’ 값을 얻는다고 보장하지만, **한 스레드가 저장한 값이 다른 스레드에게 ‘보이는가’는 보장하지 않는다.**

**동기화는 배타적 실행뿐 아니라 스레드 사이의 안정적인 통신에 꼭 필요하다.**

<br>

## 2. 멀티 스레딩환경에서 발생할 수 있는 문제와 해결 방법

> Thread.stop 메서드는 사용하지 말자!
- `Thread.stop` 메서드는 스레드를 강제로 멈추게 한다.
- 스레드가 실행 중인 작업을 완료하지 않고 중단될 수 있기 때문에, 데이터의 무결성을 손상시킬 위험이 있다.
- 따라서 이 메서드는 deprecated API로 지정되었다.
- `Thread.stop(Throwable obj)` 메서드는 자바 11에서 제거되었지만, 아무 매개변수도 받지 않는 `Thread.stop()`은 아직까지 제거 계획이 없는 듯하다.

<br>

### flag poling

- 첫 번째 스레드는 자신의 boolean 필드(flag)의 값을 주기적으로 확인(poling)하며, 해당 필드의 값이 true일 경우 스레드의 작업을 멈춘다.
- 다른 스레드가 첫 번째 스레드를 멈추고자 할 때, boolean 필드의 값을 true로 변경하여 첫 번째 스레드에게 멈추라고 신호를 보낸다.
- boolean 필드에 대한 접근이 원자적이라도 동기화나 volatile을 사용하지 않으면 메모리 가시성 문제가 존재한다.

<details>
    <summary>예시 1 - 동기화 X</summary>
<div markdown="1">

```java
// 잘못된 코드 - 이 프로그램은 얼마나 오래 실행될까?
public class StopThread {
	private static boolean stopRequested;
    
    public static void main(String[] args) throws InterruptedException {
    	Thread th = new Thread(() -> {
        	int i = 0;
            while (!stopRequested) {
            	i++;
                System.out.println(i);
            }
        });
        th.start();
        
        TimeUnit.SECONDS.sleep(1);
        stopRequested = true;
    }
}
```
</div>
</details>

- 두 가지 관점에서 예상과 달리 1초 후에 종료되지 않는다.

**1. 메모리 가시성**
- `stopRequested`를 `true`로 변경했을 때, 그 변경사항이 백그라운드 스레드에게 즉시 보이지 않을 수 있다.
- JVM 내에서 각 스레드는 종종 로컬 메모리 캐시를 사용하기 때문에, 메인 스레드에서 변경한 값이 백그라운드 스레드의 캐시에 즉시 반영되지 않을 수 있다.

**2. JVM의 최적화: 끌어올리기(hoisting) 최적화 기법**
- JVM은 코드를 실행할 때 성능 최적화를 위해 여러 변환을 수행한다.
- 그 중 하나로, 여러 번 체크하는 조건을 최적화하여 반복문을 더 효율적으로 만들려고 할 수 있다.
- `while(!stopRequested)` 구문에서 `stopRequested`가 변하지 않는다고 판단된다면, 이를 반복문 밖으로 빼내어 성능을 최적화하려고 할 수 있다.
- 이로 인해 응답 불가(liveness failure) 상태가 된다.

```java
//원래 코드
while(!stopRequested) {
    i++;
}

// 최적화한 코드
if(!stopRequested) {
    while(true) {
        i++;
    }
}
```

<br>

<details>
    <summary>예시 2 - 동기화 O (synchronized)</summary>
<div markdown="1">

```java
public class StopThread {

    private static boolean stopRequested;
    
    private static synchronized void requestStop() {
        stopRequested = true;
    }
    
    private static synchronized boolean stopRequested() { 
        return stopRequested;
    }

    public static void main(String[] args) throws InterruptedException {

        Thread backgroundThread = new Thread(() -> {
            int i = 0;
            while(!stopRequested()) {
                i++;
            }
        });

        backgroundThread.start();

        TimeUnit.SECONDS.sleep(1);
        stopRequested();
    }
}
```
</div>
</details>

- 쓰기 메서드(requestStop)와 읽기 메서드(stopRequest) 모두를 동기화했음에 주목하자.
  - 쓰기 메서드만 동기화해서는 충분하지 않다.
  - 쓰기와 읽기 모두가 동기화되지 않으면 동작을 보장하지 않는다.

<br>

<details>
    <summary>예시 3 - volitile</summary>
<div markdown="1">

```java
public class StopThread {

    private static volatile boolean stopRequested;

    public static void main(String[] args) throws InterruptedException {

        Thread backgroundThread = new Thread(() -> {
            int i = 0;
            while(!stopRequested) {
                i++;
            }
        });

        backgroundThread.start();

        TimeUnit.SECONDS.sleep(1);
        stopRequested = true;
    }
}
```
</div>
</details>

- `volatile`
  - Java에서 변수의 선언 앞에 사용하는 한정자이다.
  - **캐시가 아닌 메모리에 읽고 쓰는 연산을 수행한다.**
  - 해당 변수가 여러 스레드에 의해 접근될 수 있으며, 항상 변수의 최신값을 읽거나 쓸 수 있도록 보장한다.
  - `volatile`이 선언된 변수가 있는 코드는 최적화되지 않는다.
  - **synchronized와 달리 배타적 수행을 하지 않아 성능에 미치는 부담이 적다.**
  - (long, double을 제외한 기본 타입은 `volatile` 키워드를 사용하면 동기화를 생략해도 된다.)

> `volatile` 주의해야 할 점
- `volatile` 는 배타적 수행을 보장하지 않아 동시성 문제가 발생할 수 있다.

```java
private static volatile int nextSerialNumber = 0;

public static int generateSerialNumber() {
    return nextSerialNumber++;
}
```

- 멀티 스레드 환경에서 변수의 증가 연산자(++)와 같은 연산자는 안전하지 않다.
- ++ 연산은 두 단계로 구성된다.
  1. nextSerialNumber 값을 읽는다.
  2. nextSerialNumber 값을 1 증가시킨다.
- 만약 두 번째 스레드가 이 두 접근 사이를 비집고 들어와 값을 읽어가면 첫 번째 스레드와 똑같은 값을 돌려받게 된다.
  - 프로그램이 잘못된 결과를 계산해내는 이런 오류를 안전 실패(safety failure)라고 한다.
- 해결 방법
  - `generateSerialNumber` 메서드에 `synchronized` 한정자를 붙이면 이 문제가 해결된다.
    - 동시에 호출해도 서로 간섭하지 않으면 이전 호출이 변경한 값을 읽게 된다는 뜻이다.
  - `synchronized`를 붙였다면 `volatile`을 제거해야 한다.
- 이 메서드를 더 견고하게 하려면
  - 일련번호 같은 경우 더 큰 범위를 제공하는 long을 사용하는 것이 좋다.
  - 최대값에 도달하면 예외를 던지게 하면 좋다.

<br>

> java.util.concurrent.atomic 패키지의 AtomicLong
- 기존의 `synchronized` 키워드를 사용한 동기화 방식은 상대적으로 비용이 크기 때문에,
  - 이를 대체하고자 락 없이도(lock-free) 스레드 안전한 프로그래밍을 지원하는 클래스들이 담겨 있다.
- `volatile`은 동기화의 두 효과 중 통신 쪽만 지원하지만 이 패키지는 원자성(배타적 실행)까지 지원한다.
- 즉, `synchronized` 키워드와 달리 오버헤드 없이 원자적 연산을 제공하여 멀티 스레드 환경에서 높은 성능과 안정성을 보장한다.
```java
private static final AtomicLong nextNum = new AtomicLong();

public static long generateNumber() {
    return nextNum.getAndIncrement();
}
```

<br>

## 3. 가변 데이터 공유하지 않는 것

- 위의 문제들을 피하는 가장 좋은 방법은 애초에 가변 데이터를 공유하지 않는 것이다.
- 불변 데이터만 공유하거나 아무것도 공유하지 말자.
- 즉, **가변 데이터는 단일 스레드에서만 쓰도록 하자.**
- 객체를 안전 발행하는 방법
  - 정적 필드
  - volatile 필드
  - 락을 통해 필드 접근 (synchronized)
  - 동시성 컬렉션 (ConcurrentHashMap)
<details>
    <summary>사실상 불변(effectively immutable) 객체</summary>
<div markdown="1">

- 한 스레드가 데이터를 다 수정한 후 다른 스레드에 공유할 때는 해당 객체에서 공유하는 부분만 동기화해도 된다.
- 그러면 그 객체를 다시 수정할 일이 생기기 전까지 다른 스레드들은 동기화 없이 자유롭게 값을 읽어갈 수 있다.
</div>
</details>

<details>
    <summary>안전 발행(safe publication)</summary>
<div markdown="1">

- 다른 스레드에 사실상 불변 객체를 건네는 행위
- 안전 발행을 통해 다른 스레드는 항상 완전히 생성 또는 수정된 최신의 데이터를 보게 된다.
</div>
</details>


<br>

## 4. 핵심 정리

- 여러 스레드가 가변 데이터를 공유한다면 그 데이터를 읽고 쓰는 동작은 반드시 동기화해야 한다.
- 동기화하지 않으면 한 스레드가 수행한 변경을 다른 스레드가 보지 못할 수도 있다.
- 공유되는 가변 데이터를 동기화하는 데 실패하면 응답 불가 상태에 빠지거나 안전 실패로 이어질 수 있다.
  - 이는 디버깅 난이도가 가장 높은 문제에 속한다.
  - 간혈적이거나 특정 타이밍에만 발생할 수도 있고, VM에 따라 현상이 달라지기도 한다.
- 배타적 실행은 필요 없고 스레드끼리의 통신만 필요하다면 volatile 한정자만으로 동기화할 수 있다.
  - 다만 사용하기가 까다롭다.