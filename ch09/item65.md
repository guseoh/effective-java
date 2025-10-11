### ✅ 리플렉션보다는 인터페이스를 사용하라

리플렉션 기능을 이용하면 **런타임 시점**에서 임의의 클래스, 메소드, 필드 등에 접근 및 조작할 수 있습니다. 이를 통해 컴파일 시점에 존재하지 않은 클래스도 이용할 수 있습니다.  
물론 아무나 접근이 가능한 건 아니며, 자바 9부터는 `java.lang.reflect`를 통해 보안 설정을 할 수 있습니다.

### 리플렉션의 단점

1. 리플렉션을 사용하면 컴파일 시점에 오류를 미리 확인할 수 있는 장점을 잃게 됩니다.
   예외 처리 또한 마찬가지로, 존재하지 않는 클래스나 메서드에 리플렉션을 통해 접근하면 컴파일러가 이를 감지하지 못해 런타임 시점에 오류가 발생할 수 있습니다.
2. 리플렉션을 사용하면 코드가 지저분해집니다.
3. 리플렉션을 사용하면 일반적인 호출보다 성능이 매우 좋지 않습니다.

이러한 명확한 단점들로 인해, 리플렉션이 필요한 경우라 하더라도 최근에는 그 사용이 점차 줄어들고 있습니다.
다만 리플렉션의 장점을 살리면서도 단점을 최소화하기 위해, 반드시 필요한 범위 내에서 제한된 형태로 사용해야 합니다.

### 올바른 리플렉션 사용법

리플렉션을 올바르게 사용하는 방법은 컴파일 시점에 접근할 수 없는 클래스에 접근해야 할 때 사용해야 합니다.
이때 리플렉션은 인스턴스를 생성하는 용도로만 사용하고, 이렇게 생성된 인스턴스는 인터페이스나 상위 클래스를 통해 참조하여 사용하는 것이 바람직합니다.

```java
public class ReflectiveInstantiation {
    public static void main(String[] args) {
        // 클래스 이름을 Class 객체로 변환
        Class<? extends Set<String>> cl = null;

        try {
            cl = (Class<? extends Set<String>>)  // 비검사 형변환!
                    Class.forName(args[0]);
        } catch (ClassNotFoundException e) {
            fatalError("클래스를 찾을 수 없습니다.");
        }

        // 생성자를 얻는다
        Constructor<? extends Set<String>> cons = null;
        try {
            cons = cl.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            fatalError("매개변수 없는 생성자를 찾을 수 없습니다.");
        }

        // 집합의 인스턴스를 만든다.
        Set<String> s = null;
        try {
            s = cons.newInstance();
        } catch (IllegalAccessException e) {
            fatalError("생성자에 접근할 수 없습니다.");
        } catch (InstantiationException e) {
            fatalError("클래스를 인스턴스화할 수 없습니다.");
        } catch (InvocationTargetException e) {
            fatalError("생성자가 예외를 던졌습니다: " + e.getCause());
        } catch (ClassCastException e) {
            fatalError("Set을 구현하지 않은 클래스입니다.");
        }

        // 생성한 집합을 사용한다.
        s.addAll(Arrays.asList(args).subList(1, args.length));
        System.out.println(s);
    }

    private static void fatalError(String msg) {
        System.err.println(msg);
        System.exit(1);
    }
}
```

#### 코드 설명

1. 명령줄 인자로 클래스 이름을 입력받는다.  
   `HashSet`을 지정하면 무작위 순서가 될것이고 `TreeSet`을 지정하면 알파벳 순서로 출력될 것 입니다.
2. 입력받은 문자열 "java.util.HashSet"을 Class 객체로 변환한다.
3. 해당 클래스의 기본 생성자(Constructor) 를 얻는다.
4. 그 생성자를 통해 Set 인스턴스를 생성한다.
5. 나머지 인자 "hello", "world" 를 집합(Set)에 추가하고 출력한다.

해당 코드는 **실행 시점**에서 **애플리케이션 동작 로직을 변경**할 수 있습니다. 하지만 리플렉션의 단점 두 가지를 보여주는 코드입니다.

1. 런타임 시점에 여섯 가지 예외가 발생할 수 있습니다.
2. 클래스 이름만으로 인스턴스를 동적으로 생성하기 위해 무려 25줄의 코드를 작성했습니다. 물론 자바 7에 도입된 `ReflectiveOperationException`으로 코드 길이를 줄일 수 있지만 아직도
   코드가 지저분합니다.

<details>
    <summary>간단한 예시</summary>
<div markdown="1">

```java
public interface GreetingService {
    void greet();
}

// 런타임에만 존재하는 클래스 - 외부 모듈에서 동적으로 로드
public class EnglishGreetingService implements GreetingService {
    @Override
    public void greet() {
        System.out.println("Hello, world!");
    }
}

public static void main(String[] args) throws Exception {

    String className = "com.example.impl.EnglishGreetingService";

    Class<?> clazz = Class.forName(className);

    Object instance = clazz.getDeclaredConstructor().newInstance();

    GreetingService service = (GreetingService) instance;

    service.greet(); // 출력: Hello, world!

}

```

</div>
</details>

