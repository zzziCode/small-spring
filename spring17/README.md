---
title: "Small_spring17"
description: "small_spring17"
keywords: "small_spring17"

date: 2023-11-20T16:18:50+08:00
lastmod: 2023-11-20T16:18:50+08:00

categories:
  - 学习笔记
tags:
  - spring
  - 源码
 

# 原文作者
author: zzzi
# 开启数学公式渲染，可选值： mathjax, katex
# Support Math Formulas render, options: mathjax, katex
math: mathjax

# 开启文章置顶，数字越小越靠前
# Sticky post set-top in home page and the smaller nubmer will more forward.
#weight: 1
# 关闭文章目录功能
# Disable table of content
#toc: false


# 原文链接
# Post's origin link URL
#link:
# 图片链接，用在open graph和twitter卡片上
# Image source link that will use in open graph and twitter card
#imgs:
# 在首页展开内容
# Expand content on the home page
#expand: true
# 外部链接地址，访问时直接跳转
# It's means that will redirecting to external links
#extlink:
# 在当前页面关闭评论功能
# Disabled comment plugins in this post
#comment:
#  enable: false

# 绝对访问路径
# Absolute link for visit
#url: "small_spring17.html"


# 开启各种图渲染，如流程图、时序图、类图等
# Enable chart render, such as: flow, sequence, classes etc
#mermaid: true
---

>🥩 small_spring17

在之前的章节中我们彻底完成了spring的核心功能，将整个bean声明周期中涉及到的核心方法都进行了完善，使得bean的创建可以更加灵活，在本节中我们进一步扩充现有spring的功能，将jdbc的功能整合到现有的spring项目中，使其具有查询数据库的功能，相关的代码我放到了[仓库](https://github.com/zzziCode/small-spring)中

<!--more-->

## 原因

​		在之前的章节中，我们实现了bean生命周期中的核心功能，包括bean的定义和注册，属性填充，类型转换，加载配置文件，实现应用上下文，添加修改、初始化和销毁模块，`aware`回调注入容器资源，事件机制，AOP创建代理对象并给代理对象进行属性填充，bean的自动扫描注册，注解配置bean，解决循环依赖等核心模块。经历这些模块的扩充，现有的spring框架已经初具规模，在本节中我们进一步引入`jdbc`的模块，使得spring框架中可以实现sql语句的查询，但是这种引入并不是从头开始，而是利用一些现有的工具类，控制sql语句的执行以及结果的封装从而模拟`jdbc`中sql语句的执行

## 思路

为了实现在现有的spring项目中加入`jdbc`的功能，添加了**22个类**，下面先对对这22个类进行分类，然后再分析类与类之间的关系：

1. 对**查询结果的封装**一共包含五个类：

   ![image-20231120200028639](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528077.png)

   其中上面部分的`RowMapper`的两个实现类分别实现了对单列数据的封装处理以及对多列数据的封装处理，`SingleColumnRowMapper`内部在一行数据中拿到想要的一列数据之后进行类型转换返回。`ColumnMapRowMapper`内部将查询到的多列数据进行封装，每一列都封装成一个Map的键值对，最终将一行数据形成的Map返回

   下面部分的`ResultSetExtractor`提供一个待实现的方法`extractData`，在实现类`RowMapperResultSetExtractor`中对其进行实现，主要是调用上面模块中的对于单行数据的处理方法，对结果集中的每一行都进行处理，最终封装成一个List返回

2. 对数据库连接的处理一共有四个类：

   ![image-20231120200245142](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528083.png)

   `ConnectionHolder`类中保存`SimpleConnectionHandler`对象，内部保存数据库连接，而`DataSourceUtils`内部对数据库连接进行管理，包括获取，释放，关闭等操作。

3. 创建`PreparedStatement`以及SQL语句的执行模块，一共包含三个类：

   ![image-20231120200946453](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528085.png)

   其中实现`PreparedStatementCreator`接口的类内部创建一个`PreparedStatement`对象，而`PreparedStatement`和`Statement`对象都可以执行SQL语句，主要区别是前者是**预编译**的，可以**防止SQL注入**，而后者每次都编译

4. `PreparedStatement`的参数设置模块，一共包含两个类：

   ![image-20231120201152380](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528086.png)

   由于`PreparedStatement`在执行SQL语句时需要先设置SQL模版，在设置执行的 参数，这样可以防止SQL注入，并且SQL语句可以重复使用，只是需要重新设置参数

5. 项目中的异常模块，一共包含三个类，都是自定义的异常类：

   ![image-20231120201543411](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528087.png)

6. 辅助模块，一共有两个类：

   ![image-20231120201628287](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528088.png)

   其中`JdbcUtils`中可以操作`Statement`和`ResultSet`，包括关闭`Statement`和`ResultSet`，获取结果集的列名和列值，`SqlProvider`可以提供当前正在执行的sql语句给外部

7. 核心模块，外部调用这个模块中的`JdbcTemplate`类中的方法完成sql语句的执行，得到结果，上面的所有模块都是为了这个模块服务的：

   ![ConnectionHandler](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528089.png)

   其中`JdbcAccessor`保存所设置的数据源，`JdbcOperations`规定`JdbcTemplate`中有哪些执行SQL语句的方法，`JdbcTemplate`中就是这些方法的具体实现。

`JdbcTemplate`内部调用了上面的所有类，根据配置的数据源得到数据库连接之后，创建SQL语句的执行器，不论是`PreparedStatement`还是`Statement`，之后执行SQL语句得到结果集，利用上面的工具类进行单行的处理，封装成List，Object或者Map返回给外部调用的对象，对于外部对象来说，调用`JdbcTemplate`就可以执行SQL语句，得到查询的封装结果，内部SQL语句如何执行**对用户透明**，所以`JdbcTemplate`就是入口，实现了在现有的spring项目中引入SQL语句的查询功能，想要在哪一个bean中引入数据库查询的功能，就直接在这个bean中**注入**`JdbcTemplate`对象并执行相应的方法即可从数据库中获取到想要的结果

### 类的变化

为了在现有的spring项目中引入jdbc的内容，新增了22个类，这是一个独立的模块，项目中的bean想要使用jdbc来进行数据库的查询，那么就直接注入`JdbcTemplate`的bean对象就可以在内部使用了，所以本节中没有修改的类，只有新增的类

#### 新增的类

1. `RowMapper`：这个接口内部提供了一个封装单行查询结果的方法，可以针对查询结果中的一行数据进行处理：

   ![image-20231121125850494](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528090.png)

2. `ColumnMapRowMapper`：是`RowMapper`的实现类，将单行数据中的每一列封装成Map中的一个键值对，最终一行数据会被封装成一个Map

   ![image-20231121125951828](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528091.png)

3. `SingleColumnRowMapper`：是`RowMapper`的实现类，将单行数据中的一列进行类型转换并返回，结合上面的封装整行数据，这样就可以对单行数据进行任意操作：

   ![image-20231121130309200](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528093.png)

4. `ResultSetExtractor`：是一个接口，实现这个接口的类在内部的待实现方法中调用上面的对单行数据进行封装的工具，从而实现对整个结果集的封装：

   ![image-20231121130359584](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528094.png)

5. `RowMapperResultSetExtractor`：在内部调用上面的单行数据封装的工具，最后将结果集封装成一个Lsit返回

   ![image-20231121130457727](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528095.png)

6. `ConnectionHandler`：是一个接口，实现这个接口代表内部保存了数据库的连接，对外提供释放和get方法：

   ![image-20231121130716973](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528096.png)

7. `SimpleConnectionHandler`：上面这个接口的实现，内部保存了数据库的连接：

   ![image-20231121130756504](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528097.png)

8. `ConnectionHolder`：在内部持有一个数据库连接，也就是保存一个`ConnectionHandler`对象便于使用：

   ![image-20231121130849143](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528098.png)

9. `DataSourceUtils`：在这里完成对数据库连接的管理，获取，释放，关闭等操作

   ![image-20231121130928864](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528099.png)

10. `StatementCallback`：实现这个接口代表内部可以使用一个`Statement`的SQL执行器从而执行SQL语句：

    ![image-20231121131106212](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528100.png)

11. `PreparedStatementCreator`：实现这个接口代表创建一个预编译的`PreparedStatement`的SQL执行器，从而便于后面调用并使用：

    ![image-20231121131217743](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528101.png)

12. `PreparedStatementCallback`：实现这个接口代表内部可以使用一个`PreparedStatement`的SQL执行器从而执行SQL语句：

    ![image-20231121131302384](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528102.png)

13. `PreparedStatementSetter`：实现这个接口可以设置`PreparedStatement`的执行参数，便于后期`PreparedStatement`执行SQL语句：

    ![image-20231121131523143](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528103.png)

14. `ArgumentPreparedStatementSetter`：是`PreparedStatementSetter`的实现类，内部设置`PreparedStatement`的执行参数：

    ![image-20231121131608180](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528104.png)

15. `CannotGetJdbcConnectionException`、`IncorrectResultSetColumnCountException`、`UncategorizedSQLException`：这是三个jdbc中的自定义异常类

16. `JdbcUtils`：这是一个工具类，可以操作`Statement`和`ResultSet`，包括关闭`Statement`和`ResultSet`获取结果集的列名和列值：

    ![image-20231121131749875](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528105.png)

17. `SqlProvider`：实现这个接口代表可以向外提供当前正在执行的SQL语句：

    ![image-20231121131838594](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528106.png)

18. `JdbcAccessor`：在这个类中设置当前jdbc中需要使用的数据源，这个类很重要，有了这个类才能得到数据库的连接，从而执行SQL语句：

    ![image-20231121131946071](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528107.png)

19. `JdbcOperations`：类似于一个模版，规定`JdbcTemplate`中有哪些执行SQL语句的方法，只是提供了结构，不提供实现：

    ![image-20231121132046027](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528108.png)

20. `JdbcTemplate`：最核心的一个类，上面所有的功能都集成到了这一个类中从而完成对SQL语句的操作，对结果集的封装处理并返回:

    ![JdbcTemplate](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211925811.png)
    
    对于`JdbcTemplate`来说，最核心的类就是上面圈出来的五个方法，第一个方法与第二个方法的区别就是二者使用的SQL执行器不一样，第一个方法使用的是预编译的`PreparedStatement`，第二个方法使用的是普通的`Statement`。第三个方法是一切query类方法底层调用的最终方法，而第四和第五个方法是所有`queryForObject`和`queryForMap`要调用的方法，因为Object和Map可以统称为Object，而第四第五个方法内部所做的就是调用前三个方法中的一个，从而得到被封装到List中的查询结果，然后再从List中将被封装的结果拿出来返回即可，返回之前需要确定查询的结果集中有且仅有一条数据，然后才会将结果拿出来返回，以第四个的代码为例：
    
    ```java
    public <T> T queryForObject(String sql, Object[] args, RowMapper<T> rowMapper) {
        List<T> results = query(sql, args, new RowMapperResultSetExtractor<>(rowMapper, 1));
    
        //下面两个判断条件保证查询结果只有一条
        if (CollUtil.isEmpty(results)) {
            throw new RuntimeException("Incorrect result size: expected 1, actual 0");
        }
        if (results.size() > 1) {
            throw new RuntimeException("Incorrect result size: expected 1, actual " + results.size());
        }
        //将封装在List中的一条记录便利出来返回就是查询Object
        return results.iterator().next();
    }
    ```
    
    可以看出就是调用前三个方法中的一个，然后再将要返回的结果从List中拿出来返回即可

​		有了这些类之后，就可以引入jdbc，xml配置文件中只要配置了数据源和`JdbcTemplate`的bean就引入了jdbc的功能，从而执行SQL语句，从而得到查询的结果。哪个bean想要使用jdbc的功能，只需要注入`JdbcTemplate`的bean对象即可

### sql语句的执行

经过上面的分析，可以发现在现有的spring框架中引入jdbc的功能就是增加了一个独立的模块，下面通过`debug`的方式来分析**查询所有数据**的sql语句如何执行，了解sql语句的执行过程，后期想要在哪执行sql语句就在哪引入`JdbcTemplate`的bean对象即可：

1. 读取配置文件，执行`refresh`方法中的所有步骤，得到应用上下文和初始化好的所有bean对象，核心就是内部的`jdbcTemplate`对象，调用这个对象中的方法可以执行SQL语句：

   ![image-20231121151333728](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528111.png)

2. 执行SQL语句，先得到一个**单行结果处理器**，内部可以是`ColumnMapRowMapper`，也可以是`SingleColumnRowMapper`

   ![image-20231121151405819](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528112.png)

3. 将得到的**单行结果处理器**封装到结果处理器中，后期查询到结果后使用这个结果处理器：

   ![image-20231121151711343](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528113.png)

4. 创建一个SQL语句的执行器，内部调用`doInStatement`就可以执行SQL语句并将查询得到的结果调用前面的结果处理器进行封装：

   ![image-20231121151920685](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528114.png)

5. 执行SQL语句，也就是真正的调用上面的`doInStatement`方法，从而得到结果，并进行封装：

   ![image-20231121152046346](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528115.png)

6. 关闭数据库连接，进行收尾工作，并打印最终的结果：

   ![image-20231121152254712](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528116.png)

> 执行SQL语句之后，有几种返回值：

- List

  ![image-20231121142316449](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528117.png)

  有两种类型的返回值`List<Map>`以及`List<String>`，前一种的返回值有两种情况，多行结果的多列数据，单行结果的多列数据，第二中的返回值有两种情况，多行结果的一列数据以及单行结果的一列数据

- Object

  ![image-20231121142336605](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528118.png)

  出现这种返回值只能是查询单行结果的一列数据

- Map

  ![image-20231121142414596](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311211528119.png)

  出现这种返回值只能是查询单行数据的多列数据

出现这三种类型返回值的原因就是看SQL语句查询了多少数据，查询了多条数据，那肯定就是List，查询了一条数据，返回一整行，可以使用Map，也可以使用List，但是此时List中只有一条数据，查询了一条数据并且只返回一列时可以使用Object，也可以使用List，但是此时List中只有一条数据

## 总结

经过上面的分析，可以清楚的了解到如何在现有的spring项目中引入jdbc的内容，并且如何执行一条SQL语句并将得到的结果集进行封装，核心就是`JdbcTemplate`这个类，由于jdbc的模块是一个独立的模块，所以谁想使用谁引入`JdbcTemplate`这个类即可
