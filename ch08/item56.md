### ✅ 공개된 API 요소에는 항상 문서화 주석을 작성하라

## 1. 자바독(Javadoc)

자바에서는 자바독(Javadoc)이라는 유틸리티가 API문서의 코드가 변경될 때 마다 수정해줘야 하는데, 이 귀찮은 작업을 도와준다.

자바독은 소스코드 파일에서 문서화 주석(doc comment; 자바독 주석)이라는 특수한 형태로 기술된 설명을 추려 API 문서로 변환해준다.

<br>

### 공개된 모든 클래스, 인터페이스, 메서드, 필드 선언에 문서화 주석을 달아야 한다.

- 직렬화할 수 있는 클래스라면 직렬화 형태(아이템 87)에 관해서도 적어야 한다.
- 문서가 잘 갖춰지지 않은 API는 쓰기 헷갈려서 오류의 원인이 되기 쉽다.
- 기본 생성자에는 문서화 주석을 달 방법이 없으니 공개 클래스는 절대 기본 생성자를 사용하면 안된다.
- 유지 보수를 위해 비공개 클래스, 인터페이스, 생성자, 메서드, 필드에도 문서화 주석을 달아야 할 것이다.

<br>

### 메서드용 문서화 주석에는 해당 메서드와 클라이언트 사이의 규약을 명료하게 기술해야 한다.

- 상속용으로 설계된 클래스(아이템 19)의 메서드가 아니라면 **무엇을 하는지**를 기술해야 한다.
    - 즉, how가 아닌 what을 기술해야 한다.
- 문사화 주석에는 클라이언트가 해당 메서드를 호출하기 위한 `전제조건(precondition)`을 모두 나열해야 한다.
    - 일반적으로 전제조건은 @throws 태그로 비검사 예외를 선언하여 암시적으로 기술한다.
    - 비검사 예외 하나가 전제조건 하나와 연결되는 것이다.
    - @param 태그를 이용해 그 조건에 영향받는 매개변수에 기술할 수도 있다.
- 메서드가 성공적으로 수행된 후에 만족해야 하는 `사후조건(postcondition)`도 모두 나열해야 한다.

<br>

### 부작용도 문서화해야 한다.

- 부작용은 사후조건으로 명확히 나타나지는 않지만 시스템의 상태에 어떠한 변화를 가져오는 것을 뜻한다.
- 예를 들어, 백그라운드 스레드를 시작시키는 메서드라면 그 사실을 문서에 밝혀야 한다.

<br>

### 예시

_java.util.Map.java_
```java
public interface Map<K, V> {

     /**
     * Returns {@code true} if this map contains a mapping for the specified
     * key.  More formally, returns {@code true} if and only if
     * this map contains a mapping for a key {@code k} such that
     * {@code Objects.equals(key, k)}.  (There can be
     * at most one such mapping.)
     *
     * @param key key whose presence in this map is to be tested
     * @return {@code true} if this map contains a mapping for the specified
     *         key
     * @throws ClassCastException if the key is of an inappropriate type for
     *         this map
     * (<a href="{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified key is null and this map
     *         does not permit null keys
     * (<a href="{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
     */
    boolean containsKey(Object key);   
}
	
```

_java.util.Map.java - 한글 버전_
```java
public interface Map<K, V> {

     /**
     * 지정된 키에 대한 매핑이 이 맵에 포함되어 있으면 {@code true}를 반환합니다.  
     * 좀 더 공식적으로는, {@code Objects.equals(key, k)}가 참인 키 {@code k}에 대한  
     * 매핑이 이 맵에 존재할 때에만 {@code true}를 반환합니다.  
     * (이러한 매핑은 최대 한 개만 존재할 수 있습니다.)
     *
     * @param key 이 맵에서 존재 여부를 검사할 키
     * @return 지정된 키에 대한 매핑이 존재하면 {@code true}
     * @throws ClassCastException 키의 타입이 이 맵에서 허용되지 않는 경우
     *         (<a href="{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">선택적</a>)
     * @throws NullPointerException 지정된 키가 {@code null}이며,
     *         이 맵이 {@code null} 키를 허용하지 않는 경우
     *         (<a href="{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">선택적</a>)
     */
    boolean containsKey(Object key);   
}
	
```

<br>

## 2. 주요 Javadoc 태그 목록

`@param`
- 전제조건(precondition)에 영향받는 매개변수에 붙인다.
- 모든 매개변수에 붙이는 것을 권장한다.
- 관례상 명사구를 쓰고, 마침표를 붙이지 않는다.  
  
`@return`
- 반환 타입이 void가 아니라면 작성해야 한다.
- 관례상 명사구를 쓰고, 마침표를 붙이지 않는다.
- 코딩 표준에서 허락한다면 @return 태그의 설명이 메서드 설명과 같을 때 @return 태그를 생략해도 좋다.

`@throws`
- 발생할 가능성이 있는 모든 예외에 @throws 태그를 달아야 한다.
- if로 시작해 해당 예외를 던지는 조건을 설명하는 절이 뒤따른다.

`@code` (java 5)
- 태그로 감싼 내용을 코드용 폰트로 렌더링한다.
- 태그로 감싼 내용에 포함된 HTML 요소나 다른 자바독 태그를 무시한다.
    - HTML 메타문자인 < 기호 등을 별다른 처리 없이 바로 사용할 수 있다.
- 문서화 주석에 여러 줄로 된 코드 예시를 넣으려면 {@code} 태그를 다시 `<pre>` 태그로 감싸면 된다.
    - 즉, `<pre>{@code … 코드 … } </pre>` 형태로 쓰면 된다.

`@implSpec` (java 8)
- 클래스를 상속용으로 설계할 때는 자기사용 패턴(self-use pattern)에 대해서도 문서에 남겨 그 메서드를 올바로 재정의하는 방법을 알려줘야 한다.
- 일반적인 문서화 주석은 해당 메서드와 클라이언트 사이의 계약을 설명하지만, @implSpec 주석은 해당 메서드와 하위 클래스 사이의 계약을 설명한다.
    - 하위 클래스들이 그 메서드를 상속하거나 super 키워드를 이용해 호출할 때 그 메서드가 어떻게 동작하는지를 명확히 인지하고 사용하도록 해줘야 한다.
- -tag "implSpec:a:Implementation Requirements:” 스위치를 켜주지 않으면 @implSpec 태그를 무시해버린다.

`@literal`
- API 설명에 <, >, & 등 HTML 메타 문자를 포함할 때 사용한다.
- HTML 마크업이나 자바독 태그를 무시하게 해준다.
- {@code} 태그와 비슷하지만 코드 폰트로 렌더링하지는 않는다.
- Javadoc 첫 문장은 주로 해당 요소의 요약 설명(summary description)으로 간주된다.
- 한 클래스(혹은 인터페이스) 안에서 요약 설명이 똑같은 멤버(혹은 생성자)가 둘 이상이면 안 된다.
- 요약 설명에는 마침표를 주의해야 한다.

`@summary` (java 10)
- 문서화 주석에서 요약에 해당하는 부분을 깔끔하게 처리할 수 있게 해준다.
```java
/**
 * {@summary A suspect, such as Colonel Mustard or {@literal Mr. CoRock}.
 */
public enum Suspect {
    // (...)
}
```

`@index`  (java 9)
- 자바 9부터는 자바독이 생성한 HTML 문서에 검색(색인) 기능이 추가되어 광대한 API 문서들을 누비는 일이 한결 수월해졌다.
- {@index} 태그를 사용해 API에서 중요한 용어를 추가로 색인화할 수 있다.

<br>

## 3. 제네릭 타입/제네릭 메서드의 주석

제네릭 타입이나 제네릭 메서드를 문서화할 때는 모든 타입 매개변수에 주석을 달아야 한다.
```java
/**
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
*/
public interface Map<K, V>
```

<br>

## 4. 열거 타입의 주석
열거 타입을 문서화할 때는 상수들에도 주석을 달아야 한다. 열거 타입 자체와 그 열거 타입의 public 메서드도 물론이다.
```java
/**
* An instrument section of a symphony orchestra
*/
public enum OrchestraSection {
    /** WoodWinds, such as flute, clarinet and oboe */
    WOODWIND,
    /** Brass instruments, such as french horn and trumper */
    BRASS,
    /** Percussion instruments, such as timpani, cymbals */
    PERCUSSION,
    /** Stringed instruments, such as violin and cello */
    STRING
}
```

<br>

## 5. 애너테이션 타입의 주석
애너테이션 타입을 문서화할 때는 멤버들에도 모두 주석을 달아야 한다. 애너테이션 타입 자체도 물론이다.

필드 설명은 명사구로 한다.

애너테이션 타입의 요약 설명은 프로그램 요소에 이 애너테이션을 단다는 것이 어떤 의미인지를 설명하는 동사구로 한다.

```java
/**
 * Indicates that the annotated method is a test method that 
 * must throw the designated exception to pass
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExceptionTest {
    /**
     *  The exception that the annotated test method must throw
     *  in order to pass. (The test is permitted to throw any subtype
     *  of the type described by this class object.)
     */
    Class<? extends Throwable> value();
}
```

<br>

## 6. 패키지 문서화 주석
패키지를 설명하는 문서화 주석은 package-info.java 파일에 작성한다.

이 파일은 패키지 선언을 반드시 포함해야 하며 패키지 선언 관련 애너테이션을 추가로 포함할 수도 있다.

자바 9부터 지원하는 모듈 시스템(아이템 15)도 이와 비슷하다. 모듈 관련 설명은 module-info.java 파일에 작성하면 된다.

<br>

## 7. 스레드 안전 수준을 반드시 API 설명에 포함
클래스 혹은 정적 메서드가 스레드 안전하든 그렇지 않든, 스레드 안전 수준을 반드시 API 설명에 포함해야 한다.

직렬화할 수 있는 클래스라면 직렬화 형태로 API 설명에 포함해야 한다. (아이템 87)

<br>

## 8. Javadoc은 메서드 주석을 상속시킬 수 있다.

문서화 주석이 없는 API 요소를 발견하면 자바독이 가장 가까운 문서화 주석을 찾아준다. 이때 상위 클래스보다 그 클래스를 구현한 **인터페이스**를 먼저 찾는다.

{@inheritDoc} 태그를 사용해 상위 타입의 문서화 주석 일부를 상속할 수 있다. 클래스는 자신이 구현한 인터페이스의 문서화 주석을 재사용할 수 있다는 뜻이다.

위 태그는 유사한 문서화 주석을 유지보수하는 부담은 줄지만, 사용하기 까다롭고 제약이 있다.
