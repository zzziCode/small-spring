---
title: "Small_spring06"
description: "small_spring06"
keywords: "small_spring06"

date: 2023-11-02T18:35:38+08:00
lastmod: 2023-11-02T18:35:38+08:00

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
#url: "small_spring06.html"


# 开启各种图渲染，如流程图、时序图、类图等
# Enable chart render, such as: flow, sequence, classes etc
#mermaid: true
---

>🍏 small_spring06

​		在上节中，我们实现了从配置文件中读取bean的配置信息，也就是说在实例化之前增加了一个配置文件的读取和利用的模块，本节中我们继续完善spring框架，对外提供**修改**的接口，可以在bean 对象注册后但未实例化之前，对 Bean 的定义信息 `BeanDefinition` 执行修改操作。还可以在bean对象实例化之后进行修改。并且将将bean对象的配置文件加载，注册，实例化，修改等操作都融合到一个**上下文操作类**中，使其更加符合spring框架的特点。本节中涉及到的代码放在了[仓库](https://github.com/zzziCode/small-spring)中

<!--more-->

## 原因

​		在上面几节中，我们已经实现了很多的功能，包括IOC容器实现bean的实例化，创建带参的bean对象，属性填充和bean实例化分离，加载配置文件。但是到现在我们还没有提到应用上下文的概念，其实正常使用spring框架时，标准入口都是不同的“应用上下文”，所以本节中我们就将上面提到的所有功能都包装到应用上下文中，从而实现spring的进一步标准化

​		同时，为了在bean的生命周期中进行扩展，本节中还加入了对bean修改的接口，可以在bean实例化之前修改bean的注册信息，还可以在bean实例化之后修改bean对象，提供了两个标准接口`BeanFactoryPostProcessor`和`BeanPostProcessor`，实现前者的类，spring项目中就认为这是要在bean**实例化之前**修改bean的注册信息，实现后者的类，spring项目中就认为这是要在bean**实例化之后**修改bean对象，具体项目的整体结构为：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311022058634.png" alt="img" style="zoom:50%;" />

​		所以总结来看，提出这两个新知识点的目的在于让spring变得更加**标准化**，功能更加**丰富**

## 思路

> 在这里说明**整体**的设计**思路**，直接通过**debug**的方式来描述整个运行流程，从实例化之前的修改，到实例化，到实例化之后的修改

​		为了实现将整个项目包裹到应用上下文中，并且在其中bean的生命周期中加入修改的模块，需要将这两个部分分开介绍，首先是修改模块，在实例化之前的修改策略分为三步：

1. 实现`BeanFactoryPostProcessor`接口中的`postProcessBeanFactory`方法，在方法中编写自己的修改逻辑

2. 将自己的实现类以bean对象的模式加入IOC容器中

3. 在应用上下文的构造函数中调用refresh函数，refresh中调用修改的逻辑：

   > 构造函数中实现钩子函数的掉效果，外部使用应用上下文，就会**自动**调用refresh函数

   ```java
   public ClassPathXmlApplicationContext(String[] configLocations) throws BeansException {
       //得到需要加载的配置文件
       this.configLocations = configLocations;
       //放到构造函数中，
       refresh();
   }
   ```

   > refresh函数中调用修改的逻辑，主要修改的还是`BeanDefinition`中的属性列表，对`PropertyValues`进行增删改

​		为了实现在bean实例化之后还可以修改bean对象，具体的步骤分为以下几步：

1. 实现`BeanPostProcessor`接口中的`postProcessBeforeInitialization`和`postProcessAfterInitialization`方法，在方法中编写自己的修改逻辑

2. 将自己的实现类以bean对象的模式加入IOC容器中

3. 在应用上下文的构造函数中调用refresh函数，refresh中先将修改的逻辑保存到一个容器中，而不是像实例化之前修改一样直接在refresh函数中触发，这是因为此时bean还没有实例化

4. 在refresh函数中初始化所有的bean对象，按照之前介绍的流程，最终会走到createBean函数中，具体的流程如下：

   <img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311030859937.png" alt="image-20231103085923756" style="zoom:50%;" />

5. 在调用属性填充的`applyPropertyValues`方法之后，再增加一步`initializeBean`的方法调用，在这个方法中把刚才保存到容器中的所有修改策略拿出来，依次执行：

   <img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031106266.png" alt="image-20231103090406930" style="zoom: 67%;" />

> 以上两种修改策略都需要将自定义的修改策略注册为一个bean对象，也就是说BeanDefinition中保存有这些bean的注册信息，之后使用getBeansOfType方法，按照接口类型找到所有的实现类，就找到了所有的修改策略
>
> 所以为什么自定义修改策略时要实现接口，其一是为了按照类型找到自定义的修改策略，其二是为了规范修改策略的结构

​		以上就是修改策略的实现思路，将其加入到bean的生命周期中，从refresh开始，修改模块的执行过程为：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031106268.png" alt="image-20231103092155624" style="zoom:50%;" />

下面介绍应用上下文的实现思路，主要是引入了一个包，在包中每一个类实现自己的功能，beanFactory的获取，修改策略的执行或保存，bean注册信息的读取，总之就是将上一节中的项目在外面套了一个应用上下文的壳子，然后在其中加入了修改模块，整体的类结构为：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031106269.png" alt="image-20231103092541377" style="zoom:50%;" />

下面介绍项目中类的变化：

### 类的说明

#### 新增的类

1. `BeanFactoryPostProcessor`：是一个接口，实现这个接口的类被当做自定义的**实例化前**修改策略，具体的修改策略在内部的`postProcessBeanFactory`方法中实现

   ![image-20231103094547925](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107833.png)

2. `BeanPostProcessor`：是一个接口，实现这个接口的类被当做自定义的**实例化后**修改策略，具体的修改策略在内部的方法中实现

   ![image-20231103094559980](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107835.png)

> context包中新增的内容就是应用上下文的内容：

3. `ApplicationContext`：继承`ListableBeanFactory`接口，最终得到`beanFactory`中的所有重点方法

   ![image-20231103094502856](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107836.png)

4. `ConfigurableApplicationContext`：在继承`ApplicationContext`的基础上增加一个待实现的`refresh`方法，这是在这个方法中将修改模块中的内容**聚合**进来，是一个核心方法

   ![image-20231103094525733](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107837.png)

5. `AbstractApplicationContext`：继承DefaultResourceLoader，实现`ConfigurableApplicationContext`接口，继承的目的是为了得到资源加载器，便于后期从配置文件中加载资源，实现的目的是为了实现refresh方法，这个类存在的意义就是为了实现refresh方法：

   <img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107838.png" alt="image-20231103094704085" style="zoom:67%;" />

6. `AbstractRefreshableApplicationContext`：继承`AbstractApplicationContext`类，主要是为了得到beanFactory`：`

   <img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107839.png" alt="image-20231103094841394" style="zoom:67%;" />

7. `AbstractXmlApplicationContext`：继承`AbstractRefreshableApplicationContext`类，在其中实现从配置文件中那个加载bean注册信息的功能

   ![image-20231103094916106](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107840.png)

8. `ClassPathXmlApplicationContext`：继承`AbstractXmlApplicationContext`，这是最终对外暴露的接口，主要作用是接受外部传递来的配置文件的路径，之后调用上面提到的一系列方法，最终得到实例化后的bean对象

   ![image-20231103095026251](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107841.png)

​		经过新增这些类之后，整个项目的应用上下文扩展以及实例化前的修改逻辑基本完成，现在还没有完成的功能就是在实例化之后修改bean，这项功能被集成到了`createBean`方法中，下面被修改的类的第一个目的是为了适配以上这些修改，第二个目的是为了实现实例化之后修改bean对象

#### 修改的类

1. `AutowireCapableBeanFactory`：新增两个方法，用于实例化之后修改bean，具体的实现在`AutowireCapableBeanFactory`类中：

   ![image-20231103095549332](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107842.png)

2. `AbstractAutowireCapableBeanFactory`：实现了`AutowireCapableBeanFactory`接口，新增了四个方法：

   ![image-20231103095944495](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107843.png)

   上面几个方法的调用逻辑是在`createBean`方法中调用`initializeBean`，然后在`initializeBean`方法中调用其他方法：

   ```java
   private Object initializeBean(String beanName, Object bean, BeanDefinition beanDefinition) {
       // 1. 执行 BeanPostProcessor Before 处理
       Object wrappedBean = applyBeanPostProcessorsBeforeInitialization(bean, beanName);
   
       // 待完成内容：invokeInitMethods(beanName, wrappedBean, beanDefinition);
       invokeInitMethods(beanName, wrappedBean, beanDefinition);
   
       // 2. 执行 BeanPostProcessor After 处理
       wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
       return wrappedBean;
   }
   ```

3. `ConfigurableBeanFactory`：新增一个方法，目的是保存实例化后的修改策略，主要是在`refresh`方法中的`registerBeanPostProcessors`方法中调用，目的是为了先保存实例化后的修改逻辑，在实例化之后执行修改：

   ```java
   private void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
       //按照BeanPostProcessor这个类型找到所有的bean
       Map<String, BeanPostProcessor> beanPostProcessorMap = beanFactory.getBeansOfType(BeanPostProcessor.class);
       for (BeanPostProcessor beanPostProcessor : beanPostProcessorMap.values()) {
           //保存所有的实例化后修改策略
           beanFactory.addBeanPostProcessor(beanPostProcessor);
       }
   }
   ```

4. `AbstractBeanFactory`：实现`ConfigurableBeanFactory`接口，实现其中的`addBeanPostProcessor`方法，将实例化后的修改策略保存到一个**容器**中：

   <img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107844.png" alt="image-20231103100638559" style="zoom:67%;" />

5. `ConfigurableListableBeanFactory`：在这个接口中增加一个方法，在`DefaultListableBeanFactory`类中实现：

   ![image-20231103101027159](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107845.png)

6. `DefaultListableBeanFactory`：之前项目的核心类，在这个类中新增一个实例化所有bean对象的方法，其实就是依次获取一遍所有的bean，利用`getBean`方法的不存在就实例化的特点将所有的bean对象实例化：

   ```java
   @Override
   public void preInstantiateSingletons() throws BeansException {
       //遍历所有bean的注册信息，然后使用getBean的有就获取，没有就创建的思想来实例化所有没实例化的bean对象
       //佯装遍历，实际上是为了实例化所有bean
       beanDefinitionMap.keySet().forEach(this::getBean);
   }
   ```

​		经过以上的新增和修改，就可以实现实例化后修改bean对象，并且在修改策略执行完毕之后，将所有的bean对象初始化，以上功能全部封装到了应用上下文中，整体的调用逻辑=如下图所示：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107847.png" alt="spring-7-03" style="zoom:67%;" />

### bean的创建和获取

​		经过上面的分析，项目的整体结构变得更加清晰，相比于之前的项目，程序的运行过程变得更加复杂，从应用上下文为入口，传递一个配置文件，在内部执行refresh方法，获取到配置文件中的注册信息之后，执行实例化前的修改操作，并将所有的实例化后的修改操作保存到容器中，之后直接实例化，在实例化之后，执行实例化后的修改逻辑，也就是`createBean`中新增的逻辑，下面将以debug的形式来介绍bean的创建和获取的整体过程：

1. 初始化应用上下文，在这里传递一个配置文件的路径：

   ![image-20231103102021831](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107848.png)

2. 执行构造函数，在其中调用`refresh`方法：

   ![image-20231103102148629](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107849.png)

   > refresh方法的代码为：
   >
   > ```java
   > @Override
   > public void refresh() throws BeansException {
   >     // 1. 创建 BeanFactory，并加载 BeanDefinition
   >     //也就是加载配置文件中的内容，得到所有的bean对象，将其保存到BeanDefinition中
   >     refreshBeanFactory();
   > 
   >     // 2. 获取 BeanFactory
   >     ConfigurableListableBeanFactory beanFactory = getBeanFactory();
   > 
   >        // 3. 在 Bean 实例化之前，执行 BeanFactoryPostProcessor (Invoke factory processors registered as beans in the context.)
   >     //修改bean的属性列表，相当于在实例化之前修改bean的注册信息
   >     invokeBeanFactoryPostProcessors(beanFactory);
   > 
   >     // 4. BeanPostProcessor 需要提前于其他 Bean 对象实例化之前执行注册操作
   >     //将实例化之后的修改策略保存住
   >     registerBeanPostProcessors(beanFactory);
   > 
   >     // 5. 提前实例化单例Bean对象
   >     //实例化所有的对象之后，就可以实现
   >     beanFactory.preInstantiateSingletons();
   > }
   > ```
   
3. refresh函数中调用`refreshBeanFactory`方法，目的是为了获取到beanFactory对象，为了后期bean的注册和实例化做准备：

   ![image-20231103102429425](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107850.png)

4. 加载配置文件，直接利用上一节中创建的配置文件加载方法来加载给定的配置文件：

   ![image-20231103102607842](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107851.png)

5. 获取创建好的`beanFactory`对象，里面已经有加载好的bean注册信息，所有的bean都还没有被初始化：

   ![image-20231103102805330](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107852.png)

6. 执行**实例化前**的修改逻辑，主要是修改`BeanDefinition`中的`PropertyValues`，通过类型获取到自定义的修改逻辑之后，直接执行修改逻辑:

   ![image-20231103103102155](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107853.png)

   > 自定义的修改逻辑为：
   >
   > ```java
   > public class MyBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
   > 
   >     @Override
   >     public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
   > 
   >         BeanDefinition beanDefinition = beanFactory.getBeanDefinition("userService");
   >         PropertyValues propertyValues = beanDefinition.getPropertyValues();
   >         //增加一个字段
   >         propertyValues.addPropertyValue(new PropertyValue("company", "改为：字节跳动"));
   >     }
   > 
   > }
   > ```

   这里的实例化前修改是直接添加了一个属性字段，相当于现在有两个重复的`company`，但是新添加的最新的属性字段在后面，属性填充时会覆盖旧的，所以展示出来的就是最新的属性

   <img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031357627.png" alt="image-20231103135629012" style="zoom:67%;" />

7. 保存**实例化后**的修改逻辑，还是按照类型获取到修改逻辑，注意此时获取到之后只是简单的保存：

   ![image-20231103103449774](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107854.png)

8. 保存的具体逻辑，也就是`addBeanPostProcessor`，将其保存到一个容器中：

   ![image-20231103103633911](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107855.png)

   > 执行完实例化前修改的逻辑以及保存完实例化后修改的逻辑之后，这两个自定义的修改策略已经被保存到了IOC容器中，因为他们内部都调用了`getBeansOfType`方法，内部调用了`getBean`方法，使得这两个bean被提前实例化

9. 实例化所有的bean对象，利用`getBean`方法来遍历所有已注册的bean，将其实例化，最终会调用到`createBean`方法中：

   ![image-20231103103755491](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107856.png)

   到达`createBean`方法中之后，创建一个空bean之后，就是属性填充，此时就会用到实例化之前修改的bean注册信息

   ![image-20231103104937474](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107857.png)

10. 实例化之后的修改，在`initializeBean`方法中执行，在这个方法中调用修改逻辑：

    ![image-20231103105052632](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107858.png)

11. 修改逻辑的调用，从容器中拿到所有待执行的修改逻辑，然后依次执行，就是实现了对bean对象的修改：

    ![image-20231103105157437](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107859.png)

    >`postProcessBeforeInitialization`方法内部的逻辑为：
    >
    >```java
    >@Override
    >    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    >        if ("userService".equals(beanName)) {
    >            UserService userService = (UserService) bean;
    >            userService.setLocation("改为：北京");
    >        }
    >        return bean;
    >    }
    >```

12. 将修改之后的实例化对象保存到IOC容器中并返回，此时bean对象的信息修改为：

    ![image-20231103105553531](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107860.png)

​		经历以上这些步骤，会将待修改的bean进行修改，不修改的bean也进行实例化，此时IOC容器中保存了所有**实例化后**的bean对象，并且修改的操作也执行完成，后期想要使用这些bean对象时，直接`getBean`即可：

![image-20231103105808622](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107861.png)

## 总结

​		本节在之前的基础上实现了应用上下文，并且增加了修改模块，实现实例化前后都可以修改bean，为了实现应用上下文，将上一节中的`DefaultListableBeanFactory`包装到了`context`包的类中，对外暴露的不再是这个类，而是`ClassPathXmlApplicationContext`类，用户只需要提供一个配置文件，之后就可以尽情使用bean对象了

​		为了实现修改模块，项目将其在`refresh`函数中进行聚合，实例化前的修改主要修改的是注册信息，也及时`PropertyValues`中的内容，实例化后的修改主要是利用`set`方法来修改bean对象中的内容，在`refresh`函数中，实例化前的修改操作直接执行，实例化后的操作**先保存**到一个容器中，在`createBean`函数的空bean创建，属性填充（利用实例化前已经修改过的属性）之后再加入一步，将实例化后的bean进行修改

​		以上两个模块的结构图如下图所示，核心的地方用红框标注，`refresh`中聚合所有的修改模块并在其中执行实例化前的修改，`createBean`中新增一个`initializeBean`方法，执行实例化之后的修改，最后将`ClassPathXmlApplicationContext`暴露给用户使用：

![spring-7-03](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107847.png)
