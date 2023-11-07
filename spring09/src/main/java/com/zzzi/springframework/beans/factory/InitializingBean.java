package com.zzzi.springframework.beans.factory;

/**@author zzzi
 * @date 2023/11/4 13:43
 * 实现这个接口可以实现初始化逻辑
 */
public interface InitializingBean {
    void afterPropertiesSet() throws Exception;
}
