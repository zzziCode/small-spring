package com.zzzi.springframework.core.io;

import java.io.IOException;
import java.io.InputStream;

/**@author zzzi
 * @date 2023/11/1 14:32
 * 在这里提供获取资源文件输入流的接口
 * 规定所有读取资源文件的类都要实现这个方法从而对外提供获取文件输入流的接口
 */
public interface Resource {
    InputStream getInputStream() throws IOException;
}
