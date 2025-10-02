### ✅ 가변인수는 신중히 사용하라

가변인수는 자바 5(Java 5)부터 도입된 기능으로, 메서드를 정의할 때 인수의 개수를 미리 정하지 않고 메서드 호출 시점에 가변적으로 인수를 전달할 수 있도록 해줍니다.

이 기능은 메서드를 더 유연하고 편리하게 만들지만, 신중하게 사용해야 할 몇 가지 단점과 주의사항이 있습니다.

[//]: # (가변인수는 기술적으로 0개의 인수를 전달받아도 컴파일 에러가 발생하지 않지만, 이는 좋은 설계라고 보기 어렵습니다.  )

[//]: # (왜냐하면 0개의 인수를 가정하지 않은 내부 로직&#40;예: 반드시 하나 이상의 요소가 필요할 때&#41;이 있다면 런타임 오류가 발생할 수 있기 때문입니다.)

[//]: # (이 경우 0개 인수를 방지하거나 처리하기 위한 유효성 검사 코드를 추가해야 되며, 코드가 지저분해집니다.)

### 가변인수의 단점

1. 잘못 설계하면 유효섬 검사 코드를 추가한다고 해도 컴파일 단계가 아니라 런타임 시에 에러가 발생한다는 점입니다.  
   <br />
   가변인수는 기술적으로 0개의 인수를 전달받아도 컴파일 에러가 발생하지 않습니다.
   그러나 메서드의 내부 로직이 반드시 하나 이상의 요소를 가정하고 동작할 경우, 인수가 0개일 때 **런타임 오류가 발생**할 수 있습니다.  
   <br />
2. 성능 저하  
   <br />
   가변인수 메서드는 호출될 때마다 배열을 새로 하나 할당하고 초기화합니다. 이 배열 생성 및 가비지 컬렉션 부담 때문에 성능 저하를 일으킬 수 있습니다.
   특히 메서드가 자주 호출될 경우 그 영향은 무시할 수 없습니다.

### 단점 해결

#### 런타임 오류가 발생하는 문제 해결 - 필수 매개변수, 가변 인수 구분

가변인수가 0개일 때 발생하는 **런타임 오류 문제**를 해결하는 가장 좋은 방법은, 메서드에 반드시 필요한 최소한의 인수 개수를 구분하는 것 입니다.

```java
static int sum(int firstNumber, int... numbers) {
    // firstNumber 매개변수 덕분에 유효성 검사도 필요 없으며, 가변 인수를 적절하게 이용하고 있음
    int sum = firstNumber;
    for (int number : numers) {
        sum += number;
    }

    return sum;
}

public static void main(String[] args) {
    sum(1, 2); // 내부적으로 컴파일러가 sum(new int[]{2})으로 변환해줌
    sum(1);
    sum(); // 컴파일 에러 발생    
}

```

위에 단점은 즉 필수로 들어가야 되는 인수가 있다는 의미입니다. 이 제약을 컴파일 단에서 잡으려면 **반드시 필요한 인수는 일반 매개변수로 명시**하고 필수 인수가 아닌 인수는
가변 인수로 표현하면 런타임 오류가 아닌 컴파일 오류를 유발시킬 수 있습니다.

#### 성능 문제 해결 - 다중 정의로 해결

성능 저하의 주범인 불필요한 배열 생성을 막기 위해, 자주 사용될 것으로 예상되는 인수의 개수만큼 **메서드를 다중 정의**하여 배열 생성 비용을 피할 수 있습니다.

```java
public void foo() {
}

public void foo(int a1) {
}

public void foo(int a1, int a2) {
}

public void foo(int a1, int a2, int a3) {
}

public void foo(int a1, int a2, int a3, int... rest) {
}
```

자주 사용되는 특정 개수(0개, 1개, 2개, 3개 등)의 인수로 호출될 때마다 배열을 생성하지 않도록 일반 매개변수만을 사용해 오버로딩 메서드를 정의합니다.
그리고 인수의 개수가 예측 범위를 넘어서는 경우에만 가변인수 메서드를 사용해서 해결할 수 있지만 코드가 다소 지저분 해집니다.

이러한 다중 정의를 잘 이용한 자바 라이브러리가 `EnumSet`의 정적 팩토리 메소드들입니다.

`EnumSet`은 열거 타입 집합을 생성할 때, 가변인수를 사용하면서도 뛰어난 성능을 유지합니다.
이는 내부적으로 **비트 필드의 장점**을 이어받으면서도 열거 타입의 안전성을 제공하는 동시에, 다음과 같이 인수 개수별로 오버로딩된
정적 팩토리 메서드를 제공하여 성능을 최적화했기 때문입니다.

```java
public class EnumSetExample {

   public enum Day {
      SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
   }

   public static void main(String[] args) {


      EnumSet<Day> weekend = EnumSet.of(Day.SATURDAY, Day.SUNDAY);
      System.out.println("주말 (Weekend): " + weekend);

      // allOf() 팩토리 메서드를 사용하여 해당 Enum 타입의 모든 상수를 포함
      EnumSet<Day> allDays = EnumSet.allOf(Day.class);
      System.out.println("모든 요일 (All Days): " + allDays);

      // range() 팩토리 메서드를 사용하여 시작 상수와 끝 상수를 지정
      EnumSet<Day> weekdays = EnumSet.range(Day.MONDAY, Day.FRIDAY);
      System.out.println("주중 (Weekdays): " + weekdays);

      // complementOf() 팩토리 메서드를 사용하여 기존 EnumSet에 포함되지 않은 나머지 상수들로 구성됩니다
      EnumSet<Day> nonWeekdays = EnumSet.complementOf(weekdays);
      System.out.println("주중이 아닌 요일 (Non-Weekdays): " + nonWeekdays);

      // EnumSet의 기본적인 Set 연산

      System.out.println("\n--- Set 연산 ---");

      // 집합에 원소 추가
      weekdays.add(Day.SUNDAY);
      System.out.println("SUNDAY를 추가한 주중: " + weekdays); // EnumSet은 순서를 유지합니다 (선언 순서)

      // 집합에서 원소 제거
      weekdays.remove(Day.SUNDAY);
      System.out.println("SUNDAY를 제거한 주중: " + weekdays);

      // 특정 원소 포함 여부 확인
      System.out.println("화요일(TUESDAY)이 주중에 포함되는가? " + weekdays.contains(Day.TUESDAY));
      System.out.println("토요일(SATURDAY)이 주중에 포함되는가? " + weekdays.contains(Day.SATURDAY));

      // 집합 순회 (Iteration)
      System.out.print("주말 요일 순회: ");
      for (Day day : weekend) {
         System.out.print(day + " ");
      }
      System.out.println();
   }
}
```
