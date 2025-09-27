### ✅ 태그 달린 클래스는 클래스 계층구조를 활용하라

태그 달린 클래스(Tagged Class)는 한 클래스로 여러 객체의 종류를 표현하는 방식입니다.
이때 enum 같은 태그 필드를 두어 어떤 종류의 객체인지 구분합니다.

**예시**

```java
public class Figure {
    enum Shape {RECTANGLE, CIRCLE} // 종류

    // 태그 필드: 객체의 종류를 구분하는 필드(RECTANGLE, CIRCLE)
    private final Shape shape;

    // 이 필드들은 shape가 RECTANGLE일 때만 사용
    double length;
    double width;

    // 이 필드는 shape가 CIRCLE일 때만 사용
    double radius;

    // 원(CIRCLE)을 위한 생성자
    Figure(double radius) {
        this.shape = Shape.CIRCLE;
        this.radius = radius;
    }

    // 사각형(RECTANGLE)을 위한 생성자
    Figure(double length, double width) {
        this.shape = Shape.RECTANGLE;
        this.length = length;
        this.width = width;
    }

    // 넓이를 계산하는 메소드
    public double area() {
        switch (shape) {
            case RECTANGLE:
                return length * width;
            case CIRCLE:
                return Math.PI * (radius * radius);
            default:
                throw new IllegalArgumentException();
        }
    }
}
```

해당 코드는 `Figure`라는 하나의 클래스로 원(Circle)과 사각형(Rectangle)을 함께 표현할 수 있습니다. 이때 enum을 활용한 shape 같은 변수가 바로 태그 필드 역할을 합니다.
이 태그 필드 덕분에 Figure 클래스 인스턴스가 지금 원인지, 아니면 사각형인지 구분할 수 있습니다.

그리고 태그가 원일 때는 반지름 값(radius)만 사용하고 사각형일 때는 가로/세로 값(length, width)만 사용하는 식으로 태그에 따라 필요한 데이터 필드와 동작 방식이 달라지게 됩니다.

### 태그 달린 클래스의 문제점

- 열거 타입 선언, 태그 필드, switch 문 등 **지저분한 코드를 작성**해야 합니다. 즉 이런 태그 달린 클래스은 한 눈에 알아볼 수 없습니다.
- `Circle` 객체가 `length`나 `width` 같은 **필요 없는 필드**를 가지고 있어 쓸데없는 메모리를 잡아먹습니다. 반대로 `RECTANGLE` 객체는 `radius` 필드를 가지고 있습니다.
- 필드들을 final로 선언하려면 **필요없는 필드들까지 초기화**해야 하는 불필요한 코드를 작성해야 합니다.
- 어떤 객체인지 **`switch` 문에 의지**하기 때문에 새로운 객체를 추가하려면 `switch` 문을 수정해야 하는 단점이 있습니다.
- `Figure` 클래스가 실제로 원인지 사각형인 지 알 수 없으므로 없습니다. 즉, **하나의 클래스가 여러 객체의 의미**를 가지므로 단일 책임 원칙을 위반하게 됩니다.

이러한 문제들 때문에 태그 달린 클래스는 객체지향의 장점을 제대로 활용하지 못하는 클래스입니다. 즉 객체지향을 어설프게 흉내 낸 클래스입니다.

### 계층 구조로 문제 해결

자바의 객체지향 특징인 **상속과 다형성을 활용**한 클래스 계층구조를 사용하면 위 문제들을 모두 해결

```java
abstract class Figure {
    abstract double area(); // 공통 메소드
}

public class Circle extends Figure {
    private final double radius;

    public Circle(double radius) {
        this.radius = radius;
    }

    @Override
    public double area() {
        return Math.PI * (radius * radius);
    }
}

public class Rectangle extends Figure {
    private final double length;
    private final double width;

    public Rectangle(double length, double width) {
        this.length = length;
        this.width = width;
    }

    @Override
    public double area() {
        return length * width;
    }
}
```

위와 같이 Figure를 추상 클래스나 인터페이스로 정의하고, 각 도형(Circle, Rectangle)을 별도의 클래스로 분리하면 태그 달린 클래스의 단점을 해결할 수 있습니다.

### 결론

|           | 태그 달린 클래스                                         | 클래스 계층구조                                 |
|:----------|:--------------------------------------------------|:-----------------------------------------|
| **코드 구조** | 하나의 거대한 클래스에 모든 종류의 인스턴스가 뒤섞여 있음 (`switch` 문에 의존) | 부모 클래스와 자식 클래스들로 책임이 분리된 체계적인 구조(다형성 활용) |
| **유지보수**  | 하나의 파일에서 여러 구현을 관리하므로 복잡하고 오류가 발생하기 쉬움            | 각 클래스가 독립적으로 존재하며, 단일 책임 원칙에 충실          |
| **확장성**   | 새로운 종류 추가 시 기존 클래스를 수정해야 함 (OCP 위반)               | 새로운 클래스를 추가하기만 하면 됨 (OCP 준수)             |
| **안정성**   | `switch` 문 누락 등 런타임 오류 가능성 높음(컴파일러의 도움을 못 받음)     | 컴파일러가 추상 메서드 구현을 강제하여 런타임 오류를 방지         |