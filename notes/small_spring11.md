---
新增title: "Small_spring11"
description: "small_spring11"
keywords: "small_spring11"

date: 2023-11-10T15:39:52+08:00
lastmod: 2023-11-10T15:39:52+08:00

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
#url: "small_spring11.html"


# 开启各种图渲染，如流程图、时序图、类图等
# Enable chart render, such as: flow, sequence, classes etc
#mermaid: true

---

>🥔 small_spring11

​		在上一节中我们将IOC的部分彻底补充完整，现在的简易spring框架已经有了bean定义，注册，属性填充依赖注入，xml文件解析，应用上下文，修改，初始化和销毁，感知注入容器资源，多种方式多种类型创建bean以及事件机制，本节中在原有项目的基础上增加了`AOP`的功能，从`AOP`的实现到将`AOP`融入现有spring框架中进行介绍

<!--more-->

AOP如何实现：第11章

AOP如何加入到现有的spring框架中：第12章

## 原因

​		当bean对象已经创建完成想要进行修改，或者想要在一些方法执行前后统一执行一些操作，同时又不想每一处都进行修改，想要在不破坏源代码的情况下进行增强，此时就可以用到动态代理的知识，依据一些匹配规则，将指定的代码进行增强，这就是`AOP`的设计思想

​		本节中就引入了AOP的设计思想，根据匹配规则对方法的调用进行分类，符合匹配规则的方法会被增强，实现想要改变谁就可以改变谁的效果，具体的调用关系图如下：

![img](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111001162.png)

## 思路

​		为了实现动态代理，这里分为**两部分**介绍，第一部分介绍AOP的实现思路，第二部分将已经实现的AOP引入现有的spring框架中，增强现有的spring框架

### 第一部分

​		实现AOP的核心思想就是对执行的bean对象的所有方法进行匹配，当方法匹配成功之后，就说明这个bean需要一个代理类，代理类中被匹配成功的方法就执行增强的逻辑，然后再执行原始方法，根据这个描述可以发现几个关键的地方：**如何匹配**，**如何创建代理对象**，**如何执行增强的逻辑**。下面依次对这几个关键点进行描述：

1. 如何匹配：为了增强目标代码，AOP中提出了一个切入点表达式的知识，根据这个切入点表达式就可以判断当前bean是否会被增强，核心的功能被封装到了`AspectJExpressionPointcut`类中，主要是根据传递进来的类或者方法，根据切入点表达式判断**当前类**是否匹配，根据是否匹配返回一个`boolean`值，根据这个`boolean`值决定是否增强
2. 如何创建代理对象：根据上面的匹配结果决定是否创建代理对象，为了创建代理对象，有两种动态代理的技术，分别是`JDK`的动态代理和`Cglib`的动态代理，两种的主要区别是`JDK`是实现接口从而实现动态代理，最后会调用`invoke`方法执行增强，`Cglib`是继承从而实现动态代理，最后会调用`intercept`方法执行增强
3. 如何执行增强的逻辑：当一个bean创建了代理对象之后，就会尝试会内部的方法进行增强，但是并不是每次都需要对bean内部的全体方法进行增强，所以在代理对象中调用原始方法进行增强的之后，会进行第二次匹配，根据切入点表达式判断当前类的**当前方法**是否匹配，匹配就执行增强逻辑，不匹配就执行原始方法

​		经过上面三步就可以实现AOP，可以发现匹配了两次，第一次匹配是决定是否创建动态代理对象，第二次匹配是决定是否对当前方法执行增强逻辑，所以匹配方法有两个重载版本：

![image-20231111101937165](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915946.png)

### 第二部分

​		为了将第一部分的AOP引入现有的spring框架中，需要在bean的某一个合适的生命周期中引入，这个声明周期就是创建，当当前bean被切入点表达式匹配成功时，就会创建一个代理对象并返回，外界以为创建的还是普通对象，按照之前的使用方法使用会发现部分方法得到了增强，当当前bean没有被切入点表达式匹配成功时，会按照之前的流程创建普通bean对象并返回

​		对于用户来说，创建bean并返回没有任何改变，只是bean在调用的时候可能会被增强，所以会在`createBean`这个方法中引入AOP的模块，获取一个bean对象时就会出现两种逻辑：

![image-20231111103032806](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915948.png)

​		在新增的方法中主要是执行一个判断逻辑，如果当前初始化修改逻辑`BeanPostProcessor`实现了一个`InstantiationAwareBeanPostProcessor`**感知接口**，那么就说明现有的项目中引入了AOP，会有bean被增强，此时就需要进行切入点表达式的判断，这个逻辑与注入容器资源的逻辑类似，只要实现了一个感知接口，就说明这个类发生了对应的变化

​		一旦实现了这个感知接口，就会调用其中的`postProcessBeforeInstantiation`方法，方法内部根据配置的**代理信息**（切入点表达式标识哪些类的哪些方法会被增强，方法拦截器决定这些方法会被如何增强）来第一次匹配，判断当前的bean是否需要创建一个代理对象并返回

​		如果当前的bean匹配成功，就会创建一个代理对象并返回。外部用户拿到这个代理对象时，执行其中的方法会调用`invoke`或者`intercept`方法（动态代理的知识），内部第二次按照方法名进行细粒度的匹配，当前方法匹配成功就会按照代理信息中的规则决定当前方法如何被增强

​		在创建的过程中，需要保存代理信息，为了将代理信息进行统一包装，框架中提出了一个`AspectJExpressionPointcutAdvisor`类，内部保存了所有的代理信息，包括如何根据切入点表达式以及类的信息进行匹配，匹配成功应该执行什么逻辑

​		根据以上分析，本节中的**核心模块**一共有一下几个：

1. 封装代理信息的模块：保存切入点表达式以及匹配成功之后的增强逻辑
2. 代理对象创建模块：根据代理信息从而创建代理对象，调用什么样的增强逻辑
3. 整合模块：根据封装的代理信息决定是否调用代理对象创建模块

将`AOP`引入现有的spring框架的流程如下：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915949.png" alt="image-20231111123539718" style="zoom:80%;" />

核心是在红框中的部分，在创建普通bean**之前**引入一个代理bean对象的创建，只要当前bean会被切入点表达式匹配成功，那么就会创建一个代理bean对象，最后的一个判断逻辑中结果就不为空，此时就会直接返回，不会创建普通对象，外部用户拿到的bean对象也就是一个代理对象，是否创建代理对象取决于切入点表达式的匹配结果

### 类的变化

为了加入`AOP`机制，首先需要实现`AOP`机制，然后在创建bean的时候引入`AOP`机制，下面介绍项目中类的变化

#### 新增的类

1. `ClassFilter`：类的匹配接口，提供一个待实现的方法，用来匹配当前类是否被代理：

   ![image-20231111124331846](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915950.png)

2. `MethodMatcher`：方法的匹配接口，提供一个待实现的方法，用来匹配当前代理对象的当前方法是否要被增强：

   ![image-20231111124426841](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915951.png)

3. `Pointcut`：提供两个待实现的方法，用来提供类和方法的**匹配器**：

   ![image-20231111124525841](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915952.png)

4. `AspectJExpressionPointcut`：在这个类中实现**类和方法**与切入点表达式进行匹配的逻辑，相当于是匹配器：

   ![image-20231111124736494](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915953.png)


> 上面四个类的引用逻辑如下图，这四个类组合在一起形成了一个**匹配器**，匹配器的核心就是切入点表达式，只要传入切入点表达式就可以形成一个功能完整的匹配器：

![image-20231111125339062](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915954.png)

5. `BeforeAdvice`：这是一个前置通知接口，就是一个标记接口

6. `MethodBeforeAdvice`：方法前置通知接口，将方法执行前的通知执行逻辑定义到里面的待实现before方法中：

![image-20231111125943104](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915955.png)

7. `UserServiceBeforeAdvice`：针对`UserService`的前置通知实现，在方法执行前打印一句话，具体的执行逻辑定义在了`before`方法中：

```java
@Override
public void before(Method method, Object[] args, Object target) throws Throwable {
    System.out.println("拦截方法：" + method.getName());
}
```

8. `MethodBeforeAdviceInterceptor`：方法的拦截器，在里面决定通知的执行顺序，前置后置环绕这种类似的逻辑：

![image-20231111131115656](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915956.png)

> 上面四个类组合在一起形成了一个**模块**，可以决定被匹配器匹配到的方法如何执行通知

![image-20231111132854099](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915957.png)

9. `Advisor`：提供一个待实现的方法，可以获取代理信息中的通知并返回，通知中保存的是对指定方法进行增强的逻辑：

![image-20231111125050218](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915958.png)

10. `PointcutAdvisor`：提供一个待实现的方法，可以获取到匹配器的相关内容，也就是得到上面的`AspectJExpressionPointcut`类对象：

![image-20231111125309848](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915959.png)

11. `AspectJExpressionPointcutAdvisor`：代理信息的封装类，在这里可以配置**切入点表达式**，可以根据切入点表达式来执行**匹配器**的逻辑从而判断当前bean是否需要生成代理方法，并且还可以针对拦截到的方法执行对应的**通知**：

![image-20231111130344722](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915960.png)

> 上面几个类形成了这样一个代理信息的封装类，得到了一个**代理信息封装模块**

​		对于这个**封装类**来说，切入点表达式和通知的执行逻辑需要在xml配置文件中配置，而匹配器直接在内部new一个即可，因为匹配器的核心就是切入点表达式，new匹配器的时候只需要将配置的切入点表达式传入即可

12. `TargetSource`：这个类中封装了被代理的bean对象，对外提供一些获取bean对象信息的方法：

    ![image-20231111132319348](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915961.png)

13. `AdvisedSupport`：创建bean代理对象的参数封装类，将创建代理对象需要用到的所有参数封装到这个类中，包括匹配器（决定是否创建代理对象），拦截器（决定代理对象中的方法需要怎么增强），目标对象（被代理的真实对象）:

    ![image-20231111132640369](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915962.png)

14. `ReflectiveMethodInvocation`：封装原始方法的执行，在内部提供一个`proceed`方法，执行此方法触发原始方法的执行，例如前置通知执行完毕之后，就会调用这个方法从而触发原始方法的执行

    ![image-20231111132921038](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915964.png)

> 上面三个类为创建bean的代理对象提供**准备工作**，形成一个**代理对象创建准备模块**

15. `AopProxy`：代理对象的创建策略都需要实现这个接口，统一了他们的接口，调用其中的`getProxy`方法从而根据上面的**准备工作**创建一个bean代理对象并返回

    ![image-20231111133312277](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915965.png)

16. `JdkDynamicAopProxy`：使用`JDK`创建代理对象的策略，调用代理对象的方法最终会调用其中的`invoke`方法，在`invoke`方法中执行第二次对方法的匹配，匹配成功的方法执行对应的通知，通知中执行通知和原始方法。没匹配成功的方法执行`proceed`方法触发原始方法的执行：

    ![image-20231111133557675](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915966.png)

17. `Cglib2AopProxy`：使用`Cglib`创建代理对象的策略，调用代理对象的方法最终会调用其中的`intercept`方法，在`intercept`中执行第二次对方法的匹配，匹配成功的方法执行对应的通知，通知中执行通知和原始方法。没匹配成功的方法执行`proceed`方法触发原始方法的执行：

    ![image-20231111133808406](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915967.png)

> 上面三个类实现了代理对象的创建逻辑

18. `ProxyFactory`：接受一个`AdvisedSupport`对象，得到创建代理对象的封装参数之后选择不同的创建策略从而实现代理对象的创建，对外成为一个代理工厂，提供`getProxy`方法来提供一个创建好的代理对象：

    ![image-20231111134202959](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915968.png)


> 这个类将代理对象的创建封装起来得到一个代理工厂模块

18. `InstantiationAwareBeanPostProcessor`：是一个**感知**接口，实现这个接口就可以被spring感知到，从而调用新增的`postProcessBeforeInstantiation`方法完成代理对象的创建，或者说实现这个接口的类会被spring感知到项目中有引入`AOP`的意图，从而针对每一个bean的创建都去匹配判断是否需要创建代理对象，这个接口继承了`BeanPostProcessor`，代表他的实现类都算是一个修改逻辑：

    ![image-20231111134740938](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915969.png)

19. `DefaultAdvisorAutoProxyCreator`：是整个项目的<font color=red>**核心类**</font>，将其配置到xml文件中，并将代理信息也配置到xml文件中之后，spring在创建**每一个**bean的时候，都会经过`resolveBeforeInstantiation`方法之后最终会调用到这个类中的方法，执行的流程为：

    <img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915970.png" alt="image-20231111135957363" style="zoom:80%;" />

​		重点就是调用本类中的`postProcessBeforeInstantiation`方法，这个方法内部调用xml文件中配置的代理信息封装模块，代理信息封装模块可能有多个，每个模块中对应一个切入点表达式，从而根据这些切入点表达式判断当前bean对象是否匹配成功，匹配成功就代表当前bean需要一个代理类，此时就开始创建，对应到上面的流程图中就是**当前bean是否匹配切入点表达式**

​		先将代理信息封装模块中的信息取出来封装成一个`AdvisedSupport`，其中包含被代理的目标对象是谁，定义的通知如何执行，方法匹配器是什么（便于判断代理对象中的哪些方法需要被增强），将这些信息传递给代理工厂之后，根据选定的代理策略创建一个代理对象返回，对应到上面的流程图中就是**根据代理信息创建代理对象**

​		如果当前bean没有被匹配成功，此时创建的代理对象就是空，对应到上面的流程图中就是**创建普通对象**

---

总结来看，为了将spring加入到现有的项目中，将AOP机制分为了好几个模块，各司其职，**详细**来说分为以下几个模块：

1. 匹配器模块：内部根据切入点表达式来判断当前类或者当前类的方法是否匹配成功
2. 拦截器模块：内部定义通知，也就是如何增强相应的方法，以及通知和原始方法之间的执行顺序
3. 代理信息封装模块，将匹配器，拦截器，切入点表达式封装到一起
4. 代理对象创建准备模块：将代理对象创建需要的参数准备好
5. 代理工厂模块：将代理对象的创建封装起来
6. **核心模块**：通过实现一个感知模块从而针对每一个bean的创建加入`AOP`的逻辑，一旦匹配成功，当前bean就需要引入`AOP`的相关内容

#### 修改的类

为了引入`AOP`，需要在bean的生命周期中加入一步，这一步加入到了`createBean`中：

1. `AbstractAutowireCapableBeanFactory`：在`createBean`方法中增加了一步，针对每一个bean的创建都加入了切入点表达式的匹配工作，一旦bean匹配成功，说明当前bean需要被代理，从而创建代理对象，新的`createBean`方法变成：

   ![image-20231111103032806](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915948.png)

   关于`resolveBeforeInstantiation`方法的执行可以参考上面的流程图，主要是新增一个`AOP`核心类中的处理方法`applyBeanPostProcessorsBeforeInstantiation`，内部执行核心类中的`postProcessBeforeInstantiation`方法，判断当前bean是否需要创建代理对象

---

​		总结来看，为了将`AOP`引入spring中，增加了一个实现`BeanPostProcessor`的感知类，内部实现了对bean的匹配，根据匹配结果决定是否创建代理对象，代理对象内部实现怎样的增强，将这个感知类的核心逻辑加入到bean的创建逻辑中，从而实现引入

### bean的创建和获取

​		下面根据bean的创建和获取的流程介绍AOP如何引入到了现有的spring框架中，项目的前提是一个实现了接口的UserService类，我们想要给其中的方法前面加入一个通知，打印一句话，通知的内容为：

![image-20231111142744062](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915971.png)

为了实现AOP的功能，最新的xml文件为：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans>
    <!--====第一种类型的bean====-->
    <!--配置一个普通的bean-->
    <bean id="userService" class="com.zzzi.springframework.bean.UserService"/>


    <!--====第一种类型的bean====-->
    
    <!--配置一个代理类生成器-->
    <bean class="com.zzzi.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator"/>


    <!--====第三种类型的bean====-->
    <!--将代理信息最终全部保存到一个AspectJExpressionPointcutAdvisor对象中-->
    <!--在代理类执行之前执行什么逻辑-->
    <!--只有这个逻辑需要自己编写，其余的都是固定需要加入的东西-->
    <bean id="beforeAdvice" class="com.zzzi.springframework.bean.UserServiceBeforeAdvice"/>

    <!--拦截到方法之后，执行代理类之前执行的逻辑beforeAdvice-->
    <bean id="methodInterceptor"
          class="com.zzzi.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor">
        <!--里面注入了执行前的逻辑beforeAdvice，便于触发-->
        <property name="advice" ref="beforeAdvice"/>
    </bean>

    <!--将依赖的东西全部注册成功bean，之后就可以从IOC容器中自动获取-->
    <!--这里相当于代理信息的封装-->
    <bean id="pointcutAdvisor" class="com.zzzi.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor">
        <!--先封装一个切入点表达式用于指定哪些方法被匹配-->
        <property name="expression" value="execution(* com.zzzi.springframework.bean.IUserService.*(..))"/>
        <!--后执行匹配成功的方法执行什么逻辑-->
        <property name="advice" ref="methodInterceptor"/>
    </bean>

</beans>
```

1. 加载配置文件，xml配置文件中主要配置了三种类型的bean：

   ![image-20231111185059148](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915972.png)

2. 得到应用上下文对象：

   ![image-20231111185306751](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915973.png)

3. 获取`userService`对象，获取的过程中根据切入点表达式决定是否创建代理对象，经历以下几步：

   1. 创建普通bean对象之前调用`resolveBeforeInstantiation`中的`applyBeanPostProcessorsBeforeInstantiation`方法，遍历所有实现了`BeanPostProcessor`接口的类，看有没有`AOP`的感知类，有的话说明当前项目中引入了`AOP`的机制，当前bean可能需要创建代理对象，需要去匹配：

      ![image-20231111185720100](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915974.png)

   2. 执行`postProcessBeforeInstantiation`方法，根据xml配置文件中配置的所有切入点表达式来匹配当前的bean类型，判断是否需要创建代理对象，这里是**第一次匹配**，目的是判断是否需要创建代理对象

      ![image-20231111185847218](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915975.png)

   3. 需要创建代理对象的话，就需要将创建代理对象需要的参数`AdvisedSupport`准备好，然后调用代理工厂中的接口得到一个代理对象返回

      ![image-20231111190120723](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915976.png)

   4. 如果代理对象创建成功，就返回代理对象，如果代理对象创建失败，就创建一个普通对象返回

      ![image-20231111190358926](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915977.png)

4. 获取到bean对象后，指定对象中的方法，如果当前bean被代理了，那么执行原始方法时就会进入动态代理中的`invoke`或者`intercept`中，这里使用`JDK`的动态代理，所以以`invoke`方法为例，`Cglib`的增强逻辑同理

   ![image-20231111190631728](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915978.png)

5. 利用xml配置文件中的方法匹配器进行匹配，这里是第二次匹配，目的是为了判断当前方法是否需要增强，如果需要的话，会调用方法拦截器中规定的`invoke`逻辑来执行增强，这里的invoke方法并不是动态代理中的invoke回调方法，而是方法拦截器中的方法

   ![image-20231111190855216](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915979.png)

6. 执行前置通知

   ![image-20231111191021421](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915980.png)

7. 利用反射执行原始方法

   ![image-20231111191105520](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915981.png)

8. 得到最终的结果

   ![image-20231111191152822](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111915982.png)

## 总结

​		总结来看，为了引入AOP机制，针对bean的创建加入了两次匹配，第一次是利用类匹配器来判断当前bean是否需要创建代理对象，第二次是利用方法匹配器来判断当前代理对象的方法是否需要执行通知，也就是增强逻辑，而为了创建代理对象，通知，匹配器等内容，引入了不同的模块，AOP引入的时机在bean的创建之前，为了引入AOP机制，需要实现一个感知接口`InstantiationAwareBeanPostProcessor`，从而让spring知道项目中有引入AOP的意图，之后每一个bean的创建都会尝试给其创建一个代理对象，创建与否与切入点表达式有关，核心模块图为：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311111949860.png" alt="img" style="zoom:80%;" />
