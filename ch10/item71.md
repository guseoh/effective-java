### ✅ 필요 없는 검사 예외 사용은 피하라

#### 해당 책에 나오는 용어 표현

- 검사 예외 = `Checked Exception`
- 비검사 예외 = `Uncheck Exception`

`Checked Exception`를 싫어하는 개발자들이 많지만, 올바르게 사용하면 API와 프로그램의 품질을 향상시킬 수 있습니다.
결과를 코드로 변환하거나 언체크 예외를 던지는 방식과 달리, 체크 예외는 발생한 문제를 프로그래머가 직접 처리할 수 있게 하여 신뢰성을 높여줍니다.
물론 이를 과도하게 사용하면 오히려 사용하기 어려운 API가 될 수 있으므로, 상황에 맞게 신중하게 적용하는 것이 중요합니다.

```java
public class Item71Example {

    // Checked Exception를 던지는 메서드
    public static String readFirstLine(String path) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            return br.readLine();
        }
    }

    public static void main(String[] args) {
        List<String> paths = List.of("a.txt", "b.txt", "c.txt");

        // 상황
        // Java 8 Stream에서 사용하는 경우
        // readFirstLine()은 체크 예외를 던지므로, 예외를 처리해야 됨
        // 
        // 문제 발생
        // 컴파일 오류 발생: "unhandled exception: java.io.IOException"
        // 따라서 다음처럼 try-catch로 감싸는 람다를 작성해야 함 → 코드가 복잡해짐
        paths.stream()
                .map(path -> {
                    try {
                        return readFirstLine(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e); // Uncheck Exception로 감싸서 우회
                    }
                })
                .forEach(System.out::println);
    }
}
```

- **Checked Exception:** 컴파일러가 확인을 강제하는 예외
    - 복구 가능하거나 외적인 상황(파일 입출력, 네트워크 통신)에서 발생할 것으로 예상되는 문제에 사용
    - 호출자는 반드시 `try-catch` 또는 `throws` 로 처리해야 합니다.
        - `try-catch` 블록을 사용하여 예외를 붙잡아 처리
        - `throws` 키워드를 사용하여 자신을 호출한 상위 메서드로 예외를 전파
- **Uncheck Exception:** 컴파일러가 확인을 강제하지 않는 예외
    - 주로 프로그래머의 실수로 인해 발생하는 예외 - 즉, 런타임 오류
    - ex) `NullPointerException`, `IllegalArgumentException`

정리하자면 `Checked Exception`는 “**복구 가능한 상황**” 에만 사용해야 합니다. 즉, 호출자가 합리적으로 조치를 취할 수 있는 경우입니다.  
반대로 호출자가 조치할 방법이 없는 경우라면 `Uncheck Exception`(RuntimeException)를 사용해야 합니다.

`Checked Exception` 메서드를 사용할 때는 `try-catch`로 예외를 직접 처리하거나 `throws` 키워드를 사용해 상위 호출자에게 예외를 전파해야 하므로,
API 사용자는 예외 처리 방식을 명확히 인지해야 하는 부담을 가지게 됩니다.  
특히, Java 8 이후의 스트림(Stream) API 환경에서는 체크 예외를 직접 사용할 수 없다는 제약 때문에 코드가 복잡해집니다.

### Checked Exception 대처 방법

1. **`Optional` 사용**
   <br/>  
   `Optional`은 값이 존재할 수도 있고 존재하지 않을 수도 있음을 나타내므로, 예외를 던지는 대신 값의 부재를 명확하게 표현하는 데 사용할 수 있습니다.  
   이는 스트림(Stream) 처리에서도 유용하게 적용 가능합니다.

```java
// -------------------- before ----------------------
public String findUser(String filePath) throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
        return br.readLine(); // 파일 첫 줄(유저 이름) 반환
    }
}

public static void main(String[] args) {
    UserService service = new UserService();

    try {
        String user = service.findUser("user.txt");
        System.out.println("User: " + user);
    } catch (IOException e) {
        e.printStackTrace();
    }
}

// -------------------- after ----------------------
public Optional<String> findUser(String filePath) {
    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
        return Optional.ofNullable(br.readLine());
    } catch (IOException e) {
        return Optional.empty();
    }
}

public static void main(String[] args) {
    UserService service = new UserService();

    // 예외 처리 대신 Optional의 흐름 제어 메서드 사용
    service.findUser("user")
            .ifPresentOrElse(
                    user -> System.out.println("User: " + user),
                    () -> System.out.println("파일을 찾을 수 없거나 내용이 없습니다.")
            );
}

```

2. **상태 검사 메소드 사용**
   <br/>  
   예외 상황을 사전에 방지할 수 있는 사전에 상태를 확인할 수 있는 상태 검사 메소드를 사용해서 어느 정도 `Checked Exception`인 상황을 대처할 수 있습니다.

```java
public static void main(String[] args) {

    // before
    try {
        obj.action(args);
    } catch (TheCheckedException e) {
        // ... 예외 상황일 때 사용할 코드
    }

    // after
    if (obj.actionPermitted(args)) { // actionPermitted()는 상태 검사 메소드
        obj.action(args);
    } else {
        // ... 예외 상황일 때 사용할 코드
    }
}
```