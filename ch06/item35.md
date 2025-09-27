### ✅ ordinal 메소드 대신 인스턴스 필드를 사용하라

`ordinal` 메서드는 `enum`에서 각 상수가 몇 번째로 정의되는지 반환하는 메소드입니다.

ordinal 메소드가 매력적인 메소드처럼 보이지만 열거 타입이라서 몇 가지 문제점이 발생합니다.

1. 유지 보수가 힘듭니다. - 열거형 상수의 순서에 의존하기 때문에 순서가 변경되면 기존 코드를 많이 수정해야 됩니다.
2. 상수 선언 순서를 변경하는 순간 다른 메소드가 다르게 동작할 수 있습니다.
3. `ordinal` 메소드를 사용하면 중복된 값을 가질 수 없습니다.

### ordinal 메소드의 단점을 보여주는 코드

```java
public enum Ensemble {
    SOLO, DUET, TRIO, QUARTET, QUINTET,
    SEXTET, SETPTET, OCTET, NONET, DECTET;

    public int numberOfMusicians() {
        return ordinal() + 1;
    }
}
```

### 해결 방법 - 인스턴스 필드에 직접 값 주입

```java
public enum Ensemble {
    SOLO(1), DUET(2), TRIO(3), QUARTET(4), QUINTET(5),
    SEXTET(6), SETPTET(7), OCTET(8), NONET(9), DECTET(10);

    private final int numberOfMusicians;

    Ensemble(int size) {
        this.numberOfMusicians = size;
    }

    public int numberOfMusicians() {
        return numberOfMusicians;
    }
}
```