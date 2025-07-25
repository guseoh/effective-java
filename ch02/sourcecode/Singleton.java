package com.effective.demo.ch02.sourcecode;

public class Singleton {
    private final Logger loggerInstance;

    // 인스턴스 단 하나만 생성
    public static final Singleton INSTANCE = new Singleton();

    private Singleton() {
        this.loggerInstance = new AppLogger("cmder");
        System.out.println("cmder 싱글턴 인스턴스가 생성");
    }

    public void logInfo(String message) {
        loggerInstance.info(message);
    }

    public void logWarn(String message) {
        loggerInstance.warn(message);
    }

    public void logError(String message) {
        loggerInstance.error(message);
    }
}
