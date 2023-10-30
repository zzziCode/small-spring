---
title: "Small_spring01"
description: "small_spring01"
keywords: "small_spring01"

date: 2023-10-30T08:50:31+08:00
lastmod: 2023-10-30T08:50:31+08:00

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
#url: "small_spring01.html"


# 开启各种图渲染，如流程图、时序图、类图等
# Enable chart render, such as: flow, sequence, classes etc
#mermaid: true
---

>😀 small_spring01

本文介绍small_spring的第一节知识，简单的实现一个最基础的spring中的容器，包括定义、存储、获取这个容器中的bean对象，相比于真正的spring源码只是一个简化版本，后面的章节会慢慢的对这个容器进行扩充，文章中涉及到的代码在[仓库](https://github.com/zzziCode/small_spring)中

<!--more-->

## 思路

​		一般的spring容器需要经历两步才能将一个bean存储到IOC容器中，分别是定义、注册，其中定义一般是存放bean的类型，但是在本节中直接存放的是bean对象本身，后期将对这个功能做出修改。注册一般是将bean的名称与其类型的映射关系，但是本节中直接实例化一个bean，然后将这个bean存储到容器中，最后的获取就是按照bean的名称获取对应的bean对象

​		按照上面的分析，代码的主要逻辑就是将bean的实例化对象存储到一个容器中，之后通过bean的名称获取，实现这中功能的容器一般是Map，整个流程如下：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310301326618.png" alt="img" style="zoom: 50%;" />

## 实现

1. 定义：主要是定义一个BeanDefinition类，然后将bean的实例化对象存储进去，所以这里直接使用Object来存储这个对象
2. 注册，主要是将BeanDefinition中的bean对象及其名称之间的映射关系保存起来，代码中直接使用HashMap来存储这种映射关系
3. 获取，直接按照键，也就是bean的名称获取到对应的bean，这里并没有考虑bean不存在的问题，因为本节中只是最简单的手动实例化一个bean，然后将其存储到容器中
4. 测试：针对定义的IOC容器进行测试：

```java
@Test
public void testBeanFactory() {
    //1.定义
    //获取工厂对象
    BeanFactory beanFactory = new BeanFactory();
    //准备要注入的bean对象
    UserService userService = new UserService();
    //将其注入到BeanDefinition中
    BeanDefinition beanDefinition = new BeanDefinition(userService);
    //2.注册
    //将bean保存到IOC容器中
    beanFactory.
        registerBeanDefinition("userService", beanDefinition);
    //3.获取
    //根据名称从IOC容器中取出对应的bean
    UserService user = (UserService) beanFactory.getBean("userService");
    //执行bean的对应方法
    user.print();
}
```

可以看出测试的过程就是上面描述的三步，只不过相比于真正的spting，大大的简化了所有的步骤

## 总结

本节实现了一个最基础的不标准IOC容器，后面的章节会逐步对这个容器进行扩充
