### ✅ 전통적인 for 문보다는 for-each 문을 사용하라


## 1. 전통적인 for 문으로 컬렉션을 순회하는 코드
<details>
    <summary>컬렉션 순회하기 - for loop</summary>
<div markdown="1">

```java
for(iterator<Element> i = c.iterator(); i.hasNext(); ){
    Element e = i.next();
    //do something
}
```
</div>
</details>

<details>
    <summary>배열 순회하기</summary>
<div markdown="1">

```java
for (int i = 0; i < a.length; i++) {
    //do something
}
```
</div>
</details>

<details>
    <summary>컬렉션 순회하기 - while</summary>
<div markdown="1">

```java
Iterator<Element> i = c.iterator();
while(iterator.hasNext()) {
    Element e = i.next();
}
```
</div>
</details>

for loop와 배열 순회하기 코드는 while 문보다는 낫지만 반복자와 인덱스 변수 모두를 지저분하게 한다. 또한 이처럼 쓰이는 요소 종류가 늘어나면 오류가 생길 가능성이 높아진다.

혹시라도 잘못된 변수를 사용했을 때 컴파일러가 잡아준다는 보장이 없다.

**우리에게 진짜 필요한 건 원소들 뿐이다.**

이상의 문제는 for-each 문을 사용하면 모두 해결된다.

<br>

## 2. for-each

**for-each를 사용하면**
- 반복자와 인덱스 변수를 사용하지 않으니 코드가 깔끔해지고 오류가 날 일도 없다.
- 하나의 관용구로 컬렉션과 배열을 모두 처리할 수 있어서 어떤 컨테이너를 다루는지는 신경 쓰지 않아도 된다.

<details>
    <summary>컬렉션과 배열을 순회하는 올바른 관용구</summary>
<div markdown="1">

```java
for(Element e : elements){
    ... // do something
}
```
</div>
</details>

- 콜론(:)은 “안의(in)”라고 읽으면 된다.
- 이 반복문은 “elements 안의 각 원소 e에 대해”라고 읽는다.
- 반복 대상이 컬렉션이든 배열이든, for-each 문을 사용해도 속도는 그대로다.
- 컬렉션을 중첩해 순회해야 한다면 for-each 문의 이점이 더욱 커진다.

<br>

## 3. 컬렉션 중첩 사용

```java
enum Suit { CLUB, DIAMOND, HEART, SPADE }
enum Rank { ACE, DEUCE, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT,
    NINE, TEN, JACK, QUEEN, KING }

static Collection<Suit> suits = Arrays.asList(Suit.values());
static Collection<Rank> ranks = Arrays.asList(Rank.values());

List<Card> deck = new ArrayList<>();

for (Iterator<Suit> i = suits.iterator(); i.hasNext(); )
    for (Iterator<Rank> j = ranks.iterator(); j.hasNext(); )
        deck.add(new Card(i.next(), j.next()));
```

- 위 코드를 실행하면 `NoSuchElementException`을 던진다.

### NoSuchElementException 던지는 이유

바깥 컬렉션(suits)의 반복자에서 next 메서드가 너무 많이 불린다는 것 → 마지막 줄의 i.next()를 주목

이 next()는 ‘숫자(Suit) 하나당’ 한 번씩만 불려야 하는데, 안쪽 반복문에서 호출되는 바람에 ‘카드(Rank) 하나당’ 한 번씩 불리고 있다.

그래서 숫자가 바닥나면 반복문에서 `NoSuchElementException` 을 던진다.


### 올바른 코드

1. i.next() 호출을 바깥 반복문 안쪽으로 옮겨야 한다.
```java
for (Iterator<Suit> i = suits.iterator(); i.hasNext(); ) {
    Suit suit = i.next();
    for (Iterator<Rank> j = ranks.iterator(); j.hasNext(); ) {
        deck.add(new Card(suit, j.next()));
    }
}
```

2. for-each
```java
for (Suit suit : suits) {
    for (Rank rank : ranks) {
        deck.add(new Card(suit, rank));
    }
}
```

<br>

### 예시) 주사위
주사위 두 번 굴렸을 때 나올 수 있는 모든 경우의 수를 출력하는 코드

```java
enum Face { ONE, TWO, THREE, FOUR, FIVE, SIX }

// 같은 버그, 다른 증상!
Collection<Face> faces = EnumSet.allOf(Face.class);

for (Iterator<Face> i = faces.iterator(); i.hasNext(); )
    for (Iterator<Face> j = faces.iterator(); j.hasNext(); )
        System.out.println(i.next() + " " + j.next());
}
```

- 이 프로그램은 예외를 던지진 않지만, 가능한 조합을 (”ONE ONE” 부터 “SIX SIX”까지) 단 여섯 쌍만 출력하고 끝나버린다.
- 이 문제를 해결하려면 바깥 반복문에서 바깥 원소를 저장하는 변수를 하나 추가해야 한다.

```java
Collection<Face> faces = EnumSet.allOf(Face.class);

for (Face f1 : faces){
    for (Face f2 : faces){
        System.out.println(f1 + " " + f2);
    }
}
```

<br>

## 4. for-each 문을 사용할 수 없는 상황

### 1. 파괴적인 필터링(destructive filtering)

- 컬렉션을 순회하면서 선택된 원소를 제거해야 한다면 반복자의 `remove` 메서드를 호출해야 한다.
- 자바 8부터는 Collection의 `removeIf` 메서드를 사용해 컬렉션을 명시적으로 순회하는 일을 피할 수 있다.
<details>
    <summary>removeif</summary>
<div markdown="1">

```java
public boolean removeIf(Predicate<? super E> filter)

Description copied from interface: Collection
Removes all of the elements of this collection that satisfy the given predicate. 
Errors or runtime exceptions thrown during iteration or by the predicate are relayed to the caller.

Specified by:
removeIf in interface Collection<E>

Parameters:
filter - a predicate which returns true for elements to be removed

Returns:
true if any elements were removed
```

이 컬렉션의 모든 요소 중에서 주어진 조건(Predicate)을 만족하는 요소를 제거한다.

반복 중에 발생하거나, 조건(Predicate)에 의해 발생한 오류나 런타임 예외(RuntimeException) 는 호출자(caller)에게 그대로 전달된다.
</div>
</details>

<br>

### 2. 변형(transforming)

- 리스트나 배열을 순회하면서 그 원소의 값 일부 혹은 전체를 교체해야 한다면 리스트의 반복자나 배열의 인덱스를 사용해야 한다.
<details>
    <summary>예시</summary>
<div markdown="1">

1. ListIterator를 이용한 리스트 원소 변형 예시
```java
List<String> names = new ArrayList<>(Arrays.asList("alice", "bob", "charlie"));

ListIterator<String> it = names.listIterator();

while (it.hasNext()) {
    String name = it.next();
    it.set(Character.toUpperCase(name.charAt(0)) + name.substring(1));
}
```

2. for-each (불가능)
```java
for (String name : names)
    name = name.toUpperCase();
```

for-each 문에서는 리스트 요소를 직접 수정할 수 없지만, ListIterator의 set() 메서드를 사용하여 안전하게 원소를 바꿀 수 있다.
</div>
</details>

<br>

### 3. 병렬 반복(parallel iteration)

- 여러 컬렉션을 병렬로 순회해야 한다면 각각의 반복자와 인덱스 변수를 사용해 엄격하고 명시적으로 제어해야 한다. (2.컬렉션 중첩 사용)