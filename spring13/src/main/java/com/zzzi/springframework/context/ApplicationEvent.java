package com.zzzi.springframework.context;

import java.util.EventObject;

/**@author zzzi
 * @date 2023/11/8 13:14
 * 一个过渡类，ApplicationContextEvent会继承这个类
 */
public abstract class ApplicationEvent extends EventObject {
    public ApplicationEvent(Object source) {
        super(source);
    }
}
