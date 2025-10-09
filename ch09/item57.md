### ✅ 지역변수의 범위를 최소화하라

해당 아이템은 '클래스와 멤버의 접근 권환을 최소화하라'고 한 아이템 15 취지랑 비슷합니다. 이는 코드의 가독성과 유지보수성을 높이고 오류 발생 가능성을 낮추는 방법입니다.

### 지역 변수 범위를 줄이는 방법

#### 1. 가장 처음 쓰일 때 선언하기

지역변수의 유효 범위를 최소로 줄이는 가장 강력하고 효과적인 기법은 바로 변수를 '가장 처음 쓰일 때' 선언하고 초기화하는 것입니다.

사용하기 한참 전에 변수를 미리 선언하는 습관은 코드를 불필요하게 어수선하게 만들고 가독성을 떨어뜨립니다.
변수를 선언할 때 설정했던 타입이나 초깃값이, 정작 변수가 사용되는 시점에서는 기억나지 않을 수 있습니다.

또한, 변수를 생각 없이 일찍 선언하게 되면 변수가 실제로 필요한 범위보다 훨씬 넓은 영역에서 선언되거나, 이미 용도가 끝났음에도 여전히 '살아있는' 상태로 남아있어 성능에 영향을 끼칠 수 있습니다.

#### 2. 거의 모든 지역 변수는 선언과 동시에 초기화해야 합니다.

초기화에 필요한 정보가 중분하지 않다면 충분해질 때까지 선언을 미뤄야 합니다.
단! `try-catch`문은 이 규칙에서 예외입니다.

2-1. 초기화 중 예외 발생 가능성이 있을 경우
변수를 초기화하는 표현식 자체에서 예외를 던질 가능성이 있다면, 해당 초기화 코드는 반드시 `try` 블록 안에서 이루어져야 합니다.
그렇지 않으면 예외가 `try` 문 바깥에서 발생하게 되어 의도치 않은 결과를 초래할 수 있습니다.
<br/>
2-2. `try` 블록 밖에서도 변수를 사용해야 하는 경우
초기화 정보가 충분하지 않더라도, `try` 블록 바깥의 코드에서도 변수를 참조해야 한다면 변수는 `try` 블록 앞에서 선언되어야 합니다.
이 경우 `try` 블록 내부에서 초기화를 수행하게 되며, `try` 블록 앞에서 선언할 때 임시로 `null`으로 초기화하는 방식이 필요할 수 있습니다.

#### 3. 메소드를 작게 유지하고 한 가지 기능에 집중

### 반복문에서 지역 변수

for문에서 지역 변수는 for문 블록문 괄호 안으로 제한됩니다. 그래서 변수의 값을 for문 바깥에도 사용하고 싶으면 while문을 사용하는게 좋습니다.


<details>
    <summary>더보기</summary>
<div markdown="1">

```java
public static void main(String[] args) {
    for (Element e : c) {
        // ...
    }

    for (Iterator<Element> i = c.iterator(); i.hasNext(); ) {
        Element e = i.next();
        // ...
    }
}

public static void main2(String[] args) {
    Iterator<Element> i = c.iterator();
    while (i.hasNext()) {
        doSomething(i.next());
    }

    Iterator<Element> i2 = c2.iterator();
    while (i.hasNext()) { // 오류 발생 
        doSomething(i2.next());
    }
}
```

두번째 `while`문에서 오류는 논리 오류입니다. 만약 `i`의 순회가 다 끝났으면 두번째 `while`문은 `false`를 반환하기 때문에 논리 오류가 발생합니다.

```java
public static void main(String[] args) {

    for (Iterator<String> i = c.iterator(); i.hasNext(); ) {
        System.out.print(i.next());
    }

    for (Iterator<String> i2 = c2.iterator(); i.hasNext(); ) {
        System.out.print(i2.next());
    }
}
```

위에 같이 코드를 변경하면 논리 오류가 아닌 컴파일 오류가 발생합니다.

</div>
</details>

질문
for문 바깥에 선언된 변수도 사용할 수 있는데 바깥에 선언하면 위에 말은 말이 안되는 거 아니냐??
