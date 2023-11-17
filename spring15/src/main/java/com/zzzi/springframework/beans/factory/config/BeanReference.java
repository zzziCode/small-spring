package com.zzzi.springframework.beans.factory.config;
/**@author zzzi
 * @date 2023/11/4 16:48
 * 当一个bean对象依赖于另外一个bean对象时，就用这个对象记录依赖关系
 */
public class BeanReference {
    private final String beanName;

    public String getBeanName() {
        return beanName;
    }

    public BeanReference(String beanName) {
        this.beanName = beanName;
    }
}
