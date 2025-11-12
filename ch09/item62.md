## ✅ 다른 타입이 적절하다면 문자열 사용을 피하라

<br>

## 문자열을 쓰지 않아야 할 사례
문자열(String)은 텍스트를 표현하도록 설계되었고, 그 일을 아주 멋지게 해낸다. 
그런데 문자열은 워낙 흔하고 자바가 지원해주어 의도하지 않은 용도로도 쓰이는 경향이 있다. 이번 아이템에서는 문자열을 쓰지 않아야 할 사례를 다룬다.

<br>

### 문자열은 다른 값 타입을 대신하기에 적합하지 않다.
문자열을 다양한 데이터를 표현하는 데 편리하지만, 데이터가 원래 다른 타입(수치, 객체 등)일 경우에는 해당 타입을 직접 사용하는 것이 좋다.

예를 들어 파일, 네트워크, 키보드 입력으로부터 데이터를 받을 때 주로 문자열을 이용하지만 이 데이터가 숫자나 다른 타입(수치, 객체 등)일 경우에는 해당 타입을 직접 사용하는 것이 좋다.

즉, 받는 데이터가 수치형이라면 `int`, `float`, `BigInteger` 등 적당한 수치 타입으로 변환해야 하고, ‘예/아니오’ 질문의 답이라면 적절한 열거 타입이나 `boolean`으로 변환해야 한다.

일반화하자면 기본 타입이든 참조 타입이든 적절한 값 타입이 있다면 그것을 사용하고, 없다면 새로 하나 작성하라.

<br>

### 문자열은 열거 타입을 대신하기에 적합하지 않다.
이 부분은 아이템 34에서 다룬 내용이다.

- **타입 안전(Type-Safe)**
    - 문자열이 틀렸거나 잘못 입력되는 경우를 컴파일 시에 잡아낼 수 있다. 반면 문자열은 컴파일 시 오타나 오류를 발견하기 어렵다.
- **코드 가독성**
    - 열거 타입을 사용하면 해당 값이 어떤 목적으로 사용되는지 코드를 통해 명확하게 알 수 있다.
- **기능의 확장성**
    - 열거 타입을 사용하면 해당 값이 어떤 목적으로 사용되는지 코드를 통해 명확하게 알 수 있다.
- **싱글턴 특성**
    - 각각의 Enum 값은 하나의 인스턴스로서 싱글톤 특성을 가진다.
- **네임스페이스**
    - 열거 타입은 상수 값들을 그룹으로 묶을 수 있다. 이는 이름 충돌을 방지하고 관련된 상수 값들을 구조화한다.

<br>

### 문자열은 혼합 타입을 대신하기에 적합하지 않다.
여러 요소가 혼합된 데이터를 하나의 문자열로 표현하는 것은 적절하지 않다.

```java
String compoundKey = className + "#" + i.next();
```
이는 단점이 많은 방식이다.

첫 번째로 **parsing이 필요**하다. 문자열에서 각 요소를 추출하려면 복잡한 문자열 파싱 과정이 필요하다. 각 요소를 개별로 접근하여 파싱하므로 느리고, 귀찮고, 오류 가능성도 커진다.

두 번째로는 **각 요소 별로 적용할 수 있는 기능이 제한적**이다. 적절한 `equals()`, `toString()`, `compareTo()` 메서드를 제공할 수 없으며, String이 제공하는 기능에만 의존해야 한다.

그래서 차라리 각 요소를 별도의 필드로 갖는 전용 클래스로 만드는 편이 낫다. 이런 클래스는 보통 private static 멤버 클래스로 선언한다.(아이템 24)

<details>
    <summary>잘못된 예시 (문자열 연결 방식)</summary>
<div markdown="1">

```java
String compoundKey = className + "#" + i.next();
String[] parts = compoundKey.split("#"); // parsing 필요
String className = parts[0];
String fieldName = parts[1];
```
</div>
</details>

<details>
    <summary>좋은 예시 (전용 클래스 사용 방식)</summary>
<div markdown="1">

```java
public class SomeClass {

    private static class CompoundKey {
        private final String className;
        private final String fieldName;

        public CompoundKey(String className, String fieldName) {
            this.className = className;
            this.fieldName = fieldName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CompoundKey)) return false;
            CompoundKey that = (CompoundKey) o;
            return className.equals(that.className) &&
                   fieldName.equals(that.fieldName);
        }

        @Override
        public int hashCode() {
            return 31 * className.hashCode() + fieldName.hashCode();
        }

        @Override
        public String toString() {
            return className + "#" + fieldName;
        }
    }

    public static void main(String[] args) {
        CompoundKey key1 = new CompoundKey("User", "id");
        CompoundKey key2 = new CompoundKey("User", "id");

        System.out.println(key1.equals(key2)); // true
        System.out.println(key1);              // User#id

        // HashMap 예시
        java.util.Map<CompoundKey, String> map = new java.util.HashMap<>();
        map.put(key1, "Primary Key");

        System.out.println(map.get(key2)); // "Primary Key"
    }
}

```
</div>
</details>

<br>

### 문자열은 권한을 표현하기에 적합하지 않다.
스레드 지역변수 기능을 설계한다고 해보자. 이름처럼 각 스레드가 자신만의 변수를 갖게 해주는 기능이다.

```java
// 잘못된 예 - 문자열을 사용해 권한을 구분하였다.
public class ThreadLocal {
	private ThreadLocal() {} // 객체 생성 불가
    
    // 현 스레드의 값을 키로 구분해 저장
    public static void set(String key, Object value);
    
    // (키가 가리키는) 현 스레드의 값을 변환하다.
    public static Object get(String key);
}
```

- 이 방식의 문제는 스레드 구분용 문자열 키가 전역 네임 스페이스를 사용한다.
    - 즉, 모든 스레드가 동일한 키 네임 스페이스를 공유한다.
- 따라서 모든 스레드에 동일한 키를 사용해 set을 호출하면, 현재 스레드의 값이 덮어씌어진다.
- 의도치 않게 같은 변수를 공유하게 되어 두 클라이언트 모두 제대로 기능하지 못할 것이다.
- 보안도 취약하다. 악의적인 클라이언트라면 다른 클라이언트의 값을 get 할 수 있다.