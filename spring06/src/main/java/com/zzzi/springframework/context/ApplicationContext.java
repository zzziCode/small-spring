package com.zzzi.springframework.context;

import com.zzzi.springframework.beans.factory.ListableBeanFactory;
/**@author zzzi
 * @date 2023/11/3 12:48
 * 应用上下文的最顶层的接口
 * 继承ListableBeanFactory这个类的目的是为了得到spring的整体框架
 */
public interface ApplicationContext extends ListableBeanFactory {
}
