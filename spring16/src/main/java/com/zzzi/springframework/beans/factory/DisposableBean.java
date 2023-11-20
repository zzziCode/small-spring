package com.zzzi.springframework.beans.factory;

/**@author zzzi
 * @date 2023/11/4 13:42
 * 实现这个接口就可以实现销毁逻辑
 */
public interface DisposableBean {
    void destroy() throws Exception;
}
