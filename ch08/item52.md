### ✅ 다중정의는 신중히 사용하라

## 1. 다중 정의에 의한 문제점
<details>
    <summary>Code 52-1 컬렉션 분류기 - 오류! 이 프로그램은 무엇을 출력할까?</summary>
<div markdown="1">

```java
package Effective_java.item52;

import java.math.BigInteger;
import java.util.*;

public class Ex52_1 {
    public static String classify(Set<?> s) {
        return "집합";
    }

    public static String classify(List<?> lst) {
        return "리스트";
    }

    public static String classify(Collection<?> c) {
        return "그 외";
    }

    public static void main(String[] args) {
        Collection<?>[] collectors = {
                new HashSet<String>(),
                new ArrayList<BigInteger>(),
                new HashMap<String, String>().values()
        };

        for (Collection<?> c : collectors) {
            System.out.println(classify(c));
        }
    }
}
```
</div>
</details>

“**집합**”, “**리스트**”, “**그 외”** 를 차례로 출력할 것 같지만, 실제로 수행해보면 “**그 외**”만 세 번 연달아 출력한다.

이유는 **다중정의(overloading, 오버로딩)된 세 `classify` 중 어느 메서드를 호출할지가 컴파일 타임에 정해지기 때문**이다.

- 컴파일타임에는 for 문 안의 c는 항상 Collection<?> 타입
- 런타임에는 타입이 매번 달라지지만, 호출할 메서드를 선택하는 데는 영향을 주지 못한다.
- 따라서 컴파일타임의 매개변수 타입을 기준으로 항상 세 번째 메서드인 classify(Collection<?>)만 호출하는 것이다.

<br>

### 1.1 직관과 결과가 어긋나는 이유

**재정의한 메서드는 동적으로 선택되고, 다중정의한 메서드는 정적으로 선택되기 때문이다.**

메서드를 재정의했다면 해당 객체의 런타임 타입이 어떤 메서드를 호출할지의 기준이 된다.

메서드 재정의는 상위 클래스가 정의한 것과 똑같은 시그니처의 메서드를 하위 클래스에서 다시 정의한 것을 말한다.

메서드를 재정의한 다음 ’하위 클래스의 인스턴스’에서 그 메서드를 호출하면 재정의한 메서드가 실행된다.

**컴파일 타임에는 그 인스턴스의 타입이 무엇인지는 상관이 없다.**

<details>
    <summary>Code 52-2 재정의된 메서드 호출 메커니즘 - 이 프로그램은 무엇을 출력할까?</summary>
<div markdown="1">

```java
package Effective_java.item52;

import java.util.List;

public class Ex52_2 {
    static class Wine {
        String name() { return "포도주"; }
    }

    static class SparklingWine extends Wine {
        @Override String name() { return "발포성 포도주";}
    }

    static class Champagne extends SparklingWine {
        @Override String name() { return  "샴페인";}
    }

    public static class Overriding {
        public static void main(String[] args) {
            List<Wine> wineList = List.of(
                    new Wine(), new SparklingWine(), new Champagne()
            );

            for (Wine wine : wineList) {
                System.out.println(wine.name());
            }
        }
    }
}
```
</div>
</details>

- Wine 클래스에서 정의된 name 메서드는 하위 클래스인 SparklingWine과 Champagne에서 재정의된다.
    - 결과는 “포도주”, “발포성 포두주”, “샴페인” 차례로 출력
- for 문에서 컴파일타임 타입이 모두 Wine인 것에 무관하게 항상 ‘`가장 하위에서 정의한` ’ 메서드가 실행
- 다중정의된 메서드 사이에서는 객체의 런타임 타입은 중요하지 않다.
- 선택은 컴파일타임에 오직 매개변수의 컴파일타임 타입에 의해 이뤄진다.

<br>

### 1.2 맨 위 코드의 문제를 해결한 예제

```java
public static String classify(Collection<?> c) {
    return c instanceof Set ? "집합" : 
    c instanceof List ? "리스트" : "그 외";
}
```

- CollectionClassifier의 모든 classify 메서드를 하나로 합친 후 instanceof로 명시적으로 검사하면 말끔히 해결된다.

<br>

## 2. 다중 정의 주의할 점
> 다중정의가 혼동을 일으키는 상황을 피해야 한다.


#### 1. 안전하고 보수적으로 가려면 매개변수 수가 같은 다중정의는 만들지 말자.
   가변인수(varargs)를 사용하는 메서드라면 다중정의를 아예 하지 말아야 한다.

<br>

#### 2. 다중정의하는 대신 메서드 이름을 다르게 지어주는 것이 나을 수도 있다.
ObjectOutputStream 클래스의 write 메서드는 다중 정의가 아닌, 모든 메서드에 다른 이름을 지어주었다.  
ex) writeBoolean(boolean), writeInt(int), writeLong(long), …

<br>

#### 3. 생성자와 같은 경우엔 정적 팩터리 메서드를 활용할 수 있다.

여러 생성자가 같은 수의 매개변수를 받는 경우를 완전히 피하기 힘들 때는 대비책을 마련해 두면 도움이 될 것이다.   

매개변수 수가 같은 다중정의 메서드가 많더라도, 그 중 어느 것이 주어진 매개변수 집합을 처리할지가 명확히 구분된다면 햇갈릴 일은 없을 것이다.  

즉, 매개변수 중 하나 이상이 근본적으로 다르다(radically different)면 된다.

<details>
    <summary>근본적으로 다르다(radically different)</summary>
<div markdown="1">

- 두 타입이 (null이 아닌) 값을 서로 어느 쪽으로든 형변환할 수 없다는 뜻이다.
</div>
</details>

이 조건만 충족하면 컴파일타임 타입에 영향을 받지 않고, 매개변수들의 런타임 타입만으로 결정되게 된다.

<br>

#### 4. AutoBoxing과 Generic에 주의하라.
```java
/--(1)--/
public class SetList {
    public static void main(String[] args) {
        Set<Integer> set = new TreeSet<>();
        List<Integer> list = new ArrayList<>();

        for (int i = -3; i < 3; i++) {
            set.add(i);
            list.add(i);
        }

        for (int i = 0; i < 3; i++) {
            set.remove(i);
            list.remove(i);
        }
        System.out.println(set + " " + list); // [-3, -2, -1] [-2, 0, 2]
    }
}

/--(2)--/
public interface List<E> extends Collection<E> {
    ...
    boolean remove(Object o);
    E remove(int index);
    ...
}
```

- 첫 번째 코드는 -3부터 2 까지의 정수를 정렬된 집합과 리스트에 각각 추가하고 양쪽에 똑같이 remove 메서드를 세 번 호출한다.
- 예상 값은 `[-3, -2, -1], [-3, -2, -1]` 이지만, 실제 값은 음이 아닌 값을 제거 후 리스트에서 홀수를 제거한 후 `[-3, -2, -1], [-2, 0, 2]` 이다.
- List의 경우 `i`를 특정 원소 값이 아닌 index로 취급하여 예상과 다른 결과가 반환되었다.
- 제네릭과 오토 박싱이 등장하면서 두 메서드의 매개변수 타입이 더는 근본적으로 다르지 않게 되었다.

```java
for (int i = 0; i < 3; ++i) {
    set.remove(i);
    list.remove((Integer) i);
}
```
- `list.remove`의 인수를 `Integer`로 형변환하여 올바른 다중정의 메서드를 선택하게 하면 해결된다.

<br>

#### 5. 메서드를 다중 정의할 때 서로 다른 함수형 인터페이스라도 같은 위치의 인수로 받아서는 안된다.
```java
public class LambdaThread {
    public static void main(String[] args) {
        // 1. Thread 생성자 호출
        new Thread(System.out::println).start();

        // 2. ExecutorService의 submit 메서드 호출
        ExecutorService exec = Executors.newSingleThreadExecutor();
//        exec.submit(System.out::println); // compile 
    }
}
```

- 둘 다 System.out::println을 인자로 받고, Runnable을 받는 형제 메서드를 다중 정의하지만 2번만 컴파일 에러가 발생한다.
- ExecutorService의 submit 다중 정의 메서드 중 `Callable<T>`를 받는 메서드가 있기 때문이다.
- println이 void를 반환하므로, 반환값이 있는 Callable과 헷갈릴 수 없다고 생각하지만 다중정의 해소(resolution: 적절한 다중 정의 메서드를 찾는 알고리즘)는 이렇게 동작하지 않는다.
- 만약 println이 다중정의 없이 하나만 존재했다면 submit 메서드 호출이 제대로 컴파일 됐을 것이다.
- **서로 다른 함수형 인터페이스라도 서로 근본적으로 다르지 않다는 의미를 가진다.**

> **부정확한 메서드 참조(inexact method reference)**
> 
> > `System.out::println`은 다중 정의되어 있어서 부정확한 메서드 참조이다.  
> >ExecutorService.submit 또한 다중 정의 되어 있어, 이 경우 자바의 다중 정의 해소 알고리즘이 기대대로 동작하지 못할 수 있다.  
> > 즉, 컴파일 단계에서 다중 정의된 메서드 중에 무엇을 사용할지 정해야 하는데, 결정 알고리즘들이 답을 찾지 못한다
> 
>
