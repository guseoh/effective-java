## ✅ 클래스와 멤버의 접근 권한을 최소화하라
잘 설계된 컴포넌트는 내부 구현 정보를 외부 컴포넌트로부터 얼마나 잘 숨겼는지에 따라 평가됩니다. 이를 통해 **구현과 API를 깔끔하게 분리**할 수 있습니다.

### 캡슐화의 장점
- 시스템 개발 속도 향상: 여러 컴포넌트를 병렬로 개발할 수 있어 전체 시스템 개발 속도를 높입니다.
- 시스템 관리 비용 절감: 각 컴포넌트의 내부를 변경해도 외부에 영향을 주지 않아 유지보수가 용이합니다.
- 디버깅 용이성: 컴포넌트를 더 빠르게 파악하고 디버깅할 수 있으며, 다른 컴포넌트로 교체하는 부담이 적습니다.
- 성능 최적화에 도움: 다른 컴포넌트에 영향을 주지 않고 특정 컴포넌트만 독립적으로 최적화할 수 있습니다.
- 소프트웨어 재사용성 증가: 외부 의존성이 적은 컴포넌트는 다양한 환경에서 유용하게 사용될 가능성이 큽니다.
- 개발 난이도 감소: 큰 시스템을 제작할 때, 개별 컴포넌트의 동작을 먼저 검증하며 개발할 수 있어 난이도가 낮아집니다.

### 톱레벨 클래스와 인터페이스의 접근 수준
- public: 공개 API가 되므로, 한 번 선언하면 하위 호환성을 위해 영구적으로 관리해야 합니다.
- package-private: 해당 패키지 안에서만 접근할 수 있는 내부 구현이 됩니다.
- 최소화 원칙: 외부에서 사용할 이유가 없다면 **package-private**으로 선언하여 불필요한 노출을 막아야 합니다.
- 중첩 클래스: 한 클래스에서만 사용되는 `package-private` 접근 수준을 갖게 된 톱레벨 클래스나 인터페이스 이를 사용하는 클래스 내부에 `private static`으로 중첩시키는 것이 좋습니다. 이렇게 하면 불필요하게 외부에 노출되는 것을 막고, 코드를 더 간결하고 안전하게 유지할 수 있습니다.

톱레벨 클래스: 파일 이름과 같은 이름으로 정의된 `public` 클래스  
중첩 클래스: 톱레벨 클래스 외에 다른 클래스가 정의된 경우

보완 전: `package-private` 톱레벨 클래스인 경우
```java
package com.example.service;

public class OrderService {
    public void processOrder() {
        EmailSender sender = new EmailSender(); // EmailSender를 사용
        sender.send("주문이 완료되었습니다.");
    }
}

package com.example.service;
class EmailSender {
    public void send(String message) {
        System.out.println("이메일 전송: " + message);
    }
}
```
`EmailSender는` `package-private`이라 `com.example.service` 패키지 내에서는 어디서든 접근 가능합니다. 
하지만 `OrderService`에서만 사용되므로, 다른 클래스가 불필요하게 이 클래스에 접근할 가능성이 남아있습니다.

보완 후
```java
package com.example.service;

public class OrderService {
    public void processOrder() {
        EmailSender sender = new EmailSender();
        sender.send("주문이 완료되었습니다.");
    }

    // 중첩 클래스로 구현
    private static class EmailSender {
        public void send(String message) {
            System.out.println("이메일 전송: " + message);
        }
    }
}
```
`EmailSender`가 `OrderService` 내부에 완전히 종속되어 외부에서 접근할 수 없습니다. 이처럼 `private static` 중첩 클래스를 사용하면 캡슐화를 강화할 수 있습니다.

### 멤버(필드, 메서드 등)에 부여하는 접근 정리
| 접근 제어자       | 해당 클래스 안에서 접근 가능 여부 | 같은 패키지 안에서 접근 가능 여부 | 상속받은 클래스에서 접근 가능 여부 | 외부에서 접근 가능 여부 |
|-----------------|-----------------------------|------------|---------------------|---------------|
| public          | ✅                   | ✅                  | ✅                   | ✅             |
| protected       | ✅                   | ✅                  | ✅                   | ❌             |
| package-private | ✅                   | ✅                  | ❌                   | ❌             |
| private         | ✅                   | ❌                  | ❌                   | ❌             |


### 결론
- 꼭 필요한 것만 공개 API(public)로 설계해야 합니다.
- `public` 클래스의 인스턴스 필드는 상수가 아니라면 `public`으로 선언하지 않는 것이 좋습니다.
- 길이가 0이 아닌 `public static final` 배열 필드를 두거나, 이를 반환하는 접근자 메서드를 제공해서는 안 됩니다. 배열의 내용이 외부에서 수정될 수 있기 때문입니다.
- `protected` 멤버는 되도록 적게 만드는 것이 좋습니다. 