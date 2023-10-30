---
title: "Small_spring02"
description: "small_spring02"
keywords: "small_spring02"

date: 2023-10-30T09:51:25+08:00
lastmod: 2023-10-30T09:51:25+08:00

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
#url: "small_sprong02.html"


# 开启各种图渲染，如流程图、时序图、类图等
# Enable chart render, such as: flow, sequence, classes etc
#mermaid: true
---

>🤔 small_spring02

本节中主要是以上一节为基础，并对上一节制作的IOC容器进行改良，上一节中，bean对象的实例化是手动new出来的，这一节中将bean对象的创建交给IOC容器本身，并且为了考虑扩展性，尽可能的使得每一个类都只执行一个职责，最终让整个项目变得更加健壮

<!--more-->

## 思路

本节就是在上一节的基础上将bean的创建交给了IOC容器，并且实现了一个单例模式的bean，具体的设计图如下：

<img src="https://bugstack.cn/assets/images/spring/spring-3-01.png" alt="img" style="zoom:50%;" />

可以看出，只是在定义、注册、获取的基础上多了几个模块，例如获取bean对象的时候要判断是否已经存在，存在直接返回，不存在就创建再返回等。。。项目的核心类图如下：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310301002877.png" alt="image-20231030100215419" style="zoom:80%;" />

下面依次针对每一个类进行讲解

### 类的说明

1. `SingletonBeanRegistry`：是一个接口，只是提供了一个待实现的`getSingleton(String beanName)`方法，后期获取bean对象时，调用这个方法，尝试获取一个单例模式的bean，方法的实现在`DefaultSingletonBeanRegistry`类中，主要就是从容器中按照bean对象的名称`尝试`获取bean对象，对象不存在返回值为null

   ![image-20231030101148658](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310301057545.png)

2. `DefaultSingletonBeanRegistry`：是`SingletonBeanRegistry`的实现类，类中有如下方法：

   ![image-20231030101229608](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310301057546.png)

   内部有一个名为`singletonObjects`的HashMap容器，主要存储已经实例化后的bean对象及其名称之间的映射关系，主要实现了`getSingleton(String beanName)`方法，并且还增加了一个`addSingleton(String beanName, Object singletonObject)`方法，主要作用是对外提供一个保存实例化后的bean对象的api，在`AbstractAutowireCapableBeanFactory`类中的`createBean(String beanName, BeanDefinition beanDefinition)`中使用，主要作用是将利用反射创建的bean对象保存到容器中

3. `BeanFactory`：是一个接口，提供了一个待实现的`getBean(String name)`方法，对外暴露之后，可以实现从IOC容器中尝试获取一个单例模式的bean对象

   ![image-20231030101242268](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310301057547.png)

4. `AbstractBeanFactory`：继承了`DefaultSingletonBeanRegistry`类，实现了`BeanFactory`接口，类中现有如下方法：

   ![image-20231030102023153](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310301020694.png)

   主要是实现了getBean(String name)方法，根据名称尝试获取实例化之后的 bean对象，内部调用继承的getSingleton方法尝试获取一个单例模式的bean对象。不存在的话就调用继承的createBean方法创建并返回一个bean对象

5. `AbstractAutowireCapableBeanFactory`：主要是将继承下来的`createBean(String beanName, BeanDefinition beanDefinition)`方法实现了，内部使用反射机制从`BeanDefinition`中取出bean的类信息从而利用反射创建一个实例对象，并且调用`addSingleton`方法将其保存到`singletonObjects`中，类的结构如下：

   ![image-20231030102532007](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310301057549.png)

6. `BeanDefinitionRegistry`：是一个接口，主要提供了一个待实现的`registerBeanDefinition(String beanName, BeanDefinition beanDefinition)`方法，用来注册bean，也就是将bean的名称与其类信息绑定到一起，使用一个HashMap存储，实现类在`DefaultListableBeanFactory`中，类的结构如下：

   ![image-20231030102733823](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310301057550.png)

7. `DefaultListableBeanFactory`：项目中最**核心**的一个类，通过一层一层的叠加，每一个类都实现自己的功能，然后在这个类中将其他所有的类的功能**集成**在一起，形成的类结构如下：

   <img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310301057551.png" alt="image-20231030102957493" style="zoom:50%;" />

   所有的已实现的方法被集中到了这一个类中，外部直接通过这个类调用这些已实现的api即可

### bean的创建和获取

​		为了将bean的创建交给IOC容器，回想一下学过的知识，不手工new还能得到对象的方法就是反射，所以这里我们在注册时，也就是BeanDefinition时保存的不再是bean对象，而是一个类的信息，后期IOC容器可以通过这个类信息使用`反射`的知识来创建一个bean对象

​		并且平时在使用spring框架搭建项目时，bean可以选择很多模式，比如`singleton`，`prototype`，本节中就简单的实现单例模式的bean对象创建，为了实现单例模式，需要进行判断，当获取bean对象，对象不存在时，直接创建，并且底层需要将这个创建的bean对象保存到一个容器中，后期再次使用的时候，直接拿出这个已经创建过的对象，这样就不会反复的创建新的bean对象，从而实现单例模式，核心代码如下：

```java
public Object getBean(String name) throws BeansException {
    Object bean = getSingleton(name);
    if (bean != null) {
        return bean;
    }
    BeanDefinition beanDefinition = getBeanDefinition(name);
    return createBean(name, beanDefinition);
}
```

> bean存在时直接返回，不存在时创建了再返回

​		需要注意的是，获取bean对象的时候，经历了如下的流程：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310301144543.png" alt="image-20231030114408629" style="zoom:50%;" />

## 总结

为了实现将bean的创建交给IOC容器，本项目做了如下几点工作：

1. 注册bean的时候，保存的不再是实例化后的bean，而是bean的类信息，在需要的时候通过反射利用类信息创建一个实例即可
2. 为了实现单例模式的bean创建，调用一些方法，存在就返回，不存在就创建，并且创建之后将实例化后的bean保存到一个容器中，后期随用随取
3. 为了增强程序的健壮性，将单例模式的IOC容器的各个功能进行划分，每个类只做自己的事情，通过继承将这些方法得到，一层一层的逐步实现，最后最底层的类就**集成**了所有的方法，直接调用api就可以实现相应的功能

> 项目中的文件分为两类，一类是提供统一接口、模版或者全局都会使用的配置文件，一类是实现具体功能的支持文件，文件的主要结构如下：

![image-20231030105730924](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310301057553.png)

每个类只做自己的工作，将业务逻辑区分开





