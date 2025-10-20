## ✅ 네이티브 메서드는 신중히 사용하라

<br>

## 0. 들어가기 전
자바 네이티브 인터페이스(Java Native Interface, JNI)는 자바 프로그램이 네이티브 메서드를 호출하는 기술이다.

네이티브 메서드는 C나 C++ 같은 네이티브 프로그래밍 언어로 작성한 메서드를 말한다.

<br>

## 1. 네이티브 메서드의 주요 쓰임새

### 1.1  레지스트리 같은 플랫폼 특화 기능을 사용한다.
- 특정 플랫폼에만 특화된 기능을 사용하기 위해 네이티브 메서드를 사용할 수 있다.
- 자바 9가 새로 process API를 추가해 OS 프로세스에 접근하는 길을 열어주었다.
<details>
    <summary>process API</summary>
<div markdown="1">

```java
package Effective_java.item66;


import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaProcess {

    private static final Logger log = Logger.getLogger(JavaProcess.class.getName());

    public static void printProcessInfo() throws IOException {
        ProcessHandle processHandle = ProcessHandle.current();
        ProcessHandle.Info processInfo = processHandle.info();

        log.info("PID: " + processHandle.pid());
        log.info("Arguments: " + processInfo.arguments());
        log.info("Command: " + processInfo.command());
        log.info("Instant: " + processInfo.startInstant());
        log.info("Total CPU duration: " + processInfo.totalCpuDuration());
        log.info("User: " + processInfo.user());
    }

    public static void main(String[] args) {
        try {
            printProcessInfo();
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error printing process info", e);
        }
    }
}


10월 17, 2025 3:22:14 오후 Effective_java.item66.JavaProcess printProcessInfo
정보: PID: 24567
정보: Arguments: Optional.empty
정보: Command: Optional[/usr/lib/jvm/java-17/bin/java]
정보: Instant: Optional[2025-10-17T06:22:13.123Z]
정보: Total CPU duration: Optional[PT0.004S]
정보: User: Optional[user]

```
</div>
</details>

<br>

### 1.2 네이티브 코드로 작성된 기존 라이브러리를 사용한다.
- 기존 라이브러리를 재사용하기 위해서는 네이티브 메서드를 사용해야 한다.
- ex) 레거시 데이터를 사용하는 레거시 라이브버리

<br>

### 1.3 성능 개선할 목적으로 성능에 결정적인 영향을 주는 영역만 따로 네이티브 언어로 작성한다.
- 자바 초기 시절(Java 3 이전)이라면 이야기가 다르지만, JVM은 엄청난 속도로 발전해왔다.
- 지금의 자바는 다른 플랫폼에 견줄만한 성능을 보인다.
- 하지만 **현재는 성능을 개선할 목적으로 네이티브 메서드를 사용하는 것은 거의 권장하지 않는다.**

<br>

## 2. 네이티브 메서드의 단점
- **네이티브 언어는 메모리 훼손 오류로 부터 안전하지 않다. (아이템 50)**
    - 자바는 메모리 관리를 자동으로 처리해주는 언어이므로, 개발자가 직접 메모리를 관리할 필요가 없다.
    - 그러나 네이티브 언어를 사용하면 개발자가 직접 메모리를 할당하고 해제해야 한다.
    - 이 과정에서 메모리 할당이나 해제를 잘못 처리하면 메모리 훼손 오류가 발생할 수 있다.
- **자바보다 플랫폼에 의존적이므로 이식성이 낮다.**
    - 자바 코드는 다양한 플랫폼에 동작시킬 수 있다.
    - 네이티브 언어로 작성된 코드는 특정 운영체제나 하드웨어에 의존적이므로, 자바 코드보다 이식성이 낮다.
- **GC는 네이티브 메모리를 자동 회수하지 못하기 때문에 추적할 수 없다. (아이템 8)**
- **성능, 비용, 디버깅 측면에서 모두 좋지 않다.**