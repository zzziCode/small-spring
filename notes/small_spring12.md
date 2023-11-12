---
title: "Small_spring12"
description: "small_spring12"
keywords: "small_spring12"

date: 2023-11-12T13:19:10+08:00
lastmod: 2023-11-12T13:19:10+08:00

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
#url: "small_spring12.html"


# 开启各种图渲染，如流程图、时序图、类图等
# Enable chart render, such as: flow, sequence, classes etc
#mermaid: true
---

> 🥒 small_spring12

​		本节实现了另外一种bean的配置方式，并且对于bean的属性来说，配置的时候还可以使用占位符的形式从属性文件中读取，做到一次配置，多次使用的目的，文章中涉及到的代码放到了[仓库](https://github.com/zzziCode/small-spring)中，主要分为自动扫描bean的注册以及修改bean属性的配置方式。

<!--more-->

## 原因

​	经过之前的设计，spring中的`IOC`和`AOP`已经大致有了雏形，但其更像是一个初级的spring，因为bean的配置获取还处于最原始的编写xml配置文件的方式，并不能通过使用注解从而自动扫描，并且bean的属性配置如果需要变化，必须修改xml配置文件，所以针对这两点的不足，本节中做出了改进：

1. 使用注解和包扫描的方式实现bean的**自动注册**，省去配置xml文件的繁琐步骤
2. 使用占位符的方式将bean的属性配置更改到**属性文件**中，做到一次配置，多次使用

## 思路

​		在实现上面的两个目标之前，首先需要明确这些模块都是加入到了bean的生命周期中，从而可以增强spring中对于bean的管理。对于第一个目标来说，首先需要实现包扫描的功能，然后将包扫描到的bean都注册到注册表中，就替代了xml文件中一个一个配置的步骤，后面的依据注册表创建bean的步骤与原来一致，而包扫描的机制在xml配置文件读取的过程中。 一旦有包扫描路径的配置就会触发包扫描的机制从而完成自动注册，关于自动注册的流程如下：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311121626848.png" alt="image-20231112161517182" style="zoom:80%;" />

对于<font color=red>红框</font>来说，会针对每一个包扫描路径做一次，对于<font color=green>绿框</font>来说，会针对每一个`BeanDefinition`做一次，最后将所有包扫描路径下的所有`BeanDefinition`都保存到注册表中，而自动注册的引入时机是在**读取配置文件**时

​		为了替换bean属性中的占位符，需要在bean的实例化之前进行处理，因为xml文件读取之后并不会自动替换占位符，只会读取到类似于`${token}`的内容，所以需要在bean的实例化之前处理，否则bean创建之后对应的属性就不会是真的值，而是`${token}`,而为了将${token}替换，需要引入一些工具类，对于占位符替换的流程如下：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311121626850.png" alt="image-20231112162400438" style="zoom:80%;" />

对于红框中的流程来说，每一个bean中的每一个属性都需要进行判断，一旦存在占位符就需要替换，经过这样的流程之后，所有的占位符都会被替换，这个占位符替换模块由于实现了`BeanFactoryPostProcessor`类，所以会在**实例化之前自动触发**，相当于在这里引入这个逻辑

​		为了进行包扫描，需要引入包扫描的工具包`hutools`，主要是其中的`ClassUtil`和`StrUtil`，为了进行占位符替换，需要引入`util`包中的`Properties`类，下面介绍一下本项目中类的变化：

### 类的变化

#### 新增的类

1. `Component`：是一个注解，使用这个注解的类会在包扫描中被认定为一个bean

2. `Scope`：与`Component`组合使用，用来指定bean的作用域，默认为单例模式

3. `ClassPathScanningCandidateComponentProvider`：**包扫描的核心类**，根据传递来的**包扫描路径**调用工具包中的`ClassUtil`类提供的方法进行扫描，扫描包下所有的类，将所有使用了Component注解的类放在一个集合中返回，也就是说包扫描的结果是一个类的集合，里面的类被认为是候选bean：

   ![image-20231112134417244](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311121626851.png)

4. `ClassPathBeanDefinitionScanner`：继承了`ClassPathScanningCandidateComponentProvider`类的类，是**自动注册的核心类**，主要**核心方法**的是`doScan`，外部调用这个方法传递从配置文件中得到的包扫描路径，从而调用上面说到的包扫描方法，得到这个路径下的所有候选bean，然后依次判断这些候选类上是否指定了bean的作用域，从而设置作用域，之后处理好这些注册信息之后，将这个bean的注册信息保存到**注册表**中：

   ![image-20231112134743815](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311121626852.png)

   > 由于这个类是用来自动注册的，所以一旦xml配置文件中读取到了包扫描路径就应该调用这个类，也就是说，在xml配置文件的读取过程中调用这个类，**上面四个类形成了自动注册的模块**

5. `PropertyPlaceholderConfigurer`：**占位符替换的核心类**，根据配置文件中配置的占位符，调用工具包中的`Properties`类提供的结果，从而得到占位符中真正代表的值，从而修改bean的注册信息，之后根据修改之后的注册信息创建bean就完成了占位符配置属性也可以完成bean创建的目的。这个类的调用时机应该在实例化之前，并且类中修改了bean的注册信息，所以这个类是一个实例化前的修改操作，需要继承`BeanFactoryPostProcessor`类让spring感知到，从而在实例化之前出发里面的占位符修改逻辑：

   ![image-20231112135117550](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311121626853.png)

   一旦继承了`BeanFactoryPostProcessor`类，就会在bean实例化之前触发`postProcessBeanFactory`方法，从而调用内部的占位符修改操作，在实例化之前修改bean的注册信息

#### 修改的类

1. `XmlBeanDefinitionReader`：为了引入自动注册的功能，这个类中的读取xml配置文件的方法需要增加从xml配置文件中读取包扫描路径的代码，并且由于包扫描路径可以配置多个，所以读取到的包扫描路径是一个数组，将这个数组传递给上面的自动注册的核心类之后，依次扫描每一个包路径，完成对bean的注册，关于读取配置文件的核心代码为：

   ![image-20231112135612111](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311121626855.png)

   如果有包扫描路径的配置，这里一定能读取到，之后一定会触发自动注册的功能，如果没有配置，spring中原有通过xml配置文件配置bean的功能也还在

​		根据上面的新增和修改已经完成了两个模块的功能，自动注册的功能在xml配置文件的过程中引入，通过包扫描路径扫描到所有的候选bean，之后将其加入到注册表`BeanDefinition`中，创建bean时直接通过注册表中的信息创建即可。占位符的替换在bean的实例化之前自动触发，因为其继承了`BeanFactoryPostProcessor`类，从而成为了实例化之前的修改类，其中占位符的替换逻辑被定义到了`postProcessBeanFactory`方法中，最后会在`refresh`中的`invokeBeanFactoryPostProcessors`方法中触发

### bean的创建和获取

​		下面将两个模块的执行流程分开描述，首先描述占位符替换的流程，前提是bean的配置信息已经在xml配置文件中编写好了，而属性的依赖使用占位符填充，并且实现占位符替换的实例化前修改类已经注册到了xml配置文件中：

```xml
<bean class="com.zzzi.springframework.beans.factory.PropertyPlaceholderConfigurer">
    <property name="location" value="classpath:token.properties"/>
</bean>

<!--手动注册bean，属性使用占位符填充-->
<bean id="userService" class="com.zzzi.springframework.bean.UserService">
    <!--这一句话可以拿到token中的值-->
    <property name="token" value="${token}"/>
</bean>
```

1. 读取配置文件，此时bean的注册信息中保存的还是占位符形式的值：

   ![image-20231112152404498](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311121626856.png)

2. 触发实例化前的修改逻辑，从而实现对占位符的修改，一共经历了下面几步：

   1. 得到所有的实例化前逻辑，触发各自的方法：

      ![image-20231112152558382](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311121626857.png)

   2. 拿到配置的**资源文件路径**中的内容，尝试从其中根据键拿到值：

      ![image-20231112152712407](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311121626858.png)

   3. 尝试修改每一个bean的注册信息中的属性占位符（如果存在的话），修改的逻辑就是先查找属性中的占位符，找到之后就根据其键从资源文件中拿到值，从而替换占位符，使其成为真的值，这里的**替换**是新增一个同名属性：

      ![image-20231112153008674](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311121626859.png)

      新增同名属性之后，bean的注册信息变为：

      ![image-20231112153046006](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311121626860.png)

      创建这个bean进行**属性填充**的时候，后面的属性会覆盖前面的属性，达到更新属性的目的

3. 最终的结果为：

   ![image-20231112153154337](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311121626861.png)

   可以发现即使`token`是占位符进行配置的，最后也能拿到属性文件中的值

​		下面开始分析自动注册的运行流程，介绍其的引入时机，以及如何进行自动注册，运行的前提是xml配置文件中只配置了包扫描的路径，bean的配置全使用`Component`来配置：

```xml
<context:component-scan base-package="com.zzzi.springframework.bean"/>
```

1. 读取配置文件，最终到达`XmlBeanDefinitionReader`类中的`doLoadBeanDefinitions`方法中读取xml配置文件中的信息：

   ![image-20231112153511732](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311121626862.png)

2. 读取配置文件中`base-package`标签中配置的包扫描路径，之后调用自动注册中的接口，也就是在这里**引入了自动注册的模块**到bean的生命周期中：

   ![image-20231112153644671](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311121626863.png)

3. 根据传递来的包扫描路径执行自动注册的逻辑，一共有下面几步：

   1. 调用自动注册模块中的`doScan`方法，开始扫描这些配置的包扫描路径下的类：

      ![image-20231112153844077](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311121626864.png)

   2. 针对多个包扫描路径下的每一个路径，执行`findCandidateComponents`方法，内部调用`ClassUtil`提供的方法从而得到所有使用了`Component`注解的类，并将这些类依次保存到`BeanDefinition`中，然后将这个包扫描路径下的所有`BeanDefinition`保存到容器中返回：

      ![image-20231112154136630](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311121626865.png)

   3. 针对每一个包扫描路径下得到的所有`BeanDefinition`，依次设置其作用域scope，前提是使用了Scope注解设置了不同的作用域，最后将这些保存有class信息以及作用域的`BeanDefinition`保存到注册表中：

      ![image-20231112154540898](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311121626866.png)

      针对每一个包扫描路径都执行上面五步，最后就完成了自动注册的功能

4. 完成了自动注册之后，不在xml文件中配置bean，而是使用`Component`注解也可以得到bean对象并执行其中的方法，最终的结果为：

   ![image-20231112154653671](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311121626867.png)

   现在的问题是bean经过自动注册之后没有填充其中的属性，因为自动注册过程中只设置了作用域，其余的属性全是`null`，这个问题后面解决

## 总结

​		本节中引入了两个新的模块，首先是占位符替换模块，在xml文件中的bean属性值变成了占位符，然后占位符替换逻辑在实例化的修改类中定义，从而在实例化之前就可以自动触发，而这个占位符替换逻辑类只需要当成普通的实例化前修改逻辑配置到xml文件中即可

​		自动注册模块实现了xml文件中只配置包扫描路径就可以完成bean的注册，前提是这些包扫描路径下的bean都是用了`Component`注解，在xml配置文件的解析过程中，一旦读取到了这个包扫描路径的配置，就说明当前项目有引入自动注册的意图，此时在这个时机调用自动注册中的`doScan`接口就可以根据读取到的包扫描路径扫描到其中使用了`Component`的类，从而完成bean的注册，使bean的注册不再需要xml手动配置，两个模块之间的关系为：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311121626590.png" alt="img" style="zoom:67%;" />
