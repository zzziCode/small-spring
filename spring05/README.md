---
title: "Small_spring05"
description: "small_spring05"
keywords: "small_spring05"

date: 2023-11-01T09:21:26+08:00
lastmod: 2023-11-01T09:21:26+08:00

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
#url: "small_spring05.html"


# 开启各种图渲染，如流程图、时序图、类图等
# Enable chart render, such as: flow, sequence, classes etc
#mermaid: true
---

>🍒 small_spring05

之前几节已经实现了将bean的创建交给IOC容器，并且创建和属性填充已经分开，但是bean的定义、注册、属性填充、对象调用都是手动在测试方法中实现的，这与真正的spring并不相符，所以本节在之前的基础上将spring中bean的定义，注册，属性填充等都配置到**配置文件**中，然后编写代码读取配置文件中的信息，完成定义，注册，属性填充等操作，使其变得更**像**一个spring框架，详细的代码在[仓库](https://github.com/zzziCode/small-spring.git)中

<!--more-->

## 原因

​		在之前几节中，我们实现了bean的创建交给IOC容器，并且创建和属性填充分开的效果，但是存在的问题是，不管怎么获取bean对象，在获取之前都需要将bean的注册信息手动配置好，然后再获取bean对象，这与真实的spring框架并不相同，正常需要从**配置文件**中获取bean的注册信息，所以本节的目标是为了将手动配置bean的注册信息改成从配置文件中获取。

## 思路

​		为了实现在配置文件中读取bean的注册信息的目的，主要新增了一个资源加载和资源利用的模块，正常的bean注册和属性填充的功能保持不变。新增的功能分别处于core.io和beans模块中，从原先的在测试方法中手动编写bean的注册信息改成现在的在配置文件中加载，使其更加符合真实spring的情况

​		为了实现本节中提到的将bean的注册信息改成从配置文件中读取的目的，主要新增了一个资源**加载**和资源**利用**的模块，正常的bean注册和属性填充的功能保持不变。新增的功能分别处于`core.io`和`beans`模块中，从原先的在测试方法中手动编写bean的注册信息改成现在的在配置文件中加载，使其更加符合真实spring的情况

​		对于资源加载来说，类的整体结构图如下：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311011328804.png" alt="image-20231101125737113" style="zoom:50%;" />

​		对于资源利用来说，类的整体结构图如下：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311011328805.png" alt="image-20231101125312421" style="zoom:50%;" />

通过加入以上两个模块，实现了将bean的注册信息由手动注册改为从配置文件中加载，下面介绍本节中一些**类的变化**：

### 修改的类

在本项目中，对原有的类进行了部分修改，下面逐个介绍每一个被修改的类，以及修改的意义是什么：

1. `BeanFactory`：在这个接口中新增一个按照类型获取bean对象的待实现方法，然后在`AbstractBeanFactory`类中进行了实现，新的类结构为：

   ![image-20231101103125664](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311011328806.png)

2. `AbstractBeanFactory`：对上面提到的按照类型获取bean对象的方法进行了实现，也就是新增了一个方法，新的类结构为：

   <img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311011328808.png" alt="image-20231101103251689" style="zoom:80%;" />

3. `BeanDefinitionRegistry`：在这个接口中新增了三个待实现的方法，分别是按照名称获取bean的注册信息`getBeanDefinition(String beanName)`，按照名称判断是否存在某一个bean的注册信息`containsBeanDefinition(String beanName)`，获取所有已注册的bean的名称`getBeanDefinitionNames()`，新增方法之后在核心类`DefaultListableBeanFactory`中对这三个方法进行了实现，新的类结构为：

   ![image-20231101103646866](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311011328809.png)

4. `DefaultListableBeanFactory`：是项目中的**核心类**，实现了上面的`BeanDefinitionRegistry`提供的方法，新的类结构为：

   <img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311011328810.png" alt="image-20231101104214418" style="zoom:80%;" />

​		对上面的几个类和接口进行修改的目的是为了增加程序的健壮性和功能，新增之后对外提供的api更加丰富，并且部分方法的新增是为了服务于其他的功能，部分接口的拓展参照了spring源码，随着项目的完善，这些接口会逐渐发挥作用

​		上述修改的类几乎对本节中的目标没有大的帮助，因为本节中主要是将bean的注册信息从手动改成由配置文件中自动获取，所以核心在新增的类中，下面介绍这些新增的类做了些什么

### 新增的类

​		在本节中，新增了很多类和接口，部分是考虑到后期的项目扩展，部分是本节中的核心，总之新增了这些类之后，可以扩展项目的功能，并且保证项目的健壮性，下面依次介绍每一个新增的类都有什么含义，带来了什么影响

​		按照项目的结构安排，本项目中将bean的定义，注册，初始化等操作抽取到配置文件中，也就是说先要读取配置文件中的资源，然后在利用这些资源进行处理，从而实现将配置信息注册到bean容器中，主要分为两个部分，资源的**加载**和资源的**利用**，分别对应到`core.io`包和`beans`包，所以先介绍`core.io`包中的资源加载

#### core.io

1. `Resource`：新增的接口，里面就一个方法，用来获取文件的输入流，便于读取文件，得到文件输入流之后，才能获取到里面的资源，这里只是提供了接口，一共有三种读取资源的方式：**ClassPath**、**系统文件**、**云配置文件**，所以这个接口一共有三个实现类。最终的目的是为了得到配置文件的文件输入流。类的结构为：

   ![image-20231101105724182](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311011328811.png)

2. `ClassPathResource`：第一种加载资源的方式：**ClassPath**，对外提供接口，初始化里面的路径path和classLoader，之后利用classLoader获取到文件输入流，这样就实现了资源的加载，得到了文件的输入流，类的结构为：

   ![image-20231101110114880](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311011328812.png)

   > 核心代码为：
   >
   > ```java
   > InputStream is = classLoader.getResourceAsStream(path);
   > ```

3. `FileSystemResource`：第二种加载资源的方式：**系统文件**，对外提供接口，初始化里面的file和path路径，之后利用文件中的接口得到对应配置文件的输入流，类的结构为：

   ![image-20231101110325882](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311011328813.png)

   > 核心代码为：
   >
   > ```java
   > return new FileInputStream(this.file);
   > ```

4. `UrlResource`：第三种加载资源的方式：**云配置文件**，对外提供接口，初始化里面的url，利用url中的接口获取到配置文件的输入流，类的结构为：

   ![image-20231101110445639](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311011328814.png)

   > 核心代码为：
   >
   > ```java
   > URLConnection con = this.url.openConnection();
   > return con.getInputStream();
   > ```

> 以上三种获取资源的方式都是利用了各自提供的接口获取到文件的输入流，获取到输入流之后，就可以一视同仁，将其当成流来处理了

5. `ResourceLoader`：一个接口，提供一个获取资源的接口，在里面调用上述三种加载资源的方式中的一种。按照资源加载的不同方式，资源加载器可以把这些方式集中到统一的类服务下进行处理，外部用户只需要传递资源地址即可，简化使用。类的结构为：

   ![image-20231101110735641](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311011328815.png)

6. `DefaultResourceLoader`：在这里实现具体的资源加载，内部默认优先使用ClassPath的方式加载配置文件，之后是云配置文件，最后才是系统文件：

   <img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311011328816.png" alt="image-20231101110935612" style="zoom:67%;" />

​		上面新增的6个类实现了对配置文件的加载，也就是上面描述的第一步，资源的**加载**，简单来说就是将配置文件的输入流通过不同方式得到了，下面就是资源的利用，主要涉及到三个类。接口：`BeanDefinitionReader`、抽象类：`AbstractBeanDefinitionReader`、实现类：`XmlBeanDefinitionReader`，这三部分内容主要是合理清晰的处理了资源读取后的注册 Bean 容器操作。*接口管定义，抽象类处理非接口功能外的注册Bean组件填充，最终实现类即可只关心具体的业务实现*。形成的整体类图为：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311011328817.png" alt="image-20231101125639720" style="zoom:50%;" />

​		下面详细介绍`beans`中新增的类

#### beans

1. `BeanDefinitionReader`：接口中一共有**三种**方法，第一种是获取bean的注册信息类`BeanDefinitionRegistry`，这里面包含bean信息的注册，获取等操作。第二种是获取资源加载器，也就是**core.io**包中的`ResourceLoader`，第三种就是根据获取到的资源加载资源的定义，类的结构如下：

   ![image-20231101121138800](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311011328818.png)

2. `AbstractBeanDefinitionReader`：在这个类中主要是实现对资源加载器和注册信息类的**初始化**，为了后面资源信息的利用打基础，后面的实现类只用关注资源的利用即可，类的结构如下：

   ![image-20231101121354321](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311011328819.png)

   > 主要实现了对两个变量的初始化工作

3. `XmlBeanDefinitionReader`：本项目中**最核心**的类，主要实现了对资源的**利用**，资源的加载阶段获取到了配置文件的输入流，然后资源利用阶段就是将配置文件中的信息读取处理，之后按照配置来初始化bean的注册信息，类的结构如下：

   ![image-20231101121744877](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311011328820.png)

   使用的步骤分为以下几步：

   - 初始化bean工厂
   - 将bean工厂交给这个类管理
   - 指定配置文件的位置
   - 内部自动利用配置文件中的信息注册bean的信息到工厂中
   - 外部利用工厂来获取bean，自然就有了bean对象

   > 最核心的就是`doLoadBeanDefinitions`方法，此方法将配置文件中的信息转换为bean的注册信息，实现了读取配置文件内容的功能，简单来说就是读取文件中信息，以上三个类的类结构图如下所示，这也是本项目中**资源利用**的核心模块：

   <img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311011328805.png" alt="image-20231101125312421" style="zoom:50%;" />

4. `AutowireCapableBeanFactory`：新增的接口，为了后期项目的扩展，没有任何内容，后期准备使用这个接口来自动化处理bean工厂的配置

5. `ConfigurableBeanFactory`：新增的接口，里面保存了spring框架中的配置信息，目前只保存了bean的实例化模式，类的结构如下：

   ![image-20231101104821589](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311011328822.png)

6. `ConfigurableListableBeanFactory`：新增的一个接口，里面提供了一个`getBeanDefinition(String beanName)`方法

7. `HierarchicalBeanFactory`：新增的接口，后期会在其中提供获取父类的beanFactory的方法

8. `ListableBeanFactory`：新增的接口，提供了两个待实现的方法，分别可以获取bean的类型，获取所有已注册的bean的名称，最终的实现在`DefaultListableBeanFactory`这个类中，类的结构为：

   ![image-20231101122430504](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311011328823.png)

​		以上这些新增的类主要是实现了描述的第二步，资源的利用，将bean的注册信息由手动变成自动，在之前的章节中，测试代码中需要手动将bean的注册信息创建好，本节的目标是将这些注册信息保存到xml文件中，xml文件的例子如下：

```xml

<beans>
    <bean id="userDao" class="cn.bugstack.com.zzzi.springframework.test.bean.UserDao"/>
    <bean id="userService" class="cn.bugstack.com.zzzi.springframework.test.bean.UserService">
        <property name="uId" value="10001"/>
        <property name="userDao" ref="userDao"/>
    </bean>
</beans>
```

新的测试代码为：

```java
@Test
public void test_xml() {
    // 1.初始化 BeanFactory
    DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

    // 2. 读取配置文件&注册Bean
    XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
    reader.loadBeanDefinitions("classpath:spring.xml");

    // 3. 获取Bean对象调用方法
    UserService userService = beanFactory.getBean("userService", UserService.class);
    String result = userService.queryUserInfo();
    System.out.println("测试结果：" + result);
}
```

​		重点在7~8行，将bean的注册信息改成从配置文件中获取，而底层的逻辑就是上面这些新增的类实现的

#### util

这个包中新增了一个类`ClassUtils`，主要是用来获取默认的类加载器，类的结构如下：

![image-20231101124651995](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311011328824.png)

### bean的创建和获取

​		经历上面的扩充，bean的注册信息已经可以从配置文件中获取了，现在bean的创建的步骤首先需要经过读取配置文件的步骤，读取配置文件分为资源的**加载**和**利用**。将bean的注册信息注册到注册表中之后，获取bean的时候会根据注册信息进行bean的实例化和属性填充

​		对于整个项目来说，资源的加载和利用只是一个很小的前期准备工作，流程结构如下图所示：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311011328825.png" alt="image-20231101131726483" style="zoom:50%;" />

资源加载和利用结束之后，才是**getBean**的操作，也就是在获取bean的前面进行增加了一个读取配置文件的模块，读取完之后就是之前介绍的正常获取bean的过程，流程图如下：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311030859937.png" alt="image-20231103085923756" style="zoom:50%;" />

经历上面两个流程之后，就可以实现bean的获取，在第一部分中实现了bean的注册信息从配置文件中读取的效果，对代码进行debug之后，效果如下图：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311011551364.png" alt="image-20231101155059323" style="zoom:50%;" />

可以看出，bean的属性依赖的注册被配置到配置文件中，照样可以完成属性的注册

## 总结

​		本节中实现了bean的注册信息从配置文件中读取的效果，这是bean获取之前的一个步骤，为了实现这个步骤，新增了很多类，总结起来有两个模块，第一个是资源的加载模块，代码集中在`core.io`包中，实现了得到不同配置文件的输入流的效果，支持ClassPath，系统文件，云配置文件三种类型的配置文件。

​		第二个是资源的利用模块，主要是根据第一个模块中得到的输入流来读取配置文件中的信息，将这些信息加载出来注册给bean，相当于一个工具类。代码集中在`beans`包中，两个模块之间类的利用关系如下图所示：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311011324511.png" alt="图 6-3" style="zoom:70%;" />

​		实现了从配置文件中加载bean的注册信息之后，还对原始的项目进行了**健壮性**的改进，为了后期的扩充打好基础
