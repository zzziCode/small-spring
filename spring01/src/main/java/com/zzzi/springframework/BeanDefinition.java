package com.zzzi.springframework;

/**@author zzzi
 * @date 2023/10/29 20:52
 * 每一个类中就保存一个bean对象
 */
public class BeanDefinition {
    private Object bean;

    public BeanDefinition() {
    }
    /**@author zzzi
     * @date 2023/10/29 20:53
     * 通过构造函数注入bean
     */
    public BeanDefinition(Object bean) {
        this.bean = bean;
    }
    /**@author zzzi
     * @date 2023/10/29 20:53
     * 向外部返回当前类中保存的bean
     */
    public Object getBean(){
        return bean;
    }
}
