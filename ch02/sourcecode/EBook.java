package com.effective.demo.ch02.sourcecode;

// API 제공 시 사용자한테 노출되는 않은 객체(클래스)
class EBook implements Book {
    private String name;
    private String author;
    private String format; // PDF

    EBook(String name, String author, String format) {
        this.name = name;
        this.author = author;
        this.format = format;
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
        return "전자책 (" + format + ")";
    }

    @Override
    public void read() {
        System.out.println(getType() + " '" + name + "'를 읽습니다.");
    }
}