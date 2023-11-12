package com.zzzi.springframework.beans;
/**@author zzzi
 * @date 2023/11/4 13:05
 * 在这里实现一个自定义的异常类
 */
public class BeansException extends RuntimeException{
    public BeansException(String message) {
        super(message);
    }

    public BeansException(String message, Throwable cause) {
        super(message, cause);
    }
}
