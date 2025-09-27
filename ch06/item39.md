### ✅ 명명 패턴보다 애너테이션을 사용하라

### 명명 패턴의 단점

1. 오타가 나면 안됩니다.
2. 명명 패턴은 일종의 약속일 뿐 강제하는 기능은 없습니다. 즉, 올바른 요소에서만 사용되리라 보증할 방법이 없습니다.
3. 프로그램 요소(메소드, 객체 등)를 매개변수로 안전하게 전달할 마땅한 방법도 없으며, 컴파일러 시점에서 오류를 잡아주지 못해서 런타임 시점에 오류가 발생합니다.

이러한 문제를 보완하기 위해 JUnit 3은 4에 @Test 어노테이션을 도입했습니다.

### 마커 어노테이션 사용 예시

마커 어노테이션란 아무 매개변수 없이 단순히 마킹만 사용하는 어노테이션을 의미합니다.(@Override)

1. 어노테이션 정의

```java

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CustomAnnotation {
}
```

- **Retention:** 어노테이션 정보가 언제까지 남아있을지 지정
- **Target:** 어노테이션이 적용될 대상을 지정
  어노테이션 정의할 때 사용하는 어노테이션들을 메타어노테이션(meta-annotation)이라고 부릅니다.

2. 어노테이션 적용

```java
public class Sample {

    @CustomAnnotation
    public static void m1() {
    } // 성공

    public static void m2() {
    } // 적용 x

    @CustomAnnotation
    public static void m3() {
        throw new RuntimeException("fail"); // 실패 - 약속되지 않은 예외가 발생
    }

}
```

3. 어노테이션과 리플렉션을 이용한 로직 - 인텔리제이

```java
public class RunTests {
    public static void main(String[] args) throws Exception {
        int tests = 0;
        int passed = 0;

        Class<?> testClass = Class.forName("org.personal.project.annotation.Sample"); // Run Configuration으로 설정해도 됨
        for (Method m : testClass.getDeclaredMethods()) {
            if (m.isAnnotationPresent(CustomAnnotation.class)) {    // CustomAnnotation 어노테이션이 선언된 메서드만을 호출한다.
                tests++;
                try {
                    m.invoke(null);
                    passed++;
                } catch (InvocationTargetException wrappedExc) {
                    Throwable exc = wrappedExc.getCause();
                    System.out.println(m + " 실패 : " + exc);
                } catch (Exception e) {
                    System.out.println("잘못 사용한 테스트 @CustomAnnotation : " + m);
                }
            }
        }
        System.out.printf("성공 : %d, 실패 : %d%n", passed, tests - passed);
    }
}
```

### 매개변수를 받는 어노테이션 사용 예시

```java

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExceptionTest {
    Class<? extends Throwable> value();
}
```

```java
public class Sample2 {
    @ExceptionTest(ArithmeticException.class)
    public static void m1() { // 성공
        int i = 0;
        i = i / i;
    }

    @ExceptionTest(ArithmeticException.class)
    public static void m2() { // ArithmeticException 예외가 아닌 다른 예외가 발생해 실패 
        int[] a = new int[0];
        int i = a[1];
    }
}
```

```java
public class RunTests {
    public static void main(String[] args) throws Exception {
        int tests = 0;
        int passed = 0;

        Class<?> testClass = Class.forName("org.personal.project.annotation.Sample2"); // Run Configuration으로 설정해도 됨
        for (Method m : testClass.getDeclaredMethods()) {
            if (m.isAnnotationPresent(ExceptionTest.class)) {
                tests++;
                try {
                    m.invoke(null);
                    System.out.printf("테스트 %s 실패 : 예외를 던지지 않음 %n", m);
                } catch (InvocationTargetException wrappedExc) {
                    Throwable exc = wrappedExc.getCause();
                    Class<? extends Throwable> excType = m.getAnnotation(ExceptionTest.class).value();

                    if (excType.isInstance(exc)) {
                        passed++;
                    } else {
                        System.out.printf("테스트 %s 실패: 기대한 예외 %s, 발생한 예외 %s%n", m, excType.getName(), exc);
                    }
                } catch (Exception e) {
                    System.out.println("잘못 사용한 @ExceptionTest : " + m);
                }
            }
        }
        System.out.printf("성공 : %d, 실패 : %d%n", passed, tests - passed);
    }
}
```

이후 책에서는 매개변수로 배열을 받거나 반복 가능한 어노테이션을 사용하는 방법이 소개되지만, 사실 Spring 에서 custom annotation을
AOP에 적용하는 방법이 따로 있으며, 작성 방법이 크게 다르지 않다고 생각해서 별도로 작성하지 않았습니다.