package com.effective.demo.ch02.sourcecode;

class AppLogger implements Logger {
    private final String name;

    public AppLogger(String name) {
        this.name = name;
        System.out.println(name + ": 콘솔 로거 인스턴스 생성");
    }

    @Override
    public void info(String message) {
        System.out.println("[" + name + "] [INFO] " + message);
    }

    @Override
    public void warn(String message) {
        System.err.println("[" + name + "] [WARN] " + message); // 에러 스트림 사용
    }

    @Override
    public void error(String message) {
        System.err.println("[" + name + "] [ERROR] " + message); // 에러 스트림 사용
    }
}