### ✅ 비트 필드 대신 EnumSet을 사용하라

## 1. 비트 필드(bit field)

열거한 값들이 집합으로 사용될 경우, 예전에는 각 상수에는 서로 다른 2의 거듭제곱 값을 할당한 정수 열거 패턴을 사용해왔다.

<details>
    <summary>비트 필드 열거 상수 - 구닥다리 기법</summary>
<div markdown="1">

```java
public class Text {
    public static final int BOLD = 1 << 0;           // 1
    public static final int ITALIC = 1 << 1;         // 2
    public static final int UNDERLINE = 1 << 2;      // 4
    public static final int STRIKETHROUGH = 1 << 3;  // 8

    public void applyStyles(int styles) {
        // ...
    }
}

// 여러 스타일을 동시에 적용
int mystyles = Text.BOLD | Text.ITALIC
text.applyStyles(mystyles);

// 스타일 확인
if ((mystyles & Text.BOLD) != 0) {
		System.out.println("Bold 적용됨");
}
```
</div>
</details>

비트 필드는 컴퓨터 과학 초기에 개발된 **메모리 절약용 도구**이다.
여러 참/거짓(boolean) 옵션을 **한 바이트에 묶어 저장함**으로써 메모리를 절약한다.

하지만 비트필드는 정수 열거 상수의 단점을 그래도 지니며, 여러 문제까지 있다.

- 비트 필드 값이 그대로 출력되면 단순한 정수 열거 상수를 출력할 때보다 해석하기가 훨씬 어렵다.
- 비트 필드 하나에 녹아 있는 모든 원소를 순회하기도 까다롭다.
- 최대 몇 비트가 필요한지를 API 작성 시 미리 예측하여 적절한 타입을 선택해야 한다.
    - API를 수정하지 않고는 비트 수(32비트 or 64 비트)를 더 늘릴 수 없기 때문

<br>

## 2. EnumSet 클래스

- `java.util` 패키지의 `EnumSet` 클래스는 열거 타입 상수의 값으로 구성된 집합을 효과적으로 표현해준다.
- `Set` 인터페이스를 구현하고 `AbstarctSet`을 상속한다.
- `EnumSet`의 내부는 비트 벡터로 구현되어 원소가 총 64개 이하라면 대부분의 경우 `EnumSet` 전체를 long 변수 하나로 표현하여 비트 필드에 비견되는 성능을 보여준다.
- `removeAll`과 `retainAll` 같은 대량 작업은 비트를 효율적으로 처리할 수 있는 산술 연산을 써서 구현했다.
- `EnumSet` 은 집합 생성 등 다양한 기능의 정적 팩터리를 제공한다.
    ```java
    text.applyStyles(EnumSet.of(Style.BOLD, Style.ITALIC)); 
    ```
<details>
    <summary>EnumSet - 비트 필드를 대체하는 현대적 기법</summary>
<div markdown="1">

```java
public class Text {
		public enum Style {BOLD, ITALIC, UNDERLINE, STRIKETHROUGH }
		
		// 어떤 Set을 넘겨도 되나, EnumSet이 가장 좋다.
		public void applyStyles(Set<Style> styles) { ... }
}
```

- `Set<Style>` 을 받는 이유는 모든 클라이언트가 `EnumSet`을 건네리라 짐작되는 상황이라도 이왕이면 인터페이스를 받는 게 좋은 습관이다.
</div>
</details>

<details>
    <summary>참고</summary>
<div markdown="1">

원소가 64개 이하라면 `ReqularEnumSet`, 65개 이상이면 `JumboEnumSet`을 사용한다.

### RegularEnumSet

- `ReqularEnumSet`은 비트 벡터를 표현하기 위해 **단일 long 자료형**을 사용한다.
- long의 각 비트는 enum 값을 나타내면, 64 비트의 데이터이기 때문에 64개의 원소를 저장할수 있다.

```java
class RegularEnumSet<E extends Enum<E>> extends EnumSet<E> {
    private static final long serialVersionUID = 3411599620347842686L;

    private long elements = 0L;

    void addAll() {
        if (universe.length != 0)
            elements = -1L >>> -universe.length;
    }

    void complement() {
        if (universe.length != 0) {
            elements = ~elements;
            elements &= -1L >>> -universe.length;  // Mask unused bits
        }
    }

    // ...
}
```

### JumboEnumSet
- `JumboEnumSet`은 **long 요소의 배열**을 비트 벡터로 사용한다.
- 64개 이상의 원소를 저장한다.
- 저장된 배열 인덱스를 찾기 위해 몇 가지 추가 연산을 수행한다.

```java
class JumboEnumSet<E extends Enum<E>> extends EnumSet<E> {
    private static final long serialVersionUID = 334349849919042784L;

    private long elements[];

    private int size = 0;

    JumboEnumSet(Class<E>elementType, Enum<?>[] universe) {
        super(elementType, universe);
        elements = new long[(universe.length + 63) >>> 6];
    }

    // ...
}
```
</div>
</details>

EnumSet의 유일한 단점은 불변 EnumSet을 만들 수 없다는 것이다. 자바 11까지도 제공하지 않고 있으며, 어찌저찌 구현은 할 수 있지만 명확성과 성능 저하를 감수해야 한다.