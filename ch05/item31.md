### ✅ 한정적 와일드카드를 사용해 API 유연성을 높이라

### 용어 설명

- **와일드카드 타입:** 자바에서 와일드카드 타입은 제네릭 타입을 다룰 때 유연성을 주기 위해 사용되는 특별한 기능입니다.
  와일드카드는 다이아몬드(<>) 부호 안에 `?, ? extends T, ? super T` 형태로 사용됩니다.
- **공변성(Covariance):** 공변성은 `? extends T` 와일드카드로 구현됩니다. 이는 `T` 타입과 **그 하위 타입들을 모두 포함**하는 유연성을 제공합니다.
- **불변성(Invariance):** 불변성은 와일드카드가 없는 제네릭 타입에 해당합니다. `List<T>`처럼 **특정 타입으로 고정**되어 다른 타입과 허용하지 않습니다.

### 그럼 와일드카드 타입이 왜 필요한가?

자바에서 제네릭(Generic)을 사용할 때, `List<String>`과 `List<Object>`는 상속 관계가 아닙니다. `List<String>`이 하위 타입 `List<Object>`은 상위 타입처럼
보이지면 직접 사용할 수는 없습니다. 이런 경우 유연성을 늘리려고 와일드카드 타입을 작성해야 합니다. 주로 와일드카드 타입은 매개변수에만 사용합니다.

```java
List<String> strList = new ArrayList<>();
List<Object> objList = strList; // Incompatible types 컴파일 애러 발생

// 오류 해결 코드
List<String> strList = new ArrayList<>();
List<?> objList = strList;
```

이러한 문제를 해결하고 코드의 유연성을 높이기 위해서는 **와일드카드 타입**을 사용합니다.
와일드카드 타입은 "알 수 없는 타입"을 의미하며, 이를 사용하면 제네릭 타입 간의 관계를 유연하게 정의할 수 있습니다.

### PECS: 생산자는 `extends`, 소비자는 `super`

#### `? extends` 생성자(Producer): 불변성 문제 발생

`? extends T`는 `T`와 그 **하위 타입을 허용**하는 와일드카드입니다. 이 와일드카드는 주로 데이터를 생산(읽기)하는 역할을 할 때 사용됩니다.

```java
public class Stack<E> {
    private List<E> elements;

    // 이 메서드는 List<E> 타입만 받을 수 있어서 유연성이 부족
    public void pushAll(Iterable<E> src) {
        for (E e : src) {
            push(e);
        }
    }

    public void push(E element) {
        elements.add(element);
    }

    public E pop() {
        if (elements.isEmpty()) {
            throw new IllegalStateException("Stack is empty.");
        }
        return elements.remove(elements.size() - 1);
    }
}

public class Application {
    public static void main(String[] args) {
        Stack<Number> numberStack = new Stack<>();
        List<Integer> integerList = List.of(1, 2, 3);

        numberStack.pushAll(integerList); // 컴파일 오류 발생
    }
}
```

컴파일 오류를 일으키는 이유는 제네릭의 불변성(Invariance) 때문입니다.
자바에서는 위와 같은 이유로 `List<Integer>`와 `List<Number>`은 두 타입은 상속 관계에 있지 않으며, 서로 다른 타입으로 간주됩니다.

`Stack` 클래스의 `pushAll` 메서드는 `Iterable<E>` 타입 즉, `Stack<Number>`의 경우 정확히 `Iterable<Number>`만 인자로 받도록 정의되어 있습니다.
따라서 `Iterable<Integer>`를 전달하면 타입 불일치로 인해 오류가 발생합니다.

#### 오류 해결: `? extends T` 사용

```java
// 문제 코드
public class Stack<E> {
    private List<E> elements;

    public void pushAll(Iterable<? extends E> src) {
        for (E e : src) {
            push(e);
        }
    }

    public void push(E element) {
        elements.add(element);
    }

    public E pop() {
        if (elements.isEmpty()) {
            throw new IllegalStateException("Stack is empty.");
        }
        return elements.remove(elements.size() - 1);
    }

}

public class Application {
    public static void main(String[] args) {
        Stack<Number> numberStack = new Stack<>();
        List<Integer> integerList = List.of(1, 2, 3);

        numberStack.pushAll(integerList);
    }
}
```

`pushAll` 메서드의 매개변수를 `Iterable<E>`에서 `Iterable<? extends E>`로 변경함으로써 컴파일 오류가 해결되었습니다.
이 와일드카드는 **`E` 또는 `E`의 하위 타입을 허용**하여, `Stack<Number>`에 `List<Integer>`를 전달하는 것을 가능하게 합니다.

#### `? super` 소비자(Consumer) 사용 예시

`? super T`는 `T`와 그 **상위 타입을 허용**하는 와일드카드입니다. 이 와일드카드는 주로 데이터를 소비(추가)하는 역할을 할 때 사용됩니다.

```java

class Fruit {
}

class Apple extends Fruit {
}

public class SuperExample {

    public static void addApple(List<? super Apple> apples) {
        apples.add(new Apple());
    }

    public static void main(String[] args) {
        List<Fruit> fruits = new ArrayList<>();
        List<Object> objects = new ArrayList<>();

        addApple(fruits);
        addApple(objects);
    }
}
```

이처럼 `addApple` 메서드는 `List<? super Apple>`을 매개변수로 사용했기 때문에 `List<Apple>`뿐만 아니라 `List<Fruit>`나 `List<Object>`처럼 `Apple`의
상위 타입을 갖는 리스트에도 `Apple` 객체를 안전하게 추가할 수 있습니다.

### 결론

- 데이터를 읽어오는(생산자) 컬렉션에는 **extends**를 사용하는게 좋습니다. <? extends T>은 T와 그 하위 타입의 데이터를 안전하게 꺼내 읽을 수 있습니다.
- 데이터를 추가하는(소비자) 컬렉션에는 **super**를 사용하는게 좋습니다. List<? super T>은 T와 그 상위 타입의 데이터를 안전하게 추가할 수 있습니다.


