### ✅ 타입 안전 이종 컨테이너를 고려하라

컨테이너는 여러 데이터를 한곳에 모아 관리하는 객체로, 일반적으로 하나 또는 두 개의 타입 매개변수만 받을 수 있습니다.
예를 들어, `Set<Apple>`은 '`Apple`' 타입만 담을 수 있는 상자로 다른 타입의 데이터는 담을 수 없습니다.
마찬가지로, `Map<K, V>`는 엔트리 정보를 담기 위해 'K'(키)와 'V'(값)를 한 쌍으로 담도록 설계되었습니다.

이처럼 대부분의 컨테이너는 담을 수 있는 타입의 종류가 제한되어 있어 데이터베이스에서 이름(문자열), 나이(숫자), 성별(문자열), 생일(날짜)과 같이
여러 종류의 타입을 한 번에 담아야 할 때 문제가 발생할 수 있습니다. 왜냐하면 기존 컨테이너로는 담을 수 있는 타입의 개수가 제한되어 있기 때문입니다.

### 하나의 컨테이너에 여러 개 타입 매개변수 넣는 방법(타입 안전 이중 컨테이너) - key를 매개변수화

```java
public class Favorites {
    /**
     * key: key의 Class<?>를 사용함으로써 어떤 클래스 타입이든 상관없이 사용할 수 있음
     * value: Object은 모든 타입을 담을 수 있는 최상위 객체입니다. 
     * 그래서 컴파일 오류는 발생하지 않지만 반드시 key와 같은 타입이라는 걸 보장할 수 없습니다.
     * 이러한 시스템 설계 오류 때문에 put, get 메소드에서 타입이 일치하는지 확인해야 합니다.
     */
    private Map<Class<?>, Object> favorites = new HashMap<>();

    /**
     * 컴파일 시점에 검사
     *
     * @param type 타입 토큰이 들어오는 매개변수
     * key, value의 타입이 일치하는지 제약 조건을 코드에 작성하지 않았지만 
     * Class<T>와 T라는 두 개의 제네릭 타입 변수가 함께 사용되는 것 자체가 제약 조건 역할을 합니다.
     */
    public <T> void putFavorite(Class<T> type, T instance) {
        favorites.put(Objects.requireNonNull(type), instance);
    }

    /**
     * 런타임 시점에 검사
     *
     * cast() 메소드를 사용하여 제네릭에 들어온 타입으로 형 변환
     */
    public <T> T getFavorite(Class<T> type) {
        return type.cast(favorites.get(type));
    }

}

public static void main(String[] args) {
    Favorites f = new Favorites();

    f.putFavorites(Sring.class, "java");
    f.putFavorites(Integer.class, 0xcafebabe);
    f.putFavorites(Class.class, Favorites.class);

    String favoriteString = f.getFavorites(String.class);
    int favoriteInteger = f.getFavorites(Integer.class);
    Class<T> favoriteClass = f.getFavorites(Class.class);

    System.out.printf("%s %x %s%n", favoriteString, favoriteInteger, favoriteClass.getName());
}
```

이렇게 이중 컨테이너 패턴을 사용해도 완전히 좋은 설계가 아닙니다.
`putFavorite()` 메소드를 사용할 때 **컴파일러가 잡아주지 못하는 원시 타입(raw type)을 사용할 때 오류가 발생**합니다.

### 문제 상황 - `raw type`으로 넘겨서 타입 안전성을 우회하는 상황을 가정

```java
public class FavoritesTest {

    @Test
    void 예외_발생() {
        // given
        Favorites f = new Favorites();

        // when
        // 악의적인 클라이언트가 Class 객체를 raw type으로 넘겨서
        // 타입 안전성을 우회하는 상황을 가정
        // 이 코드는 컴파일러의 경고를 무시하고 진행됩니다.
        f.putFavorite((Class) Integer.class, "java");

        // then
        // Cannot cast java.lang.String to java.lang.Integer - ClassCastException 오류 발생
        int favoriteInteger = f.getFavorite(Integer.class);

//        assertThrows(ClassCastException.class, () -> {
//            f.getFavorite(Integer.class);
//        });
    }
}
```

<br />

### 해결(완벽히 해결이 아닌 방어?) 코드 - cast 메소드 사용

```java
public class Favorites {
    public <T> void putFavorite(Class<T> type, T instance) {
        favorites.put(Objects.requireNonNull(type), type.cast(instance));
    }
}
```

위에 내용을 정리하면 `putFavorite(Class<T> type, T instance)`으로 작성하면 컴파일러가 알아서 제네릭 타입인지 컴파일러가 검사합니다.
그러나 `raw type`으로 컴파일러를 피해 타입 안전성을 우회하는 상황에서 문제가 발생합니다.

이러한 문제를 해결하기 위해서 `putFavorite()` 메서드에 `type.cast(instance)`를 추가하면
`putFavorite((Class) Integer.class, "java")`가 호출 시 `type.cast(instance)`는 `String` 값을 `Integer`로 변환하려다 실패하고
**즉시** `ClassCastException`을 발생시킵니다.

즉, 문제를 해결할 수 없지만 문제 발생 시점(값을 넣는 시점)과 오류 발생 시점이 일치하게 만들어서 디버깅을 훨씬 쉽게 할 수 있습니다.

### 추가 TMI

이중 컨테이너에서 제네릭에 `List<String>` 같은 실체화 불가 타입은 사용할 수 없습니다.

닐 개프터가 고안한 슈퍼 타입 토큰으로 해결할 수 있지만 이 방식에서도 한계가 있습니다.

[사용 방법](https://gafter.blogspot.com/2006/12/super-type-tokens.html)  
[한계점](https://gafter.blogspot.com/2007/05/limitation-of-super-type-tokens.html)