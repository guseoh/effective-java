### ✅ 제네릭과 가변인수를 함께 쓸 때는 신중하라

## 0. 들어가기 전

가변 인수 메서드(아이템 53)와 제네릭은 자바 5 때 함께 추가되었으니 서로 잘 어우러지리라 기대하겠지만, 그렇지 않다.

가변 인수는 메서드에 넘기는 인수의 개수를 클라이언트가 조절할 수 있게 해주는데, 구현 방식에 허점이 있다.

허점은 **가변인수를 메서드로 호출하면 가변인수를 담기 위한 배열이 자동으로 만들어진다.**

그 결과 varargs 매개변수에 제네릭이나 매개변수화 타입이 포함되면 알기 어려운 컴파일 경고가 발생한다.

<br>

## 1. 제네릭과 varargs를 혼용하면 타입 안전성이 깨진다.

```java
public class Dangerous {
    static void dangerous(List<String>... stringLists){ // 1

        List<Integer> intList = List.of(42);            // 2
        Object[] objects = stringLists;                 // 3
        objects[0] = intList;                           // 4  힙 오염
        String s = stringLists[0].get(0);               // 5  ClassCastException
    }

    public static void main(String[] args) {
        dangerous(List.of("There be drangous!"));       // 6
    }
}
```

- 매개변수화 타입의 변수가 타입이 다른 객체를 참조하면 **힙 오염**이 발생한다.
- 이 메서드에서는 형변환하는 곳이 보이지 않는데도 인수를 건네 호출하면 `ClassCastException` 을 던진다.
- 제네릭을 쓰는 큰 특징은 컴파일 타임부터 런타임까지 타입 안정성을 확보하기 위한 용도로 쓰인다.
    - 하지만 위의 코드는 런타임의 타임 안전성이 깨지는 모습을 보여준다.

<details>
    <summary>코드 분석</summary>
<div markdown="1">

1. `dangerous()` 메서드는 제네릭 가변인자(varargs)를 받는다. 호출 시 내부적으로 제네릭 배열을 생성한다.
2. `Integer` 타입의 불변 리스트를 만들고 값 42를 넣어줬다.
3. `stringLists`(List 배열)를 `Object[]`에 할당한다. 이유는 배열은 공변이기 때문에 가능하다. 즉, `String` 타입을 `Object` 타입으로 사용하겠다는 뜻이다.
4. `Object[]`로 업캐스팅 되어 있기 때문에 `List<Integer>`를 끼어 넣을 수 있다. 즉, `stringLists[0]` 자리에 원래 `List<String>`이 있어야 하는데 `List<Integer>` 가 들어가 버린다.
5. `stringLists[0]`을 `List<String>` 라고 믿고 첫 번째 요소를 꺼내지만, 실제로는 `List<Integer>`가 들어있으므로 `Integer`를 반환한다. 그러므로 `ClassCastException` 가 발생한다.
</div>
</details>

<br>

## 2. 제네릭 varargs 매개변수를 안전하게 사용하는 메서드

```java
@SafeVarargs
static void safe(List<String>... stringLists) {
    for (List<String> list : stringLists) {
        System.out.println(list);
    }
}
```

자바 7 전에는 제네릭 가변인수 메서드의 작성자가 호출자 쪽에서 발생하는 경고에 대해서 해줄 수 있는 일이 없었다.

사용자는 이 경고들을 그냥 두거나 (더 흔하게는) 호출하는 곳마다 `@SuppressWarnings(”unchecked”)` 애너테이션을 달아 경고를 숨겨서 사용했다.

이런 과정은 안좋은 결과(가독성 떨어지고, 진짜 문제를 알려주는 경고마자 숨김)로 이어진다.

자바 7에서는 `@SafeVarargs` 애너테이션이 추가되어 제네릭 가변인수 메서드 작성자가 클라이언트 측에서 발생하는 경고를 숨길 수 있게 되었다.

`@SafeVarargs` 은 메서드 작성자가 그 메서드가 타입 안정함을 보장하는 장치다.

하지만 메서드가 안전한 게 확실하지 않다면  `@SafeVarargs`을 달아서는 안 된다.  

> 메서드가 안전한지 어떻게 확신할 수 있을까?

1. 가변 인수 메서드는 호출될 때마다 `varargs` 매개변수를 담는 제네릭 배열이 만들어지는데, 메서드가 이 배열에 아무것도 저장하지 말아야 한다.
2. 그 배열의 참조가 밖으로 노출되지 않아야 된다. (신뢰할 수 없는 코드가 배열에 접근할 수 없다면)

<details>
    <summary>자신의 제네릭 매개변수 배열의 참조를 노출한다. - 안전하지 않다</summary>
<div markdown="1">

```java
public class PickTwo {

    static <T> T[] toArray(T... args) {
        return args; // 가변인수 그대로 배열로 반환
    }

    static <T> T[] pickTwo(T a, T b, T c) {
        switch (ThreadLocalRandom.current().nextInt(3)) {
            case 0 : return toArray(a, b);
            case 1 : return toArray(a, b);
            case 2 : return toArray(a, b);
        }
        throw new AssertionError(); // 도달할 수 없다.
    }

    public static void main(String[] args) {
        String[] attributes = pickTwo("좋은", "빠른", "저렴한");
        System.out.println(Arrays.toString(attributes));
    }
}
```
</div>
</details>

<details>
    <summary>코드 분석</summary>
<div markdown="1">

1. 제네릭 배열을 return 했다. 즉, 밖으로 노출했다.
```java
static <T> T[] toArray(T... args) {
    return args; // 제네릭 배열을 return args-> 노출된다.
}
```

2. pickTwo를 호출하면 `toArray`가 호출되면서 제네릭 배열(args)이 노출된다. 즉, pickTwo를 쓰는 것이 `toArray`를 쓰는것과 동일하다.
```java
static <T> T[] pickTwo(T a, T b, T c) { 
    switch (ThreadLocalRandom.current().nextInt(3)) {
        case 0 : return toArray(a, b);
        case 1 : return toArray(a, c);
        case 2 : return toArray(b, c);
    }
    throw new AssertionError();
}
```

3. 이 부분에서 `toArray`를 쓸 때, 컴파일러는 `String[]`로 추론하지만 실제 생성되는 배열은 `Object[]` 이다.  
즉, 컴파일러는 toArray는 `String[]`를 리턴하지만, 실제 런타임은 `Object[]`를 리턴한다.
```java
return toArray(a, b);
```

4. 컴파일러는 호출 지점에 반환값을 String[]으로 변환하는 암묵적 캐스트([바이트코드의 CHECKCAST](https://miiiinju.tistory.com/29))를 삽입
```java
String[] attributes = pickTwo("좋은", "빠른", "저렴한");
```

5. 제네릭 가변인수 메서드에서 생성된 Object[]를 컴파일러가 String[]로 강제 캐스팅하기 때문에, 런타임에 `ClassCastException` 이 발생한다.
```java
System.out.println(Arrays.toString(attributes));
```
</div>
</details>

결과의 근본적인 문제를 살펴보면 내부적으로 만든 배열을 밖으로 노출되었기 때문이다.  
`자신의 제네릭 매개변수 배열의 참조를 노출했기 때문이다.`

<details>
    <summary>제네릭 varargs 매개변수를 안전하게 사용하는 메서드</summary>
<div markdown="1">

```java
@SafeVarargs
static <T> List<T> flatten(List<? extends T>... lists){
    List<T> result = new ArrayList<>();
    for(List<? extends T> list : lists){
        result.addAll(list);
    }
    return result;
}
```
</div>
</details>

- 제네릭이나 매개변수화 타입의 varargs 매개변수를 받는 모든 메서드에 `@SafeVarargs`를 달라.
- 안전하지 않은 varargs 메소드는 절대 작성하면 안된다.

<details>
    <summary>제네릭 varargs 매개변수를 List로 대체한 예 - 타입 안전하다.</summary>
<div markdown="1">

```java
static <T> List<T> flatten_typesafe(List<List<? extends T>> lists){
    List<T> result = new ArrayList<>();
    for(List<? extends T> list : lists){
        result.addAll(list);
    }
    return result;
}
```
</div>
</details>

- `@SafeVarargs` 애너테이션이 유일한 정답이 아니다.
- 아이템 28의 조언에 따라 varargs 매개변수를 List 매개변수로 바꿀 수도 있다.
    ```java
    List<List<? extends T>> lists
    ```



<details>
    <summary>정적 팩터리 메서드인 List.of를 활용</summary>
<div markdown="1">

```java
public class SafePickTwo {
    static <T> List<T> pickTwo(T a, T b, T c) {
        switch (ThreadLocalRandom.current().nextInt(3)) {
            case 0 : return List.of(a, b);
            case 1 : return List.of(a, c);
            case 2 : return List.of(b, c);
        }
        throw new AssertionError();
    }

    public static void main(String[] args) {
        List<String> attributes = pickTwo("좋은", "빠른", "저렴한");
        System.out.println(attributes);

    }
}
```
</div>
</details>

- `List.of` 에도 `@SafeVarargs` 애너테이션이 달려있기 때문이다.
- 이 방식의 장점은 컴파일러가 이 메서드의 타입 안전성을 검증할 수 있다는 데 있다.
    - `@SafeVarargs` 을 직접 달지 않아도 되며, 실수로 안전하다고 판단할 걱정도 없다.
- 단점은 클라이언트 코드가 살짝 지저분해지고 속도가 조금 느려질 수 있다는 정도다.