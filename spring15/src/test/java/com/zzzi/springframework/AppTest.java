package com.zzzi.springframework;

import static org.junit.Assert.assertTrue;

import com.zzzi.springframework.bean.Husband;
import com.zzzi.springframework.bean.Wife;
import com.zzzi.springframework.context.support.ClassPathXmlApplicationContext;
import org.junit.Test;

/**
 * @author zzzi
 * @date 2023/11/16 20:13
 * 在这里测试循环依赖能否解决
 */
public class AppTest {
    @Test
    public void testCircle() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring.xml");
        //两个bean都创建完成，内部的属性都填充完毕
        Wife wife = applicationContext.getBean("wife", Wife.class);
        Husband husband = applicationContext.getBean("husband", Husband.class);
        System.out.println("老公的查询结果为：" + husband.queryWife());
        System.out.println("老婆的查询结果为：" + wife.queryHusband());
    }
}
