### ✅ null이 아닌, 빈 컬렉션이나 배열을 반환하라

## 0. 들어가기 전
```java
private final List<Cheese> cheesesInStock = ...;

/**
 * @return 매장 안의 모든 치즈 목록을 반환한다.
 * 	단, 재고가 하나도 없다면 null을 반환한다.
 */
public List<Cheese> getCheeses() {
	return cheesesInStock.isEmpty() ? null : new ArrayList<>(cheesesInStock);
}
```
- 위 코드는 NullPointerException을 발생시키는 경우가 많으며, 이에 따라 디버깅이 어렵게 된다.  

<br>

```java
List<Cheese> cheeses = shop.getCheeses();
if (cheeses != null && cheeese.contains(Cheese.STILTON))
	System.out.println("좋았어, 바로 그거야.");
```
- 클라이언트는 null 상황을 처리하는 코드를 추가로 작성해야 한다.

<br>

## 1. 빈 컬렉션 반환 메서드
때로는 빈 컨테이너를 할당하는 데도 비용이 드니 null을 반환하는 쪽이 낫다는 주장이 있다. 하지만 이는 두 가지 면에서 틀린 주장이다.

1. null 대신 빈 컬렉션을 반환하더라도 성능 차이가 크지 않다.
2. 빈 컬렉션은 굳이 새로 할당하지 않고도 반환할 수 있다.

```java
public List<Cheese> getCheeses () {
		return new ArrayList<>(cheeseInStock);
}
```

위 코드는 빈 컬렉션을 반환하는 올바른 코드이다.

가능성이 작지만, 사용 패턴에 따라 빈 컬렉션 할당이 성능을 눈에 띄게 떨어뜨릴 수 있다. 이런 경우는 불변 컬렉션을 반환하여 최적화 가능하다. (불변 객체는 자유롭게 공유해도 안전하다. 아이템 17)

내부가 빈 불변 컬렉션은 Collections.emptyList, Collections.emptySet, Collections.emptyMap 이다.

이 내용을 고려하여 수정할 수 있다.

```java
public List<Cheese> getCheese() {
	return cheesesInStock.isEmpty() ? 
    	Collections.emptyList() : new ArrayList<>(cheesesInStock);
}
```

<br>

## 2. 빈 배열 반환 메서드
> 배열을 사용할 때도 마찬가지이다. **절대 null을 반환하지 말고 길이가 0인 배열을 반환하라.**

```java
public Cheese[] getCheeses() {
    return cheesesInStock.toArray(new Cheese[0]);
}
```

- toArray 메서드에 건넨 길이 0짜리 배열은 우리가 원하는 반환 타입(Cheese[])을 알려주는 역할을 한다.
- 이 방식이 성능을 떨어뜨릴 것 같다면 최적화 할 수 있다.

<br>

```java
private static final Cheese[] EMPTY_CHEESE_ARRAY = new Cheese[0];

public Cheese[] getCheeses() {
		return cheesesInStock.toArray(EMPTY_CHEESE_ARRAY);
}
```

- 빈 배열을 매번 새로 할당하지 않아도 된다.


- `toArray(new T[0])`
    - 새로운 배열을 만들어 그 배열을 반환
    - Java 8 이후부터는 배열의 크기를 0으로 설정하여 toArray를 호출하면 JVM 내부에서 최적화가 이루어짐
    - JVM이 배열의 적절한 크기를 결정하고, 필요한 경우 새로운 배열을 만들어서 반환
- `toArray(new T[t.size()])`
    - 리스트의 크기만큼의 배열을 미리 생성하여 toArray에 전달
    - toArray 메소드가 새로운 배열을 생성하지 않고, 전달된 배열을 그대로 사용하여 원소들을 저장하고 반환
    - 리스트의 크기만큼의 배열을 미리 생성하므로 메모리 할당 비용 발생

최신 JVM에서는  toArray(new T[0])가 성능적으로 우수하다는 연구 결과가 있어 이 방식을 권장한다.