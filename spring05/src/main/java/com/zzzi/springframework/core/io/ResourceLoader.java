package com.zzzi.springframework.core.io;
/**@author zzzi
 * @date 2023/11/1 14:45
 * 在这里实现统一的资源获取
 * 内部调用三种资源获取的方式
 */
public interface ResourceLoader {
    String CLASSPATH_URL_PREFIX="classpath";
    Resource getResource(String location);
}
