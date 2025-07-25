package com.effective.demo.ch02.sourcecode;

public class Fiction extends Book2 {

    private String original;

    // There is no default constructor available in 오류 발생
    public Fiction(String name, String author, String publisher, String original) {
        // super(...) 호출 불가 즉, Book2의 필드를 정의 할 수 없습니다.
        this.original = original;
    }
}