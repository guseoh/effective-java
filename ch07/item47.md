### ✅ 반환 타입으로는 스트림보다 컬렉션이 낫다

자바 7까지는 반환 타입으로 `Collection`, `Set`, `List` 같은 컬렉션 인터페이스 혹은 `Iterable` 이나 배열을 사용했지만 자바 8에 스트림이 도입되면서
상황에 맞게 어떤 반환 타입을 쓸지 애매한 상황이 발생하게 되었습니다.

### 반환 타입이 Steam일 때 발생하는 문제

`Stream`은 `Iterable` 인터페이스를 상속받지 않기 때문에 `for-each` 루프를 직접 사용하여 순회할 수 없습니다.
따라서 메서드가 `Stream`만 반환하도록 설계하면, 반환된 데이터를 곧바로 `for-each` 구문으로 처리할 수 없어서 코드가 불편해질 수 있습니다.

스트림 인터페이스는 `Iterable`의 모든 추상 메서드를 포함하고 기능적으로 유사함에도 불구하고 이러한 제약이 발생합니다.

아래 코드는 `Stream`을 `for-each` 루프에 직접 사용했을 때 발생하는 컴파일 오류를 보여줍니다.

```java
    public static Stream<String> getStreamOfStrings() {

    return List.of("apple", "banana", "cherry").stream();
}

@Test
void testStreamUseForEach() {
    Stream<String> streamOfStrings = getStreamOfStrings();

    // Foreach not applicable to type 'java.util.stream.Stream<java.lang.String>' 오류 발생
    for (String str : streamOfStrings) {
        System.out.println(str);
    }
}



```

#### 해결 방법

```java

@Test
@DisplayName("해결 방법 1")
void testStreamToListUseForEach() {
    List<String> list = getStreamOfStrings().toList();

    for (String str : list) {
        System.out.println(str);
    }
}

@Test
@DisplayName("해결 방법 2")
void testStreamToTypeCastingForEach() {
    Stream<String> streamOfStrings = getStreamOfStrings();

    // Unchecked cast 발생
    for (String str : (Iterable<String>) streamOfStrings) {
        System.out.println(str);
    }
}

// 해결 방법 3 - 어댑터 페턴 사용
public static <E> Iterable<E> iterableOf(Stream<E> stream) {
    return stream::iterator;
}

@Test
@DisplayName("해결 방법 3")
void testStreamToAdapter() {
    for (String str : iterableOf(getStreamOfStrings())) {
        System.out.println(str);
    }
}
```

해결 방법으로 형 변환, 어댑터 패턴 등 여러 방법이 있지만 반환 타입을 `Collection`으로 만드는 방법도 있습니다.
특이한 점은 `Collection` 인터페이스는 `Iterable`의 하위 타입이고 `Steam` 메소드도 제공하므로 반복(Iterable)과 스트림(Steam)
두 가지 방식을 유연하게 사용할 수 있습니다.

다만, 반환되는 데이터의 크기가 작다면 컬렉션 프레임워크를 사용해도 메모리 부담이 크지 않지만,
시퀀스의 크기가 큰 경우에는 컬렉션 전체를 메모리에 올리는 것이 비효율적일 수 있습니다.
이럴 때는 커스텀 컬렉션을 생성해 반환하는게 더 좋은 선택이 될 수 있습니다.

크기가 큰 컬렉션 프레임워크가 메모리 부담이 큰 이유는 컬렉션 프레임워크는 기본적으로 **즉시 로딩**을 사용하기 때문에 모든 요소를 메모리에 저장합니다.

### 커스텀 컬렉션

`Collect`이 `Iterable`, `Stream`을 유연하게 사용할 수 있다고 해서 무작정 사용하면 대량의 데이터가 담긴 컬렉션을 사용할 때 메모리에 큰 부담이 갈 수 있습니다.
이럴 때 컬렉션 프레임워크를 이용한 커스텀 컬렉션을 생성하면 메모리 부담을 줄일 수 있습니다.

예를 들어, 입력한 집합의 멱집합(모든 부분 집합을 원소로 하는 집합)을 구하는 상황을 가정

```java
public class PowerSet {

    /**
     * 이 메서드는 모든 부분집합을 메모리에 올리지 않고, 필요할 때마다 동적으로 생성합니다.
     */
    public static final <E> Collection<Set<E>> of(Set<E> s) {
        List<E> src = new ArrayList<>(s);

        // 집합의 원소가 30개를 초과하면 멱집합의 크기가 너무 커져 오버플로를 일으킬 수 있으므로 제한
        if (src.size() > 30) {
            throw new IllegalArgumentException("집합에 원소가 너무 많습니다(최대 30개).: " + s);
        }

        // AbstractList를 상속받아 멱집합을 나타내는 익명 클래스를 생성
        return new AbstractList<Set<E>>() {
            @Override
            public int size() {
                // 멱집합의 크기는 2^n 입니다.
                // 비트 연산 '1 << n'은 2^n과 동일
                return 1 << src.size();
            }

            @Override
            public boolean contains(Object o) {
                return o instanceof Set && src.containsAll((Set) o);
            }

            @Override
            public Set<E> get(int index) {
                Set<E> result = new HashSet<>();
                // !! 비트 벡터를 사용하기 때문에 메모리에 데이터를 올리지 않습니다. !!
                // 주어진 인덱스의 비트를 검사하여 부분집합의 요소를 결정합니다.
                // 예를 들어, 인덱스 5(0101)는 첫 번째와 세 번째 요소를 선택합니다.
                for (int i = 0; index != 0; i++, index >>= 1) {
                    if ((index & 1) == 1) {
                        result.add(src.get(i));
                    }
                }
                return result;
            }
        };
    }
}
```

`AbstractCollection`을 활용해 커스텀 컬렉션을 만들 때 `contains()`와 `size()` 메서드를 구현하는 것이 중요합니다.
이 메서드들을 효율적으로 구현하는 것이 어렵다면, `Stream`이나 `Iterable`을 반환하는 것이 더 나은 선택일 수 있습니다.

질문
Steam은 Iterable 인터페이스를 상속 받지 않아서 for-each 문에서 사용 못한다고 하는데 스트림에 forEach 메소드랑 무슨 차이가 있는거지??

for-each 문은 종료 시점을 컨트롤 할 수 있는데 forEach 메소드는 컨트롤 못하는 차이점이 있는건가??