package com.zzzi.springframework.beans.factory.config;

/**@author zzzi
 * @date 2023/10/31 19:06
 * 如果一个bean依赖另外一个bean，就在这里保存这个被依赖的bean的姓名
 * 也就是保存一个bean的引用
 */
public class BeanReference {
    private String beanName;

    public BeanReference(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }
}
