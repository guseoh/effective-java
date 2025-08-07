### ✅ try-finally 보다는 try-with-resources를 사용해라
JDBC나 스트림(Stream)과 같이 무언가에 연결하는 역할을 하는 라이브러리들은 사용 후 반드시 연결을 끊어주어야 합니다. 
이는 더 이상 사용하지 않는 자원을 그대로 두면 불필요한 낭비가 발생하기 때문입니다. 

마치 사람이 집을 비웠는데도 에어컨을 계속 켜두면 전기 낭비인 것처럼, 
코드에서도 사용하지 않는 연결을 끊지 않으면 시스템 자원을 계속 점유하게 되어 성능 저하나 예상치 못한 오류의 원인이 될 수 있습니다.
이런 자원은 보통 `try-finally` 구문을 사용하여 작업의 성공 여부와 관계없이 마지막에는 반드시 연결을 해제하도록 작성합니다.

### `try-finally` 사용
```java
// 한 가지 자원 사용 예시
static String firstLineOfFile(String path) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(path));
    try {
        return br.readLine();
    } finally {
        br.close();
    }
}

// 두 가지 자원 사용 예시
static void copy(String src, String dst) throws IOException{
    InputStream input = new FileInputStream(src);
    try {
        try {
            OutputSteam output = new FileOutputSteam(dst);
            byte[] buf = new byte[BUFFER_SIZE];
            int n;
            while ((n = input.read(buf)) >= 0){
                output.write(buf, 0, n);
            }
        } finally {
            output.close();
        }
    } finally {
        input.close();
    }
}
```
대부분의 사람들은 `try-finally` 구문은 사용해 자원을 관리합니다. 자원이 한 개인 경우에는 괜찮지만 
자원을 여러 개 사용하면 코드의 가독성뿐만 아니라 다른 문제점도 발생합니다.

예외는 `try` 블록과 `finally` 블록 모두 발생 할 수 있습니다. 
`readLine()` 메서드에서 **물리적인 문제**로 `IOException` 예외가 발생했다고 가정해 보겠습니다. 
이 문제는 코드만으로는 해결할 수 없기 때문에 `finally` 블록의 `close()` 메서드가 호출되지만 동일한 물리적인 문제로 또다시 `IOException` 예외를 던집니다.

문제는 여기서 발생합니다. **자바는 `finally` 블록에서 발생한 예외를 최종적으로 던지기** 때문에, 
`readLine()`에서 발생한 첫 번째 예외는 사라지고 `close()`가 던진 예외만 오류 메시지로 남게 됩니다. 
이 때문에 문제의 실제 원인을 파악하기가 매우 어려워져 디버깅이 복잡해지는 문제가 있습니다.
물론 이런 상황에 대비해 두 번째 예외 대신 첫 번째 예외를 기록하다록 코드를 작성할 수 있지만 이 방법은 코드가 너무 지저분해지고 그렇게 추천하지 않는 방법

### 결론
- `finally` 블록은 **'무조건 실행'** 되지만 그 안의 코드가 **'무조건 성공'** 하는 것을 보장하지는 않습니다. 
- 자바는 `finally` 블록에서 발생한 예외를 최종적으로 던지지 때문에 `try` 블록, `finally` 블록 두 곳에서 예외가 발생하면 디버깅이 힘듭니다.


### try-with-resources
Java 7에서 제공하는 `Try-With-Resources` 구문으로 해결할 수 있습니다. 
이 구조를 사용하려면 `AutoCloseable` 인터페이스를 구현해야 하며, 이 인터페이스는 단순히 `close()`라는 `void` 반환 메서드 하나만 정의하고 있습니다. 
많은 라이브러리 및 서드파티 라이브러리들은 이미 이 인터페이스를 구현하거나 확장하고 있으며, 
반드시 닫아야 하는 자원을 사용할 경우에는 이 인터페이스를 구현하는 것이 필수적입니다.
`Try-With-Resources` 구문을 사용하면 `try` 블록이 끝나는 시점에 **자동**으로 오버라이딩 된 `close()` 메소드를 호출합니다.

```java
class MyResource implements AutoCloseable {
    private String name;

    public MyResource(String name) {
        this.name = name;
        System.out.println("자원 '" + this.name + "'이 열렸습니다.");
    }

    @Override
    public void close() throws Exception {
        System.out.println("자원 '" + this.name + "'이 닫혔습니다.");
    }

    public void doSomething() {
        System.out.println("자원 '" + this.name + "'으로 작업 중");
    }
}

public class example {
    public static void main(String[] args) {
        try (MyResource resource1 = new MyResource("리소스1");
             MyResource resource2 = new MyResource("리소스2")) { // try 절을 조건문처럼 작성한게 try-with-resources 문법

            resource1.doSomething();
            resource2.doSomething();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### 컴파일러가 변환하는 실제 코드
```java
public static void main (String[] args) {
    MyResource resource1 = new MyResource("리소스1");
    Throwable primaryException = null; // 첫 번째 예외를 저장할 변수

    try {
        MyResource resource2 = new MyResource("리소스2");
        try {
            resource1.doSomething();
            resource2.doSomething();
        } catch (Throwable secondaryException) {
            primaryException = secondaryException;
            throw secondaryException;
        } finally {
            // resource2를 먼저 닫음
            if (resource2 != null) {
                if (primaryException != null) {
                    try {
                        resource2.close();
                    } catch (Throwable suppressedException) {
                        primaryException.addSuppressed(suppressedException);
                    }
                } else {
                    resource2.close();
                }
            }
        }
    } catch (Throwable primaryExceptionFromTry) {
        primaryException = primaryExceptionFromTry;
        throw primaryExceptionFromTry;
    } finally {
        // resource1을 마지막에 닫음
        if (resource1 != null) {
            if (primaryException != null) {
                try {
                    resource1.close();
                } catch (Throwable suppressedException) {
                    primaryException.addSuppressed(suppressedException);
                }
            } else {
                resource1.close();
            }
        }
    }
}
```