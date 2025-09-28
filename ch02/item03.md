### ✅ private 생성자나 열거 타입으로 싱글턴임을 보증하라

> **싱글턴(Singleton)**

싱글턴 패턴은 클래스의 인스턴스를 오직 하나만 생성할 수 있도록 보장하는 디자인 패턴입니다.

> **장점**

* 메모리 효율성 및 공유 용이: 객체 인스턴스를 단 하나만 생성하므로 메모리를 효율적으로 사용할 수 있습니다.
  또한, 이 단일 인스턴스를 여러 계층이나 다른 부분에서 쉽게 공유할 수 있습니다.

> **단점**

* 디버깅의 어려움: 단일 인스턴스가 여러 곳에서 공유되고 상태가 변경될 수 있어 예상치 못한 오류가 발생했을 때 원인을 추적하고 디버깅하기 어렵습니다.
* 테스트의 어려움: 싱글턴은 전역 상태를 가지는 경우가 많아 테스트 시 의존성 주입(Dependency Injection)과 같은 기법을 사용하기 어렵습니다. 특히, Mock 객체로 대체하기 어려워 단위 테스트를
  독립적으로 수행할 때 오류가 발생할 수 있습니다.

### 1-1 public static final 필드 방식의 싱글턴

```java
interface Logger {
    void info(String message);

    void warn(String message);

    void error(String message);
}

class AppLogger implements Logger {
    private final String name;

    public AppLogger(String name) {
        this.name = name;
        System.out.println(name + ": 콘솔 로거 인스턴스 생성");
    }

    @Override
    public void info(String message) {
        System.out.println("[" + name + "] [INFO] " + message);
    }

    @Override
    public void warn(String message) {
        System.err.println("[" + name + "] [WARN] " + message); // 에러 스트림 사용
    }

    @Override
    public void error(String message) {
        System.err.println("[" + name + "] [ERROR] " + message); // 에러 스트림 사용
    }
}

public class Singleton {
    private final Logger loggerInstance;

    // 인스턴스 단 하나만 생성
    public static final Singleton INSTANCE = new Singleton();

    private Singleton() {
        this.loggerInstance = new AppLogger("cmder"); // Singleton는 AppLogger 구현체의 무조건 의존
        System.out.println("cmder 싱글턴 인스턴스가 생성");
    }

    public void logInfo(String message) {
        loggerInstance.info(message);
    }

    public void logWarn(String message) {
        loggerInstance.warn(message);
    }

    public void logError(String message) {
        loggerInstance.error(message);
    }
}

public class SingletonTest {

    @Test
    Singleton create() {
        return Singleton.INSTANCE;
    }

    @Test
    void testLogging() {
        Singleton instance = create();

        // given
        Singleton logger = Singleton.INSTANCE;

        // when
        // 아래 3개 메소드가 AppLogger 구현체로 고정되어 있기 때문에 mock 객체를 사용하기 힘듬
        logger.logInfo("info");
        logger.logWarn("warn");
        logger.logError("error");

        // then
        assertTrue(instance == logger, "두 인스턴스는 동일");
    }
}

```

### 1-2 정적 팩토리 방식의 싱글턴

```java
public class StaticSingleton {
    private static StaticSingleton instance;

    private StaticSingleton() {
        System.out.println("인스턴스가 생성");
    }

    // 정적 팩토리 메소드
    public static StaticSingleton getInstance() throws Exception {
        if (instance == null) {
            instance = new StaticSingleton();
        } else {
            // 인스턴스가 이미 존재한다면, 예외를 발생시킵니다.
            throw new Exception("인스턴스는 이미 존재");
        }
        return instance;
    }
}
```

* 단 예외가 있다면 권한이 있으며, 리플렉션 API인 AccessibleObject.setAccessible을 사용해 private 생성자를 호출할 수 있습니다.
* 이러한 공격을 방어하려면 생성자를 수정하여 두 번째 객체가 생성되려고 할 때, 예외를 던지게 하면 됩니다.

### 2. 열거 타입 방식의 싱글턴

```java
public enum Elvis {
    INSTANCE;

    public void leaveTheBuilding() {
        // ...
    }
}
```

* public 필드 방식과는 비슷하지만 더 간결하고 추가 노력 없이 직렬화할 수 있습니다.
* 아주 복잡한 직렬화 상황이나 리플렉션 공격에서도 제 2의 인스턴스가 생기는 일을 방지할 수 있습니다.
* 대부분 상황에서는 원소가 하나뿐인 열거 타입이 싱글턴을 만드는 가장 좋은 방법 입니다.
* 단, 만들려는 싱글턴이 Enum 타입 외의 클래스를 상속해야 한다면 이 방법은 사용할 수 없습니다.
