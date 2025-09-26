### ✅ toString을 항상 재정의하라

## 1. toString() 메서드


`Object` 내에 속한 기본 `toString` 메서드는 우리가 작성한 클래스에 적합한 문자열을 반환하는 경우가 거의 없다.

`PhoneNumber@adbbd` 처럼 단순히 **클래스_이름@16진수로_표시한_해시코드** 를 반환한다.

`toString`의 일반 규약에 따르면 **간결하면서 사람이 읽기 쉬운 형태의 유익한 정보** 를 반환해야 한다.

또한 "모든 하위 클래스에서 이 메서드를 재정의하라"고 한다.

`toString` 을 잘 구현한 클래스는 사용하기에 훨씬 즐겁고, 그 클래스를 사용한 시스템은 디버깅 하기 쉽다.

객체를 println, printf, 문자열 연결 연산자(+), assert 구문에 넘길 때 혹은 디버거가 객체를 출력할 때 자동으로 불린다.

## 2. 좋은 toString() 작성 요령


**1. 객체가 가진 주요 정보를 모두를 반환하는 게 좋다.**

하지만 객체가 거대하거나 객체의 상태가 문자열로 표현하기에 적합 하지 않다면 무리가 있다. 

이런 상황일 경우 "맨해튼 거주자 전화번호부(총 1489개)" 같은 요약 정보를 담아야 한다.

**2. 값 클래스라면 toString() 포맷을 문서에 명시하는 것이 좋다.**

포맷을 명시하면 그 객체는 표준적이고, 명확하고, 사람이 읽을 수 있게 된다.

따라서 포맷을 명시하기로 했다면, 명시한 포맷에 맞는 문자열과 객체를 정적 팩터리나 생성자를 제공하는 것이 좋다.

```java
/**
 * 이 전화번호의 문자열 표현을 반환한다.
 * 이 문자열은 "XXX-YYY-ZZZZ" 형태의 12글자로 구성된다.
 * XXX는 지역 코드, YYY는 프리픽스, ZZZZ는 가입자 번호이다.
 */
@Override public String toString(){
    return String.format("%03d-%03d-%04d", areaCode, prefix, lineNum);
}
```

**3. toString이 반환한 값에 포함된 정보를 얻어올 수 있는 API를 제공하자.**

예를 들어, PhoneNumber 클래스는 지역 코드, 프리픽스, 가입자 번호용 접근자를 제공해야 한다.

그렇지 않으면 이 정보가 필요한 프로그래머는 `toString`의 반환값을 파싱할 수 밖에 없다.
```java
// 잘못된 예: toString()만 정보 제공
public class User {
    private final String name;
    private final int age;

    @Override
    public String toString() {
        return "User[name=" + name + ", age=" + age + "]";
    }
}

String s = user.toString(); // "User[name=Alice, age=30]"
String name = s.split("=")[1].split(",")[0]; // 위험한 방식
```
```java
// 올바른 예: toString() + getter 제공
public class User {
    private final String name;
    private final int age;

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    @Override
    public String toString() {
        return "User[name=" + name + ", age=" + age + "]";
    }
}

String name = user.getName(); // toString 대신 공식 API로 접근
```

## 3. toString 재정의가 필요 없는 경우


정적 유틸리티 클래스는 상태를 가지지 않고, 인스턴스를 만들 수 없으므로 `toString`을 제공할 이유가 없다.

또한, 대부분의 열거 타입도 자바가 이미 완벽한 `toString`을 제공하니 따로 재정의하지 않아도 된다.

하지만 히위 클래스들이 공유해야 할 문자열 표현이 있는 추상 클래스라면 코드 재사용성과 일관된 출력 형식을 위해 `toString`을 재정의해줘야 한다.

## 4. 정리


1. 모든 구체 클래스에서 `Object`의 `toString`을 재정의하자.
    - 상위 클래스에서 이미 재정의한 경우 예외
2. `toString`을 재정의한 클래스는 사용하기도 즐겁고, 그 클래스를 사용한 시스템을 디버깅하기 쉽게 해준다.
3. `toString`은 해당 객체에 관한 명확하고 유용한 저옵를 읽기 좋은 형태로 반환해야 한다.

### 번외 1. [AutoValue](https://www.baeldung.com/introduction-to-autovalue)


- `AutoValue` 는 Google 에서 개발한 코드 자동 생성기로 자바 코드를 줄이는 데 기여한다.
- Reflection 방식을 이용한 런타임 방식이 아닌 apt를 이용하여 컴파일 타임에 코드를 생성한다.
- MVN Repository 에서 AutoValue.jar 파일을 다운로드 후 사용한다.
- `@AutoValue` 어노테이션을 사용하여 코드를 작성할 수 있다.

<details>
    <summary>AutoValue와 코드 생성 방식</summary>
<div markdown="1">

`AutoValue`는 불변 값 객체 생성 도구로, 반복적인 `equals()`, `hashCode()`,` toString()` 등의 메서드를 자동으로 생성해주는
annotation processor 기반의 라이브러리이다.

AutoValue는 `@AutoValue`를 붙인 클래스르 작성하면, 컴파일 타임(Annotation Processing Time, apt)에 관련된
구현 클래스 (AutoValue_클래스이름)를 자동으로 생성한다.
```java
@AutoValue
abstract class Person {
    abstract String name();
    abstract int age();

    static Person create(String name, int age) {
        return new AutoValue_Person(name, age); // 자동 생성 클래스
    }
}
```
</div>
</details>

### 번외 2. [Lombok @ToString](https://www.baeldung.com/lombok-tostring)

- 자바에서 반복되는 소스 코드를 컴파일 과정에서 생성해주는 라이브러리이다.
- `@ToString` 어노테이션을 통해 코드를 작성할 수 있다.