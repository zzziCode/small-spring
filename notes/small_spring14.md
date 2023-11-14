---
title: "Small_spring14"
description: "small_spring14"
keywords: "small_spring14"

date: 2023-11-13T20:35:39+08:00
lastmod: 2023-11-13T20:35:39+08:00

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
#url: "small_spring14.html"


# 开启各种图渲染，如流程图、时序图、类图等
# Enable chart render, such as: flow, sequence, classes etc
#mermaid: true
---

>🍟 small_spring14

​	上一节中使用注解的形式进行属性填充，但是填充的是普通的bean，也就是说代理对象的属性还都是`null`，这一节中我们就来解决这个问题，改变代理对象的创建时机，使得创建的bean代理对象内部的属性也不为空，但是并不是直接让代理对象本身的属性不为空，而是增加一个`target`，`target`的内部属性不为空，相关的代码我放到了[仓库](https://github.com/zzziCode/small-spring)中

<!--more-->

> 就是将代理对象的创建延后，然后最终被代理对象中的属性就可以填充完毕，这样代理对象执行时被自己的方法拦截器拦截之后，内部target就是**属性填充**过后的对象，代理对象自身的属性为空，但是target并不为空

## 原因

​		上一节中我们实现了通过注解注入属性的功能，这一节中我们解决之前遗留的问题，主要是代理对象的创建还在bean的生命周期之外，并且创建的代理对象内部并没有填充过的属性。为了解决这个问题，我们将代理对象的创建时机移动到属性填充完毕之后且初始化之后的`postProcessAfterInitialization`方法中，核心类还是`DefaultAdvisorAutoProxyCreator`，只是创建代理对象的方法变成了`postProcessAfterInitialization`，经过这样的操作，代理对象的创建会在被代理对象属性填充之后，此时我们将代理对象中的`target`赋值为属性填充之后的被代理对象，这样就解决了代理对象中的属性没有值的问题，新的模块关系图如下：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311141414636.png" alt="img" style="zoom:67%;" />

## 思路

​		将代理对象的创建延后之后，内部`target`保存了属性填充之后的被代理对象，在代理对象执行方法时，最终会到达`JDK`中的`invoke`方法或者`Cglib`中的`intercept`方法中，在这两个方法中执行方法匹配之后，如果匹配成功则会执行方法拦截器的`invoke`方法，内部最终会调用`target`的原始方法执行，由于之前的`target`是填充的时候新建的一个空对象，所以出现了代理对象内部的属性为空的情况：
![image-20231114141734924](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311141535327.png)

在本节中`target`填充的是已经参数填充过后的被代理有参对象，所以解决了之前的问题：

![image-20231114141843416](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311141535329.png)

​		总结来说**核心步骤**就是更改了方法拦截器中的`target`，使其存储属性填充过后的bean，这样在代理对象的方法执行被拦截时，触发`target`的原始方法调用，`target`中就有值了

### 类的变化

下面介绍为了实现带参的代理对象的创建，项目中类的变化，由于本节中只是延缓了代理对象的创建时机，所以并没有新增任何类，只是将之前代理对象创建的代码移动了位置并修改了一些细微的地方。

#### 修改的类

1. `InstantiationAwareBeanPostProcessor`：接口中新增了一个方法`postProcessAfterInstantiation`，在实现类中暂时没做任何实现，直接返回true

2. `DefaultAdvisorAutoProxyCreator`：将代理对象的创建过程移动到了`postProcessAfterInitialization`方法中，方法内部target填充的是形参中的bean，也就是参数填充过后的bean。原先创建代理对象的方法`postProcessBeforeInstantiation`直接返回null：

   ```java
   @Override
   public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
       Class<?> beanClass = bean.getClass();
       if (isInfrastructureClass(beanClass))
           return bean;
   
       //拿到配置的代理信息
       Collection<AspectJExpressionPointcutAdvisor> advisors = beanFactory.getBeansOfType(AspectJExpressionPointcutAdvisor.class).values();
       for (AspectJExpressionPointcutAdvisor advisor : advisors) {
           //拿到类匹配器，从而判断当前类是否需要代理
           ClassFilter classFilter = advisor.getPointcut().getClassFilter();
           //当前切入点表达式不匹配当前bean，尝试下一个
           if (!classFilter.matches(beanClass))
               continue;
           //在这里就是匹配成功，准备创建代理对象
   
           AdvisedSupport advisedSupport = new AdvisedSupport();
           /**@author zzzi
                * @date 2023/11/14 13:47
                * 这里保存的是已经属性填充过的bean
                */
           TargetSource targetSource = new TargetSource(bean);
   
           //填充创建代理对象所需要的参数
   
           advisedSupport.setTargetSource(targetSource);
           advisedSupport.setMethodInterceptor((MethodInterceptor) advisor.getAdvice());
           advisedSupport.setMethodMatcher(advisor.getPointcut().getMethodMatcher());
           advisedSupport.setProxyTargetClass(true);
   
           //调用代理工厂中的方法得到一个代理对象并返回
           return new ProxyFactory(advisedSupport).getProxy();
       }
       //当前bean没有创建成功代理对象，就返回空
       return bean;
   }
   ```

3. `TargetSource`：创建代理对象时，要得到被代理对象实现了哪些接口，而`Cglib`是通过继承实现的，所以要增加一步判断，从而拿到真正的被代理对象所实现的接口：

   ```java
   public Class<?>[] getTargetClass() {
       Class<?> clazz = target.getClass();
       /**@author zzzi
            * @date 2023/11/14 13:49
            * 获取真正的被代理对象的类型
            */
       clazz = ClassUtils.isCglibProxyClass(clazz) ? clazz.getSuperclass() : clazz;
       return clazz.getInterfaces();
   }
   ```

4. `AbstractAutowireCapableBeanFactory`：`createBean`方法中在空bean创建之后新增一步，并且修改默认的bean对象创建逻辑为`JDK`：

   ```java
   boolean continueWithPropertyPopulation = applyBeanPostProcessorsAfterInstantiation(beanName, bean);
   if (!continueWithPropertyPopulation) {
       return bean;
   }
   //修改默认的bean对象创建逻辑为JDK
   private InstantiationStrategy instantiationStrategy = new SimpleInstantiationStrategy();
   ```

   `applyBeanPostProcessorsAfterInstantiation`方法内部触发所有`postProcessAfterInstantiation`方法的执行，核心就是触发了动态代理对象的创建，而此时被代理对象的属性已经填充过了

​		剩下的操作与之前创建动态代理一样，自定义一个通知，声明如何增强被代理对象，然后在xml文件中配置bean，通知，方法拦截器，切面。然后直接执行，当某一个bean被切面中的切入点表达式匹配成功时就会给他创建代理对象，对外没有产生任何异样，后期获取这个bean的时候，获取的也是代理对象，执行其中的方法会执行方法匹配，匹配成功的方法执行方法拦截器中的逻辑，从而执行通知和被代理对象的原始方法。用户看到的就是增强bean之后的结果了

### bean的创建和获取

下面通过debug的方式描述新的代理对象创建流程，被代理的对象是`userService`：

1. 扫描配置文件，拿到所有bean的注册信息到注册表中：

   ![image-20231114150605475](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311141535330.png)

2. `invokeBeanFactoryPostProcessors`执行实例化前的修改操作，主要是替换xml配置中的占位符，然后将字符串处理器保存到容器中：

   ![image-20231114150729673](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311141535331.png)

3. 保存实例化后的修改逻辑，这里有**两个**修改逻辑，一个是利用**注解填充属性**的`AutowiredAnnotationBeanPostProcessor`，核心方法是`postProcessPropertyValues`，另外一个是**创建代理对象**的`DefaultAdvisorAutoProxyCreator`，核心方法在`postProcessAfterInitialization`：

   ![image-20231114150950924](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311141535333.png)

4. 初始化所有的bean对象，这里只对`userService`进行了代理，所以只介绍`userService`的初始化步骤：

   1. `resolveBeforeInstantiation`返回空，什么也不做

      ![image-20231114152851691](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311141535334.png)

   2. `createBeanInstance`创建空bean，现在的bean还是没有被代理的空bean

      ![image-20231114152914545](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311141535335.png)

   3. `applyBeanPostProcessorsAfterInstantiation`中触发所有`postProcessAfterInstantiation`方法的执行，没有做任何改变

   4. `applyBeanPostProcessorsBeforeApplyingPropertyValues`给刚才的空bean填充属性：

      ![image-20231114151427109](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311141535336.png)

   5. `initializeBean`中执行了下面几步：

      - 容器资源注入：

        ![image-20231114151512378](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311141535337.png)

      - `applyBeanPostProcessorsBeforeInitialization`执行初始化前的操作，这里尝试进行应用上下文的容器资源注入

      - `invokeInitMethods`执行初始化，如果bean携带有初始化方法就在这里执行

      - `applyBeanPostProcessorsAfterInitialization`执行代理对象的创建，是**核心方法**：

        ![image-20231114151708224](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311141535338.png)

        代理对象创建成功之后，自身的属性**为空**，但是内部`target`中的属性**不是空**，后期执行方法时，被方法拦截器拦截之后，在内部调用原始方法执行时，会调用`target`的原始方法执行，**而`target`中参数已经填充**，解决了之前代理对象中参数为空的问题

   6. 如果当前被代理的对象是单例的话，那么将创建完成的代理对象保存到**单例池**中

      ![image-20231114152219160](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311141535339.png)

5. 创建完成之后，从单例池中获取到bean对象，执行相应的业务，最终的执行结果为：

   ![image-20231114152328360](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311141535340.png)

## 总结

​		本节中为了给动态代理的对象进行属性填充，另辟蹊径，不直接填充代理对象本身的属性，而是填充内部`target`被代理对象的属性，而这个被代理对象的属性填充已经在`createBean`中做过了，所以将代理对象的创建时机延后，当被代理对象的属性填充完毕之后再进行代理对象的创建，并且将`target`赋值为这个属性填充完毕之后的被代理对象，后期执行被代理对象的原始方法时会调用`target`中的原始方法执行，而`target`中保存的是属性填充过后的被代理对象，所以最终的原始方法执行时就可以使用里面的一些属性，从而解决了之前的问题

> 核心就是将代理对象的创建时机延后，`target`保存的是属性填充之后的被代理对象
