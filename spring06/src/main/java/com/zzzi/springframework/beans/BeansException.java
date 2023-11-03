package com.zzzi.springframework.beans;
/**@author zzzi
 * @date 2023/11/1 13:42
 * 定义全局的自定义异常类
 */
public class BeansException extends RuntimeException{
    public BeansException(String message) {
        super(message);
    }

    public BeansException(String message, Throwable cause) {
        super(message, cause);
    }
}
