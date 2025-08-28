### ✅ 이왕이면 제네릭 타입으로 만들라

**제네릭 전용 전**

```java
public class MyStack {
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public MyStack() {
        elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(Object e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public Object pop() {
        if (size == 0) throw new EmptyStackException();
        Object result = elements[--size];
        elements[size] = null;
        return result;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void ensureCapacity() {
        if (elements.length == size) {
            elements = Arrays.copyOf(elements, 2 * size + 1);
        }
    }
}

@Test
void problem() {

    MyStack stack = new MyStack();

    stack.push(1); // 정수형 추가
    stack.push("1"); // 문자열 추가

    Assertions.assertEquals(String.class, stack.pop().getClass());
    Assertions.assertEquals(String.class, stack.pop().getClass());
}
```

`MyStack` 클래스는 여러 타입의 객체를 다룰 수 있지만 타입 안전성이라는 심각한 문제를 내포하고 있습니다.
`Object` **배열**을 사용하기 때문에 정수, 문자열 등 어떤 객체든 `push`할 수 있지만 이는 컴파일러가 타입 오류를 미리 잡아내지 못하게 합니다.

그 결과 테스트 코드에서처럼 `pop` 메서드가 `Integer` 객체를 반환하는 상황에서 `String` 타입으로 예상하고 비교하는 경우
컴파일 시에는 문제가 없지만 런타임 오류가 발생하는 심각한 오류가 발생합니다.

이 문제를 해결하고 개발 초기 단계에 오류를 방지하려면 **제네릭(Generics)**을 사용해야 합니다.
제네릭을 사용하면 `MyStack<E>`와 같이 스택이 다룰 타입을 명확히 지정할 수 있고 이로 인해 컴파일러가 `push`나 `pop` 같은 메서드 호출 시 타입 불일치를 컴파일 오류로 잡아낼 수 있습니다.

**제네릭 전용 후**

```java
public class MyStack<E> {
    private E[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public MyStack() {
        elements = new E[DEFAULT_INITIAL_CAPACITY]; // 오류 발생
    }

    public void push(E e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public E pop() {
        if (size == 0) {
            throw new EmptyStackException();
        }

        E result = elements[--size];
        elements[size] = null;
        return result;
    }

    private void ensureCapacity() {
        if (elements.length == size) {
            elements = Arrays.copyOf(elements, 2 * size + 1);
        }
    }

}
```

제네릭을 적용할 때 일반적으로 오류나 경고가 하나 이상 발생하는데 해당 예시에서는 제네릭 클래스에서`E[] elements = new E[DEFAULT_INITIAL_CAPACITY]`와 같이 직접 제네릭 타입의
배열을 생성하는 것은 불가능합니다.
이는 런타임에 타입 정보가 지워지는 [자바의 제네릭 구현 방식(Type Erasure)](https://stackoverflow.com/questions/520527/why-do-some-claim-that-javas-implementation-of-generics-is-bad?utm_source=chatgpt.com) 때문입니다.

아래 사진과 같은 하나의 컴파일 오류만 발생했습니다.
<img width="761" height="127" alt="Image" src="https://github.com/user-attachments/assets/5ba89c5f-39a4-4d78-bfc4-28e023af2811" />

### Type parameter 'E' cannot be instantiated directly 해결 방법

1. 제네릭 배열 생성을 금지하는 제약을 대놓고 우회 - Object 배열을 생성한 후 E 배열로 형변환하는 방법 사용

```java
public class MyStack<E> {
    private E[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public MyStack() {
        elements = (E[]) new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(E e) {
        ensureCapacity();
        elements[size++] = e;
    }
    // ...
}
```

이 코드는 컴파일러 오류 대신 `Unchecked cast` 경고를 발생시킵니다.
이 경고는 **컴파일러가 타입 안전성을 보장할 수 없으니 개발자가 직접 확인해야 한다**는 의미입니다.
즉 완벽한 설계는 아니지만 해당 예시에서는 경고를 무시해도 안전합니다.

왜냐하면 `elements` 배열이 `private` 필드에 저장되고 외부로 노출되지 않기 때문입니다.
**`push` 메서드를 통해 배열에 저장되는 모든 원소의 타입은 항상 `E`로 보장**됩니다.
따라서 이 형변환은 안전하다고 확신할 수 있으며, `@SuppressWarnings("unchecked")` 애노테이션을 사용하여 경고를 숨길 수 있습니다.

2. elements 필드의 타입을 E[] -> Object[]로 변경

```java
public class MyStack<E> {
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public MyStack() {
        elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(E e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public E pop() {
        if (size == 0) {
            throw new EmptyStackException();
        }

        E result = elements[--size]; // Unchecked cast: 'java.lang.Object' to 'E' 오류 발생
        elements[size] = null;
        return result;
    }

    private void ensureCapacity() {
        if (elements.length == size) {
            elements = Arrays.copyOf(elements, 2 * size + 1);
        }
    }

}

// 해결 코드
public class MyStack<E> {
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public MyStack() {
        elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(E e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public E pop() {
        if (size == 0) {
            throw new EmptyStackException();
        }

        // Object를 E 타입으로 형변환
        @SuppressWarnings("unchecked")
        E result = (E) elements[--size];
        elements[size] = null;
        return result;
    }

    // ... 나머지 코드
}

```

`pop` 메서드에서 `elements[--size]`는 `Object` 타입이므로, 이를 E 타입 변수 `result`에 할당하려면 형변환이 필요합니다.
이 경우에도 `Unchecked cast` 경고가 발생하지만, `push` 메서드가 `E` 타입 객체만 저장하도록 보장하므로 안전합니다.
이 방법은 내부적으로 `Object` 배열을 사용하고 외부 인터페이스에서는 제네릭 타입 `E`를 유지하여 타입 안전성을 확보합니다.

### 결론

제네릭 타입의 배열을 사용하는 두 가지 방법은 각각 장단점이 있습니다.

첫 번째 방법은 `Object` 배열을 `E` 배열로 형변환하는 방식입니다.
이 방식은 **배열 생성 시 단 한 번만 형변환을 실행**하므로, 성능 면에서는 더 유리할 수 있습니다.
그러나 배열의 **런타임 타입(실제 배열 타입)**이 컴파일 타임 타입과 달라지는 **힙 오염(item32에서 나오는 내용)**을 일으킬 수 있다는 잠재적인 위험이 있습니다.
물론, 제공된 `MyStack` 예시처럼 배열이 외부로 노출되지 않는다면 타입 안전성에는 문제가 없지만 일반적인 상황에서는 주의가 필요합니다.

두 번째 방법은 필드 자체를 `Object` 배열로 유지하는 방식입니다.
이 방식은 `pop` 메서드에서 매번 `Object`를 `E`로 형변환해야 합니다.
성능상으로는 첫 번째 방법보단 안 좋을 순 있지만, 힙 오염을 일으키지 않는다는 장점이 있습니다.
내부적으로는 `Object` 배열을 사용하더라도, 외부 인터페이스는 제네릭 타입 `E`를 유지함으로써 타입 안전성을 완벽하게 보장합니다.

두 방법 모두 장단점이 있으므로, 상황에 따라 더 적합한 방식을 선택해야 합니다.