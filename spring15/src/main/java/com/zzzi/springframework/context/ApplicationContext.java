package com.zzzi.springframework.context;

import com.zzzi.springframework.beans.factory.HierarchicalBeanFactory;
import com.zzzi.springframework.beans.factory.ListableBeanFactory;
import com.zzzi.springframework.core.io.ResourceLoader;

/**@author zzzi
 * @date 2023/11/4 14:58
 * 在这定义应用上下文的顶层接口
 */
public interface ApplicationContext extends ListableBeanFactory, HierarchicalBeanFactory, ResourceLoader, ApplicationEventPublisher{
}
