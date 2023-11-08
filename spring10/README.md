---
title: "Small_spring10"
description: "small_spring10"
keywords: "small_spring10"

date: 2023-11-07T14:34:18+08:00
lastmod: 2023-11-07T14:34:18+08:00

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
#url: "small_spring10.html"


# 开启各种图渲染，如流程图、时序图、类图等
# Enable chart render, such as: flow, sequence, classes etc
#mermaid: true
---

>🍆 small_spring10

​		在上一节中，我们实现了多种类型多种方式创建bean对象，本节中在此基础上，添加了事件功能，也就是当某些事件发生时，程序能够做出一些反应，例如用户注册完成时，系统会给当前注册用户发送一些新人优惠福利。本节中涉及到的代码我放到了[仓库](https://github.com/zzziCode/small-spring)中

<!--more-->

## 原因

​		在之前的基础上，我们实现了关于IOC的很多功能，包括bean的定义，注册，获取，属性填充，加载配置文件，应用上下文，修改，初始化和销毁，Aware注入容器资源，以不同的方式创建不同种的bean对象等。但是现在还缺一个核心功能：**事件**。

​		当某些事件发生时，系统希望做出一些反映，例如当应用上下文刷新完成时，系统希望打印出刷新的时间，某些事件发生时，用户希望做出一些自定义的反映。这些都需要spring中的**事件机制**来解决，而一般的事件解决分为三个步骤：

1. 事件类应该继承 `ApplicationEvent`
2. 事件的发布者应该注入`ApplicationEventPublisher`
3. 事件监听者应该实现`ApplicationListener`

​		本节中为了实现事件机制，将其分成了几步：

1. 定义事件时继承`ApplicationContextEvent`类，其终极父类是`EventObject`
2. 事件监听器实现了`ApplicationListener`接口，其终极父类是`EventListener`
3. 事件发布者最终都是调用`ApplicationEventPublisher`接口中的`publishEvent`发布事件，为了能够调用`publishEvent`方法，需要实现这个接口，相当于注入
4. 为了事件发布之后**对应**的监听器能够被触发，所有的监听器被保存到了一个**广播器**的容器中，并且事件发布时调用的`publishEvent`方法内部调用的是广播器的`multicastEvent`方法，内部根据事件类型匹配到保存的监听器，从而进行触发，这个广播器可以说衔接了事件的发布和监听器的触发，是一个**核心组件**

​		经历以上几步，现有的项目中就加入了事件机制，新的项目结构为：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311080921718.png" alt="img" style="zoom:67%;" />

## 思路

> 为了实现上面提到的事件机制，根据上面描述的步骤来进一步细化设计思路

​		首先就是事件的定义，每一个事件的定义都需要**继承**`ApplicationContextEvent`类，之后这个事件发生后对应的监听器就可以根据事件进行进一步的处理，而事件监听器也需要**实现**一个接口`ApplicationListener`，并指定对应的泛型，代表当前监听器监听什么类型的事件，针对事件做出的操作定义在了统一的`onApplicationEvent`方法中

​		有了事件和监听器之后，最重要的就是事件发生时可以触发对应的监听器，为了做到这一点，项目中增加了一个**广播器**，将项目中所有的监听器保存到一个容器中，为了得到所有的监听器，首先需要将监听器注册到xml配置文件中，使其成为bean。由于所有的监听器都实现了`ApplicationListener`接口，所以可以按照类型从容器中拿到所有的监听器

​		当有事件发布时，会调用事件发布者中的`publishEvent`方法，内部调用广播器中的`multicastEvent`方法，这是核心。此方法根据传递来的事件类型从自己保存的所有监听器中找到匹配的监听器，从而执行其`onApplicationEvent`方法

​		在事件和对应的监听器定义好后，事件发布并触发对应监听器的流程如下：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311081051506.png" alt="image-20231108105055765" style="zoom:67%;" />

​		经历上面的描述，就实现了事件机制，核心就是事件的定义，发布，监听都需要**符合统一规定**，有固定的格式，然后发布时调用广播器的内置方法衔接事件和匹配的监听器

### 类的变化

> 为了实现事件机制，项目在原有的基础上改变了结构，下面详细介绍

#### 新增的类

1. `ApplicationEvent`：实现了`EventObject`类，是一个过渡类，会被`ApplicationContextEvent`继承

2. `ApplicationContextEvent`：所有的事件都需要**继承**这一个类，代表他们是事件类，内部没有特殊的方法

3. `ApplicationEventPublisher`：事件的发布者接口，内部定义了一个**事件发布**的方法`publishEvent`，事件的发布都统一经过这一个方法的处理，最终这个接口被`ApplicationContext`继承，在`AbstractApplicationContext`中实现`publishEvent`方法，从而在事件发布时可以直接调用这个方法，在继承图中可以很清晰的看清楚：

   <img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311081051509.png" alt="image-20231108094021709" style="zoom:80%;" />

4. `ApplicationListener`：事件的监听器需要**实现**的接口，主要是实现内部的`onApplicationEvent`方法以及指定泛型，在方法内部指定事件发生时应该做什么，指定泛型代表这个监听器监听什么类型的事件

   ![image-20231108094150544](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311081051510.png)

5. `ApplicationEventMulticaster`：**广播器**的顶级接口，加入广播器的目的是为了保存事件与监听器之间的关系，实现事件和监听器之间互不干扰，内部定义了三个带实现的方法，实现监听器的新增和删除以及事件发生时触发对应监听器的`multicastEvent`，这是一个核心方法，类的结构如下:

   ![image-20231108094456142](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311081051511.png)

6. `AbstractApplicationEventMulticaster`：广播器的**抽象类**，符合抽象类中进行初始化等准备工作，业务类中只关心业务逻辑的设计思想，在这里实现监听器的新增和删除，并且新增两个方法，用来从所有的监听器中按照事件类型筛选出匹配的监听器，用来辅助`multicastEvent`方法，类的结构如下：

   ![image-20231108094724507](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311081051512.png)

   其中有两个核心方法：

   1. `getApplicationListeners`：按照事件的类型来找到对应的监听器，将保存的每一个监听器与当前的事件进行匹配

      ```java
      protected Collection<ApplicationListener> getApplicationListeners(ApplicationEvent event) {
          LinkedList<ApplicationListener> allListeners = new LinkedList<ApplicationListener>();
          for (ApplicationListener<ApplicationEvent> listener : applicationListeners) {
              //符合要求的监听器会被保存最后返回
              if (supportsEvent(listener, event))
                  allListeners.add(listener);
          }
          return allListeners;
      }
      ```

   2. `supportsEvent`：判定监听器与事件是否匹配的方法，辅助`getApplicationListeners`方法，内部使用`isAssignableFrom`来判断是否匹配：

      ```java
      protected boolean supportsEvent(ApplicationListener<ApplicationEvent> applicationListener, ApplicationEvent event) {
          Class<? extends ApplicationListener> listenerClass = applicationListener.getClass();
          // 按照 CglibSubclassingInstantiationStrategy、SimpleInstantiationStrategy 不同的实例化类型，需要判断后获取目标 class
          Class<?> targetClass = ClassUtils.isCglibProxyClass(listenerClass) ? listenerClass.getSuperclass() : listenerClass;
          Type genericInterface = targetClass.getGenericInterfaces()[0];
      
          Type actualTypeArgument = ((ParameterizedType) genericInterface).getActualTypeArguments()[0];
          String className = actualTypeArgument.getTypeName();
          Class<?> eventClassName;
          try {
              eventClassName = Class.forName(className);
          } catch (ClassNotFoundException e) {
              throw new BeansException("wrong event class name: " + className);
          }
          // 判定此 eventClassName 对象所表示的类或接口与指定的 event.getClass() 参数所表示的类或接口是否相同，或是否是其超类或超接口。
          // isAssignableFrom是用来判断子类和父类的关系的，或者接口的实现类和接口的关系的，默认所有的类的终极父类都是Object。
          // 如果A.isAssignableFrom(B)结果是true，证明B可以转换成为A,也就是A可以由B转换而来。
          return eventClassName.isAssignableFrom(event.getClass());
      }
      ```

7. `SimpleApplicationEventMulticaster`：广播器的**业务类**，也是最后在事件发布时要使用的类，内部实现了核心方法`multicastEvent`，先拿到所有匹配的监听器，然后一一执行他们的`onApplicationEvent`方法，达到触发监听器的目的：

   ![image-20231108094912790](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311081051513.png)

#### 修改的类

1. `ApplicationContext`：为了加入事件发布的功能，继承了`ApplicationEventPublisher`接口，并在`AbstractApplicationContext`中**实现**了他的`publishEvent`方法：

   ![image-20231108095118335](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311081051514.png)

2. `AbstractApplicationContext`：修改最多的类，将事件机制集成到了这个类中，做了一下几点修改：

   - 定义一个广播器，用来后期保存创建的简单广播器对象：

     ```java
     private ApplicationEventMulticaster applicationEventMulticaster;
     ```

   - 定义`initApplicationEventMulticaster`方法，内部初始化了一个简单广播器对象，并将其保存到bean容器中，保存的目的是为了后期创建其

     ```java
     private void initApplicationEventMulticaster() {
         ConfigurableListableBeanFactory beanFactory = getBeanFactory();
         applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
         beanFactory.registerSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, applicationEventMulticaster);
     }
     ```

   - 定义`registerListeners`方法，内部从bean容器中按照类型拿到所有的监听器，并将所有的监听器保存到上面创建的简单广播器中

     ```java
     private void registerListeners() {
         Collection<ApplicationListener> applicationListeners = getBeansOfType(ApplicationListener.class).values();
         for (ApplicationListener listener : applicationListeners) {
             applicationEventMulticaster.addApplicationListener(listener);
         }
     }
     ```

   - 实现`publishEvent`方法，提供事件发布功能，内部调用简单广播器中的功能，从而可以将事件和对应的监听器之间联系起来

     ```java
     @Override
     public void publishEvent(ApplicationEvent event) {
         applicationEventMulticaster.multicastEvent(event);
     }
     ```

   - refresh方法中新增几个步骤：

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
     
         // 6. 初始化事件发布者，初始化了一个事件广播器，这里面管理事件所有的事件监听器
         initApplicationEventMulticaster();
     
         // 7. 注册事件监听器，保存所有的监听器到一个容器中
         registerListeners();
     
         // 8. 提前实例化单例Bean对象
         beanFactory.preInstantiateSingletons();
     
         // 9. 发布容器刷新完成事件
         finishRefresh();
     }
     ```

     主要是新增`6,7,9`三步

3. `ClassUtils`：工具类中添加了两个方法，用来辅助事件和其监听器之间的匹配：

   ![image-20231108133917525](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311081339604.png)

​		经历上面的新增和修改之后，项目中就有了事件处理的能力，一旦事件发生，就可以调用事件发布者的功能进行事件发布，从而匹配到对应的事件监听器进行处理，本项目中定义了三种事件：

1. 容器刷新完成时事件：容器刷新完成时发布此事件：

   ```java
   // 9. 发布容器刷新完成事件
   finishRefresh();
   //具体发布逻辑代码
   private void finishRefresh() {
       publishEvent(new ContextRefreshedEvent(this));
   }
   ```

   > 此步在`refresh`方法中，所有的操作做完了，认为容器刷新完成，此时**发布**事件

2. 容器关闭时事件：容器即将关闭时发布此事件，也就是虚拟机即将退出，销毁工作即将触发之前发布此事件，说明此事件的发布在钩子函数中的`close`函数中：

   ```java
   @Override
   public void close() {
       // 发布容器关闭事件，在销毁之前触发销毁事件的监听器
       publishEvent(new ContextClosedEvent(this));
   
       // 执行销毁单例bean的销毁方法
       getBeanFactory().destroySingletons();
   }
   ```

3. 用户自定义事件：此事件的发布由用户控制发布时机，本项目中在单元测试中发布：

   ```java
   @Test
   public void testEvent() {
       ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring.xml");
       //发布自定义事件
       applicationContext.publishEvent(new CustomEvent(applicationContext, 1019129009086763L, "成功了！"));
   
       //关闭时，关闭事件会被发布，此时对应的监听器会被触发
       applicationContext.registerShutdownHook();
   }
   ```

​		总结来看，事件机制的使用只要遵循了统一的规定，在想要发布的地方发布自己的事件即可根据发布事件中的`publishEvent`方法调用广播器中的`multicastEvent`方法从而触发对应的监听器，前提是事件和监听器都实现了统一的类或者接口

### bean的创建和获取

​		根据上面的分析，按照debug的流程分析三个事件的触发顺序，三个事件的监听器已经提前注册到了xml文件中，只等对应的事件发生时触发对应的监听器，下面介绍具体流程：

1. 初始化应用上下文，读取配置文件，在refresh方法中执行创建和获取`beanFactory`，执行初始化操作，保存销毁操作，此时得到了xml文件中配置的三个事件监听器的注册信息，下面就是本项目中比较重要的步骤了：

   ![image-20231108101951308](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311081051515.png)

2. 创建事件广播器`initApplicationEventMulticaster`，内部即将保存所有的事件监听器，这是`refresh`方法中的**第六步**：

   ![image-20231108142150908](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311081421983.png)

3. 注册事件监听器，将事件监听器保存到广播器中，这是refresh方法中的**第七步**：

   ![image-20231108142302033](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311081423273.png)

4. 实例化xml文件中的bean对象，本节中为了演示事件机制，没有注册任何多余的bean对象，但是功能结构仍然在，只需要按照前几节的介绍写好xml配置文件即可

5. `refresh`执行完毕，容器刷新完成，此时发布第一个事件，发布事件时经历以下几步：

   1. 调用实现的`publishEvent`方法，将当前容器刷新完成事件传递进去：

      ![image-20231108102638341](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311081051518.png)

   2. 事件发布方法内部调用广播器中的`multicastEvent`方法，广播器是第二步初始化的：

      ![image-20231108142543104](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311081425143.png)

   3. `multicastEvent`方法内部调用广播器内部的`getApplicationListeners`方法，根据传递来的事件找到所有匹配的监听器：

      ![image-20231108102945747](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311081051520.png)

   4. 在`getApplicationListeners`方法内部遍历保存的所有监听器，然后依次调用`supportsEvent`方法来判断事件和监听器之间的匹配关系，遍历完成将所有匹配的监听器保存到一个容器中返回：

      ![image-20231108142823745](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311081428927.png)

   5. `multicastEvent`方法根据找到的所有监听器，一一执行他们的`onApplicationEvent`方法，达到事件出现就触发对应的事件监听器的目的：

      ![image-20231108142915686](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311081429103.png)

   > 剩下两个事件的发布步骤与上面描述的一样，只是事件发布的时机不一样，最终三个事件都执行完毕后，输出的结果如下，可以发现三个事件的监听器都被触发：
   >
   > ![image-20231108143004280](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311081430430.png)

## 总结

​		经过上面的分析，事件机制是如何加入到现有项目中的步骤已经很清楚了，就是在`refresh`方法中进行了集成，使得项目有了事件处理的能力，然后定义自己的监听器和事件，在合适的时机发布事件就可以自动完成触发，前提监听器和事件都需要实现统一的类或者接口，并且监听器需要配置到xml文件中变成bean对象，事件发布需要经过统一的接口，所有的操作基本上都有规范，几个核心类的继承图如下：

![spring-11-02 (1)](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311081439935.png)
