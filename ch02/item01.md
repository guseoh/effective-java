### ✅생성자 대신 정적 팩토리 메서드를 고려하라

> **장점1 : 이름을 가질 수 있다**

* 생성자에 넘기는 매개변수와 생성자 자체만으로는 반환될 객체의 특성을 제대로 설명하지 못합니다.
* 반면 정적 팩토리는 **이름만 잘 지으면 반환될 객체의 특성을 쉽게 묘사**할 수 있습니다.
* 만약 한 클래스에 시그니처가 같은 생성자가 여러 개 필요할 것 같다면 생성자를 정적 팩토리 메서드로 바꾸고 각각의 차이를 잘 드러내는 이름을 지어주자.
* 정적 팩토리 메서드를 사용하면 메서드 이름이 다르기 때문에, 매개변수의 타입과 개수가 같은 생성자가 여러 개 생성할 수 있습니다.
* 기본 생성자를 private으로 선언해 외부에서 직접 호출하여 객체를 생성하는 것을 막을 수 있습니다.

```java
public class Book2 {

    private String name;
    private String author;
    private String publisher;

    // private 이용하여 외부에서 생성자 호출 차단
    private Book2(String name) {
        this.name = name;
    }

    // 정적 팩터리 메소드
    public static Book2 createBookByName(String name) {
        return new Book2(name);
    }

}

```

정적 팩토리 메서드는 클래스의 인스턴스를 반환하는 정적(static) 메서드를 통해 생성자를 간접적으로 호출하여 객체를 생성하는 디자인 패턴  
<br>

> **장점2 : 호출할 때마다 인스턴스를 새로 생성하지 않아도 된다.**

* 객체 내부에 미리 정의된 `static final` 상수를 반환하기 때문에 매번 새로운 객체를 생성하지 않습니다.
* 상수를 이용하기 때문에 이미 고정된 값을 사용해야 합니다.

```java

@jdk.internal.ValueBased
public final class Boolean implements java.io.Serializable, Comparable<Boolean>, Constable {

    public static final Boolean TRUE = new Boolean(true);
    public static final Boolean FALSE = new Boolean(false);

    @IntrinsicCandidate
    public static Boolean valueOf(boolean b) {
        return (b ? TRUE : FALSE);
    }
}
```

<br>

> **장점3 : 반환 타입의 하위 타입 객체를 반환할 수 있다.**

* 구현체인 하위 객체를 사용하지 않고 상위 객체(인터페이스 or 추상 클래스)만으로 API를 사용할 수 있습니다.
* 단! package-private를 이용하기 때문에 같은 패키지에서 작성해야 합니다.

```java
// API 제공 시 사용자한테 노출되는 객체(인터페이스)
public interface Book {
    String getName();

    String getAuthor();

    String getType();

    void read();
}

// API 제공 시 사용자한테 노출되는 않은 객체(클래스)
class BasicBook implements Book {
    private String name;
    private String author;

    // 접근 제어자가를 따로 입력하지 않은 package-private 접근 제어자를 사용하기 때문에 같은 패키지에서만 사용 가능
    BasicBook(String name, String author) {
        this.name = name;
        this.author = author;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public String getType() {
        return "일반 도서";
    }

    @Override
    public void read() {
        System.out.println(getType() + " '" + name + "'를 읽습니다.");
    }


}

// API 제공 시 사용자한테 노출되는 않은 객체(클래스)
class EBook implements Book {
    private String name;
    private String author;
    private String format; // PDF

    EBook(String name, String author, String format) {
        this.name = name;
        this.author = author;
        this.format = format;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public String getType() {
        return "전자책 (" + format + ")";
    }

    @Override
    public void read() {
        System.out.println(getType() + " '" + name + "'를 읽습니다.");
    }
}

// 정적 팩토리 메소드를 제공하는 객체(사용자한테 노출)
public class BookFactory {

    // 정적 팩토리 메소드1
    public static Book createBasicBook(String title, String author) {
        // 내부적으로 PhysicalBook 클래스의 인스턴스를 생성하여 반환
        return new BasicBook(title, author);
    }

    // 정적 팩토리 메소드2
    public static Book createEBook(String title, String author, String format) {
        return new EBook(title, author, format);
    }
}

// API 사용 예시
public class ApiTest {
    public static void main(String[] args) {
        // 정적 팩토리 메서드를 사용하여 객체 생성 (인터페이스, 정적 메소드 클래스만 노출)
        Book basicBook = BookFactory.createPhysicalBook("모던 자바 인 액션", "라울 게마");
        Book eBook = BookFactory.createEBook("이펙티브 자바 3판", "조슈아 블로치", "PDF");


        System.out.println("--- 생성된 책 정보 ---");
        System.out.println("1. 제목: " + basicBook.getTitle() + ", 타입: " + basicBook.getType());
        basicBook.read();
        System.out.println("2. 제목: " + eBook.getTitle() + ", 타입: " + eBook.getType());
        eBook.read();

    }
}
```

<br>

> **장점4 : 입력 매개변수에 따라 매번 다른 클래스의 객체를 반환할 수 있다.**

* 조건문을 사용하여 RandomAccess의 인스턴스 여부에 따라 동적으로 클래스의 객체를 반환합니다.

```java
public class Collections {

    // 생성자를 private 접근 제어자로 막아 외부에서의 사용 방지
    private Collections() {

    }

    // 정적 팩토리 메서드
    public static <T> List<T> unmodifiableList(List<? extends T> list) {

        if (list.getClass() == UnmodifiableList.class || list.getClass() == UnmodifiableRandomAccessList.class) {
            return (List<T>) list;
        }
        // 매개변수 list가 RandomAccess 인터페이스면 true 반환, 아니면 false 반환으로 구현체를 동적으로 변경 가능
        return (list instanceof RandomAccess ?
                new UnmodifiableRandomAccessList<>(list) :
                new UnmodifiableList<>(list));
    }
}
```

<br>

> **장점5 : 정적 팩터리 메서드를 작성하는 시점에는 반환할 객체의 클래스가 존재하지 않아도 된다.**

* 서비스 제공자 프레임워크(SPF) : 확장하고 싶은 기능만 코드를 작성해서 사용하는 Java Application을 만드는 패턴
* 대표적인 예시로 JDBC(Java Database Connectivity) 서비스 제공자 프레임워크가 있습니다.
  <br></br>

* 서비스 제공자 프레임워크(JDBC)의 구성 요소
    * 서비스 인터페이스 : 고객이 사용하는 서비스 구현체의 동작을 정의(인터페이스, 추상 클래스)
    * 서비스 등록 API : 구현체를 등록할 때 사용
    * 서비스 접근 API : 서비스의 인스턴스를 얻을 때 사용
    * 서비스 제공자 인터페이스 : 필수 구성 요소가 아닌 종종 사용하는 컴포넌트로 서비스 인터페이스의 인스턴스를 생성하는 팩토리 객체

```java
import java.sql.Driver; // 서비스 제공자 인터페이스

public class JDBCTest {

    public static void main(String[] args) {
        Connection conn = null; // 서비스 인터페이스

        try {
            Class.forName("org.sqlite.JDBC"); // 서비스 등록 API
            conn = DriverManager.getConnection("jdbc:sqlite:sample.db"); // 서비스 접근 API (정적 팩터리 메소드)
            Statement statement = connection.createStatement(); // 서비스 인터페이스

            // ...
        }
    }
}
```

일반적인 객체 생성 방식은 `Connection conn = new xxx()`처럼 `new` 키워드 뒤에 반환할 구체적인 클래스 이름 작성해야 합니다.
하지만 `DriverManager.getConnection()`를 보면 호출하는 시점에 반환될 객체의 구체적인 클래스 타입을 정의하지 않을 걸 볼 수 있습니다.
이는 `conn`변수에 어떤 구현 객체가 들어올 지 동적으로 결정할 수 있게 해줍니다.

<br>

> **단점1 : 상속을 하려면 public이나 protected 생성자가 필요한데 정적 팩토리 메서드만 제공하면 하위 클래스를 만들 수 없다.**

```java
public class Book2 {

    private String name;
    private String author;
    private String publisher;

    // private 이용하여 외부에서 생성자 호출 차단
    private Book2(String name, String author, String publisher) {
        this.name = name;
        this.author = author;
        this.publisher = publisher;
    }

    // 정적 팩터리 메소드
    public static Book2 createBookByName(String name) {
        return new Book2(name, null, null);
    }


    // 정적 팩토리 메소드
    public static Book2 createBookByAuthor(String author) {
        return new Book2(null, author, null);
    }

}
```

```java
public class Fiction extends Book2 {

    private String original;

    public Fiction(String name, String author, String publisher, String original) {
        // super(...) 호출 불가 즉, Book2의 필드를 정의 할 수 없습니다.
        this.original = original;
    }
}
```

* 정적 팩토리 메서드를 사용하면 보통 생성자에 `private` or `package-private`를 사용합니다.
* 위에 코드처럼 `Book2`에 `public` or `protected` 생성자가 존재하지 않으면 하위 클래스에서 부모 클래스의 생성자에 접근할 수 없게 되어 상속이 제한됩니다.

<br>

> **단점2 : 정적 팩토리 메서드는 프로그래머가 찾기 어렵다.**

* 좋은 메소드 네이밍 규칙이 있으면 문제없지만 정적 팩토리 메서드 이름을 불규칙하게 지으면 유지보수가 어려워질 수 있습니다.
* 그래서 아래와 같은 정적 팩토리 메서드의 네이밍 규칙을 제공합니다.  
  <br>
* `from`: 매개변수를 하나 받아서 해당 타입의 인스턴스를 반환하는 형변환 메서드
* `of`: 여러 매개변수를 받아 적합한 타입의 인스턴스를 반환하는 집계 메서드
* `valueOf` : `from`과 `of`의 더 자세한 버전
* `instance` or `getInstance` : 매개변수로 명시한 인스턴스를 반환하지만, 같은 인스턴스임을 보장하지는 않는다.
* `create` or `newInstance` : instance 혹은 getInstance와 같으나 매번 새로운 인스턴스를 생성해 반환함을 보장한다.
* `getType` : `getInstance`와 같으나 생성할 클래스가 아닌 다른 클래스에 팩토리 메서드를 정의할 때 사용한다.
* `newType` : `newInstance`와 같으나 생성할 클래스가 아닌 다른 클래스에 팩토리 메서드를 정의할 때 사용한다.
* `type` : `getType`과 `newType`의 간결한 버전