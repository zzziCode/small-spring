---
title: "Small_spring08"
description: "small_spring08"
keywords: "small_spring08"

date: 2023-11-06T13:09:13+08:00
lastmod: 2023-11-06T13:09:13+08:00

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
#url: "small_spring08.html"


# 开启各种图渲染，如流程图、时序图、类图等
# Enable chart render, such as: flow, sequence, classes etc
#mermaid: true
---

>🍍 small_spring08

在上一节中，我们在原有项目的基础上增加了初始化和销毁模块，并且实现方式有两种，分别是xml配置和实现接口，在初始化和销毁时可以进行资源的管理。本节中我们进一步**扩展**bean的功能，使其可以获取到spring中的一些容器资源，为了获得这些容器资源，需要一些成员变量接收，然后在生命周期中增加这些变量的注入代码，最后bean对象就可以使用这些容器资源了，具体的代码我放到了[仓库](https://github.com/zzziCode/small-spring)中

<!--more-->

## 原因

​		之前几节中已经将项目的功能一步步的扩充了，这些功能分别是bean的定义，注册，属性填充，xml文件配置，应用上下文，修改，初始化和销毁。这些功能使得现在的简易spring框架更加完善，但是这些功能并没有增强bean本身的能力。当bean想要使用spring的一些容器资源，例如bean内部想要使用类加载器，想要获得应用上下文，从而知道总共有哪些bean等操作、现有的spring框架还无能为力，所以本节中重点解决的就是如何让bean本身能够获得spring的**容器资源**

​		对于每一个bean来说，其想要使用的spring容器资源是不同的，所以不能统一的将所有的容器资源给每一个bean，而是转换思维，利用之前的思路，spring提供一个接口，然后谁实现这个接口，谁就有了对应的功能。这里也是一样，想要使用什么容器资源，就实现什么接口，接口内部提供一个set方法，这样就可以在合适的地方调用bean内部的这个set方法完成set注入，例如：

![image-20231106140004209](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311061526668.png)

​		spring提供这样一个接口，实现这个接口的bean对象必须实现`setBeanName`方法，从而在合适的地方既可以将`BeanName`注入给bean。这样的接口有很多，本项目中提供四种接口，分别是：`ApplicationContextAware`，`BeanFactoryAware`，`BeanClassLoaderAware`，`BeanNameAware`，不同的bean可以继承不同的接口实现**各取所需**的效果。

​		扩充bean自身功能之后，项目的框架变为：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311061413043.png" alt="img" style="zoom:67%;" />

## 思路

​		为了实现bean可以使用容器资源，项目做了如下设计：

1. 定义一个统一的`Aware`接口作为一个标志，想要使用具体的资源，就先要实现这个接口

2. 针对每一种资源提供一种接口，这个接口需要继承`Aware`接口，代表他是提供资源的接口，内部针对不同的资源提供不同的set方法

3. 不同的bean想要使用不同的容器资源，就需要实现不同的资源接口，并覆盖里面的set方法，然后在内部定义一些属性来接收这些资源，例如：

   ![image-20231106141119361](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311061526670.png)

   `UserService`中想要使用四个容器资源，那么就需要按照统一的设计方式，实现接口，定义成员属性，重写set方法。

4. spring在**合适的时机**将容器资源通过实现的set方法注入进bean内部，之后bean就可以使用这些资源了

​		为了注入这些容器资源，需要找到一个合适的时机统一注入，而这个注入时机spring选择在了实例化后修改bean属性的时候。因为这些容器资源不经过xml文件的配置，所以经过实例化后，bean的一些基础属性都填充好了，而这些容器资源的注入也可以看做是修改，所以将这个注入时机放到了实例化后的修改操作当中，也就是`initializeBean`中。

​		针对不同的容器资源，存在的时机又是不同的，有可能在`initializeBean`注入容器资源的时候，在这里获取不到目标容器资源，例如`ApplicationContext` 的获取并不能直接在创建 Bean 时候就可以拿到，所以需要在 `refresh` 操作时，把 `ApplicationContext` 写入到一个包装的 `BeanPostProcessor` 中的实现类中去，之后将其封装成修改策略来执行，内部简单调用set方法即可，具体的注入流程如下：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311061526671.png" alt="image-20231106143230830" style="zoom:67%;" />

​		上面的容器资源注入流程中，只有一个特殊的`ApplicationContext` 注入时有一些区别，因为他在`initializeBean`中已经获取不到了，所以只能在其还存在时保存住，然后在最后统一注入，为了保存`ApplicationContext` ，定义了一个包装处理器，也就是一个实例化后的修改逻辑，在内部就是调用set方法来注入`ApplicationContext` 资源，其余的容器资源直接注入即可

​		为了实现容器资源的注入，项目中新增了一些类，修改了一些类，下面详细介绍这些类的变化：

### 类的变化

#### 新增的类

1. `Aware`：是一个标记接口，实现这个接口的接口变成了资源接口，bean实现资源接口就会拥有对应的容器资源，内部没有任何内容

2. `ApplicationContextAware`：提供应用上下文资源的接口，内部有一个待实现的set方法

   ![image-20231106144056219](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311061526673.png)

3. `BeanFactoryAware`：提供`beanFactory`资源的接口，内部有一个待实现的set方法

   ![image-20231106144135400](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311061526674.png)

4. `BeanClassLoaderAware`：提供类加载器资源的接口，内部有一个待实现的set方法

   ![image-20231106144213024](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311061526675.png)

5. `BeanNameAware`：提供bean名称的资源接口，内部有一个待实现的set方法

   ![image-20231106144247248](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311061526676.png)

6. `ApplicationContextAwareProcessor`：为了保存应用上下文的容器资源，最后统一注册而添加的包装处理器，是一个实例化后修改的策略类，内部实例化修改就是使用set方法来注入应用上下文接口：

   ![image-20231106144425361](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311061526677.png)

​		增加了这些类之后，还需要修改一些类，这样才能在合适的时机注入容器资源

#### 修改的类

1. `AbstractApplicationContext`：修改了refresh方法，增加了一步，单独保存一个`ApplicationContextAwareProcessor`对象，并且利用构造函数保存当前的应用上下文，目的是为了将应用上下文的注入操作延后，与其他注入操作统一到一起：

   ```java
   @Override
   public void refresh() throws BeansException {
       // 1. 创建 BeanFactory，并加载 BeanDefinition
       refreshBeanFactory();
   
       // 2. 获取 BeanFactory
       ConfigurableListableBeanFactory beanFactory = getBeanFactory();
   
       // 3. 添加 ApplicationContextAwareProcessor，让继承自 ApplicationContextAware 的 Bean 对象都能感知所属的 ApplicationContext
       beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
   
       // 4. 在 Bean 实例化之前，执行 BeanFactoryPostProcessor (Invoke factory processors registered as beans in the context.)
       invokeBeanFactoryPostProcessors(beanFactory);
   
       // 5. BeanPostProcessor 需要提前于其他 Bean 对象实例化之前执行注册操作
       registerBeanPostProcessors(beanFactory);
   
       // 6. 提前实例化单例Bean对象
       beanFactory.preInstantiateSingletons();
   }
   ```

2. `AbstractBeanFactory`：增加了一个类加载器的属性，同时提供一个get方法，便于后期容器资源注入时直接调用这个方法：

   ![image-20231106153835057](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311061538272.png)

3. `AbstractAutowireCapableBeanFactory`：修改了`initializeBean`方法，在这里统一执行容器资源注入的方式，分别调用不同的set方法来注入，由于使用了`instanceof`，谁实现了对应的容器资源接口谁就能完成注入

   ```java
   private Object initializeBean(String beanName, Object bean, BeanDefinition beanDefinition) {
       // 可以直接注入的容器资源直接调用set方法注入
       if (bean instanceof Aware) {
           if (bean instanceof BeanFactoryAware) {
               ((BeanFactoryAware) bean).setBeanFactory(this);
           }
           //这个注入调用了上面AbstractBeanFactory中增加的get方法
           if (bean instanceof BeanClassLoaderAware){
               ((BeanClassLoaderAware) bean).setBeanClassLoader(getBeanClassLoader());
           }
           if (bean instanceof BeanNameAware) {
               ((BeanNameAware) bean).setBeanName(beanName);
           }
       }
   
       // 无法直接注入的容器资源被包装到了实例化后的修改逻辑中
       Object wrappedBean = applyBeanPostProcessorsBeforeInitialization(bean, beanName);
   
       // 执行 Bean 对象的初始化方法
       try {
           invokeInitMethods(beanName, wrappedBean, beanDefinition);
       } catch (Exception e) {
           throw new BeansException("Invocation of init method of bean[" + beanName + "] failed", e);
       }
   
       // 2. 执行 BeanPostProcessor After 处理
       wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
       return wrappedBean;
   }
   ```

​		在这里完成了容器资源的统一注入，对于`BeanFactoryAware`，`BeanFactoryAware`，`BeanNameAware` 来说，可以直接注入，所以谁需要谁就直接注入，是否需要通过`instanceof`来判断，只要实现了对应的接口，spring就认为这个bean需要对应的容器资源

​		对于 `ApplicationContextAware` 来说，无法在这里统一注入，因为这里无法直接获取到`ApplicationContext`，所以在前面能够获取到`ApplicationContext`的地方，将其放到了一个包装处理器中的成员属性当中，这个包装处理器就是一个实例化后的修改逻辑，其修改逻辑就是调用set属性将容器资源注入。在refresh方法中被保存到了实例化后修改逻辑的容器中之后，然后在`initializeBean`中会被调用，从而出发set的容器资源注入，也就完成了统一的容器资源注入

​		所以总结来说，注入的方式有两种，形式是统一的：

1. 直接注入容器资源，因为可以直接获取到

2. 包装成一个实例化后的修改逻辑对象，内部先保存容器资源，然后触发修改逻辑时就完成了注入，这样做的原因是因为统一注入的时候容器资源已经获取不到了，所以先行保存

   > 最终的注入形式都是调用set方法

### bean的创建和获取

​		经过上面的分析，已经知道了容器资源的注入时机，对于特殊的无法直接注入的容器资源如何处理，现在我们通过debug的方式来说明增加容器资源注入之后，bean的创建和获取的区别，现在项目中有两个bean对象，分别是`userDao`和`userService`，其中`userDao`在xml中配置了初始化和销毁的逻辑，`userService`中只是注入了四个容器资源，为了实现容器资源的注入，`userService`中进行了一些处理，通过实现几个接口，定义几个属性，重写几个set方法尝试进行容器资源的注入：

![image-20231106152211409](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311061526678.png)

​		下面描述其运行过程：

1. 初始化应用上下文，传递一个配置文件的路径：

   ![image-20231104102941451](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108233.png)

2. 执行构造函数，在其中调用`refresh`方法：

   ![image-20231103102148629](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107849.png)

   > refresh方法的代码为：
   >
   > **其中第三步是新增的一步**
   >
   > ```java
   > @Override
   > public void refresh() throws BeansException {
   >     // 1. 创建 BeanFactory，并加载 BeanDefinition
   >     refreshBeanFactory();
   > 
   >     // 2. 获取 BeanFactory
   >     ConfigurableListableBeanFactory beanFactory = getBeanFactory();
   > 
   >     // 3. 添加 ApplicationContextAwareProcessor，让继承自 ApplicationContextAware 的 Bean 对象都能感知所属的 ApplicationContext
   >     beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
   > 
   >     // 4. 在 Bean 实例化之前，执行 BeanFactoryPostProcessor (Invoke factory processors registered as beans in the context.)
   >     invokeBeanFactoryPostProcessors(beanFactory);
   > 
   >     // 5. BeanPostProcessor 需要提前于其他 Bean 对象实例化之前执行注册操作
   >     registerBeanPostProcessors(beanFactory);
   > 
   >     // 6. 提前实例化单例Bean对象
   >     beanFactory.preInstantiateSingletons();
   > }
   > ```

3. 创建`beanFactory`并获取，这不是本节中的重点：

   ![image-20231106150751741](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311061526679.png)

4. 保存应用上下文的容器资源到一个**包装处理器**当中，其实就是一个实例化后的修改逻辑对象，通过构造函数保存当前这个容器资源：

   ![image-20231106150854564](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311061526680.png)

   ![image-20231106150943526](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311061526681.png)

   可以看出，通过构造函数保存之后，类中的成员变量就是对应的容器资源，其中实例化后的修改逻辑就是调用set方法完成容器资源的注入，只需要在合适的时机触发这个方法执行即可

5. 执行实例化前的修改，保存实例化后的修改，这不是本项目中的重点：

   ![image-20231104103738869](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108235.png)

6. 实例化所有的bean对象，最终到达`createBean`方法中，执行空bean的创建和bean属性填充：

   ![image-20231104103936186](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108236.png)

7. 进入`initializeBean`方法之后，对于能直接注入的容器资源，就直接注入，对于不能直接注入的容器资源，会被封装到包装处理器当中，触发他的修改逻辑就能进行注入

   - 直接注入的容器资源

     ![image-20231106151509151](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311061526682.png)

   - 无法直接注入的容器资源，触发修改逻辑，在修改逻辑中完成注入

     ![image-20231106151705127](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311061526683.png)

8. 执行`invokeInitMethods`中的初始化方法，以及其他一些处理，完成bean的创建，此时就可以利用容器中的bean对象执行一些业务了

![image-20231106152036704](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311061526684.png)

最终的执行结果如下，发现即使xml文件中不配置这些容器资源，也能完成注入：

![image-20231106160546123](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311061606072.png)

## 总结

​		本节中实现了向bean对象中注入容器资源的功能，将容器资源注入的操作统一在了一起，可以直接注入的资源直接注入，不能直接注入的资源封装到了一个包装处理器中，在包装处理器的实例化后修改逻辑中执行容器资源的注入，几个类的依赖关系如下：

![spring-9-02](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311061526685.png)

​		其中四个资源接口实现了一个统一的`Aware`接口，然后对于不能直接注入的应用上下文资源，在refresh方法中封装到了一个包装处理器的实例化后修改逻辑中，对于能直接注入的资源，直接在`AbstractAutowireCapableBeanFactory`类的`initializeBean`方法中完成注入
