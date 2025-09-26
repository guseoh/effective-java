### ✅ 멤버 클래스는 되도록 static으로 만들라

## 1. 중첩 클래스(nested class)

- 중첩 클래스는 다른 클래스 안에 정의된 클래스를 말한다.
    ```java
    class Outer {
        // Some Fields...
        class Inner {
            // Some Fields...
        }
    }
    ```

- 자신을 감싼 바깥 클래스에서만 쓰여야 하며, 그 외의 쓰임새가 있다.
- 중첩 클래스의 종류는
    - 정적 멤버 클래스 (Static Nested Class)
    - (비정적) 멤버 클래스 (Non-Static Nested Class)
    - 익명 클래스 (Anonymous Inner Class)
    - 지역 클래스 (Method Local Inner Class)
- 이 중 정적 멤버 클래스를 제외한 나머지는 내부 클래스(inner class)에 해당한다.

<br>

## 2. 정적 멤버 클래스(Static Nested Class)

<details>
    <summary>정적 멤버 클래스 코드</summary>
<div markdown="1">

```java
public class OuterClass {

  private int x = 10;

  private static class InnerClass {
    void test() {
      OuterClass outerClass = new OuterClass();
      
      // 인스턴스를 통해서 접근 가능
      outerClass.x = 100;
      
      // 컴파일 에러
      x = 100;
    }
  }
}
```

</div>
</details>

- 정적 멤버 클래스는 다른 클래스 안에 선언되고, 바깥 클래스의 `private` 멤버에도 접근할 수 있다는 점만 제외하고는 일반 클래스와 똑같다.
- 정적 멤버 클래스는 **다른 정적 멤버와 똑같은 접근 규칙**을 적용받는다.

> Q. 정적 멤버 클래스는 언제 사용할까?
>> A. 외부 클래스의 인스턴스 생성 없이 내부 클래스에 접근하기 위한 용도이다.  
> > 그러므로 내부 클래스를 외부에 노출 시키려는 용도로 사용하고 싶을 경우에는 내부 클래스에 static을 붙이는 게 외부 클래스의 인스턴스 생성 없이 사용 가능하므로 사용하기에 유용하다.

<br>

### 2.1 다른 정적 멤버와 똑같은 접근 규칙

1. 정적 멤버 클래스는 **바깥 클래스의 인스턴스에 종속되지 않고, 바깥 클래스 자체에 속하는 멤버**이다.
    - 이것은 static 필드나 static 메서드가 클래스 레벨에서 존재하는 것과 같은 원리이다.
    - 예를들어, `Outer` 클래스 안에 `static class Nested`를 만들면, 이 `Nested` 클래스는 `Outer` 인스턴스가 없어도 접근할 수 있다.

    ```java
    Outer.Nested obj = new Outer.Nested();
    ```
   이는 static 메서드를 `Outer.someStaticMethod()` 형태로 호출할 수 있는 것과 완전히 같은 규칙이다.
2. [다른 정적 멤버(static 변수, static 메서드)](https://mangkyu.tistory.com/47)처럼, **정적 멤버 클래스도 바깥 클래스의 인스턴스와 무관하게 사용**할 수 있다.
    - `static 필드` → 인스턴스 없이 접근 가능
    - `static 메서드` → 인스턴스 없이 호출 가능
    - `정적 멤버 클래스` → 인스턴스 없이 생성 가능
3. 정적 멤버 클래스는 **바깥 클래스의 static 필드와 static 메서드에는 접근 가능**하다.
    - 정적 멤버 클래스와 바깥 클래스의 static 멤버는 모두 클래스 차원에서 소속된 멤버이기 때문이다.
   ```java
    class Outer {
    private static int staticValue = 10;
    
        static class Nested {
            void print() {
                System.out.println(staticValue);
            }
        }
    }
    ```

<br>

## 3. (비정적) 멤버 클래스 (Non-Static Nested Class)
<details>
    <summary>(비정적) 멤버 클래스 코드</summary>
<div markdown="1">

```java
class Foo {

  void bar() {
  }

  class NestedFoo {

    void bar() {
      Foo.this.bar();
    }
  }
}

class Main {
  public static void main(String[] args) {
    NestedFoo nestedFoo = new Foo().new NestedFoo();
    nestedFoo.bar();
  }
}
```
</div>
</details>

- 비정적 멤버 클래스의 인스턴스와 바깥 인스턴스 사이의 관계는 멤버 클래스가 인스턴스화 될 때 확립되며, 더 이상 변경할 수 없다.
    - **바깥 클래스 인스턴스에 자동으로 연결**된다.
- 비정적 멤버 클래스 `NestedFoo`의 인스턴스는 바깥 클래스인 `Foo`와 암묵적으로 연결된다.
- 이는 비정적 멤버 클래스의 인스턴스 메서드에 정규화된 `this`를 사용해 바깥 인스턴스의 메서드를 호출하거나 바깥 인스턴스의 참조를 가져올 수 있다.
    - 하지만 해당 행위는 비정적 멤버 클래스의 인스턴스 안에 만들어져 메모리 공간을 차지하며, 생성 시간도 더 느리다.

> Q. 비정적 멤버 클래스는 언제 사용할까?
>> A. 어댑터 패턴에 이용된다.  
> > 즉 **다른 타입의 인스턴스를 리턴할** 때 이용된다

<details>
    <summary>HashMap에 사용중인 비정적 멤버 클래스 코드</summary>
<div markdown="1">

```java
public class HashMap<K,V> extends AbstractMap<K,V>
    implements Map<K,V>, Cloneable, Serializable {
    ...
    public Set<K> keySet() {
        Set<K> ks = keySet;
        if (ks == null) {
            ks = new KeySet();
            keySet = ks;
        }
        return ks;
    }
    final class KeySet extends AbstractSet<K> {
        public final int size()                 { return size; }
        public final void clear()               { HashMap.this.clear(); }
        public final Iterator<K> iterator()     { return new KeyIterator(); }
        public final boolean contains(Object o) { return containsKey(o); }
        public final boolean remove(Object key) {
            return removeNode(hash(key), key, null, false, true) != null;
        }
        public final Spliterator<K> spliterator() {
            return new KeySpliterator<>(HashMap.this, 0, -1, 0, 0);
        }
        public final void forEach(Consumer<? super K> action) {
            Node<K,V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (Node<K,V> e : tab) {
                    for (; e != null; e = e.next)
                        action.accept(e.key);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }
}
```
</div>
</details>

“다른 타입의 인스턴스를 리턴한다.” 말을 풀어 설명하면

- 어떤 클래스의 인스턴스를 감싸 마치 다른 클래스의 인스턴스처럼 보이게 하는 뷰로 사용한다는 것이다.
- 위의 코드와 비슷하게 Set과 List 같은 다른 컬렉션 인터페이스 구현들에도 이용된다.

<details>
    <summary>AbstractSet의 구현 코드</summary>
<div markdown="1">

```java
public class MySet<E> extends AbstractSet<E> {

    @Override
    public Iterator<E> iterator() {
        return new MyIterator();
    }

    @Override
    public int size() {
        return 0;
    }
    
    private class MyIterator implements Iterator<E> {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public E next() {
            return null;
        }
    }
}
```
</div>
</details>

**멤버 클래스에서 바깥 인스턴스에 접근할 일이 없다면 무조건 static을 붙여서 정적 멤버 클래스로 만들자.**

1. 불필요한 메모리 누수
    - 비정적 멤버 클래스는 **바깥 클래스 인스턴스에 대한 숨은 참조**를 갖는다.
    - 바깥 클래스가 인스턴스를 전혀 사용하지 않아도, 내부적으로 자동으로 참조하기 때문에 GC(가비지 컬렉션)가 바깥 인스턴스를 수거하지 못할 수 있다.
2. 성능 오버헤드
    - 비정적 멤버 클래스는 바깥 인스턴스와 자동으로 연결되기 때문에, 생성 과정에서 **바깥 인스턴스를 명시적으로 전달하는 숨은 비용**이 있다.

    ```java
    Outer outer = new Outer();
    Outer.Inner inner = outer.new Inner();
    ```

    - 내부적으로 `inner` 객체는 `outer`에 대한 참조를 저장해야 한다.

<br>

## 4. 익명 클래스 (Anonymous Inner Class)

- 멤버와 달리, 쓰이는 시점에 선언과 동시에 인스턴스가 만들어진다.
- 비정적 문맥에서 사용될 때만 바깥 클래스의 인스턴스를 참조할 수 있다.
    <details>
    <summary>Code</summary>
    <div markdown="1">
    
    ```java
    public class TestClass {
      Integer intInstance = 10;
      
      void doX() {
          new SInterface() {
              @Override
              public void doSometing() {
                  //바깥 인스턴스 참조
                  System.out.println(intInstance);
              }
          };
      }
  }
    
  interface SInterface {
    void doSometing();
  }
        
  ```
    </div>
    </details>
  
- 정적 문맥에서라도 상수 변수 이외의 정적 멤버는 가질 수 없다.
    - 즉, 상수 표현을 위해 초기화된 **final 기본 타입과 문자열 필드**만 가질 수 있다.
  <details>
    <summary>Code</summary>
    <div markdown="1">
    
    ```java
    public class TestClass {
        // 익명클래스에서 사용 불가
        Integer intInstance = 10;
    
        static void doX() {
            new SInterface() {
                static final int x = 0;
    //            static int y = 0; // 컴파일에러
                @Override
                public void doSometing() {
                }
            };
        }
    }
    
    interface SInterface {
        void doSometing();
    }
    ```
    </div>
    </details>

<details>
    <summary>익명 클래스의 제약 사항</summary>
<div markdown="1">

- 선언한 지점에서만 인스턴스를 만들 수 있고, instatnceof 검사나 클래스의 이름이 필요한 작업은 수행할 수 없다.
- 익명 클래스를 사용하는 클라이언트는 그 익명 클래스가 상위 타입에서 상속한 멤버 외에는 호출할 수 없다.
- 여러 인터페이스를 구현할 수 없고, 인터페이스를 구현하는 동시에 다른 클래스를 상속할 수도 없다.
- 익명 클래스를 사용하는 클라이언트는 그 익명 클래스가 상위 타입에서 상속한 멤버 외에는 호출할 수 없다.
- 익명 클래스는 표현식이 중간에 등장하므로 짧게 작성 해야한다(10줄 이하로)
</div>
</details>

<details>
    <summary>익명 클래스 예시</summary>
<div markdown="1">

```java
List<Integer> list = Arrays.asList(10, 5, 6, 7);
Collections.sort(list, new Comparator<Integer>() {
    @Override
    public int compare(Integer o1, Integer o2) {
        return Integer.compare(o1, o2);
    }
});

System.out.println(list);
```
</div>

```java
// Java 8 이후에 람다 등장
Collections.sort(list, (o1, o2) -> Integer.compare(o1, o2));
```
</details>

<br>

## 5. 지역 클래스

- 가장 드물게 사용된다.
- 지역 변수를 선언할 수 있는 곳에서 선언 가능하고 유효 범위도 지역변수와 같다.
- 멤버 클래스처럼 이름이 있고, 반복해서 사용할 수 있다.
- 익명 클래스처럼 비정적 문맥에서 사용될 때만 바깥 인스턴스를 참조할 수 있다.
- 정적 멤버는 가질 수 없으며 가독성을 위해 짧게 작성되어야 한다.

<details>
    <summary>지역 클래스 예시</summary>
<div markdown="1">

```java
public class TestThread {
  public static void main(String[] args) {
      Thread goodNightThread = new Thread(() -> {
          try {
              for (int i = 0; i <10; i++){
                  Thread.sleep(1000);
                  System.out.println("양 " + i + "마리...");
              }
          } catch (InterruptedException e) {
              e.printStackTrace();
          }
      });
      goodNightThread.start();
  }
}
```
</div>
</details>

- [Local inner class](https://velog.io/@mimah/Java-Local-inner-class)

<br>

## 6. 정리

- 메서드 밖에서도 사용해야 하거나 메서드 안에 정의하기엔 너무 길다면 **멤버 클래스**로 만든다.
- 멤버 클래스의 인스턴스 각각이 바깥 인스턴스를 참조한다면 **비정적**으로, 그렇지 않으면 **정적**으로 만들자.
- 중첩 클래스가 한 메서드 안에서만 쓰이면서 그 인스턴스를 생성하는 시점이 단 한곳이고 해당 타입으로 쓰기에 적합한 클래스나 인터페이스가 이미 있다면 **익명 클래스**로 만들고, 그렇지 않으면 **지역 클래스**로 만들자.

