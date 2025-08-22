### ✅ 인터페이스는 타입을 정의하는 용도로만 사용하라

## 1. 인터페이스 용도
- 인터페이스는 자신을 구현한 클래스의 인스턴스를 참조할 수 있는 타입 역할을 한다.
- 클래스가 어떤 인터페이스를 구현한다는 것은 **자신의 인스턴스로 무엇을 할 수 있는지를 클라이언트에 얘기해주는 것**이다.
- 인터페이스는 오직 이 용도로만 사용해야 한다.

<br>

### 1.1 상수 인터페이스
- 상수 인터페이스는 **메서드 없이, 상수를 뜻하는 static final 필드로만 가득 찬** 인터페이스
- 이 상수들을 사용하려는 클래스에서는 정규화된 이름(qualified name)을 쓰는 걸 피하고자 그 인터페이스를 구현하곤 한다.
<details>
    <summary>상수 인터페이스 안티패턴 - 사용금지!!!</summary>
<div markdown="1">

```java
public interface PhysicalConstants {
    // 아보가드로 수 (1/몰)
    static final double AVOGADROS_NUMBER = 6.022_140_857e23;
		
    // 볼츠만 상수 (J/K)
    static final double BOLTZMANN_CONSTANT = 1.380_648_52e-23;

    // 전자 질량 (kg)
    static final double ELECTRON_MASS = 9.109_383_56e-31;
}
```
</div>
</details>

- `final`이 아닌 클래스 A가 인터페이스를 구현한다면 A를 구현한 **하위 클래스에서는 인터페이스가 정의한 상수들로 오염**된다.
    - 사용하지 않을 수도 있는 상수를 포함하여 모두 가져온다.
- 클래스 내부에서 사용하는 상수는 외부 인터페이스가 아니라 내부 구현에 해당한다.
    - 따라서 `상수 인터페이스를 구현하는 것은 내부 구현을 API로 노출하는 행위`이다.
    - 사용자에게 혼란을 주기도 하며, 심하게는 클라이언트 코드가 내부 구현에 해당하는 이 상수들에 종속되게 한다.
    - 그래서 이 상수들이 더 이상 쓰이지 않더라도 [바이너리 호환성](https://docs.oracle.com/javase/specs/jls/se7/html/jls-13.html)을 위해 여전히 상수 인터페이스를 구현하고 있어야 한다.
- 자바 플랫폼 라이브러리에도 상수 인터페이스가 몇 개 있으나, 인터페이스를 잘못 활용한 예이니 따라 해서는 안 된다.

<br>

## 2. 상수를 공개할 목적이라면
1. 특정 클래스나 인터페이스와 강하게 연관된 상수라면 그 클래스나 인터페이스 자체에 추가해야 한다.
   <details>
    <summary>클래스 내부에 상수 선언</summary>
    <div markdown="1">
    
    ```java
    public class Order {

        // 주문 상태를 나타내는 상수
        public static final int STATUS_PENDING = 0;   // 주문 대기 중
        public static final int STATUS_PAID = 1;      // 결제 완료
        public static final int STATUS_SHIPPED = 2;   // 배송 중
        public static final int STATUS_COMPLETED = 3; // 배송 완료
    
        private int status;
    
        public Order() {
            this.status = STATUS_PENDING;
        }
    
        public void setStatus(int status) {
            this.status = status;
        }
    
        public boolean isShipped() {
            return this.status == STATUS_SHIPPED;
        }
    }
    ```
   
    - `Order` 클래스는 **주문(Order)** 과 관련된 모든 동작을 담당한다.
    - `Order.STATUS_PAID`처럼 클래스명으로 접근 가능하고, 코드 가독성도 높아진다
    </div>
    </details>
   
   <details>
    <summary>인터페이스 내부에 상수 선언</summary>
    <div markdown="1">
    
    ```java
    public interface HttpStatus {
        int OK = 200;
        int CREATED = 201;
        int BAD_REQUEST = 400;
        int UNAUTHORIZED = 401;
        int NOT_FOUND = 404;
        int INTERNAL_SERVER_ERROR = 500;
    }
    ```

    - HTTP 상태 코드는 HTTP 프로토콜이라는 개념에 강하게 묶여 있다.
    - 따라서 `HttpStatus` 인터페이스에 상수를 두는 것은 자연스럽다.
    - 이 경우 `HttpStatus.OK` 방식으로 참조한다.
    </div>
    </details>

2. 열거(Enum) 타입으로 나타내기 적합한 상수라면 열거 타입으로 만들어 공개하면 된다.
    <details>
    <summary>Enum 타입 활용</summary>
    <div markdown="1">
    
    ```java
    public enum Day{ MON, TUE, WED, THU, FRI, SAT, SUN};
    ```
    </div>
    </details>
3. 인스턴스화 할 수 없는 유틸리티 클래스(아이템 4)에 담아 공개하자.
    <details>
    <summary>상수 유틸리티 클래스</summary>
    <div markdown="1">
    
    ```java
    public class PysicalConstants{
    private PysicalConstants(){}; // 인스턴스화 방지
    
    public static final double AVOGARDROS_NUMBER = 6.022_140_857e23;
    public static final double BOLTZMANN_CONSTANT = 1.380_648_52e-23;
    public static final double ELECTRON_MASS = 9.109_383_56e-3;
    } 
    ```

   - 유틸리티 클래스에 정의된 상수를 클라이언트에서 사용하려면 클래스 이름까지 함께 명시해야 한다.
     - `PysicalConstants.AVOGARDROS_NUMBER`
   
   </div>
   </details>

> 숫자 리터럴에 사용된 **밑줄(_)**을 주목해보자.  
자바 7부터 허용되는 이 밑줄은 숫자 리터럴 값에는 아무런 영향을 주지 않으면서, 읽기는 훨씬 편하게 해준다.  
고정소수점 숫자든 부동소수점 숫자든 5자리 이상이라면 밑줄을 고려하자.
