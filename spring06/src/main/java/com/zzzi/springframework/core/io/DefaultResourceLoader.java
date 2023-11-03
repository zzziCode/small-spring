package com.zzzi.springframework.core.io;

import cn.hutool.core.lang.Assert;

import java.net.MalformedURLException;
import java.net.URL;

public class DefaultResourceLoader implements ResourceLoader{
    @Override
    public Resource getResource(String location) {
        Assert.notNull(location, "Location must not be null");
        if(location.startsWith(CLASSPATH_URL_PREFIX)){
            //获取到“classpath:”后面的内容
            return new ClassPathResource(location.substring(CLASSPATH_URL_PREFIX.length()+1));
        }else{
            try {
                URL url=new URL(location);
                return new UrlResource(url);
            } catch (MalformedURLException e) {
                return new FileSystemResource(location);
            }
        }
    }
}
