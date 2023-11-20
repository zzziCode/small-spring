package com.zzzi;

import static org.junit.Assert.assertTrue;

import com.zzzi.springframework.bean.Husband;
import com.zzzi.springframework.context.support.ClassPathXmlApplicationContext;
import org.junit.Test;
/**@author zzzi
 * @date 2023/11/20 11:01
 * 在这里测试类型转换的功能
 */
public class AppTest 
{

    @Test
    public void testConverter(){
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring.xml");
        Husband husband = applicationContext.getBean("husband", Husband.class);
        System.out.println(husband);
    }
}
