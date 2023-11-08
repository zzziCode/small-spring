package com.zzzi.springframework.event;

import com.zzzi.springframework.context.event.ApplicationContextEvent;
/**@author zzzi
 * @date 2023/11/8 14:13
 * 自定义的事件，外部可以传递两个参数，对应的监听器可以拿到
 */
public class CustomEvent extends ApplicationContextEvent {
    private Integer id;
    private String message;

    public CustomEvent(Object source, Integer id, String message) {
        super(source);
        this.id = id;
        this.message = message;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
