### ✅ equals를 재정의하려거든 hashCode도 재정의해라
- equals : 두 객체의 **내용이 같은지**, 동등성(equality)를 비교하는 메서드
- hashCode : 두 객체가 **같은 객체**인지, 동일성(identity)를 비교하는 메서드

### Object 명세 내용
1. equals 비교에 사용되는 정보가 변경되지 않았다면, 애플리케이션이 실행되는 동안 그 객체의 hashCode 메서드는 몇 번을 호출해도 일관되게 항상 같은 값을 반환해야 한다. 
단, 애플리케이션을 다시 실행한다면 이 값이 달라져도 상관없다.
2. equals(Object)가 두 객체를 같다고 판단했다면, 두 객체의 hashCode는 똑같은 값을 반환해야 한다.
3. equals(Object)가 두 객체를 다르다고 판단했더라도, 두 객체의 hashCode가 서로 다른 값을 반환할 필요는 없다. 
단, 다른 객체에 대해서는 다른 값을 반환해야 해시테이블의 성능이 좋아진다.

이 두 메서드는 함께 사용될 때 매우 중요한 규칙을 가지는데요 바로 **equals가 true를 반환하는 두 객체는 반드시 동일한 hashCode 값을 가져야 한다**는 것입니다.
그런데 만약 `equals`만 오버라이드하고 `hashCode`를 오버라이드하지 않는다면 이 규칙이 깨질 수 있어 
`HashMap`, `HashSet`과 같은 Hash 기반 컬렉션에서 예상치 못한 문제를 발생할 수 있습니다. 
예를 들어, 분명 같은 내용의 객체를 넣었는데도 중복으로 저장되거나 검색이 되지 않는 문제가 발생할 수 있습니다.
```java
class Person {
    private String name;
    private int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    // equals만 오버라이딩
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Person)) return false;
        Person other = (Person) obj;
        return this.name.equals(other.name) && this.age == other.age;
    }

    // hashCode는 오버라이딩하지 않음
}

public class Main {
    public static void main(String[] args) {
        Set<Person> set = new HashSet<>();
        Map<PhonedNumber, String> map = new HashMap<>();
        
        Person p1 = new Person("Kim", 20);
        Person p2 = new Person("Kim", 20);
        map.put(new PhonedNumber(123, 123, 1234), "peel");
        
        set.add(p1);
        set.add(p2); // equals로 보면 중복된 객체

        System.out.println("set 크기: " + set.size()); // 기대: 1, 실제: 2

        // 포함 여부 확인
        System.out.println("map.get(new PhonedNumber(123, 123, 1234)): " + map.get(new PhonedNumber(123, 123, 1234))); // 기대값: peel, 실제값 null
        System.out.println("set.contains(new Person(\"Kim\", 20)): " + set.contains(new Person("Kim", 20))); // 기대: true, 실제: false
    }
}
```
이런 결과가 나온 이유는 두 객체가 `equals()`로 논리적으로 동일하더라도 `hashCode()`를 재정의 하지 않아서 hashCode가 다르면 해시 
기반 컬렉션의 일반적인 규약을 위반하게 됩니다. 
이로 인해 같은 값을 가진 객체라도 서로 다른 해시 버킷에 저장되거나, `get()` 시 올바른 위치를 찾지 못해 조회에 실패하게 됩니다.
특히 `HashMap`은 내부적으로 해시 버킷을 먼저 탐색하고, 해당 버킷에 있는 엔트리들과 `equals()` 비교를 하기 때문에, 
애초에 해시값이 다르면 해당 버킷 자체에 접근조차 하지 않습니다. 
따라서 `equals()`만 재정의하고 `hashCode()`를 재정의하지 않으면 
논리적으로 동일한 객체라도 조회 시 `null`이 반환되는 문제가 발생할 수 있습니다.

서로 다른 인스턴스들 32비트 정수 범위에 균일하게 분배

### 🚫 hashCode 최악의 시나리오
```java
  @Override public int hashCode() { return 42; }
```
- 모든 객체가 해시테이블의 버킷 하나에 담겨 LinkedList 처럼 동작하게 됩니다. 평균 수행 시간이 O(1) -> O(n)으로 느려져서 객체가 많아지면 쓸 수 없게 됩니다.


### 🙆🏼 이상적인 hashCode 오버라이딩 방법
서로 다른 인스턴스에 다른 해시코드를 반환해야 Hash 관련 컬렉션 성능이 좋아집니다. (세번째 규약)

좋은 `hashCode`를 작성하는 간단한 요령
1. int 변수 result를 선언하고 첫 번째 핵심 필드의 해시 코드로 초기화합니다.
2. 나머지 핵심 필드 f 각각에 대해 다음 과정을 반복합니다.  
   a. f 필드의 해시코드 c를 계산합니다.
    1. 기본 타입 필드라면, Type.hashCode(f)를 수행 (Type: 해당 기본 타입의 박싱 클래스)
    2. 참조 타입 필드면서 이 클래스의 equals 메서드가 이 필드의 equals를 재귀적으로 호출해 비교한다면, 이 필드의 hashCode 를 재귀적으로 호출한다. 계산이 더 복잡해질 것 같으면, 이 필드의 표준형을 만들어 그 표준형의 hashCode를 호출한다. 필드의 값이 null이면 0을 사용한다. (보통 0을 사용)
    3. 필드가 배열이라면, 핵심 원소 각각을 별도 필드처럼 다룬다. 이상의 규칙을 재귀적으로 적용해 각 핵심 원소의 해시코드를 계산한 다음, (2.b)단계 방식으로 갱신한다. 배열에 핵심 원소가 하나도 없다면 단순히 상수(0을 추천)를 사용한다. 모든 원소가 핵심 원소라면 Arrays.hashCode를 사용한다.

   b. (2.a) 단계에서 계산한 해시코드 c 로 result를 갱신

``` java
   result = **31** \* result + c;
```
31 을 곱하는 이유 : 31은 홀수이며 소수이기 때문이다.
- 곱할 숫자가 짝수고 오버플로가 발생하면→ 2를 곱하면 시프트 연산과 같은 결과를 주기 때문에 정보를 잃게 된다.
- 소수를 곱하는것은 전통적으로 해온 방식(나머지연산에서 충돌을 줄이기 위한방법으로 주로 사용)이다.
- \*31은 시프트연산과 뺄셈으로 대체해 최적화 할 수 있다.
  - 31\*i == (i<<5)-i

3. 최종적으로 result 를 반환한다.

### 주의사항
- equals 메서드에 사용되지 않는 필드는 hashCode 계산에서도 제외해야 합니다. (두번째 규약내용)
- hashCode가 반환하는 값의 생성 규칙을 외부에 공개하지 않는 것이 좋습니다.
- 추후에 계산 방식을 자유롭게 바꿀 수 있습니다.

### int 예제 코드
```java
    @Override public int hashCode() {
        int result = Short.hashCode(areaCode);
        result = 31 * result + Short.hashCode(prefix);
        result = 31 * result + Short.hashCode(lineNum);
        return result;
    }
  
    public static void main(String[] args) {
        Map<PhoneNumber, String> m = new HashMap<>();
        m.put(new PhoneNumber(707, 867, 5309), "Jenny");
  
        System.out.println(m.get(new PhoneNumber(707, 867, 5309)));
        // hashCode를 재정의 했으므로 "Jenny" 가 나온다
    }
```
`equals`에 사용되는 `areaCode`, `prefix`, `lineNum` 세 필드를 모두 사용해 해시 코드를 계산하면, 
`HashMap` 같은 컬렉션에서 `new PhoneNumber(707, 867, 5309)` 객체를 키로 사용했을 때 "Jenny"라는 값을 제대로 찾을 수 있습니다.

###  String 타입의 hashCode 예제 코드
```java
  @Override
  public int hashCode() {
      int result = name.hashCode(); // 
      result = 31 * result + gender.hashCode();
      result = 31 * result + age.hashCode();
      System.out.println("좋은 해시코드 구현 방법 : "+result+"  Object의 hash : "+ Objects.hash(name, gender, age));
      return result;
  }
    @DisplayName("hashCode 만들기")
    @Test
    void hashCode_테스트(){
        Member jessi = new Member("Jessi", "woman", "22");
        Member jessiCopy = new Member("Jessi", "woman", "22");
        assertEquals(jessi.hashCode(), jessiCopy.hashCode());
    }
```
해시 충돌이 적은 방법을 써야 한다면 [com.google.common.hash.Hashing](https://guava.dev/releases/21.0/api/docs/com/google/common/hash/Hashing.html) 를 참고

### 캐싱을 이용한 hashCode 예시
클래스가 불변이고 hashCode() 계산 비용이 크다면, 해시 코드를 캐싱하여 성능을 향상시킬 수 있습니다.
```java
      private int hashCode;
  
      @Override 
      public int hashCode() {
          int result = this.hashCode;
          if(result == 0){ 
              result = Short.hashCode(areaCode);
              result = 31 * result + Short.hashCode(prefix);
              result = 31 * result + Short.hashCode(lineNum);
              this.hashCode = result;
          }
          return result;
      }
```
- **즉시 초기화:** 객체가 생성되는 시점에 해시 코드를 계산하여 저장합니다.
- **지연 초기화:** `hashCode()`가 처음 호출될 때만 계산하고 저장합니다. 아래 예시는 이 방식을 사용합니다.

