# effective-java

## 스터디 운영 방안

- 모임 시간: 매주 금요일 22시
- 분량: 주 2 ~ 3개 아이템
- 운영 방식: 온라인

## 2장. 객체 생성과 파괴

| 아이템                                                                                                           | 담당자                                                                          | 질문 여부 |
|:--------------------------------------------------------------------------------------------------------------|:-----------------------------------------------------------------------------|:------|
| [아이템 1. 생성자 대신 정적 팩터리 메서드를 고려하라](https://github.com/peeljunKim/effective-java/discussions/49)                 | [김필준](https://github.com/peeljunKim/effective-java/blob/main/ch02/item01.md) | O     |
| [아이템 2. 생성자에 매개변수가 많다면 빌더를 고려하라](https://github.com/peeljunKim/effective-java/discussions/53)                 | [오지훈](https://github.com/peeljunKim/effective-java/blob/main/ch02/item02.md) | x     |
| [아이템 3. private 생성자나 열거 타입으로 싱글턴임을 보증하라](https://github.com/peeljunKim/effective-java/discussions/51)         | [김필준](https://github.com/peeljunKim/effective-java/blob/main/ch02/item03.md) | O     |
| [아이템 4. 인스턴스화를 막으려거든 private 생성자를 사용하라](https://github.com/peeljunKim/effective-java/discussions/57)          | [오지훈](https://github.com/peeljunKim/effective-java/blob/main/ch02/item04.md) | O     |
| [아이템 5. 자원을 직접 명시하지 말고 의존 객체 주입을 사용하라](https://github.com/peeljunKim/effective-java/discussions/54)           | [김필준](https://github.com/peeljunKim/effective-java/blob/main/ch02/item05.md) | x     |
| [아이템 6. 불필요한 객체 생성을 피하라](https://github.com/peeljunKim/effective-java/discussions/58)                         | [오지훈](https://github.com/peeljunKim/effective-java/blob/main/ch02/item06.md) | O     |
| [아이템 7. 다 쓴 객체 참조를 해제하라](https://github.com/peeljunKim/effective-java/discussions/55)                         | [김필준](https://github.com/peeljunKim/effective-java/blob/main/ch02/item07.md) | O     |
| [아이템 8. finalizer와 cleaner 사용을 피하라](https://github.com/peeljunKim/effective-java/discussions/59)              | [오지훈](https://github.com/peeljunKim/effective-java/blob/main/ch02/item08.md) | x     |
| [아이템 9. try-finally보다는 try-with-resources를 사용하라](https://github.com/peeljunKim/effective-java/discussions/56) | [김필준](https://github.com/peeljunKim/effective-java/blob/main/ch02/item09.md) | O     |

## 3장. 모든 객체의 공통 메서드

| 아이템                                                                                                    | 담당자 | 질문 여부 |
|:-------------------------------------------------------------------------------------------------------|:----|:------|
| [아이템 10. equals는 일반 규약을 지켜 재정의하라](https://github.com/peeljunKim/effective-java/discussions/61)         | 오지훈 | O     |
| [아이템 11. equals를 재정의하려거든 hashCode도 재정의하라](https://github.com/peeljunKim/effective-java/discussions/62) | 김필준 | O     |
| [아이템 12. toString을 항상 재정의하라](https://github.com/peeljunKim/effective-java/discussions/63)              | 오지훈 | O     |
| [아이템 13. clone재정의는 주의해서 진행하라](https://github.com/peeljunKim/effective-java/discussions/64)             | 김필준 | O     |
| [아이템 14. Comparable을 구현할지 고려하라](https://github.com/peeljunKim/effective-java/discussions/65)           | 오지훈 | O     |

## 4장. 클래스와 인터페이스

| 아이템                                                                                                              | 담당자 | 질문 여부 |
|:-----------------------------------------------------------------------------------------------------------------|:----|:------|
| [아이템 15. 클래스와 멤버의 접근 권한을 최소화하라](https://github.com/peeljunKim/effective-java/discussions/86)                     | 김필준 | O     |
| [아이템 16. public 클래스에서는 public 필드가 아닌 접근자 메서드를 사용하라](https://github.com/peeljunKim/effective-java/discussions/87) | 오지훈 | O     |
| [아이템 17. 변경 가능성을 최소화하라](https://github.com/peeljunKim/effective-java/discussions/88)                             | 김필준 | O     |
| [아이템 18. 상속보다는 컴포지션을 사용하라](https://github.com/peeljunKim/effective-java/discussions/89)                          | 오지훈 | O     |
| [아이템 19. 상속을 고려해 설계하고 문서화하라, 그러지 않았다면 상속을 금지하라](https://github.com/peeljunKim/effective-java/discussions/90)     | 김필준 | O     |
| [아이템 20. 추상 클래스보다는 인터페이스를 우선하라](https://github.com/peeljunKim/effective-java/discussions/91)                     | 오지훈 | O     |
| [아이템 21. 인터페이스는 구현하는 쪽을 생각해 설계하라](https://github.com/peeljunKim/effective-java/discussions/92)                   | 김필준 | O     |
| [아이템 22. 인터페이스는 타입을 정의하는 용도로만 사용하라](https://github.com/peeljunKim/effective-java/discussions/93)                 | 오지훈 | O     |
| [아이템 23. 태그 달린 클래스보다는 클래스 계층구조를 활용하라](https://github.com/peeljunKim/effective-java/discussions/94)               | 김필준 | O     |
| [아이템 24. 멤버 클래스는 되도록 static으로 만들라](https://github.com/peeljunKim/effective-java/discussions/95)                  | 오지훈 | O     |
| [아이템 25. 톱레벨 클래스는 한 파일에 하나만 담으라](https://github.com/peeljunKim/effective-java/discussions/96)                    | 김필준 | X     |

## 5장. 제네릭

| 아이템                                                                                                 | 담당자 | 질문 여부 |
|:----------------------------------------------------------------------------------------------------|:----|:------|
| [아이템 26. 로 타입은 사용하지 말라](https://github.com/peeljunKim/effective-java/discussions/97)                | 오지훈 | O     |
| [아이템 27. 비검사 경고를 제거하라](https://github.com/peeljunKim/effective-java/discussions/98)                 | 김필준 | X     |
| [아이템 28. 배열보다는 리스트를 사용하라](https://github.com/peeljunKim/effective-java/discussions/99)              | 오지훈 | O     |
| [아이템 29. 이왕이면 제네릭 타입으로 만들라](https://github.com/peeljunKim/effective-java/discussions/100)           | 김필준 | O     |
| [아이템 30. 이왕이면 제네릭 메서드로 만들라](https://github.com/peeljunKim/effective-java/discussions/101)           | 오지훈 | X     |
| [아이템 31. 한정적 와일드카드를 사용해 API 유연성을 높이라](https://github.com/peeljunKim/effective-java/discussions/102) | 김필준 | O     |
| [아이템 32. 제네릭과 가변인수를 함께 쓸 때는 신중하라](https://github.com/peeljunKim/effective-java/discussions/103)     | 오지훈 | O     |
| [아이템 33. 타입 안전 이종 컨테이너를 고려하라](https://github.com/peeljunKim/effective-java/discussions/104)         | 김필준 | O     |

## 6장. 열거 타입과 애너테이션

| 아이템                                                                                                      | 담당자 | 질문 여부 |
|:---------------------------------------------------------------------------------------------------------|:----|:------|
| [아이템 34. int 상수 대신 열거 타입을 사용하라](https://github.com/peeljunKim/effective-java/discussions/105)            | 오지훈 | X     |
| [아이템 35. ordinal 메서드 대신 인스턴스 필드를 사용하라](https://github.com/peeljunKim/effective-java/discussions/106)     | 김필준 | O     |
| [아이템 36. 비트 필드 대신 EnumSet을 사용하라](https://github.com/peeljunKim/effective-java/discussions/107)           | 오지훈 | O     |
| [아이템 37. ordinal 인덱싱 대신 EnumMap을 사용하라](https://github.com/peeljunKim/effective-java/discussions/108)     | 김필준 | O     |
| [아이템 38. 확장할 수 있는 열거 타입이 필요하면 인터페이스를 사용하라](https://github.com/peeljunKim/effective-java/discussions/109) | 오지훈 | O     |
| [아이템 39. 명명 패턴보다 애너테이션을 사용하라](https://github.com/peeljunKim/effective-java/discussions/110)              | 김필준 | O     |
| [아이템 40. @Override 애너테이션을 일관되게 사용하라](https://github.com/peeljunKim/effective-java/discussions/111)       | 오지훈 | O     |
| [아이템 41. 정의하려는 것이 타입이라면 마커 인터페이스를 사용하라](https://github.com/peeljunKim/effective-java/discussions/112)    | 김필준 | O     |

## 7장. 람다와 스트림

| 아이템                                                                                            | 담당자 | 질문 여부 |
|:-----------------------------------------------------------------------------------------------|:----|:------|
| [아이템 42. 익명 클래스보다는 람다를 사용하라](https://github.com/peeljunKim/effective-java/discussions/113)     | 오지훈 | O     |
| [아이템 43. 람다보다는 메서드 참조를 사용하라](https://github.com/peeljunKim/effective-java/discussions/114)     | 김필준 | X     |
| [아이템 44. 표준 함수형 인터페이스를 사용하라](https://github.com/peeljunKim/effective-java/discussions/115)     | 오지훈 | X     |
| [아이템 45. 스트림은 주의해서 사용하라](https://github.com/peeljunKim/effective-java/discussions/116)         | 김필준 | O     |
| [아이템 46. 스트림에서는 부작용 없는 함수를 사용하라](https://github.com/peeljunKim/effective-java/discussions/117) | 오지훈 | O     |
| [아이템 47. 반환 타입으로는 스트림보다 컬렉션이 낫다](https://github.com/peeljunKim/effective-java/discussions/118) | 김필준 | O     |
| [아이템 48. 스트림 병렬화는 주의해서 적용하라](https://github.com/peeljunKim/effective-java/discussions/119)     | 오지훈 | O     |

## 8장. 메서드

| 아이템                                                                                                  | 담당자 | 질문 여부 |
|:-----------------------------------------------------------------------------------------------------|:----|:------|
| [아이템 49. 매개변수가 유효한지 검사하라](https://github.com/peeljunKim/effective-java/discussions/120)              | 김필준 | O     |
| [아이템 50. 적시에 방어적 복사본을 만들라](https://github.com/peeljunKim/effective-java/discussions/121)             | 오지훈 | O     |
| [아이템 51. 메서드 시그니처를 신중히 설계하라](https://github.com/peeljunKim/effective-java/discussions/122)           | 김필준 | O     |
| [아이템 52. 다중정의는 신중히 사용하라](https://github.com/peeljunKim/effective-java/discussions/123)               | 오지훈 | O     |
| [아이템 53. 가변인수는 신중히 사용하라](https://github.com/peeljunKim/effective-java/discussions/124)               | 김필준 | O     |
| [아이템 54. null이 아닌 빈 컬렉션이나 배열을 반환하라](https://github.com/peeljunKim/effective-java/discussions/125)    | 오지훈 | O     |
| [아이템 55. 옵셔널 반환은 신중히 하라](https://github.com/peeljunKim/effective-java/discussions/126)               | 김필준 | O     |
| [아이템 56. 공개된 API 요소에는 항상 문서화 주석을 작성하라](https://github.com/peeljunKim/effective-java/discussions/127) | 오지훈 | X     |

## 9️장. 일반적인 프로그래밍 원칙

| 아이템                                                                                                     | 담당자 | 질문 여부 |
|:--------------------------------------------------------------------------------------------------------|:----|:------|
| [아이템 57. 지역변수의 범위를 최소화하라](https://github.com/peeljunKim/effective-java/discussions/128)                 | 김필준 | O     |
| [아이템 58. 전통적인 for 문보다는 for-each 문을 사용하라](https://github.com/peeljunKim/effective-java/discussions/129)  | 오지훈 | X     |
| [아이템 59. 라이브러리를 익히고 사용하라](https://github.com/peeljunKim/effective-java/discussions/130)                 | 김필준 | X     |
| [아이템 60. 정확한 답이 필요하다면 float와 double은 피하라](https://github.com/peeljunKim/effective-java/discussions/131) | 오지훈 | X     |
| [아이템 61. 박싱된 기본 타입보다는 기본 타입을 사용하라](https://github.com/peeljunKim/effective-java/discussions/132)        | 김필준 | O     |
| [아이템 62. 다른 타입이 적절하다면 문자열 사용을 피하라](https://github.com/peeljunKim/effective-java/discussions/133)        | 오지훈 | X     |
| [아이템 63. 문자열 연결은 느리니 주의하라](https://github.com/peeljunKim/effective-java/discussions/134)                | 김필준 | X     |
| [아이템 64. 객체는 인터페이스를 사용해 참조하라](https://github.com/peeljunKim/effective-java/discussions/135)             | 오지훈 | O     |
| [아이템 65. 리플렉션보다는 인터페이스를 사용하라](https://github.com/peeljunKim/effective-java/discussions/136)             | 김필준 | O     |
| [아이템 66. 네이티브 메서드는 신중히 사용하라](https://github.com/peeljunKim/effective-java/discussions/137)              | 오지훈 | O     |
| [아이템 67. 최적화는 신중히 하라](https://github.com/peeljunKim/effective-java/discussions/138)                     | 김필준 | O     |
| [아이템 68. 일반적으로 통용되는 명명 규칙을 따르라](https://github.com/peeljunKim/effective-java/discussions/139)           | 오지훈 | X     |

## 10장. 예외

| 아이템                                                                                                                   | 담당자 | 질문 여부 |
|:----------------------------------------------------------------------------------------------------------------------|:----|:------|
| [아이템 69. 예외는 진짜 예외 상황에만 사용하라](https://github.com/peeljunKim/effective-java/discussions/140)                           | 김필준 | X     |
| [아이템 70. 복구할 수 있는 상황에는 검사 예외를, 프로그래밍 오류에는 런타임 예외를 사용하라](https://github.com/peeljunKim/effective-java/discussions/141) | 오지훈 | O     |
| [아이템 71. 필요 없는 검사 예외 사용은 피하라](https://github.com/peeljunKim/effective-java/discussions/142)                           | 김필준 | O     |
| [아이템 72. 표준 예외를 사용하라](https://github.com/peeljunKim/effective-java/discussions/143)                                   | 오지훈 | O     |
| [아이템 73. 추상화 수준에 맞는 예외를 던져라](https://github.com/peeljunKim/effective-java/discussions/144)                            | 김필준 | X     |
| [아이템 74. 메서드가 던지는 모든 예외를 문서화하라](https://github.com/peeljunKim/effective-java/discussions/145)                         | 오지훈 | X     |
| [아이템 75. 예외의 상세 메시지에 실패 관련 정보를 담으라](https://github.com/peeljunKim/effective-java/discussions/146)                     | 김필준 | X     |
| [아이템 76. 가능한 한 실패 원자적으로 만들라](https://github.com/peeljunKim/effective-java/discussions/147)                            | 오지훈 | O     |
| [아이템 77. 예외를 무시하지 말라](https://github.com/peeljunKim/effective-java/discussions/148)                                   | 김필준 | O     |

## 11장. 동시성

| 아이템                                                                                                      | 담당자 | 질문 여부 |
|:---------------------------------------------------------------------------------------------------------|:----|:------|
| [아이템 78. 공유 중인 가변 데이터는 동기화해 사용하라](https://github.com/peeljunKim/effective-java/discussions/149)          | 오지훈 | O     |
| [아이템 79. 과도한 동기화는 피하라](https://github.com/peeljunKim/effective-java/discussions/150)                     | 김필준 | X     |
| [아이템 80. 스레드보다는 실행자, 태스크, 스트림을 애용하라](https://github.com/peeljunKim/effective-java/discussions/151)       | 오지훈 | X     |
| [아이템 81. wait 과 notify 보다는 동시성 유틸리티를 이용하라](https://github.com/peeljunKim/effective-java/discussions/152) | 김필준 | O     |
| [아이템 82. 스레드 안전성 수준을 문서화하라](https://github.com/peeljunKim/effective-java/discussions/153)                | 오지훈 | X     |
| [아이템 83. 지연 초기화는 신중히 사용하라](https://github.com/peeljunKim/effective-java/discussions/154)                 | 김필준 | O     |
| [아이템 84. 프로그램의 동작을 스레드 스케줄러에 기대지 말라](https://github.com/peeljunKim/effective-java/discussions/155)       | 오지훈 | O     |

## 1️2장. 직렬화

| 아이템                                                                                                                 | 담당자 | 질문 여부 |
|:--------------------------------------------------------------------------------------------------------------------|:----|:------|
| [아이템 85. 자바 직렬화의 대안을 찾으라](https://github.com/peeljunKim/effective-java/discussions/156)                             | 김필준 | O     |
| [아이템 86. Serializable 을 사용할 지는 신중히 결정하라](https://github.com/peeljunKim/effective-java/discussions/157)              | 오지훈 | O     |
| [아이템 87. 커스텀 직렬화 형태를 고려해보라](https://github.com/peeljunKim/effective-java/discussions/158)                           | 김필준 | X     |
| [아이템 88. readObject 메서드는 방어적으로 작성하라](https://github.com/peeljunKim/effective-java/discussions/159)                  | 오지훈 | X     |
| [아이템 89. 인스턴스 수를 통제해야 한다면 readResolve보다는 열거 타입을 사용하라](https://github.com/peeljunKim/effective-java/discussions/160) | 김필준 | O     |
| [아이템 90. 직렬화된 인스턴스 대신 직렬화 프록시 사용을 검토하라](https://github.com/peeljunKim/effective-java/discussions/161)               | 오지훈 | O     |
