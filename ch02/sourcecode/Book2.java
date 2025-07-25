package com.effective.demo.ch02.sourcecode;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Book2 {

    private String name;
    private String author;
    private String publisher;

    // private 이용하여 외부에서 생성자 호출 차단
    private Book2(String name, String author, String publisher) {
        this.name = name;
        this.author = author;
        this.publisher = publisher;
    }

    // 정적 팩터리 메소드
    public static Book2 createBookByName(String name) {
        return new Book2(name, null, null);
    }


    // 정적 팩토리 메소드
    public static Book2 createBookByAuthor(String author) {
        return new Book2(null, author, null);
    }

}
