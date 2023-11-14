package com.zzzi.springframework.core.io;

import java.io.IOException;
import java.io.InputStream;

/**@author zzzi
 * @date 2023/11/4 14:40
 * 在这里添加获取配置文件输入流的方法
 */
public interface Resource {
    InputStream getInputStream() throws IOException;
}
