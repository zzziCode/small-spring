package com.zzzi.springframework.context;

import com.zzzi.springframework.beans.BeansException;
/**@author zzzi
 * @date 2023/11/3 12:49
 * 在这里新增一个refresh的待实现方法，在里面整个之前项目中的DefaultListableBeanFactory
 * 和修改模块
 */
public interface ConfigurableApplicationContext extends ApplicationContext{
    void refresh() throws BeansException;
}
