package com.zzzi.springframework.core.io;

import cn.hutool.core.lang.Assert;

import java.net.MalformedURLException;
import java.net.URL;

public class DefaultResourceLoader implements ResourceLoader{
    @Override
    public Resource getResource(String location) {
        Assert.notNull(location, "Location must not be null");
        if(location.startsWith(CLASSPATH_URL_PREFIX)){
            return new ClassPathResource(location.substring(CLASSPATH_URL_PREFIX.length()+1));
        }else{
            try {
                URL url=new URL(location);
                return new UrlResource(url);
                //传递的不是一个远程URL路径，此时就无法创建，报错之后就剩下第三种文件系统了
            } catch (MalformedURLException e) {
                return new FileSystemResource(location);
            }
        }
    }
}
