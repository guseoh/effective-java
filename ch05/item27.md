### ✅ 비검사 경고를 제거하라

비검사 경고(Unchecked Warning)는 컴파일러가 코드를 읽을 때
'이 코드는 타입이 안전한지 완전히 확신할 수 없으니 런타임에 오류가 날 수도 있으니 꼭 확인해 봐' 라고 미리 알려주는 경고입니다. 이러한 경고는 잠재적인 문제를 알려주는 신호로 절대 무시하면 안됩니다.

특히 제네릭을 사용할 때 많이 발생하며, 형변환 검사, 메소드 호출 경고, 매개변수화 가변인수 타입 경고, 변환 경고 등 다양한 경고를 볼 수 있습니다.


### 그럼 왜 비검사 경고가 발생할까요??
가장 큰 이유는 타입을 명시하지 않았기 때문입니다.
- [**Raw 타입**](https://donghyeon.dev/%EC%9D%B4%ED%8E%99%ED%8B%B0%EB%B8%8C%EC%9E%90%EB%B0%94/2021/03/25/raw-%ED%83%80%EC%9E%85%EC%9D%80-%EC%82%AC%EC%9A%A9%ED%95%98%EC%A7%80-%EB%A7%90%EC%9E%90/#:~:text=List%3C%3F%20extends%20Number%3E-,Raw%20%ED%83%80%EC%9E%85,-raw%20%ED%83%80%EC%9E%85%EC%9D%B4%EB%9E%80%2C) **사용:** 제네릭 타입을 명시하지 않고 `List list = new ArrayList()`처럼 코드를 작성하면, 컴파일러는 이 리스트에 어떤 타입의 객체가 들어올지 없어서 비검사 경고가 발생합니다.
- **제네릭 배열 생성:** 자바는 런타임에 타입 소거가 일어나기 때문에 `new List<String>[]` 같은 제네릭 배열 생성을 허용하지 않습니다. 이 때문에 제네릭과 가변인수(varargs)를 함께 사용할 때 경고가 발생하기도 합니다.

**제네릭에 가변인수 사용 예제**
```java
    @Test
    void varargsWarningTest() {
        List<String> list1 = new ArrayList<>();
        list1.add("사과");

        List<String> list2 = new ArrayList<>();
        list2.add("바나나");

        // Unchecked generics array creation for varargs parameter 오류 발생
        printLists(list1, list2);
    }

    @SuppressWarnings("unchecked") // Possible heap pollution from parameterized vararg type 경고를 숨기기 위해 사용
    public static <T> void printLists(List<T>... lists) {
        for (List<T> list : lists) {
            System.out.println(list);
        }
    }
```

### 비검사 경고를 제거하는 방법
1. 가장 좋은 해결책은 **제네릭 타입 명시**입니다. 
2. 안전하다고 확신할 때 `@SuppressWarnings("unchecked")` 사용할수 있습니다. 코드 설계상 Raw 타입을 사용해야 되는 경우가 있습니다. 이 때 주석으로 사용 이유, 왜 안전한지 등 정확한 설명을 작성해야 합니다.
