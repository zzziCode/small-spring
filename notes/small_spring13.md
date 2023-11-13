---
title: "Small_spring13"
description: "small_spring13"
keywords: "small_spring13"

date: 2023-11-13T09:43:01+08:00
lastmod: 2023-11-13T09:43:01+08:00

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
#url: "small_spring13.html"


# 开启各种图渲染，如流程图、时序图、类图等
# Enable chart render, such as: flow, sequence, classes etc
#mermaid: true
---

>🌶️ small_spring13

​		上一节中我们新增了一中bean的注册方式：包扫描+`@Component`注解。这种方式实现了只用指定扫描哪些包，程序自动将这些包下使用了`@Component`注解的bean注册到注册表中，同时bean还可以使用了占位符的形式从属性文件中读取属性值来填充，但是这两部分是分开的，所以本节中在bean自动注册过程中引入了注解进行属性注入的模块，可以使用`@Value`注解可以注入普通属性，使用`@Autowired`和`@Qualifier`注解注入bean对象，相关的代码我放到了[仓库](https://github.com/zzziCode/small-spring)中

<!--more-->

## 原因

​		上节中实现了bean的自动扫描注册以及占位符形式注入bean的属性，但是这两部分是分开处理的，也就是说，bean的自动扫描注册的过程中没有引入任何属性，注册表存储的`BeanDefinition`中的属性列表`PropertyValues`部分没有在xml文件中配置的属性为空，所以本节中在bean的自动扫描注册的过程中引入了属性填充的功能，可以使用注解来进行属性填充，加入的模块关系如下图：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311131054285.png" alt="img" style="zoom:67%;" />

主要是在`refresh`函数中进行属性占位符的替换，然后在空bean创建之后，属性填充之前将属性列表构造好直接填充，然后在属性填充时只需要填充xml文件中配置的bean属性

## 思路

​		为了在自动扫描注册之后引入注解属性填充的功能，肯定需要在bean的生命周期中进行改造，本节中为了引入注解属性填充的功能，需要在空bean创建之后，属性填充之前执行注解属性填充的功能，从而加载通过注解配置的属性来进行填充，之后再进行正常的xml配置文件中的属性填充。在xml文件读取的过程中，一旦配置了包扫描路径，此时就会执行bean的自动扫描注册，注册表中bean的`BeanDefinition`中的`PropertyValues`为空，之后再读取xml配置文件，如果此时还配置了bean，就会读取一份新的`PropertyValues`，将其存入注册表中时会更新注册表，从而此时的bean的`BeanDefinition`存储了两部分数据，第一部分是xml直接配置的属性，不为空，第二部分是使用注解配置的属性，为空。

​		下面的步骤就是将执行占位符替换，如果xml配置属性的过程中存在占位符，就在这一步进行替换，然后就是空bean的创建，下一步就是注解属性填充的部分，根据注解中配置的属性直接填充空bean中的属性，这一步补全了上面说的`BeanDefinition`中的第二部分为空的属性，下来就是正常的属性填充，这一步利用上面所说的`BeanDefinition`中的第一部分不为空的属性进行填充。经过这一步之后，bean的创建就完成了，之后完成一些剩下的步骤，就可以获取bean执行一些业务了。下面分步骤介绍注解属性填充的引入流程：

1. 加载xml配置文件的时候，将包扫描路径下的所有bean加入注册表，同时将注解处理器`AutowiredAnnotationBeanPostProcessor`的bean也**手动**加入注册表
2. 触发实例化前的修改逻辑，将上一步bean中的占位符替换成真正的值，**同时**保存一个字符串处理器便于后面使用
3. 保存实例化后的修改逻辑，本节中的重点就是保存`AutowiredAnnotationBeanPostProcessor`的bean到容器中，便于后期触发
4. 从`createBean`方法中的`applyBeanPostProcessorsBeforeApplyingPropertyValues`为入口，里面触发`AutowiredAnnotationBeanPostProcessor`的`postProcessPropertyValues`方法，内部处理**注解属性填充**。针对Value中的占位符使用到了之前保存的**字符串处理器**进行属性填充，针对`Autowired`和`Qualifier`使用**`getBean`**的方式进行属性填充
5. 执行`createBean`之后的操作正常的从xml文件中读取到的**正常属性填充**，然后容器资源注入，初始化前操作，初始化，初始化后操作，保存销毁逻辑，返回bean

总结起来就是四步： 

1. 将注解处理器加入注册表中
2. 将字符串处理器加入容器中
3. 将注解处理器加入容器中
4. 触发容器中的注解处理器的执行进行属性填充

具体的**流程图**如下图所示：

<img src="https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311131910828.png" alt="image-20231113142204520" style="zoom:67%;" />

可以发现，在空bean创建之前都是一些准备工作，为了将注解属性填充引入现有的项目中，核心的步骤就是在`createBean`中的空bean创建之后引入了新的一步`applyBeanPostProcessorsBeforeApplyingPropertyValues`

### 类的变化

下面介绍为了引入注解属性填充，项目中类的变化情况：

#### 新增的类

1. `StringValueResolver`：是一个待实现的接口，内部提供一个待实现的方法，可以根据传入的带占位符的字符串利用资源文件将占位符替换成真正的值，也就是上面提到的**字符串处理器**：

   ![image-20231113135057652](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311131910831.png)

2. `Value`，`Autowired`，`Qualifier`：这是三个为了实现注解属性填充而引入的注解，`Value`是为了进行普通属性填充，`Autowired`和`Qualifier`是配合起来进行bean的依赖注入

3. `AutowiredAnnotationBeanPostProcessor`：注解属性填充的核心类，实现了`InstantiationAwareBeanPostProcessor`接口，根接口是`BeanPostProcessor`，也就是说这是一个实例化后的修改逻辑，最终会从容器中获取到这个修改逻辑，执行其中的`postProcessPropertyValues`方法，从而进行注解属性填充，而这个类的触发时机在`createBean`中的空bean创建之后：

   ![image-20231113135553577](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311131910832.png)

#### 修改的类

1. `PropertyPlaceholderConfigurer`：主要修改的地方是内部新增了一个字符串处理器类，字符串处理器内部的`resolveStringValue`方法调用一个抽象出来的`resolvePlaceholder`方法实现占位符的替换，这个字符串处理器不不仅在xml属性占位符的替换时使用，还在Value注解占位符替换的时候使用。在执行完xml配置中的属性占位符的替换时，这个字符串处理器会被**保存**到`beanFactory`中的一个容器中：

   ![image-20231113140009587](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311131910833.png)

2. `ConfigurableBeanFactory`：新增两个待实现的方法，`addEmbeddedValueResolver`方法是在xml属性占位符替换的时候调用，用来存储字符串处理器便于后面使用。`resolveEmbeddedValue`方法是在注解属性填充中处理Value注解中的占位符时调用，内部调用所有保存的字符串处理器，尝试处理当前这个占位符：

   ![image-20231113140518352](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311131910834.png)

3. `AbstractBeanFactory`：实现了`ConfigurableBeanFactory`接口，从而实现了上面新增的两个方法，同时**新增了一个容器**用来保存所有的字符串处理器，便于后面直接调用这两个方法保存字符串处理器和调用字符串处理器：

   ![image-20231113140629943](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311131910835.png)

4. `ClassPathBeanDefinitionScanner`：在包路径扫描完毕，bean自动注册之后，添加一步进行手动保存`AutowiredAnnotationBeanPostProcessor`的bean注册信息，便于后面从容器中获取到`AutowiredAnnotationBeanPostProcessor`这个实例化后的修改策略，从而记性注解属性填充：

   ![image-20231113140910814](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311131910836.png)

5. `InstantiationAwareBeanPostProcessor`：新增一个待实现的方法`postProcessPropertyValues`，在其中定义注解属性填充的逻辑，最终在`AutowiredAnnotationBeanPostProcessor`类中实现：

   ![image-20231113141311588](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311131910838.png)

6. `DefaultAdvisorAutoProxyCreator`：由于其实现了`InstantiationAwareBeanPostProcessor`接口，而这个接口中新增了待实现的方法，所以这里简单实现了新增的`postProcessPropertyValues`方法，**内部没做任何实现**，只是为了语法编译通过：

   ![image-20231113141342753](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311131910839.png)

7. `AbstractAutowireCapableBeanFactory`：在`createBean`方法中创建空bean之后引入一步`applyBeanPostProcessorsBeforeApplyingPropertyValues`，主要是调用注解属性填充的逻辑，内部利用`instanceof`找到实现了`InstantiationAwareBeanPostProcessor`接口的`AutowiredAnnotationBeanPostProcessor`类，调用其中的`postProcessPropertyValues`方法，内部根据普通属性填充和bean属性填充分开操作，针对普通属性调用`beanFactory`中的`resolveEmbeddedValue`方法来操作，针对bean属性调用`getBean`方法来操作：

   ![image-20231113141603542](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311131910840.png)

   `applyBeanPostProcessorsBeforeApplyingPropertyValues`方法的代码为：

   ```java
   protected void applyBeanPostProcessorsBeforeApplyingPropertyValues(String beanName, Object bean, BeanDefinition beanDefinition) {
       for (BeanPostProcessor beanPostProcessor : getBeanPostProcessors()) {
           if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor){
               //根据配置的注解得到所有的属性注入值
               //这里针对原有的属性列表没有做任何改变，原封不动的返回，如果此时
               PropertyValues pvs = ((InstantiationAwareBeanPostProcessor) beanPostProcessor).postProcessPropertyValues(beanDefinition.getPropertyValues(), bean, beanName);
               if (null != pvs) {
                   for (PropertyValue propertyValue : pvs.getPropertyValues()) {//只要获取到的属性列表不是空，就新增一份
                       beanDefinition.getPropertyValues().addPropertyValue(propertyValue);
                   }
               }
           }
       }
   }
   ```

8. `BeanFactory`：内部新增一个**只按照类型**获取bean对象的方法，在`DefaultListableBeanFactory`类中实现：

   ![image-20231113161706111](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311131910841.png)

9. `DefaultListableBeanFactory`：对上面的只按照类型获取bean对象的方法进行实现，如果同一个类型只获取到一个就正常返回这个bean，如果获取到多个就不知道选哪一个，此时**抛出异常**：

   ```java
   public <T> T getBean(Class<T> requiredType) throws BeansException {
       List<String> beanNames = new ArrayList<>();
       for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
           Class beanClass = entry.getValue().getBeanClass();
           if (requiredType.isAssignableFrom(beanClass)) {
               beanNames.add(entry.getKey());
           }
       }
       //如果一个类型下有一个对象才返回
       if (1 == beanNames.size()) {
           return getBean(beanNames.get(0), requiredType);
       }
       //一个类型下有多个bean对象就抛出异常
       throw new BeansException(requiredType + "expected single bean but found " + beanNames.size() + ": " + beanNames);
   }
   ```

​		根据上面的分析可以得出，最核心的代码就是在`createBean`中引入了`applyBeanPostProcessorsBeforeApplyingPropertyValues`方法从而在bean的生命周期中引入了注解属性填充的功能，剩下的一些类的变化都是为了辅助注解属性填充功能的加入

### bean的创建和获取

​		根据上面的分析过程，下面使用**debug**来描述注解属性填充的流程，前提是本项目中有两个bean，其中`userService`依赖于`userDao`，依赖关系通过`Autowired`注解的形式注入，并且`userService`中还有一个使用`Value`注入的普通属性，xml配置文件中只配置了包扫描路径：

1. 加载配置文件，进入`doLoadBeanDefinitions`方法中读取xml配置文件中的内容

   ![image-20231113183441327](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311131910842.png)

2. 调用`scanPackage`中的`doScan`方法读取包扫描路径下的类

   ![image-20231113183558520](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311131910843.png)

3. 包扫描完毕之后，将注解属性填充模块的核心类`AutowiredAnnotationBeanPostProcessor`手动存入注册表中，便于后期按照类型取出这个模块执行注解填充属性的功能：

   ![image-20231113183850762](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311131910844.png)

4. 正常扫描xml文件中单独配置的bean，这里会扫描到字符串处理的逻辑，将其保存到注册表中：

   ![image-20231113184041628](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311131910845.png)

5. `invokeBeanFactoryPostProcessors`方法中触发实例化之前的修改逻辑，这里尝试替换bean属性中所有的占位符，针对每一个bean的每一个属性都尝试替换占位符：

   ![image-20231113184318404](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311131910846.png)

6. 所有的属性替换完毕之后，保存一个字符串处理器，便于后面`@Value`注解执行属性注入的时候使用：

   ![image-20231113184545140](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311131910847.png)

7. 保存实例化后的修改逻辑到容器中，这里主要是保存`AutowiredAnnotationBeanPostProcessor`核心类到容器中，然后在合适的时机触发：

   ![image-20231113184743779](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311131910848.png)

8. 实例化所有的bean，在这里增加一步注解填充属性的步骤，以`userService`为例，一共经历下面几步：

   1. 空bean的创建：

      ![image-20231113185145096](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311131910849.png)

   2. 触发之前保存的按照注解填充属性：

      ![image-20231113185350259](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311131910850.png)

   3. 调用之前保存的字符串处理器处理`@Value`注解的属性填充：

      ![image-20231113190114695](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311131910852.png)

   4. 调用`getBean`方法处理`@Autowired`注解的属性填充：

      ![image-20231113190211328](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311131910853.png)

9. 执行后面的步骤，得到最终的bean，执行相关的业务，最终的结果为：

   ![image-20231113185758180](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311131910854.png)

## 总结

​		本节中引入了注解填充属性的功能，引入时机在空bean创建之后，正常方式属性填充之前。为了实现注解填充属性的功能，核心类是实现了`InstantiationAwareBeanPostProcessor`接口的`AutowiredAnnotationBeanPostProcessor`类，内部根据使用的注解进行属性填充，针对普通属性注入时使用的占位符设计了字符串处理器来替换，针对bean属性新增了一个只按照类型获取bean对象的方法，最终实现了注解填充属性的功能
