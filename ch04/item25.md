### ✅ 톱레벨 클래스는 한 파일에 하나만 담으라

톱레벨 클래스은 클래스 내부에 다른 클래스가 정의되지 않은 독립적으로 정의된 클래스를 의미합니다.

```java
class A { // 톱레벨 클래스
    static final String NAME = "start";
}

class B { // 톱레벨 클래스
    static final String NAME = "end";
}

public static void main(String[] args) {
    System.out.println(A.NAME + " " + B.NAME);
}
```

하나의 파일에 여러 개의 톱 레벨 클래스를 정의하면 문제가 발생할 수 있습니다. 특히 `public` 클래스는 파일당 하나만 존재해야 하며 파일 이름과 동일해야 합니다.
이를 어기면 컴파일 오류가 발생합니다.

이 문제를 피하기 위해 `package-private` 접근 제어자를 사용하여 여러 클래스를 하나의 파일에 둘 수는 있습니다.
하지만 이렇게 할 경우 클래스 순서를 변경하면 전혀 다른 결과가 나오는 문제가 발생할 수 있습니다.

### 해결방법

중첩 클래스로 구현하면 이런 문제를 해결할 수 있습니다.

```java
public class AB {
    private static class A {
        static final String NAME = "start";

    }

    private static class B {

        static final String NAME = "end";
    }

    public static void main(String[] args) {
        System.out.println(A.NAME + " " + B.NAME);
    }
}
```