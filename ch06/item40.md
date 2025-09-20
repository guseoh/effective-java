### ✅ @Override 애너테이션을 일관되게 사용하라

## 0. 들어가기 전

자바가 기본적으로 제공하는 애너테이션 중 프로그래머에게 가장 중요한 것은 `@Override` 일 것이다.

`@Override` 는 메서드 선언에만 달 수 있으며, **상위 타입의 메서드를 재정의**했음을 뜻한다.

보통 IDE에서 재정의 기능을 사용하면 자동으로 `@Override` 애너테이션을 붙여준다.

<br>

## 1. @Override를 붙이지 않을 경우

```java
public class Bigram {
    private final char first;
    private final char second;

    public Bigram(char first, char second) {
        this.first = first;
        this.second = second;
    }

    public boolean equals(Bigram bigram) {
        return bigram.first == first && bigram.second == second;
    }

    public int hashCode() {
        return 31 * first + second;
    }

    public static void main(String[] args) {
        Set<Bigram> s = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            for (char ch = 'a'; ch <= 'z'; ch++) {
                s.add(new Bigram(ch, ch));
            }
        }
        System.out.println(s.size());
    }
}
```
-` Set`은 중복을 허용하지 않으니 26을 출력될 것 같지만, 실제로는 260이 출력된다.
- equals를 **‘재정의(overriding)’** 한 게 아니라 **‘다중정의(overloading, 아이템 52)**’ 해버렸다.
    - Object의 equals를 재정의하려면 매개변수 타입을 `Object`로 해야만 한다.
    - 그래서 `Object`에서 상속한 `equals`와는 별개인 `equals`를 새로 정의한 꼴
    - Object의 equals는 `==` 연산자와 똑같이 객체 식별성(identity)만을 확인한다.

<br>

## 2. @Override를 붙인 경우
```java
public class Bigram {
    private final char first;
    private final char second;

    public Bigram(char first, char second) {
        this.first = first;
        this.second = second;
    }
		@Override
    public boolean equals(Object o) {
		    if (!(o instanceof Bigram))
				    return false;
        return bigram.first == first && bigram.second == second;
    }
		
		@Override
    public int hashCode() {
        return 31 * first + second;
    }

    public static void main(String[] args) {
        Set<Bigram> s = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            for (char ch = 'a'; ch <= 'z'; ch++) {
                s.add(new Bigram(ch, ch));
            }
        }
        System.out.println(s.size());
    }
}
```
- 매개변수 object + @Override 선언
<details>
    <summary>s.add(new Bigram(ch, ch)); 과정</summary>
<div markdown="1">

예를 들어 `‘a’` 일 때:

1. `new Bigram(’a’, ‘a’)` 생성
2. `hashCode()` 호출 → `31 * ‘a’ + ‘a’ =` 고유 정수값 계산
3. `HashSet`이 해당 해시 버킷 확인 → 비어 있으면 추가

두 번째 루프에서 또 `new Bigram(’a’, ‘a’)`가 들어올 때:
1. 같은 해시코드(`31 * ‘a’ + ‘a’)`가 계산
2. HashSet 버킷 안에 이미 같은 해시코드를 가진 객체가 있는지 확인
3. `equals(Object o)` 호출 → 이미 저장된 `(’a’, ‘a’)`와 새로 만든 `(’a’, ‘a’)` 비교 → true
4. 중복으로 판단되어 추가되지 않는다.
</div>
</details>

따라서, **상위 클래스의 메서드를 재정의하려면 모든 메서드에 @Override 애너테이션을 달자.**

<br>

## 3. @Override를 일관성 있게 사용하자

- 구체 클래스에서 상위 클래스의 추상 메서드를 재정의할 때는 굳이 `@Override`를 달지 않아도 된다.
    - 구체 클래스인데 아직 구현하지 않은 추상 메서드가 남아 있다면 컴파일러가 그 사실을 바로 알려주기 때문
    - 물론 재정의 메서드 모두 `@Override`를 일괄로 붙여도 상관은 없다.
- `@Override`는 클래스뿐 아니라 인터페이스의 메서드를 재정의할 때도 사용할 수 있다.
    - 구현하려는 인터페이스에 [디폴트 메서드](https://www.baeldung.com/java-static-default-methods)가 없음을 안다면 이를  구현한 메서드에는 `@Override`를 생략해 코드를 조금 더 깔끔히 유지해도 좋다.
- 하지만 추상 클래스나 인터페이스에서는 상위 클래스나 상위 인터페이스의 메서드를 재정의하는 모든 메서드에 `@Override`를 다는 것이 좋다.
    - 상위 클래스가 구체 클래스든 추상 클래스든 마찬가지이다.
- 예를 들어, `Set` 인터페이스는 `Collection` 인터페이스를 확장했지만 새로 추가한 메서드는 없다.
    - 모든 메서드 선언에 `@Override`를 달아 실수로 추가한 메서드가 없음을 보장했다.

<br>

## 4. 정리
- 재정의한 모든 메서드에 `@Override` 애너테이션을 의식적으로 달면 실수했을 때 컴파일러가 바로 알려줄 것이다.
- 예외는 한 가지뿐이다. 구체 클래스에서 상위 클래스의 추상 메서드를 재정의한 경우엔 이 애너테이션을 달지 않아도 된다.