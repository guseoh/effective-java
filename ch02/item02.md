- 정적 팩터리와 생성자에는 똑같은 제약이 있다.
    - 선택적 매개변수가 많을 때 적절히 대응하기 어렵다는 점이다.
- 예를 들어, 식품 영양 정보의 경우 트랜스지방, 포화지방 등 20개가 넘는 선택 항목으로 이뤄진다.
    - 그런데 대부분 제품은 이 선택 항목 중 대다수의 값이 그냥 0이다.
- 이러한 케이스에 `점층적 생성자 패턴`, `자바 빈즈 패턴`, `빌더 패턴`을 고려할 수 있다.

### 점층적 생성자 패턴 (telescoping constructor pattern)

- 필수 매개변수만 받는 생성자, 필수 매개변수와 선택 매개변수 1개를 받는 생성자,  ••• 형태로 선택 **매개변수를 전부 다 받는 생성자까지 늘려가는 방식**
    - **확장하기 어렵다**
- 점층적 생성자 패턴 예시
    
    ```java
    public class NutritionFacts {
        private final int servingSize;  // (mL, 1회 제공량)     필수
        private final int servings;     // (회, 총 n회 제공량)  필수
        private final int calories;     // (1회 제공량당)       선택
        private final int fat;          // (g/1회 제공량)       선택
        private final int sodium;       // (mg/1회 제공량)      선택
        private final int carbohydrate; // (g/1회 제공량)       선택
    
        public NutritionFacts(int servingSize, int servings) {
            this(servingSize, servings, 0);
        }
    
        public NutritionFacts(int servingSize, int servings,
                              int calories) {
            this(servingSize, servings, calories, 0);
        }
    
        public NutritionFacts(int servingSize, int servings,
                              int calories, int fat) {
            this(servingSize, servings, calories, fat, 0);
        }
    
        public NutritionFacts(int servingSize, int servings,
                              int calories, int fat, int sodium) {
            this(servingSize, servings, calories, fat, sodium, 0);
        }
        public NutritionFacts(int servingSize, int servings,
                              int calories, int fat, int sodium, int carbohydrate) {
            this.servingSize  = servingSize;
            this.servings     = servings;
            this.calories     = calories;
            this.fat          = fat;
            this.sodium       = sodium;
            this.carbohydrate = carbohydrate;
        }
    }
    ```
    
    - 이 클래스의 인스턴스를 만들려면 원하는 매개변수를 모두 포함한 생성자 중 가장 짧은 것을 골라 호출하면 된다.
- 인스턴스 생성
    
    ```java
    NutritionFacts cocaCola = new NutritionFacts(240, 8, 100, 0, 35, 27);
    ```
    
    - 원치 않는 매개변수까지 포함하여 값을 지정해줘야 한다.

- 매개변수 개수가 많아지면 클라이언트 코드를 작성하거나 읽기 어렵다.
    - 각 값의 의미가 무엇인지 헷갈릴 것
    - 매개변수가 몇 개인지도 주의해서 세어야 할 것
    - 순서가 바뀌게 된다면 컴파일러는 알아채지 못 한다.

### 자바빈즈 패턴 (JavaBeans pattern)

- 매개변수가 없는 생성자로 객체를 만든 후, `세터(setter)` 메서드들을 호출해 원하는 매개변수의 값을 설정하는 방식이다.
    - **일관성이 깨지고, 불변으로 만들 수 없다.**
- 자바빈즈 패턴 예시
    
    ```java
    public class NutritionFacts {
    	private int servingSize = -1;  // 필수
    	private int servings = -1;     // 필수
    	private int calories = 0;
    	private int fat = 0;
    	private int sodium = 0;
    	private int carbohydrate = 0;
    
    	public NutritionFacts() {}
    
    	public void setServingSize(int val) { servingSize = val; }
    	public void setServings(int val) { servings = val; }
    	public void setCalories(int val) { calories = val; }
    	public void setFat(int val) { fat = val; }
    	public void setSodium(int val) { sodium = val; }
    	public void setCarbohydrate(int val) {carbohydrate = val; }
    }
    ```
    
    - 점층적 생성자 패턴의 단점들이 보이지 않는다.
        - 코드가 길어지긴 했지만 인스턴스를 만들기 쉽고, 그 결과 더 읽기 쉬운 코드가 되었다.
- 자바빈즈 패턴 심각한 단점
    
    ```java
    NutritionFacts cocaCola = new NutritionFacts();
      cocaCola.setServingSize(240);
      cocaCola.setServings(8);
      cocaCola.setCalories(100);
      cocaCola.setSodium(35);
      cocaCola.setCarbohydrate(27);
    ```
    
    - 객체 하나를 만들려면 `메서드를 여러 개 호출`해야 한다.
    - 객체가 완전히 생성되기 전까지는 `일관성이 무너진 상태`에 놓이게 된다.
    - 일관성이 깨진 객체를 만들면 버그를 심은 코드와 그 버그 때문에`디버깅도 쉽지 않다.`
    - **이러한 문제들로 클래스를 불변으로 만들 수 없으며, 스레드 안전성을 얻으려면 프로그래머가 추가 작업을 해줘여한다.**
- 단점을 완화하고자 생성이 끝난 객체를 수동으로 얼리고(freezing), 얼리기 전에는 사용할 수 없도록 하기도 한다.
    - 이 방법은 실전에서 사용하지 않는다.

### 빌더 패턴 (Builder pattern)

- 필요한 객체를 직접 만드는 대신, `필수 매개변수`만으로 생성자를 호출해 빌더 객체를 얻는다.
    - 빌더 객체가 제공하는 일종의 세터 메서드들로 원하는 선택 매개변수들을 설정한다.
    - 매개변수가 없는 build 메서드를 호출해 객체를 얻는다.
- 빌더 패턴 예시
    
    ```java
    public class NutritionFacts {
    	private final int servingSize;
    	private final int servings;
    	private final int calories;
    	private final int fat;
    	private final int sodium;
    	private final int carbohydrate;
    
    	public static class Builder {
    		private final int servingSize;  // 필수
    		private final int servings;     // 필수
    		private int calories = 0;
    		private int fat = 0;
    		private int sodium = 0;
    		private int carbohydrate = 0;
    
    		public Builder(int servingSize, int servings) {
    			this,servingSize = serginsSize;
    			this.servings = servings;
    		}
    
    		public Builder fat(int val) {
    			fat = val;
    			return this;
    		}
    
    		public Builder sodium(int val) {
    			sodium = val;
    			return this;
    		}
    
    		public Builder carbohydrate(int val) {
    			carbohydrate = val;
    			return this;
    		}
    
    		public NutritionFacts build() {
    			return new NutritionFacts(this);
    		}
    	}
    
    	private NutirionFacts(Builder builder) {
    		servingSize = builder.servingSize;
    		servings = builder.servings;
    		calories = builder.calories;
    		fat = builder.fat;
    		sodium = builder.fat;
    		carbohydrate = builder.carbohydrate;
    	}
    }
    ```
    
    - NutirionFacts 클래스는 불변이며, 모든 매개변수의 기본값들을 한곳에 모아뒀다.
    - 빌더의 세터 메서드들은 빌더 자신을 반환하기 때문에 연쇄적으로 호출할 수 있다.
        - 이러한 방식을 `플루언트 API(fluend API)` 혹은 `메서드 연쇄(method chaining)`이라 한다.
- 인스터스 생성
    
    ```java
    NutritionFacts cocaCola = new NutritionFacts.Builder(240, 8)
    	.calories(100)
    	.sodium(35)
    	.carbohydrate(27)
    	.build();
    ```
    
    - 코드는 쓰기 쉽고, 무엇보다도 읽기 쉽다.
    - 빌더패턴은 명명된 선택적 매개변수를 흉내 낸 것이다.
- 유효성 검사
    - 잘못된 매개변수를 최대한 일찍 발견하려면 빌더의 생성자와 메서드에서 입력 매개변수를 검사하고,
        - build 메서드가 호출하는 생성자에서 여러 매개변수에 걸친 불변식을 검사한다.
    - 이런 **불변식**을 보장하려면
        - 빌더로부터 매개변수를 복사한 후 해당 객체 필드들도 검사해야 한다.
        - 어떤 매개변수가 잘못되었는지를 알려주는 메시지를 담아 `IllegalArgumentException`을 던지면 된다.

### 계층적 빌더 패턴

- 빌더 패턴은 계층적으로 설계된 클래스와 함께 쓰기에 좋다.
- 계층적 빌더 패턴 예시
    
    ```java
    public abstract class Pizza {
    	public enum Topping { HAM, MUSHROOM, ONION, PEPPER, SAUSAGE }
    	final Set<Topping> toppings;
    
    	abstract static class Builder<T extends Builder<T>> {
    		EnumSet<Topping> toppings = EnumSet.noneOf(Topping.class);
    		public T addTopping(Topping topping) {
    			toppings.add(Objects.requireNonNull(topping));
    			return self(); // 제네릭으로 자기 자신의 타입을 반환
    		}
    
    		abstract Pizza build();
    
    		protected abstract T self(); // 하위 클래스에서 구현
    	}
    
    	Pizza(Builder<?> builder) {
    		toppings = builder.toppings.clone();
    	}
    }
    ```
    
    - Pizza.Builder 클래스는 [재귀적 타입 한정](https://www.notion.so/23a7c8e312368074860cf940a96f02af?pvs=21)을 이용하는 제네릭 타입
        - 추상 메서드인 self를 더해 하위 클래스에서 형변환하지 않고도 메서드 연쇄를 지원할 수 있다.
    
    ```java
    public class NyPizza extends Pizza {
    	public enum Size { SMALL, MEDIUM, LARGE }
    	private final Size size;
    
    	public static class Builder extends Pizza.Builder<Builder> {
    		private final Size size;
    
    		public Builder(Size size) {
    			this.size = Objects.requireNonNull(size);
    		}
    
    		@Override
    		public NyPizza build() {
    			return new NyPizza(this);
    		}
    
    		@Override
    		protected Builder self() {
    			return this;		
    		}
    
    		public NyPizza(Builder builder) {
    			super(builder);
    			size = builder.size;
    		}
    	}
    }
    ```
    
    - 각 하위 클래스의 빌더가 정의한 build 메서드는 해당하는 구체 하위 구체 클래스를 반환하도록 선언
        - NyPizza.Builder는 NyPizza를 반환한다는 뜻
    - 하위 클래스의 메서드가 상위 클래스의 메서드가 정의한 반환 타입이 아닌, 그 하위 타입을 반환하는 기능
        - `공변 반환 타이핑(covariant return typing)`이라 한다.
        - 이 기능을 이용하면 클라이언트가 형변환에 신경 쓰지 않고도 빌더를 사용할 수 있다.
- 인스턴스 생성
    
    ```java
    // 열거 타입 상수를 정적 임포트
    NyPizza pizza = new NyPizza.Builder(SMALL)
    	.addTopping(SAUSAGE)
    	.addTopping(ONION)
    	.build();
    ```
    
    - 빌더를 이용하면 가변인수(varargs) 매개변수를 여러 개 사용할 수 있다.
        - 각각을 적절한 메서드로 나눠 선언하면 된다.
        - 아니면 메서드를 여러 번 호출하도록 하고 각 호출 때 넘겨진 매개변수들을 하나의 필드로 모을 수도 있다.

### +) lombok

- [Java Lombok 라이브러리의 @Builder 어노테이션](https://projectlombok.org/features/Builder)을 통해 간편하게 빌더를 만들 수 있다.
- 예시
    
    ```java
    import lombok.Builder;
    
    @Builder // 에노테이션만 작성해주면 builder()와 build() 메서드 자동 생성해준다.
    public class NutritionFacts {
    
        private int servingSize = -1;  // 필수
        private int servings = -1;     // 필수
        private int calories = 0;
        private int fat = 0;
        private int sodium = 0;
        private int carbohydrate = 0;
    
        public NutritionFacts(int servingSize, int servings) {
            this.servingSize = servingSize;
            this.servings = servings;
        }
    }
    ```
    

### 용어

- **재귀적 타입 한정 (Recursive Type Bound)**
    
    ```java
    abstract static class Builder<T extends Builder<T>> {
    ```
    
    - **자기 자신의 타입을 제네릭 파라미터로 받는 형태**
        - 이를 재귀적 타입 한정
