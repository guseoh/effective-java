package com.effective.demo.ch02.sourcecode;

// API 제공 시 사용자한테 노출되는 않은 객체(클래스)
class BasicBook implements Book {
    private String name;
    private String author;

    // 접근 제어자가를 따로 입력하지 않은 package-private 접근 제어자를 사용하기 때문에 같은 패키지에서만 사용 가능
    BasicBook(String name, String author) {
        this.name = name;
        this.author = author;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public String getType() {
        return "일반 도서";
    }

    @Override
    public void read() {
        System.out.println(getType() + " '" + name + "'를 읽습니다.");
    }
}