---
title: "Small_spring03"
description: "small_spring03"
keywords: "small_spring03"

date: 2023-10-30T20:51:28+08:00
lastmod: 2023-10-30T20:51:28+08:00

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
#url: "small_spring03.html"


# 开启各种图渲染，如流程图、时序图、类图等
# Enable chart render, such as: flow, sequence, classes etc
#mermaid: true
---

>🍈 small_spring03

本文主要是在上一章的基础上对上一章中代码存在的问题进行改造，上一章中奖bean对象的创建交给了IOC容器，利用反射创建bean对象并保存到IOC容器中，但是忽略了一点，上章中代码只能创建不携带参数的bean对象，所以这一章主要解决的问题就是创建有参数的bean对象，具体的代码在[仓库](https://github.com/zzziCode/small-spring.git)中

<!--more-->

## 原因

​		为什么上一章中只能创建不带参数的bean对象呢，主要问题出现在`createBean`函数中，可以查看createBean的代码就能够发现问题：

```java
@Override
protected Object createBean(String beanName, BeanDefinition beanDefinition) throws BeansException {
    Object bean;
    try {
        //利用反射,根据传递来的类信息创建一个实例化对象
        bean = beanDefinition.getBeanClass().newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
        throw new BeansException("Instantiation of bean failed", e);
    }

    //在这里就是创建成功
    //调用继承自DefaultSingletonBeanRegistry中的addSingleton方法将这个对象保存到容器中
    addSingleton(beanName, bean);
    //将创建的bean返回，返回值用于getBean的返回值
    return bean;
}
```

​		在第六行中，直接利用反射，调用`newInstance`方法创建一个bean对象，此处调用时并没有区分bean对象是否携带参数，导致创建出来的bean对象全都是无参的，所以需要改进的地方就在这里，将这里创建bean对象的代码进行扩充，接收bean对象的参数，就可以在创建bean对象时创建携带参数的bean对象了

## 思路

为了实现上面描述的：可以创建携带参数的bean对象，需要解决两个问题：

1. 如何接受或者如何传递bean对象的参数
2. 如何对现有项目合理的扩展，实现可以创建携带参数的bean对象

​		为了解决第一个问题，我们可以在获取bean对象的时候就传递一些参数，这样既可以用参数区分获取的对象是谁，又可以在没有目标bean对象时，根据传递的参数创建一个目标bean对象。而对外暴露的获取bean对象的接口为`getBean`，所以第一个问题的突破口就在`getBean`函数上

​		为了解决第二个问题，可以在`createBean`的函数中进行改造，引入一个扩展类，用来实现根据不同的参数创建不同的bean对象，对于创建bean对象来说，有两种方式：

1. 利用`JDK`的**反射**机制
2. 利用`CGlib`的**字节码**机制

所以我们可以单独建立一个实例化模块，专门用来创建bean对象，根据接受的参数不同创建不同的bean对象，对外暴露一个创建bean对象的接口，然后`createBean`方法中**不是简单的调用`newInstance`方法**，而是调用这个封装好的接口来创建目标对象，形成的结构图为：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310311241940.png" alt="图 4-1" style="zoom: 50%;" />

形成的新的类图如下，可以发现只是多了左边的一个实例化模块，然后在原有的类结构的基础上增加了一些方法，主要是对外提供的接口可以接受bean对象的参数了

![image-20231031124847218](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310311248959.png)

在这个类结构图的基础上，对变化的类进行说明，没有描述的部分请参考第二章的[文档](https://zzzicode.github.io/post/small_spring02/)

### 类的说明

1. `InstantiationStrategy`：是一个接口，主要提供了创建带参的bean对象的接口`instantiate(BeanDefinition beanDefinition, String beanName, Constructor ctor, Object[] args)`，四个参数的含义分别代表bean对象的类信息，bean对象的名称，创建bean对象用到的构造函数，构造函数中的参数。在`createBean`方法中调用这个方法之后，就可以得到携带参数的bean对象，接口的结构为：

   ![image-20231031125857597](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310311318726.png)

   > 这是本章节的核心接口

2. `SimpleInstantiationStrategy`：是`InstantiationStrategy`的其中一个实现类，也是项目中第一个实例化bean对象的**策略类**。内部使用JDK的反射机制来创建携带参数的bean对象，类的结构为：

   ![image-20231031125924252](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310311318727.png)

3. `CglibSubclassingInstantiationStrategy`：是`InstantiationStrategy`的另外一个实现类，也是项目中第二个实例化bean对象的**策略类**。内部使用了ASM字节码框架来创建携带参数的bean对象，类的结构为：

   ![image-20231031125940874](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310311318728.png)

   > 注意：要使用CGlib创建bean对象，需要引入cglib的依赖
   >
   > ```xml
   > <dependency>
   >     <groupId>cglib</groupId>
   >     <artifactId>cglib</artifactId>
   >     <version>3.3.0</version>
   > </dependency>
   > ```

4. `AbstractAutowireCapableBeanFactory`：创建bean对象的类，在原有的基础上保存了一个成员变量，来代表当前使用哪一种实例化策略，默认使用的是**CGlib**的策略，并且createBean方法中不再是直接创建bean对象，而是调用了一个`createBeanInstance`方法，原先的类的结构为：

   ![image-20231030102532007](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310301057549.png)

   新的类的结构为：

   ![image-20231031130058902](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310311318729.png)

   红色圈起来的部分是新增的重点，实现了按照指定的策略创建带参的bean对象的功能

5. `AbstractBeanFactory`：原来的类中，getBean方法中先尝试直接获取bean对象，否则直接调用`createBean`方法创建bean对象，现在将这些工作交给了`doGetBean`方法，又封装了一层，新的结构为：

   ![image-20231031130335141](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310311318731.png)

   将获取Bean的操作抽取出来变成doGetBean方法的目的是为了减小代码量，因为两个重载版本的getBean方法都需要获取bean对象，只是传递的参数不一样，所以将这部分代码抽取出来，后期调用时传递的参数不一样即可：

   ![image-20231031133443399](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310311334811.png)

> 以上就是本节中的代码比与上一节多出来或者修改的部分，目的是为了实现创建带参的bean对象

### bean的创建和获取

为了获得带参数的bean，最外层获取bean的时候就需要传递参数，根据参数的不同来匹配不同的bean对象，初始状态下，会利用传递来的参数创建一个新的带参的bean对象保存到IOC容器中并返回，整体的流程为：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310311313921.png" alt="未命名文件" style="zoom:67%;" />

上一章中获取bean对象的流程为：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310301144543.png" alt="image-20231030114408629" style="zoom:50%;" />

可以看出，相比于上一章，在createBean的时候增加了两步，本章中按照实例化策略，根据接受的参数来创建带参的bean对象，其整体步骤还是与上一章**保持一致**

​		核心就是如何利用传递来的参数来创建带参bean对象，主要是在增加的两个方法`createBeanInstance`和`instantiate`中，前一个方法通过参数列表的类型匹配到目标构造函数，然后将目标构造函数传递给`instantiate`方法，在`instantiate`方法中完成创建，`createBeanInstance`方法的代码如下：

```java
private Object createBeanInstance(BeanDefinition beanDefinition, String beanName, Object[] args) {
    //如果参数列表为空，直接调用实例化策略创建无参对象并返回
    if (args == null)
        return getInstantiationStrategy().instantiate(beanDefinition, beanName, null, null);

    //在这里说明需要创建带参bean对象
    //1.尝试找到目标构造函数
    Constructor constructorToUse = null;

    //获取bean对象的类信息
    Class beanClass = beanDefinition.getBeanClass();
    //获取到这个bean对象的所有构造函数
    Constructor[] declaredConstructors = beanClass.getDeclaredConstructors();
    //遍历所有的构造函数，用参数列表去匹配，从而找到目标构造函数
    for (Constructor declaredConstructor : declaredConstructors) {
        //获取到当前构造函数的参数列表
        Class[] parameterTypes = declaredConstructor.getParameterTypes();
        //参数个数都不一样，肯定不匹配,继续查找下一个
        if (parameterTypes.length != args.length)
            continue;
        //参数个数一样，看类型是否一样
        int i = 0;
        for (; i < parameterTypes.length; i++) {
            //参数列表的类型不匹配,继续搜索合适的构造函数
            if (parameterTypes[i] != args[i].getClass())
                break;
        }
        //参数列表匹配到了末尾说明找到目标构造函数
        if (i == parameterTypes.length) {
            constructorToUse = declaredConstructor;
            break;
        }
    }
    //在这里创建带参的bean对象
    return getInstantiationStrategy().instantiate(beanDefinition, beanName, constructorToUse, args);
}
```

可以看出，核心就是利用反射得到当前bean对象的所有构造方法，然后再得到每一个构造方法 的参数列表，**对参数列表进行匹配**，需要注意的是，参数匹配时，由于`getBean`接受参数时使用的是**Object数组**，所以传递int参数时会将其转换成Integer，故而匹配参数时会出现问题，所以bean对象的构造函数中，**不使用基本类型存储参数**

## 总结

为了创建带参数的bean，主要改进了`getBean`方法和`createBean`方法，前者使得程序可以接受bean对象的参数，后者可以利用这个参数创建带参的bean对象，内部调用了新增的实例化模块，默认使用`CGlib`的方式创建bean对象，参考的类图如下：

![图 4-2](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202310311318694.png)

创建bean对象的过程中存在一些问题，但是整体上还是可以创建带参的bean对象了，主要原理就是按照传递来的参数来找到目标有参构造函数，从而创建带参bean对象
