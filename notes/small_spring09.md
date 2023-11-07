---
title: "Small_spring09"
description: "small_spring09"
keywords: "small_spring09"

date: 2023-11-06T18:19:26+08:00
lastmod: 2023-11-06T18:19:26+08:00

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
#url: "small_spring09.html"


# 开启各种图渲染，如流程图、时序图、类图等
# Enable chart render, such as: flow, sequence, classes etc
#mermaid: true
---

>🍑 small_spring09

​		在上一节中，我们实现了向bean中注入容器资源的功能，注入时不用关心配置文件中，需要什么就实现什么资源接口即可，这使得bean的功能更强大也更灵活。但是至今为止，我们还只是创建单例的bean，没有实现如何创建原型模式的bean，并且bean的创建只能从配置文件中获取，一旦bean涉及到的配置太多，xml文件的编写就会异常复杂，所以本节中有两个目标：

1. 创建多种模式的bean（单例或者原型）
2. 以多种方式创建bean对象

​		本节涉及到的代码我放到了[仓库](https://github.com/zzziCode/small-spring)中

<!--more-->

## 原因

​		为了创建多种类型的bean对象，我们将`createBean`的代码进一步改进，增加bean的注册信息，使其包含当前bean的模式，如果当前bean是单例模式，那么我们将其创建出来并保存到容器中方便下次使用，如果当前bean是原型模式，那么每次我们都新建并不保存这个bean，这样可以保证每次都是新的bean对象，总结起来，为了实现第一个目标，做了两点改变：

1. 修改bean的注册类`BeanDefinition`，使其拥有模式这个状态：

   <img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311071055017.png" alt="image-20231106200844520" style="zoom:67%;" />

2. 修改创建方式`createBean`的代码：

   ```java
   @Override
   protected Object createBean(String beanName, BeanDefinition beanDefinition, Object[] args) throws BeansException {
       Object bean = null;
       try {
           bean = createBeanInstance(beanDefinition, beanName, args);
           // 给 Bean 填充属性
           applyPropertyValues(beanName, bean, beanDefinition);
           // 执行 Bean 的初始化方法和 BeanPostProcessor 的前置和后置处理方法
           bean = initializeBean(beanName, bean, beanDefinition);
       } catch (Exception e) {
           throw new BeansException("Instantiation of bean failed", e);
       }
   
       // 注册实现了 DisposableBean 接口的 Bean 对象
       registerDisposableBeanIfNecessary(beanName, bean, beanDefinition);
   
       // 判断 SCOPE_SINGLETON、SCOPE_PROTOTYPE来决定是否保留当前bean
       if (beanDefinition.isSingleton()) {
           addSingleton(beanName, bean);
       }
       return bean;
   }
   ```

   ​		以上实现多种模式的bean对象的流程如下，相比于非单例模式的创建，单例模式只是多了一步保存到容器中的过程：

   <img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311071055019.png" alt="image-20231106202149569" style="zoom:50%;" />

   ​		为了将复杂bean创建过程中繁杂的xml配置转换成java代码，我们引入了几个类，核心就是`FactoryBean`类，如果一个bean想要不通过配置文件创建，那么就需要继承这个类，然后将创建逻辑在这个类中的`getObject`方法中实现，编码完毕之后，将这个`FactoryBean`对象交给配置文件来管理，配置文件只创建`FactoryBean`对象，内部真正包裹的bean对象在暂时不用管

   ​		在`doGetBean`的过程中，不管是首次创建获得的bean对象还是直接从容器中拿到的bean对象，拿到之后都包裹了一个`getObjectForBeanInstance`方法，目的是为了拿到**真正需要**的bean对象，因为上面说到，如果bean的创建被移动到`FactoryBean`中的`getObject`方法中时，配置文件创建的就是`FactoryBean`对象，而不是内部真正包裹的bean对象，所以在此时需要拿到这个真正的bean

   ​		`getObjectForBeanInstance`方法内部做了类型判断，如果是普通的bean对象，那么就是目标bean对象，不用任何处理直接返回，如果是工厂bean对象，那么首先尝试从缓存中拿，否则就调用其中的`getObject`方法，拿到真正的bean对象返回，具体的创建过程如下图所示：

   <img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311071055020.png" alt="image-20231106203659590" style="zoom:67%;" />

   ​		可以看出，只是加入了一个`FactoryBean`，内部包装了一个真正的bean，如果真存在这种类型的bean，将内部包装的bean获取到就可以了

## 思路

​		为了实现非单例模式的bean创建，我们在注册的时候增加模式信息用来保存当前bean是否是单例模式，创建时根据这个模式信息来判断bean是否保存，不是单例模式的bean不保存，下次使用时从容器中获取不到，就会直接**重新创建**，创建的过程中还是不保存当前bean，这样每次都是重新创建，自然就不是单例模式的bean对象了，总结起来有两点，一是修改bean注册信息，二是非单例模式bean不保存

​		为了实现bean的第二种创建方式，经历了以下几个流程：

1. 定义一个类，实现`FactoryBean`接口，在`getObject`内部创建真正的bean，然后将这个实现`FactoryBean`接口的类交给xml文件管理，这个类并不是真正的bean，而是一个**外壳**
2. 正常执行项目的流程，`doGetBean`时会根据配置文件创建bean对象，此时这个bean对象有可能是配置文件中创建的普通bean，也有可能是实现了`FactoryBean`的外壳，所以需要将这两种类型统一处理，也就是调用`getObjectForBeanInstance`方法
3. `getObjectForBeanInstance`方法内部判断是普通的bean还是外壳，通过类型判断，普通的bean直接返回，外壳需要将内部真正的bean拿出来，需要经历以下几步：
   - 先从缓存中尝试获取真正的单例bean
   - 获取到直接返回真正的bean
   - 没获取到要么是当前bean不是单例，要么当前bean是单例但是第一次获取
   - 此时调用`getObjectFromFactoryBean`方法，根据内部调用`getObject`方法获取真正的bean，根据是否是单例决定是否保存到缓存中
4. 获取到所有真正的bean之后，执行自己的业务

​		总结来看，bean的第二种创建方式就是将复杂的xml配置移动到了`getObject`方法中用java代码代替，并且在获取bean对象时增加一步`getObjectForBeanInstance`的判断，从而拿到真正的bean对象，而不是`FactoryBean`类型的外壳，最终项目的整体结构为：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311062050298.png" alt="img" style="zoom: 67%;" />

### 类的变化

#### 新增的类

1. `FactoryBean`：是一个接口，实现这个接口的类在内部的getObject方法中定义真正bean的创建方式，这个类本身只是一个外壳，交给xml文件管理

   ![image-20231107085221262](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311071055021.png)

2. `FactoryBeanRegistrySupport`：在bean的生命周期中加入的一个类，加入之前的继承图为：

   <img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311071055022.png" alt="image-20231107085527125" style="zoom:67%;" />

   加入之后的继承图为：

   <img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311071055024.png" alt="image-20231107085700524" style="zoom: 67%;" />

   在继承链中新增一个类的目的是为了从继承`FactoryBean`的外壳中获取真正的bean对象，对于单例的bean，还设置了一个缓存容器来保存真正的bean对象，当缓存中没有时，需要调用外壳中的`getObject`方法得到真正的bean对象，同时保存单例bean到缓存中，类的结构图为：

   ![image-20231107093430530](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311071055025.png)

#### 修改的类

1. `BeanDefinition`：修改bean的注册信息，新增一个模式状态信息，目的是为了保存bean的模式信息，从而根据模式信息创建不同类型的bean：

   <img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311071055017.png" alt="image-20231106200844520" style="zoom:67%;" />

2. `XmlBeanDefinitionReader`：由于bean的注册信息多了一项，所以可以在xml配置文件中指定模式状态，于是xml文件读取时就需要加入读取模式状态信息的代码，最新的读取bean的注册信息的代码为：

   ```java
   // 解析标签
   Element bean = (Element) childNodes.item(i);
   String id = bean.getAttribute("id");
   String name = bean.getAttribute("name");
   String className = bean.getAttribute("class");
   String initMethod = bean.getAttribute("init-method");
   String destroyMethodName = bean.getAttribute("destroy-method");
   //新增的一个属性，当其不为空时注入到bean的注册信息中
   String beanScope = bean.getAttribute("scope");
   ```

3. `AbstractAutowireCapableBeanFactory`：为了创建非单例模式的bean，在创建时需要判断状态，不是单例模式的bean不保存，从而下次获取无法从容器中获取，只能新建，达到原型模式的特点，为了实现这一点，改变了`createBean`方法中的代码：

   ```java
   @Override
   protected Object createBean(String beanName, BeanDefinition beanDefinition, Object[] args) throws BeansException {
       Object bean = null;
       try {
           bean = createBeanInstance(beanDefinition, beanName, args);
           // 给 Bean 填充属性
           applyPropertyValues(beanName, bean, beanDefinition);
           // 执行 Bean 的初始化方法和 BeanPostProcessor 的前置和后置处理方法
           bean = initializeBean(beanName, bean, beanDefinition);
       } catch (Exception e) {
           throw new BeansException("Instantiation of bean failed", e);
       }
   
       // 注册实现了 DisposableBean 接口的 Bean 对象
       registerDisposableBeanIfNecessary(beanName, bean, beanDefinition);
   
       // 判断 SCOPE_SINGLETON、SCOPE_PROTOTYPE来决定是否保留当前bean
       if (beanDefinition.isSingleton()) {
           addSingleton(beanName, bean);
       }
       return bean;
   }
   ```

   另外当bean对象不是单例模式时，这个bean不保存销毁策略，也就是说暂时不执行销毁策略：

   ![image-20231107090512816](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311071055026.png)

4. `AbstractBeanFactory`：这个类从继承`DefaultSingletonBeanRegistry`变为继承`FactoryBeanRegistrySupport`，目的是为了增加从外壳中获取真正bean对象的功能，而这个类的改变是将获取到的bean对象进一步处理，如果是外壳的话，还需要进一步处理，这个类中做了以下几步处理：

   - 在`doGetBean`中将获取到的bean（从容器中或者首次创建）放到`getObjectForBeanInstance`中进一步处理

   - 在`getObjectForBeanInstance`方法中针对实现`FactoryBean`的这种外壳bean进行特殊处理，利用继承自`FactoryBeanRegistrySupport`中的方法来获取到其中**真正的bean**，具体的代码为：

     ```java
     private Object getObjectForBeanInstance(Object beanInstance, String beanName) {
         //普通的bean，不是外壳bean，直接返回
         if (!(beanInstance instanceof FactoryBean)) {
             return beanInstance;
         }
        	//在这里说明当前的bean是一个实现FactoryBean的外壳bean
         //从缓存中尝试获取工厂bean对象，能获取到肯定是单例
         Object object = getCachedObjectForFactoryBean(beanName);
     
         //获取不到要么是没有，要么是非单例
         if (object == null) {
             FactoryBean<?> factoryBean = (FactoryBean<?>) beanInstance;
             //调用这个方法，调用getObject方法创建真正的bean对象
             //是单例对象还需要将其保存到缓存中
             object = getObjectFromFactoryBean(factoryBean, beanName);
         }
         return object;
     }
     ```

5. `DefaultSingletonBeanRegistry`:新增了一个常量，给继承他的`FactoryBeanRegistrySupport`类使用：

   <img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311071055027.png" alt="image-20231107093457209" style="zoom:67%;" />

> 经历以上几步就可以得到真正需要的bean对象，之后就可以拿这些bean对象执行自己的业务，核心就是为了将bean的创建变为使用java代码，从而进行了一些改变，目的是为了拿到这些真正的bean

### bean的创建和获取

​		经历上面的分析，已经知道了bean的不同创建方式是如何最终得到统一的bean对象的，下面采用debug的方式来介绍新的bean的创建和获取方式，项目中有两个bean对象，`userService`是普通的bean，注册信息在xml文件中配置，`userDao`的注册信息的配置转移到了实现`FactoryBean`接口中的`getObject`方法中，xml配置文件中配置的是实现`FactoryBean`接口的类信息，也就是xml文件中保存的是外壳，下面介绍在这种背景下如何获取bean对象

1. 配置xml文件，其中`userDao`的部分变成了配置实现`FactoryBean`的部分：

   ![image-20231107103516307](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311071055028.png)

2. 读取配置文件，进入`refresh`方法中，执行前面五步：

   ![image-20231107103632457](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311071055029.png)

3. 实例化所有的对象，最终调用的`doGetBean`方法中，首次获取调用`createBean`方法之后，此时容器中已经存在了一个bean对象，需要经过`getObjectForBeanInstance`方法拿到这个bean对象内部真正的bean对象：

   ![image-20231107103938148](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311071055030.png)

   此时可以对比`createBean`中保存的bean对象和当前经过`getObjectForBeanInstance`方法获取到的bean对象之间的区别，两个的名称都叫做`proxyUserDao`，但是里面保存的内容是不一样的：

   ![image-20231107104110405](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311071055031.png)

   `singletonObjects`中保存的是xml配置文件中创建出来的bean对象，就是一个实现了`FactoryBean`的外壳，`factoryBeanObjectCache`中保存的是外壳bean中使用`getObject`方法创建出来的**真实bean**对象

4. 实例化完成之后，就可以使用bean的名称获取到bean，进行业务操作了，对于本项目来说，实例化完成之后，只有单例的bean对象被保存了，原型模式的bean每次都需要新建，最终项目的执行结果为：

   ![image-20231107104532857](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311071055032.png)

   `userDao`的创建被移动到了一个实现`FactoryBean`的java类中，不管是xml文件中配置bean的注册信息进行创建还是在java类中进行创建，都可以得到bean对象
   
   > 为了测试单例模式和原型模式是否奏效，进行了单元测试，最终的结果为：
   >
   > ![image-20231107105948152](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311071100223.png)
   >
   > 其中userDao是单例模式，所以地址是一样的，userService是原型模式，即使内部的内容一致，地址也不一致，所以最终两个bean不会相等

## 总结

​		本节中实现了两个目标，第一个目标是可以创建原型模式的bean对象，实现方式就是优化了bean的注册信息，增加了一个模式来标记当前bean是单例还是原型模式，第二个目标是增加了一种从java类中创建bean对象的方式，这种方式将创建bean的方式从xml配置移动到了java类中，xml配置中只配置实现`FactoryBean`的外壳，最终拿到xml配置中的bean之后，需要增加从这个bean中拿到真正bean的过程，项目中新增的类图结构为：

![spring-10-02](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311071049794.png)

​		核心就是在继承链中新增了一个`FactoryBeanRegistrySupport`类，在其中从外壳bean中调用起`getObject`方法得到真正的bean，并把单例的bean保存到缓存中
