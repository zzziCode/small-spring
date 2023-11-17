---
title: "Small_spring15"
description: "small_spring15"
keywords: "small_spring15"

date: 2023-11-16T14:02:08+08:00
lastmod: 2023-11-16T14:02:08+08:00

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
#url: "small_spring15.html"


# 开启各种图渲染，如流程图、时序图、类图等
# Enable chart render, such as: flow, sequence, classes etc
#mermaid: true
---

>🥬 small_spring15

经过之前的编码，spring最核心的IOC和AOP的内容已经基本搭建完成，IOC章节中实现了bean的定义和注册，属性填充，读取配置文件，应用上下文，修改，初始化，销毁，aware感知，`FactoryBean`，事件监听。AOP章节中实现了对普通bean进行增强，自动扫描bean的注册，代理对象注入属性的内容。但是到目前为止，一个关键的问题还没有解决，那就是循环依赖，本节中就来解决这个问题，相关的代码我放到了[仓库](https://github.com/zzziCode/small-spring)中

<!--more-->

## 原因

​		在bean的创建过程中，需要依赖一些属性，将这些属性填充完整才能完成bean的创建，而这些属性不只是普通属性，有可能在bean的创建过程中，还会依赖一些其他的bean，一旦这些被依赖的bean在创建过程中也依赖当前的bean，就会产生循环依赖的问题，如下图所示一共有三种循环依赖的情况：

![img](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311161413872.png)

但是三种循环依赖的本质上是一样的，都是在创建过程中依赖一个正在创建的bean，这样相互依赖导致bean无法创建成功，本节中来解决这些问题

## 思路

​		为了解决循环依赖的问题，spring引入了三级缓存的概念，其中每一级缓存都各司其职从而解决了循环依赖的问题，下面就以第二种A和B创建过程中互相依赖的情况来描述三级缓存如何解决循环依赖：

1. 创建A，此时会先创建一个空bean，然后将这个空bean保存到**三级缓存中**，不管后面是否能用到，之后进行属性填充
2. 在属性填充的过程中，需要填充B，此时就会去**一级缓存**中找对应的B，发现B不存在，再去**二级缓存**中找，也没找到，再去**三级缓存**中找，还是没找到，此时新建B
3. 创建B的时候也会先创建一个空bean，然后也将这个空bean保存到**三级缓存**中，，之后进行属性填充
4. 在属性填充的过程中，需要填充A的属性，此时就会去**一级缓存**中找对应的A，发现A不存在，再去**二级缓存**中找，也没找到，再去**三级缓存**中找，找到之前注册的空A，然后就判断这个A是否需要AOP，需要的话就创建一个代理对象，不需要的话就是普通对象，然后将这个对象放到二级缓存中并填充给B中A属性
5. B执行其他的属性填充，其他的工作，然后完成B的创建
6. 完成创建之后，将这个B保存到**一级缓存**中，然后将其从**三级缓存**中删除
7. 之后继续A属性的填充，此时由于**三级缓存**中提前保存了未实例化完成的A，于是B可以创建完成，A也就可以用这个B进行属性填充，从而完成创建
8. A创建完成之后，执行正常的生命周期，此时的A还是一个普通对象，属性填充完成之后，B中的A属性不再是空。后面就需要判断是否需要进行AOP，如果前面进行了AOP，这里就不需要进行AOP，最后调用`getSingleton`方法得到最终版的结果。在这个方法中，如果提前进行了AOP，直接从**二级缓存**中就可以得到最终的答案，如果没有提前进行AOP，此时会从**三级缓存**中拿到最终的对象（普通或者代理）放进**一级缓存**中

相当于如果出现了循环依赖，提前进行了AOP，那么**二级缓存**中就会存储尝试AOP之后的A，进行正常的生命周期到达正常的AOP步骤时，不用再AOP，最后直接将**二级缓存**中的A保存到单例池中即可。如果没有出现循环依赖，最终也会调用`getSingleton`方法，然后重新创建一个AOP对象，关于这里的结束可以看下面的描述：

---

>**假设bean需要AOP**

如果出现循环依赖，利用**三级缓存**中保存的信息就可以创建出来一个bean代理对象，然后将其保存到**二级缓存**中，最后保存到单例池中时，会从**二级缓存**中拿到这个创建出来的bean代理对象，此时由于AOP提前，所以`initializeBean`方法中的正常AOP步骤不会执行，这是**第一个**正常AOP失效的地方
如果没有出现循环依赖，会正常经过bean的生命周期，之后执行AOP的操作，虽然当前bean得到了一个代理对象，但是最后的代码经过了一步：

```java
Object exposedObject = bean;
if (beanDefinition.isSingleton()) {
    // 获取代理对象，获取到真正需要的bean
    exposedObject = getSingleton(beanName);
    //将创建完成的bean对象，不管是否出现了循环依赖，都将其保存到最终的单例池中
    registerSingleton(beanName, exposedObject);
} 
```
这个代码中的`getSingleton`会尝试从**一级**和**二级**缓存中拿到bean对象，但是由于没有循环依赖，所以拿不到，于是会从**三级缓存**中重新创建一个代理bean对象，相当于之前经过正常AOP流程创建出来的bean对象**失效**不会保存到单例池中，这是**第二个**正常AOP失效的地方，当没有出现循环依赖时，可以看到正常创建出来的bean代理对象和真正保存到单例池中的代理对象是不一样的：
![image](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311161655198.png)

所以即使将`initializeBean`方法中的正常AOP流程去掉也不会对当前的项目有什么影响，因为本项目中的正常AOP流程已经**失效**了

---

核心点就是一个提前暴露的**三级缓存**，还有一个`getSingleton`方法内部决定bean的获取逻辑，如果出现循环依赖，先从**一级缓存**中拿，然后从**二级缓存**中拿，最后才从**三级缓存**中拿，保证bean的单例性。提前暴露的目的是为了打破循环

核心点总结如下：

1. 空bean一创建，**三级缓存**中就保存了一个`ObjectFactory`对象，可以根据当前创建的空bean来创建对象，如果当前对象需要AOP，那么就创建代理对象，否则就创建普通对象，这个bean还没有经历正常的生命周期，为的是出现循环依赖时打破这个循环
2. 出现循环依赖时，`getSingleton`方法中控制了**三级缓存**的顺序，从而打破这个循环依赖
3. 后期bean创建完成需要保存到单例池中时，为了保持bean的单例性，会从**三级缓存**中拿bean对象

下面以流程图的形式描述A和B出现循环依赖时怎么创建：

![未命名文件 (4)](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311171238151.png)

出现了循环依赖就会用**第三级缓存**打破，用**第二级缓存**保持单例性，然后创建完成之后，将bean对象最终保存到**第一级缓存**单例池中

### 类的变化

为了解决循环依赖，对项目中的类进行了一些修改：

1. `DefaultSingletonBeanRegistry`：在这个类中增加三级缓存的容器，并且修改`getSingleton`方法的逻辑，在其中控制三级缓存之间的读取流程，并且对外提供向第三级缓存中保存空白普通bean的方法，修改向第一级缓存单例池中添加bean的代码：

   ![image-20231116162258820](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311162111367.png)

   核心就是`getSingleton`代码：

   ```java
   public Object getSingleton(String beanName) {
       //从一级缓存中拿
       Object singletonObject = singletonObjects.get(beanName);
       if (null == singletonObject) {
           singletonObject = earlySingletonObjects.get(beanName);
           // 从二级缓存中拿
           if (null == singletonObject) {
               //从三级缓存中拿
               //专门用来打破循环依赖
               ObjectFactory<?> singletonFactory = singletonFactories.get(beanName);
               if (singletonFactory != null) {
                   singletonObject = singletonFactory.getObject();
                   // 把三级缓存中的代理对象中的真实对象获取出来，放入二级缓存中
                   earlySingletonObjects.put(beanName, singletonObject);
                   singletonFactories.remove(beanName);
               }
           }
       }
       return singletonObject;
   }
   ```

2. `InstantiationAwareBeanPostProcessor`：这个接口中新增加一个待实现的方法，用来将对对象的AOP提前，AOP中进行匹配器的匹配，当前对象**不一定**创建代理对象：
   ![image-20231116164251338](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311162111368.png)

3. `DefaultAdvisorAutoProxyCreator`：类中将创建代理对象的方法封装起来，在两个方法中调用，分别是`postProcessAfterInitialization`和`getEarlyBeanReference`方法中，第一个方法中调用创建代理对象的方法是因为执行了正常的AOP流程，第二个方法中调用创建代理对象的方法是因为出现了循环依赖，要先将一个空对象创建出来打破循环依赖，第二个方法经过包装最终会在第三级缓存中的`getObject`方法中调用。并且一旦当前对象**提前**经过了AOP，那么后面在正常AOP的触发时机到达时就不用再进行AOP了，为了实现这种动态的判断，将提前AOP的对象都在**容器**中保存一份，这个容器是`earlyProxyReferences`：

   ![image-20231116164734400](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311162111369.png)

4. `AbstractAutowireCapableBeanFactory`：在这个类中修改bean的生命周期过程，当一个空bean创建出来之后，就在三级缓存中保存一个`lambda`表达式，表达式内部保存的是上面说的`getEarlyBeanReference`方法逻辑，也就是说当从三级缓存中获取对象时会调用保存的`getEarlyBeanReference`方法，从而将对象的**AOP提前**，如果当前对象确实需要AOP，那么就会创建代理对象。否则就是普通对象，然后将这个普通对象保存到二级缓存中并返回

   并且在将对象创建完成之后，会将创建完成的代理对象从二级缓存中拿到并保存到单例池中

   ![image-20231116165043150](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311162111370.png)

5. `ObjectFactory`：这是一个函数式接口，当一个空bean创建完成之后，就利用这个接口类型的`lambda`表达式保存刚创建出来的空bean信息：

   ![image-20231116192754523](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311162111371.png)

​		总的来说就是在将单例池一级缓存改成三级缓存，然后在创建对象时就将未创建完成的bean保存到三级缓存中提前暴露，这样提前暴露就可以打循环依赖，并且如果在创建过程中出现循环依赖，就要将针对对象尝试AOP的步骤提前，此时需要记录一下，后期正常AOP的流程就不用执行了

### bean的创建和获取

下面从bean的创建和获取的角度出发讲解如何解决循环依赖的问题，项目中有`wife`和`husband`两个bean，剩下的bean都是为了实现spring中基础功能所注册的bean，其中`wife`和`husband`互相依赖，而`wife`需要进行`AOP`代理：

1. 读取配置文件，`refresh`方法中调用`preInstantiateSingletons`方法开始创建`wife`的bean对象，创建时会先尝试去缓存中拿：

   ![image-20231116191307938](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311162111372.png)

2. 尝试从三级缓存中拿到wife对象，但是初始状态下拿不到`wife`的bean对象，也就是`getSingleton`方法会返回`null`：

   ![image-20231116191405568](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311162111373.png)

3. 此时需要创建`wife`的bean对象，也就是调用`createBean`方法，最终会执行`doCreateBean`方法，此时只是将xml文件中配置的bean的注册信息扫描完成，但是并没有完成注册：

   ![image-20231116202532377](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311162111375.png)

4. 执行`doCreateBean`方法，内部先创建一个空bean，并且将创建逻辑保存到第三级缓存中：

   ![image-20231116202908603](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311162111376.png)

5. 给`wife`填充属性，这里由于wife内部依赖`husband`，而`husband`内部又依赖`wife`，所以会出现**循环依赖**，一共经历以下几步来解决循环依赖，从而完成对`wife`的创建：

   1. 填充`hubsband`属性，调用`getBean`方法尝试从缓存中拿到`husband`：

      ![image-20231116203239661](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311162111377.png)

   2. `getBean`方法中调用`doGetBean`方法，内部先调用`getSingleton`方法，尝试从三级缓存中拿到`husband`对象，由于`husband`还没有创建，所以缓存中还没有，所以会调用`createBean`方法创建`husband`对象，<font color=red>注意此时wife正在创建中</font>：

      ![image-20231116203546016](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311162111378.png)

   3. 创建`husband`的bean对象，执行`doCreateBean`方法，内部先创建一个空bean，并且将创建逻辑保存到第三级缓存中，此时的第三级缓存中保存的有一个没创建完成的`wife`和`husband`：

      ![image-20231116203819165](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311162111379.png)

   4. 对`husband`进行属性填充，`husband`内部依赖`wife`，而`wife`正在创建中，但是这里可以利用第三级缓存中还没有创建完成的提前暴露的`wife`先完成`husband`的属性填充：

      ![image-20231116204101475](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311162111380.png)

   5. `getBean`方法中调用`doGetBean`方法，内部先调用`getSingleton`方法，尝试从三级缓存中拿到`wife`对象完成`husband`中对`wife`的属性填充，此时第三级缓存中保存了`wife`没有创建完成的对象，于是可以拿到

      ![image-20231116204236210](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311162111381.png)

   6. 调用`getObject`拿到需要的`wife`对象，内部调用保存的`lambda`表达式中的`getEarlyBeanReference`方法，将AOP的操作提前，主要是调用`getEarlyBeanReference`方法完成AOP的工作：

      ![image-20231116204353554](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311162111382.png)

   7. 在`getEarlyBeanReference`方法内部，先将这个提前AOP的对象保存到容器中标记一下，然后调用`wrapIfNecessary`完成对`wife`的代理对象的创建并返回：

      ![image-20231116204459281](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311162111383.png)

   8. 从第三级缓存中拿到创建出来的`wife`代理对象之后，将其保存到二级缓存中，并将三级缓存中关于`wife`的信息删除，此时返回这个创建的代理对象，代理对象内部的`target`真正被代理对象还没有创建完，属性还没有填充完成，但是这个`wife`代理对象用来给`husband`做属性填充是足够的：

      ![image-20231116204759513](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311162111384.png)

   9. `husband`通过`getBean`方法得到一个`wife`的代理对象之后进行属性填充，可以看到`wife`的属性已经填充好了：

      ![image-20231116205004634](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311162111385.png)

   10. 完成对`husband`的创建，此时已经打破了循环依赖，完成了对`husband`的创建，也就是说，`wife`所依赖的`husband`创建出来了，此时将`husband`保存到单例池中，也就是一级缓存中，并且将`husband`保存到剩下两级缓存中的内容删除，这些操作都是为了保持bean的单例性：

       ![image-20231116205517886](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311162111386.png)

   11. 至此完成`husband`的创建

6. `wife`调用`getBean`方法经历上面的10步就拿到了创建成功的`husband`对象，`husband`内部依赖的`wife`此时还没有创建完成，此时就开始`wife`的属性填充：

   ![image-20231116205749789](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311162111387.png)

7. 属性填充完毕之后，`wife`的创建也结束了，由于xml文件中配置了切面，标记要对`wife`进行增强，所以上面`husband`获取`wife`对象时获取到的是代理对象，当`wife`创建成功之后，因为AOP提前了，所以此时的`wife`是一个**普通对象**，而`wife`经过了AOP增强，需要将**代理对象**保存到单例池中，所以下一步就是将真正需要保存的bean拿出来保存到单例池中：

   ![image-20231116210100456](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311162111388.png)

8. 在创建`husband`对象时，创建的`wife`代理对象保存到了二级缓存中，所以此时就调用`getSingleton`方法从二级缓存中拿到真实的代理对象，然后将其保存到单例池中:

   ![image-20231116210304221](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311162111389.png)

9. 完成对`wife`的创建后，`husband`内部的`wife`也填充完成，此时`wife`和`husband`都创建完毕，执行自己的业务逻辑，可以发现出现循环依赖的两个bean都创建成功，并且还执行了AOP的增强：

   ![image-20231116210429697](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311162111390.png)

​		经过上面的步骤，循环依赖被完美的解决，但是需要注意的是，上面的`wife`需要进行AOP，所以创建的是代理对象，如果`wife`不需要AOP，那么上面的第八步中调用`getSingleton`拿到的`exposedBean`和`bean`是一样的，都是普通的bean。如果`wife`需要AOP，但是`wife`没有循环依赖的问题，那么就会执行正常的AOP流程，不会执行提前AOP，最后调用`getSingleton`尝试拿到AOP对象时拿到的是一个**新的代理对象**：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311161655198.png" alt="image" style="zoom:67%;" />

也就是说没有出现循环依赖时，经过正常AOP创建出来的代理对象**失效**，没有什么作用，最终保存到单例池中供外部使用的代理对象是`getSingleton`方法中拿到的新代理对象，但是只是正常的AOP流程失效，并不会对spring对外提供的功能产生影响

## 总结

​		经历上面的步骤之后，完美的解决了循环依赖的问题，主要是将使用三级缓存将出现循环依赖的bean提前暴露，并且保持其单例性，打破循环依赖之后，bean就可以正常创建，只是将bean所依赖的bean的地址提前拿到，完成一个bean的创建之后，另外一个bean的创建也可以完成，之后由于地址相同，内容同步更新，最后就可以完成所有bean的创建
