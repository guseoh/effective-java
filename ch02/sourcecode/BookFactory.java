package com.effective.demo.ch02.sourcecode;

// 정적 팩토리 메소드를 제공하는 객체(사용자한테 노출)
public class BookFactory {

    // 정적 팩토리 메소드1
    public static Book createBasicBook(String title, String author) {
        // 내부적으로 BasicBook 클래스의 인스턴스를 생성하여 반환
        return new BasicBook(title, author);
    }

    // 정적 팩토리 메소드2
    public static Book createEBook(String title, String author, String format) {
        return new EBook(title, author, format);
    }
}