### ✅ 문자열 연결은 느리니 주의하라

문자열은 다양한 상황에서 자주 사용되며, 여러 문자열을 `+` 연산자로 연결하는 경우도 많습니다.
고정된 크기의 문자열을 만드는 데에는 큰 문제가 없지만, 문자열을 반복적으로 변환하거나 이어 붙일 때는 성능 저하가 발생할 수 있습니다.  
이는 문자열이 불변(immutable) 객체이기 때문에 문자열을 결합할 때마다 기존 문자열의 내용을 새로 복사해야 하며,
이로 인해 수행 시간은 문자열의 개수에 따라 제곱(n²)에 비례해 증가하게 됩니다.

### 예시

```java
public String statement() {
    String result = "";

    for (int i = 0; i < numItems(); i++) {
        result += lineForItem(i); // 문자열 연결
    }

    return result;
}
```

이렇게 문자열을 반복적으로 연결하는 경우 큰 성능 저하가 일어납니다. 이런 경우에는 `String` 대신 `StringBuilder`를 사용하면 해결됩니다.

```java
public String statement2() {
    StringBuilder result = new StringBuilder(bumItems() * LINE_WIDTH);

    for (int i = 0; i < numItems(); i++) {
        result.append(lineForItem(i)); // 문자열 연결
    }

    return result.toString();
}
```

`numItems()`가 100개를 반환하고, `lineForItem()`이 길이 80의 문자열을 반환하도록 실행한 결과,
`StringBuilder`를 사용한 코드가 문자열 결합 방식보다 약 6.5배 더 빠르게 동작했습니다.
문자열 결합은 연산 시간이 제곱(n²)에 비례하기 때문에, 항목 수가 많아질수록 두 방식의 성능 차이는 더욱 커집니다.

