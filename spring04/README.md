---
title: "Small_spring04"
description: "small_spring04"
keywords: "small_spring04"

date: 2023-10-31T16:33:05+08:00
lastmod: 2023-10-31T16:33:05+08:00

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
#url: "small_spring04.html"


# 开启各种图渲染，如流程图、时序图、类图等
# Enable chart render, such as: flow, sequence, classes etc
#mermaid: true
---

>🍐 small_spring04

本节中主要是修改上一节中留下来的小问题，上一节中实现了带参的bean对象创建，但是一旦当前bean对象依赖另一个bean对象时，这种创建方式就会失效，因为上一节的带参bean对象创建需要先得到所有的参数。也就是说，当内部依赖另外一个bean对象时，需要先将这个bean对象new出来，但是这又不符合bean对象的设计模式了，我们前面千辛万苦将bean的创建交给了IOC容器，所以这里需要另辟蹊径，来实现bean的另外一种创建模式，具体的代码在[仓库](https://github.com/zzziCode/small-spring)中

<!--more-->

## 原因

​		由于spring中，bean与bean之间经常存在依赖关系，最典型的就是service中的bean肯定会依赖于dao中的bean，也就是说，service中的bean有一个参数，参数类型是一个bean。上一节中我们实现了带参bean对象的创建，但是创建bean对象的时候，需要将这个bean对象需要的所有参数都准备好，这就出现了一个问题：

> 创建service中的bean时，需要将dao中的bean准备好，也就是提前需要得到dao中的bean对象，这不符合**需要时才创建**的逻辑，所以我们在这一节中就将解决这个问题

​		为了实现使用时才创建，需要改变思路，上一节中是将所有的参数准备好，然后直接创建bean对象，这一节中将bean对象依赖的所有关系都记录下来，普通类型的参数直接记录值，bean类型的参数记录一个依赖关系，然后先创建一个空的bean对象，之后再进行**属性填充**，如下图所示：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310312102680.png" alt="image-20231031164604160" style="zoom:50%;" />

​		上一节中我们创建bean的时候，使用getBean函数需要传递bean需要的参数，这样才能创建带参的bean对象，这一节中我们不传递任何参数，只是将需要哪些参数的信息保存到一个类中，先创建空的bean对象，然后再进行**属性填充**，新的项目结构为：

![image-20231031165753636](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310312106904.png)

不再是创建bean时就填充属性，而是创建好了之后再填充属性，这样就不用在getBean时就传递已经实例化的bean属性了，也就是`getBean("userService")`时不需要实例化`userDao`的bean对象，只有内部真正需要的时候才创建

在注册bean的时候，不仅保存其类信息，还保存其所有依赖的属性，便于后期属性注入

## 思路

​		为了将bean的定义和属性的填充分开，本章中对`createBean`的方法进一步进行了增强，在上一节中为了实现带参bean的创建，在`createBean`方法中引入了`createBeanInstance`方法，根据传递来的参数列表匹配到对应的构造函数从而创建bean对象，本节中进一步对`createBean`方法进行增强，先调用`createBeanInstance`方法创建一个空的bean对象，然后利用bean注册时保存的`propertyValues`来进行属性填充,改动的地方如下图蓝色区域所示：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310311834414.png" alt="img" style="zoom:50%;" />

​		为了实现属性填充，本节中又添加了三个新的类，并且在原来项目的基础上对一些类进行了修改，下面具体描述哪些类做了修改，并且新增了哪些类

### 类的说明

1. `BeanReference`：如果某一个bean依赖另外一个bean对象，那么在记录这个bean的所有属性时，被依赖的bean会以这个类的对象出现，内部存储的是这个被依赖的bean的名称，这个类主要起到的作用是记录bean之间的依赖关系，当一个bean被依赖时，不会立马创建bean对象，类的结构为：

   ![image-20231031183810861](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310312102683.png)

2. `PropertyValue`：这个类中保存了当前bean中的某一个参数的信息，包括参数的名称，参数的值，也就是这个类中有两个属性，类的结构如下：

   ![image-20231031183930906](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310312102684.png)

   当当前bean的属性是普通属性时，name保存这个属性的名称，value保存这个属性的值，当当前bean的属性是另外一个bean时，例如**service**中的bean依赖与dao中的bean。那么name保存这个bean的名称，也就是保存**dao**中bean的名称，value保存的不再是**dao**中bean对象，而是保存一个`BeanReference`对象，内部存储了dao中**bean**对象的名称，这样做的目的是为了减缓bean的创建时机

3. `PropertyValues`：这个类保存了当前bean的所有属性，每一个属性都保存在一个`PropertyValue`类中，将所有的属性保存到一个`List<PropertyValue>`中，在注册bean的时候，不再是单单保存bean的类信息，连同这个容器也一起注册到`BeanDefinition`中，其中保存了当前这个bean的所有属性，这也说明`BeanDefinition`这个类要做出修改

4. `BeanDefinition`：对这个类做出修改，不再是只保存当前这个bean的类信息，还保存当前这个类的所有属性信息，保存到一个`propertyValues`对象中的`List<PropertyValue>`中，新的类结构如下：

   ![image-20231031184527965](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310312102685.png)

5. `AbstractAutowireCapableBeanFactory`：上一节中介绍在这个类中实现创建带参bean对象的功能，但是本节中将带参bean对象的创建分为两步，**第一步**是创建空的bean，**第二步**是属性填充`applyPropertyValues`，这个方法是**新增**的，形成的类结构为：

   ![image-20231031190103985](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310312102686.png)

   内部利用bean注册时保存的属性列表`PropertyValues`来逐一填充，下面进行测试，详细代码在[仓库](https://github.com/zzziCode/small-spring.git)中

   对于**第一步**来说，debug的结果如下，可以看出刚创建好的bean还没有属性填充进去，并且传递的参数args也是空：

   ![image-20231031205025978](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310312102687.png)

   对于第二步来说，debug的结果如下，普通属性直接填充，bean属性先创建再填充：

   ![image-20231031205502439](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310312102688.png)

​		经历上面的改造，整个项目的框架就搭好了，那么项目中是如何实现bean的实例化和属性填充分开的呢，我们接下来介绍

### bean的创建和获取	

​		在上一节中，我们在获取bean的时候，将bean的参数也传递了进去，也就是getBean的时候，同时传递参数，本节中对此进行以下几点改变：

1. `getBean`不再传递参数，而是将参数保存到`PropertyValues`类中

2. bean实例化时一直调用无参构造，使得bean创建之后，属性都为空

3. 调用`createBeanInstance`方法创建bean对象之后，得到一个空对象，然后再进行属性填充，属性填充的代码为：

   ```java
   protected void applyPropertyValues(String beanName, Object bean, BeanDefinition beanDefinition) {
       try {
           //根据注册时保存的属性拿到当前bean的属性列表
           PropertyValues propertyValues = beanDefinition.getPropertyValues();
           //遍历属性列表
           for (PropertyValue propertyValue : propertyValues.getPropertyValues()) {
               String name = propertyValue.getName();
               Object value = propertyValue.getValue();
               //属性为bean时单独处理
               if (value instanceof BeanReference) {
                   // A 依赖 B，获取 B 的实例化
                   BeanReference beanReference = (BeanReference) value;
                   //在这里才创建属性中的bean
                   value = getBean(beanReference.getBeanName());
               }
               //依次填充每一个属性
               BeanUtil.setFieldValue(bean, name, value);
           }
       } catch (Exception e) {
           throw new BeansException("Error setting property values：" + beanName);
       }
   }
   
   ```

   ​		第10~15行是最关键的代码，这一段代码中描述的是，如果当前bean依赖的是另外一个bean（取出的属性值是一个`BeanReference`类型的对象），此时才创建当前这个bean对象，内部调用`getBean`方法，还是按照之前分析的思路，先创建空bean，然后再属性填充，如果属性填充的过程中又依赖bean，那么再临时创建这个bean对象，继续执行先创建空bean，再属性填充的操作，具体的流程如下图，可以看到多了一步**属性填充**的操作：

   <img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311030859937.png" alt="image-20231103085923756" style="zoom:50%;" />

   ​		上面描述了创建bean的过程，分为创建空bean和属性填充，那么外部是如何获取这个bean的呢，给出一段测试的代码，详细的代码在[仓库](https://github.com/zzziCode/small-spring.git)中：
   
   ```java
   @Test
   public void test_BeanFactory() {
       // 1.初始化 BeanFactory
       DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
   
       // 2. UserDao 注册，由于userDao没有属性，所以不用设置属性
       beanFactory.registerBeanDefinition("userDao", new BeanDefinition(UserDao.class));
   
       // 3. UserService 设置属性[uId、userDao]
       PropertyValues propertyValues = new PropertyValues();
       propertyValues.addPropertyValue(new PropertyValue("uId", "10001"));
       propertyValues.addPropertyValue(new PropertyValue("userDao",new BeanReference("userDao")));
   
       // 4. UserService 注入bean
       BeanDefinition beanDefinition = new BeanDefinition(UserService.class, propertyValues);
       beanFactory.registerBeanDefinition("userService", beanDefinition);
   
       // 5. UserService 获取bean，这里不再直接传递参数，而是将参数的传递交给了
       UserService userService = (UserService) beanFactory.getBean("userService");
       userService.queryUserInfo();
   }
   ```
   
   ​		可以发现，获取bean调用的`getBean`操作并没有传递任何参数，也就是调用的底层的无参构造，然后在获取bean的时候，将bean所依赖的一些属性保存到了`PropertyValues`中，重点注意第**12**行，这里`userService`的bean依赖于us`e`rDao的bean，保存属性时不是直接保存`userDao`的bean对象，而是保存了一个`BeanReference`的对象，内部存储了`userDao`的bean对象的**名称**

## 总结

经历上面的改造，项目实现了bean的创建与属性填充分开的操作，主要是在`AbstractAutowireCapableBeanFactory`类中将这两步操作分开，实现了bean可以依赖另外的bean，并且在使用的时候才新建bean对象
