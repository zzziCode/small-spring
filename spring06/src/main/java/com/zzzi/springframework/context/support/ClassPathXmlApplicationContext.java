package com.zzzi.springframework.context.support;

/**
 * @author zzzi
 * @date 2023/11/3 13:12
 * 这里是对外暴露的接口
 */
public class ClassPathXmlApplicationContext extends AbstractXmlApplicationContext {
    private String[] configLocations;

    //提供三种形式的构造函数
    public ClassPathXmlApplicationContext(String[] configLocations) {
        this.configLocations = configLocations;
        /**@author zzzi
         * @date 2023/11/3 13:19
         * 调用这个函数，在这里整合DefaultListableBeanFactory类和修改模块
         */
        refresh();
    }

    public ClassPathXmlApplicationContext(String configLocation) {
        this(new String[]{configLocation});
    }

    //配置文件一定要指定，所有这个构造函数用不到
    public ClassPathXmlApplicationContext() {
    }


    @Override
    protected String[] getConfigLocations() {
        return configLocations;
    }
}
