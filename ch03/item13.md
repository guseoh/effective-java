### ✅ clone 재정의는 주의해서 진행하라
`Cloneable` 인터페이스는 복제해도 되는 클래스임을 알려주는 믹스인 인터페이스입니다. 
여기서 믹스인은 상속 구조를 가지지 않고 다른 클래스에 **원하는** 기능을 전달하는 역할

이러한 목적은 가지고 만든 인터페이스지만 가장 큰 문제점이 있습니다.

`Cloneable` 인터페이스는 복제를 허용한다는 **신호를 JVM에 보내기 위해 만들**어졌지만 정작 가장 큰 문제점을 가지고 있습니다. 
바로 자체적으로 clone 메서드를 포함하고 있지 않다는 점입니다.
대신 `Object` 클래스에 있는 `protected` 접근 제어자로 선언된 `clone` 메서드를 상속받아 사용해야 합니다. 
`protected` 접근 제어자는 같은 패키지 내의 클래스나 상속 관계에 있는 클래스에서만 접근을 허용합니다. 
따라서 `Cloneable`을 구현한 클래스의 인스턴스를 다른 패키지에서 사용하려고 하면 접근 오류가 발생하게 됩니다. 
이와 같은 문제점을 가지고 있지만 Cloneable 방식은 유용하게 쓰이고 있습니다.

```java
public class Object {
    @IntrinsicCandidate
    protected native Object clone() throws CloneNotSupportedException;

}
```

### clone 메소드 일반 규약 - 생성된 복사본 객체와 원본 객체의 관계
```java
x.clone() != x; // 복사본과 원본은 서로 다른 참조 값을 가집니다. 즉 별개의 객체라는 의미

x.clone().getClass() == x.getClass(); // 복사본과 원본은 같은 클래스 타입

x.clone().equals(x); // 복사본과 원본은 논리적으로 동등

// 이와 같은 관례상 복사된 객체와 원본 객체는 독립적이어야 합니다. 이를 만족하려면 아래와 같은 코드를 작성해서 객체를 반환합니다.
super.clone(); 
```
super.clone()을 호출하는 것과 생성자를 직접 호출하는 것은 컴파일러 오류가 발생하지 않겠지만 조금 차이가 있습니다.

clone() 메서드는 기존 객체의 상태를 그대로 복사하여 새로운 객체를 만드는 데 목적이 있습니다. 
반면 super.clone() 대신 생성자를 직접 호출하면 **상속받은 하위 클래스의 고유한 필드와 정보는 복제되지 않고 상위 클래스의 새로운 객체만 만들어지는 문제**가 발생합니다. 
따라서 기존 객체의 모든 정보를 온전히 복제하려면 super.clone()을 사용하는 것이 올바른 방법입니다.

이 문제를 좀 더 구체적으로 살펴보기 위해 간단한 예시를 살펴보겠습니다.

### 예시
클래스 A가 있고, 이를 상속받은 클래스 B가 있다고 가정
```java
class A implements Cloneable {
    @Override
    public Object clone() {
        return new A(); // new A()를 직접 호출
    }
}

class B extends A {
    // A의 clone()을 상속받아 사용
}
```
clone() 메서드를 재정의할 때 new A()와 같이 생성자를 직접 호출하면 상위 클래스인 A의 객체만 생성됩니다. 
따라서 B 클래스의 인스턴스에서 clone()을 호출하더라도 B의 고유한 정보는 복제되지 않고 A 타입의 객체만 반환됩니다. 
이는 Object.clone()의 핵심 원칙인 **복제된 객체는 원본 객체와 동일한 클래스 타입을 가져야 한다**는 규칙을 위반하게 되는 결정적인 원인입니다. 
이 때문에 객체를 올바르게 복제하려면 반드시 super.clone()을 사용해 상위 클래스까지 포함한 모든 필드를 복사해야 합니다.

단! 이런 문제는 final 클래스는 상속이 불가능하기 때문에 문제가 발생하지 않습니다.

### 올바른 부모 클래스 clone 메소드 정의 방법
```java
class A implements Cloneable {
    int value;

    @Override
    public Object clone() {
        try{
            // super.clone()을 호출하여 객체를 복사하며, 반환 타입은 Object이므로 형변환을 해야 합니다,
            return  (A) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // 논리적으로 예외가 발생하면 안되는 상황에서 해당 예외 사용
        }
    }
}
```
AssertionError 예외를 던지는 이유는 try-catch 문으로 CloneNotSupportedException 예외를 잡은 상태이므로 예외가 발생하면 안되는 상황이라고 생각해서 
AssertionError 예외를 던져서 논리적으로 예외가 발생할 수 없다는 의미를 전달 할 수 있습니다.

### 가변 객체를 필드로 선언 시 발생하는 문제
Cloneable 구현체에서 가변 객체를 필드로 선언하는 순간 또 다른 문제가 발생합니다.
```java
public class Stack implements Cloneable {
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public Stack_BadImplementation() {
        this.elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(Object e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public Object pop() {
        if (size == 0)
            throw new EmptyStackException();
        Object result = elements[--size];
        elements[size] = null; 
        return result;
    }

    private void ensureCapacity() {
        if (elements.length == size)
            elements = Arrays.copyOf(elements, 2 * size + 1);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
```
`clone() `메서드를 사용할 때 `Cloneable`을 상속받아 단순히 `super.clone()`을 호출하면 size 필드와 같은 기본 타입은 정확히 복제됩니다. 
그러나 **가변 객체**인 `elements` 필드는 복제 후에도 여전히 원본 Stack 인스턴스의 필드를 참조하는 문제가 발생합니다. 
이는 super.clone()이 기본적으로 얕은 복사를 수행하기 때문입니다. 
이러한 문제를 해결하고 가변 객체의 값을 정확히 복사하려면, clone() 메서드를 재정의하여 elements 필드에 대한 깊은 복사 로직을 직접 구현해야 합니다.

- **얕은 복사:** 객체 자체만 복사하고, 객체 내부에 있는 참조 타입 필드는 참조 주소만 복사
- **깊은 복사:** 객체 자체는 물론, 객체 내부에 있는 모든 참조 타입 필드까지 모두 복사
```java
@Override 
public Stack clone() {
    try {
        Stack result = (Stack) super.clone();
        result.elements = elements.clone(); 
        return result;
    } catch (CloneNotSupportedException e) {
        throw new AssertionError();
    }
}
```
가변 객체인 elements 필드를 따로 깊게 복사하여 처음 생성한 인스턴스 필드에 넣어주면 해결됩니다. 

해당 문제랑 다른 HashTable이나 HashMap 같은 key-value 자료 구조는 clone() 메서드를 호출할 때 문제가 발생합니다.
이러한 자료구조는 내부의 buckets 배열과 그 안의 Entry 객체들이 참조만 복사되는 얕은 복사 문제가 발생합니다. 
이로 인해 원본과 복제본이 동일한 내부 데이터 구조를 공유하게 되어, 복제본에서 데이터를 변경하면 원본까지 영향을 미치는 부작용이 생깁니다. 
이러한 문제를 해결하기 위해 깊은 복사를 직접 구현해야 하지만 clone() 메서드는 구조적으로 상속과 Cloneable 인터페이스에 의존하는 등 단점이 많아 사용을 권장하지 않습니다. 
따라서 String이나 불변 객체와 가변 객체가 단순히 혼합된 구조의 클래스와 달리 key-value 자료 구조와 같이 복잡한 내부 구조를 가진 클래스의 경우 
clone() 메서드 대신 생성자나 정적 팩토리 메서드를 사용하여 새로운 객체를 생성하는 것이 더 안전하고 좋은 방법입니다.