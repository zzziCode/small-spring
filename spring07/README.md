---
title: "Small_spring07"
description: "small_spring07"
keywords: "small_spring07"

date: 2023-11-03T18:42:11+08:00
lastmod: 2023-11-03T18:42:11+08:00

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
#url: "small_spring07.html"


# 开启各种图渲染，如流程图、时序图、类图等
# Enable chart render, such as: flow, sequence, classes etc
#mermaid: true
---

>🥭 small_spring07

​		在上一节中，我们实现了应用上下文，并且在应用上下文中加入了修改模块，主要是在bean的生命周期的实例化之前和之后分别加入修改逻辑，本节中继续在这个项目的基础上增加一个模块：**初始化和销毁模块**，分别用于初始化bean内部所需要的一些信息，以及在bean使用完毕之后，销毁bean的实例化信息，使得项目向着标准的spring框架更进一步，本节中涉及到的代码放到了[仓库](https://github.com/zzziCode/small-spring)中

<!--more-->

## 原因

​		在之前的章节中，`UserDao`类中有一个`hashMap`容器，但是初始化的操作一直放在了`static`代码块中，这种方式虽然可行，但是spring框架中有更好的解决办法。我们可以将这一部分操作放到spring的初始化操作中，执行时机是在bean的实例化的时候，属性填充完毕之后，就可以执行`static`代码块中的操作

```java
public class UserDao {
    private static Map<String, String> hashMap = new HashMap<>();
    static {
        hashMap.put("1", "张三");
        hashMap.put("2", "李四");
        hashMap.put("3", "王五");
    }
    public String queryUserName(String uId) {
        return hashMap.get(uId);
    }
}
```

​		同时，在bean容器使用完毕之后，没有任何销毁操作，如果bean中用到了一些必须释放的资源，在之前的设计中，是无法释放的。所以销毁方法就在这里提出来用来解决这些问题，销毁方法的执行时机是在虚拟机关闭时，调用一个钩子函数，来执行所有的销毁收尾工作。

​		对于初始化和销毁操作来说，有**两种**实现方式：

1. 在项目中定义接口，然后外部实现这个接口，自定义初始化和销毁方法。内部调用**统一命名**的方法，就可以调用到自定义的初始化和销毁方法，例如在上一节中的实例化前的修改逻辑：

   ![image-20231103204052886](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108214.png)

2. 在bean中定义初始化和销毁操作，然后将初始化和销毁方法的名称**配置到配置文件**中，之后再读取配置文件时，将这两个方法名注册到`BeanDefinition`中，后期到了初始化和销毁方法的执行实际时，只要判断是否存在这两个方法名，然后使用**反射**调用即可。这种方式与bean的配置很相似，只要配置了一个bean，读取配置文件时就能读取到

​		通过加入以上两个模块，项目的整体结构变为：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311032043101.png" alt="img" style="zoom: 50%;" />

## 思路

​		为了增加初始化和销毁模块，本节中提供了两种方法，分别是实现接口和xml配置，针对两种不同的方法，加入的方式也不同。下面分别描述两种方法是如何加入到原有项目中的：

1. 实现接口：

   项目中提供了两个接口`InitializingBean` 和`DisposableBean` ，在接口中分别有一个待实现的方法，只要bean实现这个接口，就是这个接口的实现类，从而调用`bean instanceof InitializingBean`或者`bean instanceof DisposableBean`时就会为`true`，此时就可以直接用bean直接调用相应方法来调用对应的初始化或者销毁方法

   ![image-20231104090821155](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108220.png)

2. xml配置：

   直接在bean的类中自定义初始化方法和销毁方法的方法名和方法体，然后在配置文件中增加两个配置，分别是`init-method`和`destroy-method`，在其中指定方法名。

   ```xml
   <bean id="userDao" class="com.zzzi.springframework.test.bean.UserDao" init-method="initDataMethod" destroy-method="destroyDataMethod"/>
   ```

   只要在这里配置了，那么在加载bean的注册信息时，就一定会读取到其中配置的方法名，然后在`BeanDefinition`中增加了两个字段，用于保存读取到的方法名

   <img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108221.png" alt="image-20231104091152225" style="zoom: 67%;" />

   之后在初始化和销毁方法的调用时机出现时，就利用得到的方法名，直接通过**反射**来触发对应方法的执行

   ![image-20231104091345835](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108222.png)

​		上面说到，初始化和销毁有自己的调用时机，对于初始化来说，在本项目中的调用时机是在bean**实例化之后**并且在**修改操作执行当中**，也就是上节中介绍的`initializeBean`，其中的`invokeInitMethods`中就是初始化逻辑的执行，在这里可以初始化一些系统资源，比如数据库中数据的读取，部分参数初始化，具体的执行时机如图所示：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108223.png" alt="image-20231104093119912" style="zoom: 67%;" />

​		对于销毁操作而言，一般用来进行资源的销毁，流的释放等操作，这些操作一般需要在程序退出之前进行操作，所以本项目中注册了一个钩子函数，在虚拟机退出之前执行这个钩子函数，钩子函数内部调用了销毁模块，也就是说销毁模块的调用时机是在**虚拟机退出之前**调用的，所以销毁模块的方法需要先读取并**保存**，在合适的时机在进行调用，这与实例化后的修改操作类似，现将执行策略保存到容器中，后期执行直接调用容器中保存的策略。保存到时机如下图所示：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108224.png" alt="image-20231104094630103" style="zoom:67%;" />

​		可以看出，销毁逻辑保存的时机是在`createBean`方法调用快结束的时候，将这些销毁逻辑保存到一个容器中，其中使用了适配器封装销毁逻辑，因为要将销毁逻辑保存到容器中，容器中存储的元素需要是**同种类型**，销毁逻辑的实现方法又有两种不同类型，所以用适配器封装，之后在虚拟机退出之前调用容器中保存的销毁逻辑

​		通过以上的分析，初始化模块的调用是直接在实例化修改的过程中，所以不需要保存的容器，调用的代码不用任何封装：

```java
private void invokeInitMethods(String beanName, Object bean, BeanDefinition beanDefinition) throws Exception {
    // 1. 实现接口 InitializingBean
    if (bean instanceof InitializingBean) {
        ((InitializingBean) bean).afterPropertiesSet();
    }
    // 2. 注解配置 init-method {判断是为了避免二次执行初始化}
    String initMethodName = beanDefinition.getInitMethodName();
    if (StrUtil.isNotEmpty(initMethodName) && !(bean instanceof InitializingBean)) {
        Method initMethod = beanDefinition.getBeanClass().getMethod(initMethodName);
        if (null == initMethod) {
            throw new BeansException("Could not find an init method named '" + initMethodName + "' on bean with name '" + beanName + "'");
        }
        //利用反射执行配置文件中配置的初始化方法
        initMethod.invoke(bean);
    }
}
```

​		对于销毁操作来说，由于不是立马调用，所以需要经过封装的操作，下面详细介绍为了封装销毁模块以及实现初始化模块，项目中类的变化

### 类的说明

#### 新增的类

1. `DisposableBean`：是一个接口，实现了这个接口的类需要实现其中的`destroy`方法，在里面定义销毁的执行逻辑，增加这个类的目的是为了让bean实现这个接口从而定义销毁逻辑或者让适配器继承这个接口，之后实现统一的`destroy`方法。类的结构如下：

   ![image-20231104095408730](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108226.png)

2. `DisposableBeanAdapter`：上面提到的适配器，为了将不同种类型的销毁方法进行统一封装，最终保存到一个容器中，并且在里面实现了destory方法，钩子函数中最终调用的也是这个方法，实现了销毁的逻辑，增加这个类的目的是为了统一封装不同的销毁对象类型，并且在其中定义统一的销毁逻辑。类的结构如下：

   ![image-20231104095646540](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108227.png)

   其中如果销毁逻辑是通过实现接口实现的，那么bean属性内部就自带了`destroy`方法，直接调用即可。如果是xml配置文件，那么就可以通过`BeanDefinition`中的`getDestroyMethodName`方法拿到配置文件中的方法名，然后通过反射调用。整体的执行逻辑如下：

   ```java
   @Override
   public void destroy() throws Exception {
       /**@author zzzi
            * @date 2023/11/3 19:19
            * 在这里实现两种destroy的调用
            */
       // 1. 实现接口 DisposableBean
       if (bean instanceof DisposableBean) {
           ((DisposableBean) bean).destroy();
       }
   
       // 2. 注解配置 destroy-method {判断是为了避免二次执行销毁}
       if (StrUtil.isNotEmpty(destroyMethodName) && !(bean instanceof DisposableBean && "destroy".equals(this.destroyMethodName))) {
           Method destroyMethod = bean.getClass().getMethod(destroyMethodName);
           if (null == destroyMethod) {
               throw new BeansException("Couldn't find a destroy method named '" + destroyMethodName + "' on bean with name '" + beanName + "'");
           }
           destroyMethod.invoke(bean);
       }
   
   }
   ```

3. `InitializingBean`：是一个接口，实现了这个接口的类需要实现其中的`afterPropertiesSet`方法，在里面定义初始化的执行逻辑，增加这个接口的目的只是为了让bean去实现，从而自定义初始化逻辑。类的结构如下：

   ![image-20231104095956999](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108228.png)

#### 修改的类

1. `BeanDefinition`：在其中新增了两个属性并提供了对应的`setter`和`getter`方法，当初始化和销毁逻辑是通过xml配置时，在读取配置文件的时候，就将配置的方法名保存到这两个属性中，修改这个类的目的是为了保存xml配置文件中的初始化和销毁方法名，便于后期反射时使用。类的结构为：

   ![image-20231104100253088](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108229.png)

2. `SingletonBeanRegistry`：新增一个待实现的方法`destroySingletons`，在内部拿到保存好的销毁逻辑，调用每一个销毁逻辑的`destroy`方法，这个方法存在的逻辑是为了进一步封装，destory方法中只需要关心每一个销毁逻辑的业务执行，对于容器中所有的销毁逻辑调用，交给`destroySingletons`方法执行。`destroy`方法的具体实现在`DisposableBeanAdapter`类的说明中提到了，修改这个类的目的是为了提供遍历保存销毁逻辑的容器，针对每一个销毁逻辑都调用`destroy`方法的**框架**。类的结构为，：

   ![image-20231104100611121](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108230.png)

3. `DefaultSingletonBeanRegistry`：实现了`SingletonBeanRegistry`中新增的方法`destroySingletons`，并且新增了一个方法`registerDisposableBean`和一个容器`disposableBeans`，容器中保存了所有的销毁逻辑。在`destroySingletons`遍历这个容器中所有已保存的销毁逻辑，调用他的destroy方法来触发销毁逻辑的执行。而`registerDisposableBean`方法在`AbstractAutowireCapableBeanFactory`类中的`registerDisposableBeanIfNecessary`方法中调用，修改这个类的目的是为了将销毁逻辑保存到容器中，并且遍历这个容器，调用每个销毁逻辑的`destroy`方法，新的类结构为，：

   ![image-20231104101606499](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108231.png)

4. `AbstractAutowireCapableBeanFactory`：这个类中新增了一个方法`registerDisposableBeanIfNecessary(String beanName, Object bean, BeanDefinition beanDefinition)`，实现了一个空方法i`nvokeInitMethods(String beanName, Object bean, BeanDefinition beanDefinition)`，新增的方法是为了保存销毁的逻辑到容器中，在保存的过程中使用适配器来统一封装，实现空方法是为了执行初始化的逻辑，二者都是在`createBean`方法中调用的，修改这个类的目的是为了**执行**初始化逻辑，**保存**销毁逻辑，这是本项目中的核心类。类的结构变为：

   ![image-20231104101109785](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108232.png)

​		通过以上新增的类和修改的类，就会将初始化和销毁模块加入项目中，需要注意的是统一执行初始化逻辑的函数名为`invokeInitMethods`，内部调用`afterPropertiesSet`方法或者配置文件中的方法来触发执行，而统一执行销毁逻辑的方法就叫做`destroy`，外部被一个`destroySingletons`方法**包裹**，内部针对每一个销毁逻辑调用`destroy`方法

### bean的创建和获取

​		经过上面的分析，已经清楚了初始化逻辑和销毁逻辑是如何加入到项目中的，下面使用`debug`的方式来说明bean的创建和获取的过程中，初始化和销毁逻辑如何执行，项目中用到的两个bean对象中，`UserDao`使用配置文件的方式加入初始化和销毁逻辑，`UserService`使用实现接口的方式加入初始化和销毁逻辑

1. 初始化应用上下文，传递一个配置文件的路径：

   ![image-20231104102941451](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108233.png)

2. 执行构造函数，在其中调用`refresh`方法：

   ![image-20231103102148629](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107849.png)

   > refresh方法的代码为：
   >
   > ```java
   > @Override
   > public void refresh() throws BeansException {
   >  // 1. 创建 BeanFactory，并加载 BeanDefinition
   >  //也就是加载配置文件中的内容，得到所有的bean对象，将其保存到BeanDefinition中
   >  refreshBeanFactory();
   > 
   >  // 2. 获取 BeanFactory
   >  ConfigurableListableBeanFactory beanFactory = getBeanFactory();
   > 
   >     // 3. 在 Bean 实例化之前，执行 BeanFactoryPostProcessor (Invoke factory processors registered as beans in the context.)
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

3. `refresh`函数中调用`refreshBeanFactory`方法，目的是为了获取到`beanFactory`对象，为了后期bean的注册和实例化做准备：

   ![image-20231103102429425](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107850.png)

4. 加载配置文件，直接利用之前创建的配置文件加载方法来加载给定的配置文件：

   ![image-20231103102607842](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311031107851.png)

5. 读取配置文件中的每一个标签，尝试加载配置的**初始化和销毁方法名**：

   ![image-20231104103636893](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108234.png)

   由于`UserDao`中配置了初始化方法和销毁方法名，所以此时读取到了配置，而`UserService`不是xml配置实现初始化和销毁，所以读取不到，显示出来的效果中，userService中的两个属性为空

6. 执行实例化前的修改，保存实例化后的修改，这不是本项目中的重点：

   ![image-20231104103738869](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108235.png)

7. 实例化所有的bean对象，最终到达`createBean`方法中，执行空bean的创建和bean属性填充：

   ![image-20231104103936186](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108236.png)

8. 执行实例化后的修改和初始化逻辑，具体的代码为，初始化的逻辑在`invokeInitMethods`中执行：

   ```java
   private Object initializeBean(String beanName, Object bean, BeanDefinition beanDefinition) {
       // 1. 执行 BeanPostProcessor Before 处理
       Object wrappedBean = applyBeanPostProcessorsBeforeInitialization(bean, beanName);
   	//2. 执行初始化逻辑
       try {
           invokeInitMethods(beanName, wrappedBean, beanDefinition);
       } catch (Exception e) {
           throw new BeansException("Invocation of init method of bean[" + beanName + "] failed", e);
       }
   
       // 3. 执行 BeanPostProcessor After 处理
       wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
       return wrappedBean;
   }
   ```

9. Before执行完成之后，执行初始化逻辑，对于`userDao`来说，是xml中配置的，所以会到代码中的第二个判断逻辑中，对于userService来说，是实现接口，所以会到代码的第一个判断逻辑中：

   1. `userDao`的执行

   ![image-20231104104224865](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108237.png)

   ![image-20231104104324371](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108238.png)

   2. `userService`的执行：

      ![image-20231104104906690](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108239.png)

      ![image-20231104104927444](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108240.png)

10. 注册保存当前对象的所有销毁逻辑：

    ![image-20231104104427978](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108241.png)

    内部创建了一个适配器来统一封装

    ![image-20231104104456594](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108242.png)

    最终保存成功，对于`userDao`来说，由于是xml配置，所以`destroyMethodName`可以读取到内容，而`userService`是实现接口，所以销毁逻辑直接在bean继承的方法体中，所以`userService`的`destroyMethodName`为空：

    ![image-20231104105343190](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108243.png)

11. 所有的bean对象都实例化完成，执行完初始化逻辑，保存完销毁逻辑之后，注册钩子函数，内部保存要调用的逻辑，这个逻辑就是遍历保存销毁逻辑的容器`disposableBeans`，调用每一个销毁逻辑的`destroy`方法，也就是上面提到的`destroySingletons`方法：

    ![image-20231104105618173](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108245.png)

    关于`destroySingletons`方法的代码如下：

    ```java
    public void destroySingletons() {
        //拿到所有销毁方法的方法名
        Set<String> keySet = this.disposableBeans.keySet();
        Object[] disposableBeanNames = keySet.toArray();
    
        for (int i = disposableBeanNames.length - 1; i >= 0; i--) {
            Object beanName = disposableBeanNames[i];
            //拿到的是一个销毁对象，里面封装了销毁的执行逻辑
            DisposableBean disposableBean = disposableBeans.remove(beanName);
            try {
                //依次调用destroy方法
                disposableBean.destroy();
            } catch (Exception e) {
                throw new BeansException("Destroy method on bean with name '" + beanName + "' threw an exception", e);
            }
        }
    }
    ```

12. 所有的业务执行完毕之后，在虚拟机退出之前调用钩子函数执行销毁逻辑：

    ![image-20231104110120733](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108246.png)

13. 最终的结果：

    ![image-20231104110212665](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311041108247.png)

​		经过上面的分析，可以发现初始化和销毁逻辑的**加入点**是在`createBean`方法中，初始化加入即执行，而销毁逻辑在程序即将退出才执行，为了实现初始化和销毁逻辑，增加了一些类，也修改了一些类。

## 总结

​		为了扩展项目的功能，加入了初始化和销毁模块，为此引入了一些类，其中最重要的就是两个接口`InitializingBean`和`DisposableBean`和一个类`AbstractAutowireCapableBeanFactory`，引入接口的目的是为了提供统一的模板，修改类的目的是为了引入初始化和销毁模块，初始化和销毁模块的实现方式有两种，分别是xml配置和实现接口，最终整体的框架图如下：

<img src="https://bugstack.cn/assets/images/spring/spring-8-04.png" alt="图 8-4" style="zoom: 67%;" />

​	
