package com.zzzi.springframework.beans.factory.config;

/**@author zzzi
 * @date 2023/10/30 12:41
 * 给项目中增加一个自定义的异常类
 * 重载三个构造方法
 */
public class BeansException extends RuntimeException {

    public BeansException() {
    }

    public BeansException(String message) {
        super(message);
    }

    public BeansException(String message, Throwable cause) {
        super(message, cause);
    }
}
