### ✅ 다 쓴 객체 참조를 해제하라
자바의 중요한 특징 중 하나인 가비지 컬렉터(Garbage Collector)는 객체 메모리 관리를 자동화해주지만, 
메모리 관리에 전혀 신경 쓰지 않아도 된다고 오해해서는 안 됩니다.

간단한 스택 예시를 통해 살펴보겠습니다.

```java
public class Stack {
    private static final int DEFAULT_INITAL_CAPACITY = 16;

    private Obejct[] elements;
    private int size = 0;

    public Stack() {
        elements = new Object[DEFAULT_INITAL_CAPACITY];
    }

    public void push(Object e) {
        ensureCapacity();
        elements[size++];
    }

    public Object pop() {
        if (size == 0) {
            throw new EmptyStackException();
        }
        return elements[--size];
    }

    /**
     * 원소를 위한 공간을 적어도 하나 확보
     * 배열 크기를 늘려야 할 때마다 대략 두 배로 증가
     */
    private void ensureCapacity() {
        if (elements.length == size) {
            elements = Arrays.copyOf(elements, 2 * size + 1);
        }
    }
}
```
위에 코드를 보면 특별한 문제가 없어 보이지만 메모리 누수에 문제가 있습니다. 이러한 메모리 누수가 심해지면 성능 저하를 넘어 시스템 다운까지 될 수도 있습니다.

스택의 `pop()` 메서드는 스택의 핵심 특징인 LIFO 원칙에 따라 가장 최근에 들어온 요소를 반환하고 스택에서 삭제하는 기능을 수행해야 합니다. 
하지만 만약 `elements[--size]`처럼 단순히 반환값만 가져오고 size 인덱스만 이동시킨다면 
실제로는 해당 위치의 값(객체에 대한 참조)이 메모리에서 삭제되지 않은 채 그대로 남아 있게 됩니다.

### 왜 가비지 컬렉터는 회수하지 못할까요?
`pop()`를 사용하는 의미는 더 이상 사용되지 않아야 할 객체들을 가져올 때 사용합니다. 이 때 가비지 컬렉터에 의해 회수되지 않습니다. 
왜냐하면 스택은 `elements` 배열로 저장소 풀을 만들어 활성 영역, 비활성 영역 원소를 관리하는데 가비지 컬렉터는 이걸 인지 할 수가 없습니다.
이로 인해 `elements` 배열 안에 해당 객체에 대한 참조가 여전히 남아 있게 됩니다.
즉 스택에서 '논리적으로'는 제거되었지만 '물리적으로'는 배열 안에 참조가 남아 있어 가비지 컬렉션의 대상이 되지 못하고 메모리 누수가 발생하는 것입니다.
그래서 개발자는 활용하지 않는 객체를 null 처리해서 가비지 컬렉션에 알려줘야 됩니다.

### 해결 방법
```java
public Object pop(){
    if(size ==0){
        throw new EmptyStackException();
    }
    Object result = elementes[--size];
    elements[size] = null; // 다 쓴 참조 해제
    return result;
}
```
위에 코드처럼 더 이상 사용하지 않을 객체를 `pop()` 메서드 내에서 `null` 처리하는 것이 가장 권장되는 방법이지만 
`pop()` 외부에 개별적으로 `null` 처리를 할 수도 있지만 그렇게 하면 코드가 복잡해지고 관리하기 어려워집니다.

만약 null 처리된 객체를 나중에 다시 사용하려고 시도한다면 당연히 `NullPointerException` 예외가 발생할 것입니다.

이러한 메모리 누수 문제는 스택(Stack) 클래스에만 국한되지 않습니다. 
자기 메모리(저장소 풀)를 직접 관리하는 모든 클래스를 구현할 때는 항상 메모리 누수에 주의해야 합니다. 

### Cache (캐시) 관련 메모리 누수
캐시(Cache)에 저장하고 그 사실을 까먹어 메모리에서 제거되지 않고 남아 있는 경우가 있습니다.
예를 들어, MemoryLeakCache 클래스의 HashMap 예시가 이에 해당합니다.
```java
public class MemoryLeakCache {
    private Map<String, String> map = new HashMap<>();

    public void initCache() {
        map.put("Anil", "엔지니어");
        map.put("Shashik", "자바 엔지니어");
        map.put("Ram", "의사");
    }

    public Map<String, String> getCache() {
        return map;
    }

    public void forEachDisplay() {
        map.entrySet().forEach(entry -> {
            String key = entry.getKey();
            String val = entry.getValue();
            System.out.println(key + " :: " + val);
        });
    }

    public static void main(String[] args) {
        MemoryLeakCache cache = new MemoryLeakCache();
        cache.initCache();
        cache.forEachDisplay();
    }
}
```
캐시를 비우지 않으면, map에 저장된 객체들이 계속 메모리에 남아 있게 됩니다. 
예를 들어 `map.remove("Anil")` 과 같이 개별적으로 삭제하거나,
`map = null` 혹은 `map.clear()` 를 통해 전체를 비워야 합니다.
하지만 `WeakHashMap`을 사용하면 외부 참조가 없어질 때 자동으로 수집될 수 있습니다.

### WeakHashMap
WeakHashMap은 키(Key)에 대한 참조가 Weak Reference로 유지됩니다. 
이는 외부에서 해당 키를 더 이상 참조하지 않으면 WeakHashMap에 저장된 값들을 가비지 컬렉션의 대상이 되어 메모리에서 자동으로 제거될 수 있도록 합니다.

또는 캐시의 크기를 제한하는 정책(LRU)을 구현하거나 특정 시간이 지나면 자동으로 만료되는 캐시 라이브러리(Guava Cache, Caffeine 등)를 사용하는 것도 좋습니다.
```java
public static void main(String[] args) {
        Map<Integer, String> map = new HashMap<>();

        Integer key1 = 1000;
        Integer key2 = 2000;

        map.put(key1, "test a");
        map.put(key2, "test b");

        key1 = null;

        System.gc();  //강제 Garbage Collection

        map.entrySet().stream().forEach(el -> System.out.println(el));
}

public static void main(String[] args) {
    Map<Integer, String> map = new WeakHashMap<>();

    Integer key1 = 1000;
    Integer key2 = 2000;

    map.put(key1, "test a");
    map.put(key2, "test b");

    key1 = null;

    System.gc();  //강제 Garbage Collection

    map.entrySet().stream().forEach(el -> System.out.println(el));
}
```

### LinkedHashMap
`LinkedHashMap은` `HashMap`의 모든 기능을 가지면서도, 요소들이 삽입된 순서(insertion order)나 마지막으로 접근된 순서(access order)를 기억하는 특징을 가집니다.
특히 access order를 활용하면 캐시의 메모리 관리 정책 중 하나인 LRU(Least Recently Used)를 효율적으로 구현할 수 있습니다.

```java
public class LRUCacheWithLinkedHashMap<K, V> extends LinkedHashMap<K, V> {

    private final int cacheSize; 

    public LRUCacheWithLinkedHashMap(int cacheSize) {
        /**
         * @param initialCapacity: 초기 용량
         * @param loadFactor: 로드 팩터
         * @param accessOrder: true로 설정하면 접근 순서대로 정렬
         */
        super(cacheSize + 1, 1.0f, true); 
        this.cacheSize = cacheSize;
    }

    /**
     * 캐시가 최대 크기를 초과했을 때 가장 오래된 엔트리를 자동으로 제거할지 결정합니다.
     * 이 메서드가 true를 반환하면 엔트리가 제거됩니다.
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > cacheSize;
    }

    public static void main(String[] args) {
        // 캐시 최대 3개 최대로 제한
        LRUCacheWithLinkedHashMap<String, String> cache = new LRUCacheWithLinkedHashMap<>(3);

        System.out.println("초기 캐시 상태: " + cache);

        cache.put("apple", "사과");
        cache.put("banana", "바나나");
        cache.put("cherry", "체리");
        System.out.println("3개 추가 후: " + cache); 

        // 캐시 크기 초과 상황
        // 가장 오래된 apple이 제거되고 "date가 추가
        cache.put("date", "대추");
        System.out.println("4개째 추가 후: " + cache); 

        // 기존 요소 접근 (접근 순서 변경)
        cache.get("banana"); // banana가 가장 최근에 접근된 요소가 됩니다.
        System.out.println("banana 접근 후: " + cache);

        // 캐시 크기 초과 상황
        // 가장 오래된 cherry가 제거되고 elderberry가 추가
        cache.put("elderberry", "엘더베리");
        System.out.println("5개째 추가 후 (cherry 제거): " + cache);
    }
}
```