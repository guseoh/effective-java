### ✅ 자원을 직접 명시하지 말고 의존 객체 주입을 사용하라

많은 클래스 하나 이상의 자원(객체, 클래스, 인터페이스)에 의존합니다.

```java
public class SpellChecker {
    private static final Lexicon dictionary = new Lexicon();
}
```

위에 코드를 보면 `SpellChecker` 클래스는 `dictionary` 객체가 한 번 생성되고 `Lexicon` 구현체에 의존하게 됩니다.
즉 이러한 강한 의존성은 `SpellChecker`는 `Lexicon`이라는 특정 구현체에 의존하기 때문에 만약 `Lexicon`가 아닌 다른 것으로 교체하려면 `Lexicon` 관련 코드를 수정해야 됩니다.

이런 의존성 높은 설계를 낮게 해주는게 의존 객체 주입입니다.

```java
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    public AccountService(AccountRepository accountRepository, EmailService emailService, PasswordEncoder passwordEncoder, AppProperties appProperties) {
        this.accountRepository = Objects.requireNonNull(accountRepository);
        this.emailService = Objects.requireNonNull(emailService);
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder);
        this.appProperties = Objects.requireNonNull(appProperties);
    }
}
```

`AccountService` 객체가 생성될 때 `AccountRepository` 타입의 객체는 외부로부터 주입받습니다.
즉 `AccountService`는 이 `accountRepository` 객체를 직접 생성하지 않고 전달받아 사용합니다.
이렇게 주입받은 `accountRepository` 객체의 메서드를 호출할 때
`accountRepository` 변수에 실제로 어떤 구현체(ex. JpaAccountRepository 또는 InMemoryAccountRepository)가
들어있는지는 전혀 알지 못하며 알 필요도 없습니다.
단지 `AccountRepository` 구현체인 메소드를 호출합니다.

결론 자바의 특징인 다형성과 생성자를 이용해 의존성을 줄일 수 있다.

이런 패턴을 팩토리 메소드 패턴을 사용하여 사용할 수 도 있지만

<details>
    <summary>팩터리 메서드 패턴</summary>
<div markdown="1">

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

</div>
</details>

자바 8에 도입된 `Supplier` 인터페이스는 팩토리 메소드 패턴을 활용하여 주입 관련 로직을 구성에 대해 다양한 기능을 제공합니다.(리소스 지연 관리 등등)

```java
Mosaic create(Supplier<? extends Tile> tileFactory) {..}
```

### 정리

* 클래스가 내부적으로 하나 이상의 자원(클래스, 인터페이스)에 의존하고, 그 자원이 클래스 동작에 영향을 준다면 싱글턴과 정적 유틸리티 클래스는 사용하지 말자.
* 이 자원들을 클래스가 직접 만들게 해서도 안된다.
* 필요한 자원을 (혹은 그 자원을 만들어주는 팩터리를) 생성자에 (혹은 정적 팩터리나 빌더에) 넘겨주자. 의존 객체 주입이라 하는 이 기법은 클래스의 유연성, 재사용성, 테스트 용이성을 기막히게 개선해준다.