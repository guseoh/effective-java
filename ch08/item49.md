### ✅ 매개변수가 유효한지 검사하라

일반적으로 개발자는 메서드의 매개변수를 다룰 때 인덱스 값은 음수가 아니어야 한다거나 객체는 `null`이 아니어야 한다는 등
기본적인 유효성 제약 조건을 바라고 코드를 작성합니다. 그러나 이러한 조건을 문서화해도 실제로 문제가 발생하면 오류의 원인을 빠르게 파악하고 디버깅하기가 쉽지 않다는 어려움이 있습니다.

### 매개변수 내부 코드 실행 전 검사를 하지 못하면 발생하는 문제

1. 메소드가 수행되는 중간에 모호한 예외를 던지며 실패할 수 있습니다.

2. 메소드가 잘 수행되지만 잘못된 결과를 반환할 수 있습니다.

3. 메소드가 실행될 때는 아무 문제가 없어 보였지만, 내부적으로는 객체의 상태를 잘못 바꿔 놓아서 나중에 전혀 다른 곳에서 예상치 못한 오류가 발생할 수 있습니다.

#### 예시 코드

```java
public class Shopping {
    private final List<String> items = new ArrayList<>();
    private double discountRate = 0.0;

    public void addItem(String item, int quantity) {
        String upperCaseItem = item.toUpperCase(); // 사전에 검사를 안하고 item에 null이 들어오면 오류 발생

        if (quantity >= 0) {
            for (int i = 0; i < quantity; i++) {
                items.add(upperCaseItem);
            }
        }
    }

    public void setDiscountRate(double rate) {
        discountRate = rate;
    }

    public double calculateTotal(double basePrice) {
        return basePrice * (1.0 - discountRate);
    }

    public List<String> getItems() {
        return items;
    }
}

@Log4j2
public class Item49Test {
    Shopping shopping = new Shopping();

    @Test
    @DisplayName("1번상황 - NullPointerException 예외 발생")
    void test01() {

        assertThrows(NullPointerException.class, () -> {
            shopping.addItem(null, 1);
        }, "NullPointerException 예외 발생");
    }

    @Test
    @DisplayName("2번 상황 - 메소드는 성공하지만 잘못된 결과 반환")
    void test02() {
        // 정상적인 상품 추가
        shopping.addItem("notebook", 2);
        int initialCount = shopping.getItems().size();

        // 수량을 음수를 넣은 상황은 말도 안되는 상황인데 메소드는 성공
        // 성공한 이유: 매개변수로 음수가 들어와서 addItem 메소드 조건절에서 false 반환해서 for 문을 실행하지 않음
        shopping.addItem("book", -3);
        int finalCount = shopping.getItems().size();

        log.info("initialCount = {} finalCount = {}", initialCount, finalCount);

    }

    @Test
    @DisplayName("3번 상황 - 미래의 calculateTotal에서 논리? 오류 발생")
    void test03() {
        double basePrice = 100000.0;

        // 0 ~ 1.0의 값이 들어와 되는데 범위를 벗어나면 나오면 나중에 논리 오류가 발생
        shopping.setDiscountRate(1.5);

        double finalPrice = shopping.calculateTotal(basePrice);

        // 기대되는 값은 양수지만 실제 값은 음수가 나옴
        log.info("finalPrice = {}", finalPrice);
    }
}
```

이때 메소드에 유효성 검사 코드를 작성하면 잘못된 값이 넘어왔을 때 즉각적으로 예외를 던질 수 있습니다.

#### 해결 코드

```java
public class ShoppingAfter {

    private List<String> items = new ArrayList<>();
    private double discountRate = 0.0;

    /**
     * * @param item     추가할 상품 이름 (null이 아니어야 함)
     *
     * @param quantity 추가할 수량 (음수가 아니어야 함)
     * @throws NullPointerException     item이 null일 경우
     * @throws IllegalArgumentException quantity가 음수일 경우
     */
    public void addItem(String item, int quantity) {
        // null 검사 - Objects.requireNonNull() 사용
        Objects.requireNonNull(item, "상품 이름은 null일 수 없습니다.");

        // @throw 사용 
        if (quantity < 0) {
            throw new IllegalArgumentException("수량은 음수일 수 없습니다: " + quantity);
        }

        if (quantity > 0) {
            String upperCaseItem = item.toUpperCase();
            for (int i = 0; i < quantity; i++) {
                items.add(upperCaseItem);
            }
        }
    }

    /**
     * * @param rate 할인율 (0.0  ~ 1.0)
     *
     * @throws IllegalArgumentException rate가 0.0 미만이거나 1.0을 초과할 경우
     */
    public void setDiscountRate(double rate) {
        if (rate < 0.0 || rate > 1.0) {
            throw new IllegalArgumentException("할인율은 0.0 (0%) 이상 1.0 (100%) 이하여야 합니다. 현재 값: " + rate);
        }
        this.discountRate = rate;
    }

    public double calculateTotal(double basePrice) {
        return basePrice * (1.0 - discountRate);
    }

    public List<String> getItems() {
        return items;
    }
}
```

자바 7에 추가된 `Objects.requireNonNull` 메소드는 `null` 검사를 간소화하고
throws로 예외를 던지고 자바독(JavaDoc) 태그를 작성해서 문서화 할 수 있습니다.  
또한, 자바 9에서는 인덱스 관련 문제를 해결하기 위해 범위 검사 메소드인 `checkFromIndexSize`, `checkFromToIndex`, `checkIndex가` 추가되어
간단하게 범위 검사를 수행할 수 있습니다.

### assert 키워드

`public`이 아닌 메소드에서 매개변수 유효성 검사를 `assert` 키워드를 이용해서 매개변수 유효성을 검사할 수 있습니다.
`assert`은 뒤에 나오는 조건이 무조건 참이라고 선언 한다는 의미입니다.

```java
private static void sort(long a[], int offset, int length) {
    assert a != null;
    assert offset >= 0 && offset <= a.length;
    assert length >= 0 && length <= a.length - offset;
    // ...
}
```
