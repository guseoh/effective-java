### ✅ 스트림 병렬화는 주의해서 적용하라

## 1. 자바의 동시성 프로그래밍

- 처음 릴리즈부터 지원: 스레드, 동기화, wait/notify 지원
- java 5: java.util.concurrent 라이브러리, 실행자(Executor) 프레임워크 지원
- java 7: 고성능 병렬 분해(parallel decomposition) 프레임워크인 [포크-조인(fork-join)](https://upcurvewave.tistory.com/653) 패키지를 추가
- java 8: parallel 메서드를 한 번 호출하면 파이프라인을 병렬 실행할 수 있는 스트림 지원
> 이처럼 자바로 동시성 프로그램을 작성하기가 쉬워지고 있지만, **안정성(safety)과 응답 가능(liveness) 상태를 유지하기 위해 코드를 올바르게 작성하는 것을 어려운 작업**이다.

<details>
    <summary>동시성</summary>
<div markdown="1">

- 싱글 코어에서 멀티스레드를 동작시키기 위한 방식으로, 멀티 태스킹을 위해 **여러 개의 스레드가 번갈아가면서 실행**되는 성질을 말한다.
- 즉, 여러 작업이 동시에 실행되고 있는 **것처럼** 구현되는 것
</div>
</details>

<details>
    <summary>병렬성</summary>
<div markdown="1">

- 멀티 코어에서 멀티스레드를 동작 시키는 방식으로, 한 개 이상의 스레드를 포함하는 각 코어들이 동시에 실행되는 성질을 말한다.
- 즉, 여러 작업이 **실제로** 동시에 실행되고 있는 것
</div>
</details>

<details>
    <summary>스트림을 사용해 처음 20개의 메르센 소수를 생성하는 프로그램</summary>
<div markdown="1">

```java
package Effective_java.item48;

import java.math.BigInteger;
import java.util.stream.Stream;

public class Ex48_1 {
    static final BigInteger ONE  = BigInteger.ONE;
    static final BigInteger TWO  = BigInteger.TWO;

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        primes()
                .map(p -> TWO.pow(p.intValueExact()).subtract(ONE))
                .filter(mersenne -> mersenne.isProbablePrime(50))
                .limit(20)
                .forEach(System.out::println);

        long endTime = System.currentTimeMillis();
        System.out.println(String.format("코드 실행 시간: %20dms", endTime - startTime));
    }

    static Stream<BigInteger> primes() {
        return Stream.iterate(TWO, BigInteger::nextProbablePrime);
    }
}


/*--------------------------------------------*/
코드 실행 시간:    9541ms
```
</div>
</details>

<details>
    <summary>스트림 파이프라인의 parllel() 호출</summary>
<div markdown="1">

```java
package Effective_java.item48;

/**
 * 병렬 스트림 사용
 */
import java.math.BigInteger;
import java.util.stream.Stream;

public class Ex48_1_2{

    static final BigInteger ONE = BigInteger.ONE;
    static final BigInteger TWO = BigInteger.TWO;

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        primes()
                .map(p -> TWO.pow(p.intValueExact()).subtract(ONE))
                .parallel()
                .filter(mersenne -> mersenne.isProbablePrime(50))
                .limit(20)
                .forEach(System.out::println);

        long endTime = System.currentTimeMillis();
        System.out.println(String.format("코드 실행 시간: %20dms", endTime - startTime));
    }

    static Stream<BigInteger> primes() {
        return Stream.iterate(TWO, BigInteger::nextProbablePrime);
    }
}

/*--------------------------------------------*/
응답 불가 + CPU 많이 사용
```
</div>
</details>

- `응답 불가 + CPU 많이 사용`이 나타나는 이유는 스트림 라이브러리가 이 파이프라인을 병렬화하는 방법을 찾아내지 못했기 때문이다.
- **데이터소스가 `Stream.iterate`거나 중간 연산으로 `limit`를 쓰면 파이프라인 병렬화로는 성능 개선을 기대할 수 없다.**
    - 위의 코드는 두 경우에 모두 해당된다.
- `limit`를 다룰 때 CPU 코어가 남는다면 원소가 몇 개 더 처리한 후 제한된 개수 이후의 결과를 버려도 아무런 해가 없다고 가정한다.
> 스트림 파이프라인을 마구잡이로 병렬화하면 성능이 오히려 나빠질 수도 있다.

<br>

## 2. 스트림 병렬화를 적용하는 경우
> 스트림의 소스가 ArrayList, HashMap, HashSet, ConcurrentHashMap의 인스턴스거나 배열, int 범위, long 범위일 때 병렬화의 효과가 좋다.

1. **모두 데이터를 원하는 크기로 정확하게 손쉽게 나눌 수** 있어서 일을 다수의 스레드에 분배하기에 좋다는 특징이 있다.
2. 원소들을 순차적으로 실행할 때의 **참조 지역성(locality of reference)** 이 뛰어나다.
    - 이웃한 원소의 참조들이 메모리에 연속해서 저장되어 있다는 뜻이다.
    - 참조 지역성이 낮으면 스레드는 데이터가 주 메모리에서 캐시 메모리로 전송되어 오기를 기다리는 시간이 길어진다. (캐시 히트율이 낮다.)
    - 기본 타입의 배열이 참조 지역성이 뛰어나다.

<br>

## 3. 종단 연산과 병렬화

- 스트림 파이프라인의 종단 연산의 동작 방식도 병렬 수행 효율에 영향을 준다.
- 종단 연산에서 수행하는 작업량이 파이프라인 전체 작업에서 상당 비중을 차지하면서 순차적인 연산일 경우
    - 스트림 파이프라인 병렬화에 적합하지 않다.
- 예를 들어, `collect`와 같은 종단 연산은 내부적으로 수행해야 할 작업이 많다.
    - 결과를 수집하기 위해 내부적으로 컬렉션을 생성하고, 중간 연산의 결과를 이 컬렉션에 추가한다.
    - 이 과정은 병렬화하기 어렵기 때문에 병렬화에 부적합하다.
- `reduce`, `min`, `max`, `count` 같은 연산일 경우
    - 입력 원소들을 하나의 결과로 합치는 연산이므로, 이 연산을 수행하는 데 필요한 작업은 간단하고 병렬로 수행할 수 있다.
    - 그러므로 스트림 파이프라인 병렬화에 적합하다.

<br>

## 4. 주의해야 할 점

- 직접 구현한 Stream, Iterable, Collection이 병렬화할 경우
    - `spliterator` 메서드를 반드시 재정의하고 성능 평가를 꼼꼼히 테스트하라.
- Stream API 명세
    1. **결합 법칙을 만족해야 한다.**
        - reduce 연산에 사용되는 accumulator와 combiner 함수는 반드시 결합 법칙을 만족해야 한다.
    2. **간섭받지 않아야 한다.**
        - 파이프라인이 수행되는 동안 데이터 소스가 변경되지 않아야 한다.
    3. **상태를 갖지 않아야 한다.**
        - 입력 이외의 상태에 영향을 받지 않고, 입력에만 의존한 결과를 반환해야 한다.
- 스트림 병렬화는 오직 성능 최적화 수단임을 기억해야 한다.
    - 다른 최적화와 마찬가지로 변경 전후로 반드시 성능을 테스트하여 병렬화를 사용할 가치가 있는지 확인해야 한다.(Item 67)
- 조건이 잘 갖춰질 경우 `parallel` 메서드 호출 하나로 거의 프로세서 코어 수에 비례하는 성능 향상을 만끽할 수 있다.

<br>

## 5. 핵심 정리

- 계산도 올바로 수행하고 성능도 빨라질 거라는 확신 없이는 스트림 파이프라인 병렬화는 시도 하지 말라.
- 스트림을 잘못 병렬화하면 프로그램을 오동작하거나 성능을 급격히 떨어뜨린다.
- 스트림의 소스가 ArrayList, HashMap, HashSet, ConcurrentHashMap의 인스턴스나 배열,  int/long 범위 일 때 병렬화의 효과가 가장 좋다.
- 계산도 정확하고 성능도 좋아졌음이 확실해졌을 때만 스트림 병렬화를 사용하라.