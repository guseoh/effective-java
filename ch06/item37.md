### ✅ ordinal 인덱싱 대신 EnumMap을 사용하라

```java
public class Plant {
    // 식물의 생애 주기를 관리하는 열거 타입
    enum LifeCycle {
        ANNUAL, // 한해살이
        PERENNIAL, // 여러해살이
        BIENNIAL // 두해살이
    }

    final String name;
    final LifeCycle lifeCycle;

    Plant(String name, LifeCycle lifeCycle) {
        this.name = name;
        this.lifeCycle = lifeCycle;
    }

    @Override
    public String toString() {
        return name;
    }
}

public static void main(String[] args) {
    Set<Plant>[] plantsByLifeCycle = (Set<Plant>[]) new Set[Plant.LifeCycle.values().length];


    for (int i = 0; i < plantsByOrdinal.length; i++) {
        plantsByLifeCycle[i] = new HashSet<>();
    }

    Set<Plant> garden = new HashSet<>();
    garden.add(new Plant("바질", Plant.LifeCycle.ANNUAL));
    garden.add(new Plant("로즈마리", Plant.LifeCycle.PERENNIAL));
    garden.add(new Plant("당근", Plant.LifeCycle.BIENNIAL));
    garden.add(new Plant("튤립", Plant.LifeCycle.PERENNIAL));
    garden.add(new Plant("해바라기", Plant.LifeCycle.ANNUAL));

    for (Plant p : garden) {
        plantsByLifeCycle[p.lifeCycle.ordinal()].add(p);
    }

    System.out.println("한해살이 (ANNUAL): " + plantsByLifeCycle[Plant.LifeCycle.ANNUAL.ordinal()]);
    System.out.println("여러해살이 (PERENNIAL): " + plantsByLifeCycle[Plant.LifeCycle.PERENNIAL.ordinal()]);
    System.out.println("두해살이 (BIENNIAL): " + plantsByLifeCycle[Plant.LifeCycle.BIENNIAL.ordinal()]);
}
```

위에 코드가 동작은 하지만 문제가 많은 코드입니다.

1. **타입 안전성 문제:** 배열은 제네릭과 호환되지 않으니 비검사 형변환을 수행해야 하고 깔끔히 컴파일 되지 않습니다  
   <br />
   **문제점:** `Set<Plant>[] plantsByLifeCycle = (Set<Plant>[]) new Set[Plant.LifeCycle.values().length]` 이 코드에서 비검사 형변환
   경고가
   발생합니다.  
   <br />

   **이유:** 배열은 런타임에 자신이 어떤 타입의 객체를 담을지 알고 있어야 하는데 `new Set[]`은 `Set<Plant>`가 아니라 그냥 `Set`의 배열을 만듭니다.
   `Set<Plant>`라는 타입 정보가 사라져 버립니다. 그래서 컴파일러는 `(Set<Plant>[])`라는 형변환이 안전한지 확신할 수 없어 경고를 보냅니다.
   이는 잘못된 타입의 객체를 배열에 넣을 경우 `ClassCastException`이 발생할 수 있는 잠재적인 위험이 발생합니다.  
   <br />
2. 배열의 각 인덱스 의미를 모르니 출력 시 직접 이름 or 설명을 달아야합니다.  
   <br />
   **문제점:** `plantsByLifeCycle[p.lifeCycle.ordinal()]`과 같이 숫자로만 배열에 접근하면 코드의 의도를 파악하기 어렵습니다.  
   <br />
3. 정숫값을 사용해야한다.  
   <br />

   **문제점:** 만약 잘못된 값을 사용하면 ArrayIndexOutOfBoundsException 예외 발생  
   <br />
4. `enum` 타입에 필드의 순서가 변경되면 `ordinal()` 메소드 관련 코드를 수정해야 합니다.

### 해결 방법 - 실질적인 열거 타입 상수를 값을 매핑( EnumMap<>() )

```java
public static void main(String[] args) {
    Map<Plant.LifeCycle, Set<Plant>> plantByLifeCycle = new EnumMap<>(Plant.LifeCycle.class);

    for (Plant.LifeCycle lc : Plant.LifeCycle.values()) {
        plantByLifeCycle.put(lc, new HashSet<>());
    }

    Set<Plant> garden = new HashSet<>();
    garden.add(new Plant("바질", Plant.LifeCycle.ANNUAL));
    garden.add(new Plant("로즈마리", Plant.LifeCycle.PERENNIAL));
    garden.add(new Plant("당근", Plant.LifeCycle.BIENNIAL));
    garden.add(new Plant("튤립", Plant.LifeCycle.PERENNIAL));
    garden.add(new Plant("해바라기", Plant.LifeCycle.ANNUAL));

    for (Plant p : garden) {
        plantByLifeCycle.get(p.lifeCycle).add(p);
    }

    System.out.println("plantByLifeCycle = " + plantByLifeCycle);
}
```

1. 타입 안전성 보장  
   `Map<Plant.LifeCycle, Set<Plant>> plantByLifeCycle = new EnumMap<>(Plant.LifeCycle.class)` 코드는 제네릭과 완벽하게 호환됩니다. 컴파일러는
   이 맵이 `Plant.LifeCycle`을 키로 사용하고 `Set<Plant>`를 값으로 사용한다는 것을 정확히 알고 있기 때문에 잘못된 타입의 데이터를 넣으려는 시도를 컴파일 단계에서 미리 차단합니다.   
   따라서 비검사 형변환 경고나 런타임 오류가 발생할 위험이 없습니다.  
   <br/>
2. 가독성 및 유지 보수성 향상  
   `plantByLifeCycle.get(p.lifeCycle).add(p)` 코드는 `ordinal()` 대신 열거형 상수`(p.lifeCycle)` 자체를 키로 사용하므로 코드의 의도를 직관적으로 파악할 수
   있습니다.  
   <br/>
3. `enum`에 상수 순서 변경에 영향을 받지 않습니다.  
   `EnumMap`은 **키로 열거형 상수를 사용**하기 때문에, 상수의 순서가 바뀌어도 전혀 영향을 받지 않습니다. 새로운 상수가 추가되더라도 기존 코드를 수정할 필요가 없어 유지 보수가 매우 용이합니다.
   `EnumMap`은 내부적으로는 배열을 사용해 `ordinal()`의 성능을 활용해서 성능은 비슷비슷 합니다.  
   <br/>
   추가적으로 스트림을 이용하면 코드 가독성과 parallelStream() 병렬 스트림을 사용하면 데이터 양이 많을 때 처리 속도를 높일 수 있습니다.

```java
public static void main(String[] args) {

    System.out.println(Arrays.stream(garden)).collect(groupingBy(p -> p.lifeCycle));

    System.out.println(Arrays.stream(garden)).collect(groupingBy(p -> p.lifeCycle, () -> new EnumMap<>(LifeCycle.class), toSet()))
}
```

### 2차원 배열(중첩 enum)의 인덱스에 ordinal()로 매핑

```java
// 상태를 나타내는 enum (고체, 액체, 기체)
public enum Phase {
    SOLID,
    LIQUID,
    GAS;

    // 상태 변화 (녹음, 어는 점, 끓음 ...)
    public enum Transition {
        MELT,
        FREEZE,
        BOIL,
        CONDENSE,
        SUBLIME,
        DEPOSIT;

        /**
         * TRANSITIONS: 2차원 배열 [from][to] 구조로 정의 = 상태 전이 표
         */
        private static final Transition[][] TRANSITIONS = {
                {null, MELT, SUBLIME},
                {FREEZE, null, BOIL},
                {DEPOSIT, CONDENSE, null}
        };

        /**
         *
         * 상태 간 이동을 표현하는 메소드
         *
         * @param from 상태 변화 전 상태
         * @param to 상태 변화 후 상태
         * @return 어떤 상태 변화했는지 반환
         */
        public static Transition from(Phase from, Phase to) {
            return TRANSITIONS[from.ordinal()][to.ordinal()];
        }
    }
}

```

**문제점**

- 컴파일러는 `ordinal()`이 배열의 어떤 인덱스에 매핑되는지 알 수 없습니다. 이로 인해 새로운 상태가 추가되거나 순서가 바뀌면 `TRANSITIONS` 배열을 함께 수정해야만 합니다.
- `TRANSITIONS` 배열이 2차원 배열이라서 크기가 제곱으로 커집니다. 이로 인해 `null`로 채워지는 칸도 늘어납니다.

### 문제 해결 - 중첩 EnumMap

```java
public enum Phase {
    SOLID, LIQUID, GAS;

    // 하나의 상수에 2가지 value(Phase) 정의
    public enum Transition {
        MELT(SOLID, LIQUID), FREEZE(LIQUID, SOLID),
        BOIL(LIQUID, GAS), CONDENSE(GAS, LIQUID),
        SUBLIME(SOLID, GAS), DEPOSIT(GAS, SOLID);

        private final Phase from;
        private final Phase to;

        Transition(Phase from, Phase to) {
            this.from = from;
            this.to = to;
        }

        // 맵을 초기화하는 코드
        private static final Map<Phase, Map<Phase, Transition>> m =
                Stream.of(values())
                        .collect(
                                groupingBy(
                                        t -> t.from, //
                                        () -> new EnumMap<>(Phase.class),
                                        toMap(
                                                t -> t.to,
                                                t -> t,
                                                (x, y) -> y,
                                                () -> new EnumMap<>(Phase.class)
                                        ))
                        );

        // 상태 변화 전(from) 후(to) 상태를 받아서 어떤 상태 변환 하는지 반환하는 메소드
        public static Transition from(Phase from, Phase to) {
            return m.get(from).get(to);
        }
    }
}
```

단일 `enum`이나 중첩 `enum`에서 `ordinal()` 메소드를 사용해서 발생하는 문제를 `EnumMap`를 통해서 해결할 수 있습니다.