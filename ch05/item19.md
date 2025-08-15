### ✅ 상속을 고려해 설계하고 문서화하라. 그러지 않았다면 상속을 금지하라

상속은 좋은 기능이지만 오버라이딩한 메소드를 문서화하지 않으면 예상치 못한 문제를 일으킬 수 있습니다.

### 상속을 고려한 설계 시 문서화 핵심

상속용 클래스를 설계하고 문서화할 때 가장 중요한 원칙은 **`메서드 재정의 시 내부적으로 어떤 일이 일어나는지 명확하게 문서로 남기는 것`** 입니다.
이는 상속을 통해 복잡한 구조의 메소드 흐름을 파악하기 위함입니다.

- **재정의할 수 있는 메소드 정의:** 재정의는 오버라이딩이랑 같은 의미로 public, protected로 선언된 메소드 중 final이 아닌 모든 메소드를 뜻하며, 모든 상황을 문서로 남겨야 합니다.
- **메소드 호출 순서 명시:** `public` 메서드가 내부적으로 어떤 다른 메서드를 호출하는지 그리고 그 호출 순서는 어떻게 되는지 상세히 작성해야 합니다. 이를 통해 재정의할 때 어떤 영향을 미치는지 예측할
  수 있습니다.

### @implSpec

`@implSpec` 태그는 메서드의 **구현 요구사항(implementation requirements)** 을 설명하는 데 사용됩니다.
즉, 해당 메서드가 내부적으로 어떻게 동작하는지 어떤 다른 메서드를 호출하는지 등을 상세히 기술하는 용도로 사용합니다.

`AbstractCollection의 remove 메소드 예시`

```java
public abstract class AbstractCollection<E> implements Collection<E> {

    /**
     * {@inheritDoc}
     *
     * @implSpec
     * This implementation iterates over the collection looking for the
     * specified element.  If it finds the element, it removes the element
     * from the collection using the iterator's remove method.
     *
     * <p>Note that this implementation throws an
     * {@code UnsupportedOperationException} if the iterator returned by this
     * collection's iterator method does not implement the {@code remove}
     * method and this collection contains the specified object.
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     */
    public boolean remove(Object o) {
        Iterator<E> it = iterator();
        if (o == null) {
            while (it.hasNext()) {
                if (it.next() == null) {
                    it.remove();
                    return true;
                }
            }
        } else {
            while (it.hasNext()) {
                if (o.equals(it.next())) {
                    it.remove();
                    return true;
                }
            }
        }
        return false;
    }
}
```

`@implSpec` 내의 설명을 보면 `remove` 메서드 내부적으로 **iterator()** 와 it.remove() 메서드를 호출한다는 사실을 명확히 알 수 있습니다.
만약 하위 클래스에서 `iterator()` 메서드를 재정의한다면 `remove()` 메서드의 동작에도 영향을 미칠 수 있음을 예측할 수 있습니다.
또한 `iterator`가 `remove` 메서드를 지원하지 않을 경우 **UnsupportedOperationException**이 발생할 수 있다는 경고도 함께 제공되어 하위 클래스 작성자가 오류를 미리 인지하고
대비할 수 있게 도와줍니다.

추가적으로 @implSpec 태그는 자바 8에서 처음 도입되어 자바 11에서는 선택사항으로 남겨져 있습니다.  
`-tag "implSpec:a:Implementation Requirements:"`로 지정

### 상속을 고려한 설계 방법

1. 클래스의 내부 동작 과정 중간에 끼어들 수 있는 훅(hook)을 잘 선별하여 `protected` 메소드 형태로 공개해야 할 수도 있습니다.

```java
public abstract class AbstractList<E> extends AbstractCollection<E> implements List<E> {

    /**
     * Removes from this list all of the elements whose index is between
     * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.
     * Shifts any succeeding elements to the left (reduces their index).
     * This call shortens the list by {@code (toIndex - fromIndex)} elements.
     * (If {@code toIndex==fromIndex}, this operation has no effect.)
     *
     * <p>This method is called by the {@code clear} operation on this list
     * and its subLists.  Overriding this method to take advantage of
     * the internals of the list implementation can <i>substantially</i>
     * improve the performance of the {@code clear} operation on this list
     * and its subLists.
     *
     * @implSpec
     * This implementation gets a list iterator positioned before
     * {@code fromIndex}, and repeatedly calls {@code ListIterator.next}
     * followed by {@code ListIterator.remove} until the entire range has
     * been removed.  <b>Note: if {@code ListIterator.remove} requires linear
     * time, this implementation requires quadratic time.</b>
     *
     * @param fromIndex index of first element to be removed
     * @param toIndex index after last element to be removed
     */
    protected void removeRange(int fromIndex, int toIndex) {
        ListIterator<E> it = listIterator(fromIndex);
        for (int i = 0, n = toIndex - fromIndex; i < n; i++) {
            it.next();
            it.remove();
        }
    }

    public void clear() {
        removeRange(0, size());
    }
}
```

`protected` 메서드는 하위 클래스에서 부모 클래스의 특정 기능을 효율적으로 재정의하거나 확장할 수 있도록 돕는데
List 구현체의 removeRange 메서드가 바로 그 좋은 예시입니다.
만약 `clear()` 메서드가 요소를 하나씩 제거하는 방식으로 구현되어 있다면 n개의 요소를 지우는 데 n번의 `remove()` 호출이 필요합니다.
이는 전체 성능을 요소 수의 제곱(n2)에 비례해 느리게 만들 수 있습니다.
이러한 비효율성을 해결하기 위해 내부적으로 여러 요소를 한 번에 효율적으로 제거하는 `removeRange()` 같은 메서드가 필요하며
이 메서드는 최종 사용자에게 노출되지 않고 하위 클래스나 부모 클래스 내부에서만 호출되어야 하므로 protected로 선언한 것 입니다.
이렇게 하면 하위 클래스 개발자는 `clear()` 메서드를 재구현할 필요 없이 부모 클래스가 제공하는 `removeRange()`를 통해 최적화된 `clear()` 기능을 활용할 수 있게 됩니다.
즉 `protected` 메서드는 하위 클래스가 부모 클래스의 '내부 구현에 접근'하여 성능을 개선하거나 특정 기능을 유연하게 확장할 수 있는 통로 역할을 하는 셈입니다.
`protected` 메서드를 어떤 메서드에 적용할지 결정하는 명확한 규칙은 없지만 하위 클래스가 어떤 목적으로 이 메서드를 재정의하거나 호출할지 명확히 예상하고
실제로 하위 클래스를 만들어 시험해보는 것이 가장 좋은 방법입니다.
`protected` 메서드는 외부 인터페이스가 아닌 내부 구현에 속하므로 그 수는 최소한으로 유지해야 하지만 너무 적게 만들면
상속을 통한 유연성이라는 이점을 잃게 되므로 이 균형을 찾는 것이 상속을 고려한 좋은 클래스 설계의 핵심입니다.

2. 상속용으로 설계한 클래스는 배포 전에 반드시 하위 클래스를 만들어 검증해야 합니다.  
   <br/>
3. 상속용 클래스의 생성자는 직접적으로 간접적으로든 재정의 가능 메소드를 호출해서는 안됩니다.

```java
class Parent {
    public Parent() {
        System.out.println("부모 생성자 호출 시작");
        doSomething(); // 하위 클래스에서 재정의된 메서드 호출
        System.out.println("부모 생성자 호출 종료");
    }

    public void doSomething() {
        System.out.println("부모 클래스 메소드");
    }
}

class Child extends Parent {
    private String message = "test message";

    public Child() {
        System.out.println("자식 클래스 생성자 호출");
    }

    @Override
    public void doSomething() {
        System.out.println(message.toUpperCase());
    }
}

public class Test {
    public static void main(String[] args) {
        Child child = new Child();
    }
}

```

자식 클래스 생성자를 호출할 때 부모 클래스의 생성자가 먼저 호출합니다.
이때 부모 클래스 생성자에 `doSomething()` 메소드는 자식 클래스에서 재정의 된 메소드 이므로 Child 관련 구현체들은 재정의된 메소드를 사용합니다.
그런데 아직 자식 클래스가 초기화 되지 않았는데 `Child`의 `doSomething()`를 사용하려고 하니 오류가 발생합니다.

단! private, final, static 메소드는 재정의가 불가능하니 부모 생성자에서 호출해도 됩니다.

4. clone과 readObject 모두 직접적으로든 간접적으로든 재정의 가능 메소드를 호출해서는 안됩니다.
   readObject의 경우 하위 클래스의 상태가 미처다 역직렬화되기 전에 호출하게 됩니다. clone의 경우 하위 클래스의 clone 메소드가 복제본의 상태를 수정하기 전에 재정의한 메소드를 호출합니다

5. 상속용으로 설계하지 않은 클래스는 상속을 금지
   클래스 상속을 금지하는 방법은 클래스에 `final` 키워드를 붙이거나 모든 생성자를 `private` 또는 `package-private`으로 선언하고 정적 팩터리 메서드를 제공하는 것입니다.
   정적 팩터리 메서드를 사용하면 외부에 생성자를 노출하지 않고도 객체를 얻을 수 있습니다. 다만 `package-private` 생성자의 경우 같은 패키지 내의 클래스는 상속을 받거나 생성자를 직접 호출할 수
   있습니다.

### 결론
상속용 클래스를 설계할 때는 하위 클래스에서 재정의할 메서드들을 한 클래스에 모아두고 집중시키는 것이 좋습니다. 
그리고 상속용 클래스를 만들 때는 내부 동작을 정확히 이해할 수 있도록 `@implSpec` 태그을 활용하여 메서드의 내부 구현 방식을 상세하게 문서화해야 합니다.

