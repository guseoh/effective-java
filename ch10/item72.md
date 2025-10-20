## ✅ 표준 예외를 사용하라

<br>

## 1. 표준 예외의 장점

- 개발자가 작성한 API가 다른 사람이 익히고 사용하기 쉬워진다.
    - 많은 프로그래머에게 이미 익숙해진 규약을 그대로 따르기 때문
- 낯선 예외를 사용하지 않게 되어 읽기가 쉬워진다.
- 예외 클래스 수가 적을수록 메모리 사용량도 줄고 클래스를 적재하는 시간도 적게 걸린다.

<br>

## 2. 많이 재사용되는 예외

### `IllegalArgumentException`

- 호출자가 인수로 부적절한 값을 넘길 때 던지는 예외
- ex) 반복 횟수를 지정하는 매개변수에 음수를 건넬 때 쓸 수 있다.
- 특수한 일부는 따로 구분해서 쓴다.
    - null 값을 허용하지 않은 메서드에 null을 건네면 `IllegalArgumentException`이 아닌 `NullPointerException`을 던진다.
    - 어떤 시퀀스의 허용 범위를 넘는 값을 건넬 때도 `IllegalArgumentException` 보다는 `IndexOfBoundsException`을 던진다.

### `IllegalStateExcetpion`

- 대상 객체의 상태가 호출된 메서드를 수행하기에 적합하지 않을 때 주로 던진다.
- ex) 제대로 초기화되지 않은 객체를 사용하려 할 때 던질 수 있다.

### `ConcurrentModificatonException`

- 단일 스레드에서 사용하려고 설계한 객체를 여러 스레드가 동시에 수정하려 할 때 던진다.
    - 외부 동기화 방식으로 사용하려고 설계한 객체도 마찬가지
- 동시 수정을 확실히 검출할 수 없으므로, 문제가 생길 가능성을 알려주는 정도의 역할로 쓰인다.

### `UnsupportedOperationException`

- 클라이언트가 요청한 동작을 대상 객체가 지원하지 않을 때 던진다.
- 보통 구현하려는 인터페이스의 메서드 일부를 구현할 수 없을 때 사용
- ex) 원소를 넣을 수만 있는 List 구현체에 대고 누군가 remove 메서드를 호출하면 이 예외를 던질 것이다.

| 예외 | 주요 쓰임 |
|------|-----------|
| `IllegalArgumentException` | 허용하지 않는 값이 인수로 건네졌을 때 |
| `IllegalStateException` | 객체가 메서드를 수행하기에 적절하지 않은 상태일 때 |
| `NullPointerException` | `null`을 허용하지 않는 메서드에 `null`을 건넸을 때 |
| `IndexOutOfBoundsException` | 인덱스가 범위를 넘어섰을 때 |
| `ConcurrentModificationException` | 허용하지 않는 동시 수정이 발생했을 때 |
| `UnsupportedOperationException` | 호출한 메서드를 지원하지 않을 때 |


<br>

## 3. 표준 예외 확장

- 표준 예외만으로는 충분한 정보를 제공하지 못할 때가 있다.
- 이때, 사용자 정의 예외를 생성하여 필요한 정보나 기능을 추가할 수 있다.
- 단, 예외는 직렬화할 수 있다. (아이템 12)
    - 직렬화에는 많은 부담이 따르니 나만의 예외를 새로 만들지 않아야 할 근거로 충분할 수 있다.

<br>

## 4. `IllegalStateException` vs `IllegalArgumentException`

- 호출자가 인자를 잘못 전달 →  `IllegalArgumentException`
- 메서드가 객체의 부적절한 상태에서 호출 → `IllegalStateException`

| 비교 항목 | `IllegalArgumentException` | `IllegalStateException` |
|------------|------------------------------|--------------------------|
| 원인 | 잘못된 메서드 인자 | 부적절한 객체 상태 |
| 책임 | 호출자 (caller) | 객체 (callee) |
| 예시 | `setAge(-1)` | `send()` before `connect()` |
| 일반적 메시지 | `"Invalid argument: age"` | `"Cannot call send() before connect()"` |