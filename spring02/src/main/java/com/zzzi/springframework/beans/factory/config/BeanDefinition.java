package com.zzzi.springframework.beans.factory.config;

/**@author zzzi
 * @date 2023/10/30 12:35
 * 这个类主要保存的是bean对象的类信息
 * 并对外提供api实现bean类信息的保存以及获取
 */
public class BeanDefinition {
    private Class beanClass;

    /**@author zzzi
     * @date 2023/10/30 12:36
     * 使用构造函数对外提供api保存当前bean的类信息
     */
    public BeanDefinition(Class beanClass) {
        this.beanClass = beanClass;
    }
    /**@author zzzi
     * @date 2023/10/30 12:37
     * 对外提供get和set方法实现对类信息的操作
     */

    public Class getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }
}
