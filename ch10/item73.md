### ✅ 추상화 수준에 맞는 예외를 던지라

저수준 예외는 시스템의 하위 계층(파일, 데이터베이스, 네트워크, I/O 등)에서 발생하는 구체적이고 기술적인 예외를 의미하며,
이는 물리적 / 기술적 실패가 문제의 원인이 되고 보통 비즈니스 로직에서는 직접적인 의미가 없습니다.  
이러한 저수준 예외를 발생 즉시 처리하지 않고 바깥으로 던져버리면 상위 레벨의 API가 오염되므로,
이 문제를 피하기 위해서는 상위 계층에서 저수준 예외를 잡아 자신의 추상화 수준에 맞는 예외로 변환하여 던져야 하는데, 이를 바로 예외 번역(Exception Translation)이라고 합니다.

#### 예외 변환 없이 단순히 전파되는 예시 - Spring MVC

```text
[Controller Layer]   ← High-level exception
- 예외를 throws로 감싸서 던져버리면 같은 예외에 해당 계층으로 전파
- 해당 예외는 스프링의 전역 예외 처리기(@ControllerAdvice 등)나 JVM으로 전파 

[Service Layer]    ← High-level exception 
- 동일한 예외가 Controller 계층으로 그대로 전달

[Repository / Data Layer]    ← Low-level exception 
- 동일한 예외가 Controller 계층으로 그대로 전달

[Infrastructure Layer] (DB, File, Network)   ← Low-level exception - 오류 발생 지점 (ex. SQLException, FileNotFoundException)
```

이렇게 같은 예외가 전파되면 상위 계층에서는 오류가 왜 발생했는지 어떤 상황인지를 이해하기 어렵습니다. ++ 디버깅이 어렵습니다.

### 해결책: 예외 번역 (Exception Translation)

예외 번역(Exception Translation)이란 하위 계층에서 발생한 저수준 예외를 상위 계층이 이해할 수 있는 고수준 예외를 변환하는 기법을 말합니다.

#### 예외 번역 예시

```text
[Controller]      → catch BusinessException         - 비즈니스 로직 관점에서 의미 있는 예외 처리
[Service]         → throw BusinessException(e)      - 예외를 감싸서 DataAccessException 예외로 변환
[Repository]      → throw DataAccessException(e)    - SQLException 저수준 예외를 추상화된 예외로 변환
[Infrastructure]  → throw SQLException              - 예외 발생 지점
```

#### 왜 계층마다 구분이 중요한가?

1. 계층 간 결합도 감소

- 상위 계층(Service, Controller)이 `SQLException`, `IOException` 등 특정 구현 기술에 직접 의존하지 않도록 분리할 수 있습니다.
- 각 계층은 자신이 이해해야 하는 수준의 추상화된 예외만 다루면 됩니다.
- 즉, Repository의 내부 구현을 DB를 바꾸더라도, Service Layer의 코드를 전혀 수정할 필요가 없도록 만드는 유연성의 기반이 됩니다.

2. 의미 있는 예외로 추상화

- `SQLException`은 DB 연결 실패 오류지만, `DataAccessException`은 **'데이터 접근 실패'라는 추상화된 의미로 해당 관점에서 디버깅에 집중할 수 있습니다.

3. 원인 추적 가능 (Exception Chaining)

- 저수준 예외를 단순히 무시하거나 새로운 예외로 덮어쓰지 않고, `throw new HighLevelException("메시지", cause)` 형태로 **예외 연쇄(Exception
  Chaining)** 를 사용합니다.
- 이를 통해 상위 계층은 고수준 예외를 처리하는 동시에, 필요할 경우 스택 트레이스를 통해 **가장 밑바닥에 있는 실제 원인** 까지 추적하여 정확한 디버깅을 수행할 수 있습니다.

### 예외 연쇄(Exception chaining)

하위 계층에서 발생한 예외(cause)를 상위 계층의 예외에 포함시켜 문제의 근본 원인(root cause)을 추적할 수 있도록 하는 기법입니다.  
즉, 하위 계층에서 발생한 예외 정보를 단순히 숨기지 않고, 상위 계층의 새로운 예외 객체에 원인(cause)으로 전달하여 연결하는 것입니다.

하위 계층에서 발생한 예외 정보가 상위 계층 예외를 발생시킨 문제를 디버깅하는데 유용할때

하위 계층 예외(원인 cause)는 상위 계층 예외로 전달된다, 상위 계층 예외의 접근자 메서드(Throwble.getCause)를 호출하면 해당 정보를 꺼낼 수 있다.

```java

try {
        // ...
        } catch (LowerLevelException cause) {
        throw new HigherLevelException(cause); // 저수준 예외를 고수준 예외에 감싸서 던져버림
}

// 상위 계층 예외 클래스 정의
class HigherLevelException extends Exception {
    HigherLevelException(Throwable cause) {
        super(cause); // 원인 예외(cause) 저장
    }
}

```

#### 원인 예외 접근 방법

```java
try {
        // ...
        } catch (HigherLevelException e) {
        System.out.println("원인 예외: " + e.getCause());
}
```

예외 변환(Exception Translation)과 예외 인쇄는 강력하지만, 모든 상황에 적용해서는 안 됩니다. 가능하다면 하위 계층에서 예외가 발생하지 않도록 미리 방지하는 것이 최선입니다.

예외 발생을 미리 방지하기 위에서는 상위 계층 메서드의 매개변수 값을 하위 계층 메서드로 건네기 전에 유효성 검사를 해서
예외가 발생할 만한 매개변수를 하위 계층 메서드로 전달되지 않게끔 만들면 예외를 사전에 예방할 수 있습니다.