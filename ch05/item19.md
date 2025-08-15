### ✅ 상속을 고려해 설계하고 문서화하라. 그러지 않았다면 상속을 금지하라

상속은 좋은 기능이지만 오버라이딩한 메소드를 문서화하지 않으면 예상치 못한 문제를 일으킬 수 있습니다.

### 상속을 고려한 설계 시 문서화 핵심

상속용 클래스를 설계하고 문서화할 때 가장 중요한 원칙은 **`메서드 재정의 시 내부적으로 어떤 일이 일어나는지 명확하게 문서로 남기는 것`** 입니다.
이는 상속을 통해 복잡한 구조의 메소드 흐름을 파악하기 위함입니다.

- **재정의할 수 있는 메소드 정의:** 재정의는 오버라이딩이랑 같은 의미로 public, protected로 선언된 메소드 중 final이 아닌 모든 메소드를 뜻합니다.
- **메소드 호출 순서 명시:** `public`으로 공개된 메소드는 또 다른 메소드를 호출할 수 있으니 호출 순서, 호출 결과를 잘 정리해야 합니다.
- 재정의 가능 메소드를 호출할 수 있는 모든 상황을 문서로 남겨야 합니다.

메서드 호출 순서 명시: public 메서드가 내부적으로 어떤 다른 메서드를 호출하는지, 그리고 그 호출 순서는 어떻게 되는지 상세히 기술해야 합니다. 이를 통해 하위 클래스에서 특정 메서드를 재정의했을 때 부모
클래스의 다른 메서드 동작에 어떤 영향을 미치는지 예측할 수 있습니다.

재정의 가능 메서드 호출 상황 문서화: 부모 클래스의 어떤 메서드가 재정의 가능 메서드를 호출하는지 모든 상황을 문서로 남겨야 합니다. 이는 특정 메서드를 재정의했을 때 어떤 부작용이 발생할 수 있는지 사전에
파악하는 데 도움을 줍니다.

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

@implSpec 내의 설명을 보면 remove 메서드 내부적으로 **iterator()** 와 it.remove() 메서드를 호출한다는 사실을 명확히 알 수 있습니다.
만약 하위 클래스에서 iterator() 메서드를 재정의한다면, remove() 메서드의 동작에도 영향을 미칠 수 있음을 예측할 수 있습니다.
또한 iterator가 remove 메서드를 지원하지 않을 경우 **UnsupportedOperationException**이 발생할 수 있다는 경고도 함께 제공되어 하위 클래스 작성자가 오류를 미리 인지하고
대비할 수 있게 도와줍니다.

추가적으로 @implSpec 태그는 자바 8에서 처음 도입되어 자바 11에서는 선택사항으로 남겨져 있습니다.  
`-tag "implSpec:a:Implementation Requirements:"`로 지정

클래스의 내부 동작 과정 중간에 끼어들 수 있는 훅을 잘 선별하여 protected 메소드 형태로 공개해야 할 수도 있습니다.

