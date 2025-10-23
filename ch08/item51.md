### ✅ 메소드 시그니처를 신중히 설계하라

이번 아이템에서는 개별 아이템으로 두기 애매한 API 설계요령들을 모아 두었습니다.

### 1. 메소드 이름을 신중하게 짓자

#### 핵심 목표

- 같은 패키지에 속한 다른 이름들과 일관되게 짓는 게 취우선 목표
- 개발자 커뮤니티에서 널리 받아들여지는 이름을 사용

#### 명명 규칙

- 표준 명명 규칙을 잘 지키자
- 긴 이름을 피하자
- 애매하면 자바 라이브러리의 API 가이드 참조

### 2. 편의 메소드를 너무 많이 만들지 말자

- 메소드가 너무 많으면 배우고, 사용하고, 문서화하고, 테스트하고, 유지보수하기 어렵습니다.
- 클래스나 인터페이스는 자신의 각 기능을 완벽히 수생하는 메소드로 제공
- 별로 사용하지 않을 것 같으면 만들지 말 것

### 3. 매개변수 목록은 짧게 유지하자

- IDE가 조금 해결해주지만 4개 이상의 매개변수면 API 문서를 계속 보면서 개발을 해야 되므로 4개 이하가 좋습니다.
- 같은 타입의 매개변수가 연속으로 나오는 경우 헷갈립니다.

매개변수 목록을 짧게 줄여주는 3가지 방법

#### 3-1. 여러 메소드로 쪼갠다.

```java
public class Item51BeforeExample {

    /**
     * 기존 코드 - 무조건 5개의 매개변수를 다 알아야 함
     *
     * 지정된 범위 [fromIndex, toIndex) 내에서 주어진 원소(target)의 
     * 첫 번째 발생 인덱스를 찾습니다.
     *
     * @param list 검색할 리스트
     * @param target 찾을 원소
     * @param start 검색을 시작할 인덱스 (포함)
     * @param end 검색을 끝낼 인덱스 (미포함)
     *
     */
    public static int beforeMethode(List<String> list, String target, int start, int end) {
        List<String> subList = list.subList(start, end);

        int index = subList.indexOf(target);

        if (index == -1) {
            return -1;
        }

        return start + index;
    }
}

public class Item51AfterExample {
    private static List<String> extractSubList(List<String> list, int start, int end) {
        return list.subList(start, end);
    }

    private static int findElementIndex(List<String> list, String target, int start) {
        int index = list.indexOf(target);
        return (index == -1) ? -1 : start + index;
    }

    public static int findElementIndexInRange(List<String> list, String target, int start, int end) {
        List<String> subList = extractSubList(list, start, end);
        return findElementIndex(subList, target, start);
    }
}
```

메서드를 분리한다고 해서 무조건 전체 파라미터 수 자체가 줄어드는 건 아니지만, 각각의 메서드에서 필요한 파라미터만 받도록 쪼개는 것이 핵심입니다.

#### 3-2. 매개변수 여러 개를 묶어주는 도우미 클래스를 만드는 것

```java
public class Item51BeforeExample {

    public static int beforeMethode(List<String> list, String target, int start, int end) {
        List<String> subList = list.subList(start, end);

        int index = subList.indexOf(target);

        if (index == -1) {
            return -1;
        }

        return start + index;
    }

}

public class Item51AfterExample {

    static class Range {
        private final int start;
        private final int end;

        public Range(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }
    }

    public static int afterMethode(List<String> list, String target, Range range) {
        List<String> subList = list.subList(range.getStart(), range.getEnd());
        int index = subList.indexOf(target);
        return (index == -1) ? -1 : range.getStart() + index;
    }

}
```

#### 3-3. 앞에 두 가지 방법을 혼합한 것으로 빌더 패턴을 메소드 호출에 응용

```java
public class Item51Example {

    static class SearchRequest {
        private final List<String> list;
        private final String target;
        private final int start;
        private final int end;

        private SearchRequest(Builder builder) {
            this.list = builder.list;
            this.target = builder.target;
            this.start = builder.start;
            this.end = builder.end;
        }

        public List<String> getList() {
            return list;
        }

        public String getTarget() {
            return target;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        // Builder 패턴 구현
        public static class Builder {
            private List<String> list;
            private String target;
            private int start = 0;
            private int end = Integer.MAX_VALUE;

            public Builder list(List<String> list) {
                this.list = list;
                return this;
            }

            public Builder target(String target) {
                this.target = target;
                return this;
            }

            public Builder start(int start) {
                this.start = start;
                return this;
            }

            public Builder end(int end) {
                this.end = end;
                return this;
            }

            public SearchRequest build() {
                return new SearchRequest(this);
            }
        }
    }

    public static int builderMethode(SearchRequest request) {
        int end = Math.min(request.getEnd(), request.getList().size());
        List<String> subList = request.getList().subList(request.getStart(), end);
        int index = subList.indexOf(request.getTarget());
        return (index == -1) ? -1 : request.getStart() + index;
    }

    public static void main(String[] args) {
        List<String> names = Arrays.asList("Kim", "Lee", "Park", "Choi", "Jung");

        // 가독성이 높고 파라미터 실수 줄어줌
        SearchRequest request = new SearchRequest.Builder()
                .list(names)
                .target("Choi")
                .start(1)
                .end(5)
                .build();

        int index = builderMethode(request);
        System.out.println("Index: " + index);
    }
}

```

#### 매개변수의 타입으로는 클래스보다는 인스턴스가 낫다

매개변수로 적합한 인터페이스가 존재한다면, 구현체(HashMap) 대신 인터페이스(Map)를 직접 사용하는 것이 좋습니다.  
예를 들어, `public void someLogicExecute(Map<K, V> map)`과 같이 선언하면,
이 메서드는 `TreeMap`, `ConcurrentHashMap` 등 **어떠한 `Map` 구현체던지 간에 활용**할 수 있어 응집도가 낮아집니다.

#### boolean 보다는 원소 2개짜리 열거 타입이 낫다

화씨 온도와 섭씨 온도를 나타내는 `public enum TemperatureScale { FAHRENHEIT, CELSIUS }`와 같은 열거 타입이 있다고 가정하고,
이 열거 타입을 매개변수로 받아 적합한 온도계(`Thermometer`) 인스턴스를 만든다고 할 때,
열거 타입을 사용하지 않은 경우는 `Thermometer.newInstance(true)`처럼 호출하여 `true`가 섭씨를 의미하는지 등 생성 코드만을 보고는 의도를
명확히 파악하기 어렵습니다.  
하지만 열거 타입을 사용한 경우에는 `Thermometer.newInstance(TemperatureScale.CELSIUS)`와 같이 호출하게 되어, 코드가 훨씬 읽기 좋고 명확해지는
이점을 얻을 수 있습니다.