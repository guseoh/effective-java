package com.effective.demo.ch02.sourcecode;

// API 제공 시 사용자한테 노출되는 객체(인터페이스)
public interface Book {
    String getName();
    String getAuthor();
    String getType();
    void read();
}