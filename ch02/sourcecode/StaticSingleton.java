package com.effective.demo.ch02.sourcecode;

public class StaticSingleton {
    private static StaticSingleton instance;

    private StaticSingleton() {
        System.out.println("인스턴스가 생성");
    }

    // 정적 팩토리 메소드
    public static StaticSingleton getInstance() throws Exception {
        if (instance == null) {
            instance = new StaticSingleton();
        } else {
            // 인스턴스가 이미 존재한다면, 예외를 발생시킵니다.
            throw new Exception("인스턴스는 이미 존재");
        }
        return instance;
    }
}