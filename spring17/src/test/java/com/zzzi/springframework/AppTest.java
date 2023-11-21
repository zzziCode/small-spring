package com.zzzi.springframework;


import com.zzzi.springframework.context.support.ClassPathXmlApplicationContext;
import com.zzzi.springframework.jdbc.support.JdbcTemplate;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * @author zzzi
 * @date 2023/11/21 19:44
 * 在这里测试jdbc的功能
 */
public class AppTest {
    private JdbcTemplate jdbcTemplate;

    //对应用上下文进行初始化
    @Before
    public void init() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring.xml");
        jdbcTemplate = applicationContext.getBean(JdbcTemplate.class);
    }

    //查询全部数据，返回一个List<Map>
    @Test
    public void queryForListTest() {

        List<Map<String, Object>> allResult = jdbcTemplate.queryForList("select * from tbl_book");
        for (int i = 0; i < allResult.size(); i++) {
            System.out.printf("第%d行数据：", i + 1);
            Map<String, Object> objectMap = allResult.get(i);
            //调用每一行中Map的toString方法打印结果
            System.out.println(objectMap);
        }
    }

    //查询所有数据中的一列，返回一个List<String>
    @Test
    public void queryListWithColumnClassTypeTest() {

        //按照方法重载的逻辑从而得到不同的调用结果
        List<String> allResult = jdbcTemplate.queryForList("select name from tbl_book", String.class);
        for (int i = 0; i < allResult.size(); i++) {
            System.out.printf("第%d行数据：", i + 1);
            String username = allResult.get(i);
            System.out.println(username);
        }
    }

    //查询某一个数据的一列，返回一个List<String>
    @Test
    public void queryListWithColumnClassTypeWithArgTest() {

        //这个List中只保存了一个结果
        List<String> allResult = jdbcTemplate.queryForList("select name from tbl_book where id=?", String.class, 1);
        for (int i = 0; i < allResult.size(); i++) {
            System.out.printf("第%d行数据：", i + 1);
            String username = allResult.get(i);
            System.out.println(username);
        }
    }

    //查询某条数据，返回一个List<Map>
    @Test
    public void queryListWithArgTest() {

        List<Map<String, Object>> allResult = jdbcTemplate.queryForList("select * from tbl_book where id=?", 1);
        for (int i = 0; i < allResult.size(); i++) {
            System.out.printf("第%d行数据：", i + 1);
            Map<String, Object> row = allResult.get(i);
            System.out.println(row);
        }
    }

    //查询单行数据中的一列，返回一个String
    @Test
    public void queryObjectTest() {

        String username = jdbcTemplate.queryForObject("select name from tbl_book where id=1", String.class);
        System.out.println(username);
    }

    //查询单行数据，返回一个Map
    @Test
    public void queryMapTest() {

        Map<String, Object> row = jdbcTemplate.queryForMap("select * from tbl_book where id=1");
        System.out.println(row);
    }

    //查询单行数据，返回一个Map
    @Test
    public void queryMapWithArgTest() {

        Map<String, Object> row = jdbcTemplate.queryForMap("select * from tbl_book where id=?", 1);
        System.out.println(row);
    }
}
