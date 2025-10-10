### ✅ 정확한 답이 필요하다면 float와 double은 피하라

## 1. float와 double 타입은 금융 계산과 맞지 않는다.
float와 double 타입은 과학과 공학 계산용으로 설계되었다. 이진 부동소수점 연산에 쓰이며, 넓은 범위의 수를 빠르게 정밀한 ‘근사치’로 계산하도록 세심하게 설계되었다.

따라서 정확한 결과가 필요할 때는 사용하면 안된다. → 특히 금융 관련 계산

0.1 혹은 10의 음의 거듭제곱 수(10⁻¹, 10⁻² 등)를 표현할 수 없기 때문이다.

```java
private static void floatingCalculate() {
    double funds = 1.00;
    int itemBought = 0;

    for (double price = 0.10; funds >= price; price += 0.10) {
        funds -= price;
        itemBought++;
    }

    System.out.println(itemBought + "개 구입");
    System.out.println("잔돈(달러) : " + funds);
}
```

- 프로그램을 실행해보면 사탕 3개를 구입한 후 잔돈은 0.39999999999달러가 남았음을 알게 된다.

<br>

## 2. 금융 계산에는 BigDecimal, int 혹은 long을 사용해야 한다.

```java
private static void bigDecimalCalculate() {
    final BigDecimal TEN_CENTS = new BigDecimal(".10");

    int itemBought = 0;

    BigDecimal funds = new BigDecimal(1.00);

    for (BigDecimal price = TEN_CENTS; funds.compareTo(price) >= 0; price = price.add(TEN_CENTS)) {
        funds = funds.subtract(price);
        itemBought++;
    }

    System.out.println(itemBought + "개 구입");
    System.out.println("잔돈(달러) : " + funds);
}
```

- double 타입을 BigDecimal로 교체했다.
- 이 프로그램을 실행하면 사탕 4개를 구입한 후 잔돈은 0달러가 남는다.
- 하지만 BigDecimal에는 단점이 두 가지 있다.
    1. 불편하다.
    2. 느리다.

<br>

## 3. 정수 타입을 사용한 해법

```java
private static void integerCalculate() {
    int itemBought = 0;
    int funds = 100;

    for (int price = 10; funds >= price; price += 10) {
        funds -= price;
        itemBought++;
    }

    System.out.println(itemBought + "개 구입");
    System.out.println("잔돈(센트) : " + funds);
}
```

- BigDecimal의 대안으로 int 혹은 long 타입을 쓸 수도 있다.
- 값의 크기가 제한되고, 소수점을 직접 관리해야 한다.