package com.zzzi.springframework.beans.factory.config;


public class BeansException extends RuntimeException {
    public BeansException(String msg) {
        super(msg);
    }

    public BeansException(String s, ReflectiveOperationException e) {
        super(s, e);
    }
}
