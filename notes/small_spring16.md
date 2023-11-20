---
title: "Small_spring16"
description: "small_spring16"
keywords: "small_spring16"

date: 2023-11-19T13:23:39+08:00
lastmod: 2023-11-19T13:23:39+08:00

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
#url: "small_spring16.html"


# 开启各种图渲染，如流程图、时序图、类图等
# Enable chart render, such as: flow, sequence, classes etc
#mermaid: true
---

>🍖 small_spring16

在之前的章节中，我们实现了`spring`中最重要的`IOC`和`AOP`的相关核心功能，并且利用三级缓存解决了循环依赖的问题，但是在属性填充的过程中，一直都是直接将字符串传递给bean中对应的属性，这会导致一些类型转换的问题，并且并不是所有属性都是`string`的类型，所以本节中我们将会解决bean属性填充中类型转换的问题，以`applyPropertyValues`方法为入口，相关代码我放到了[仓库](https://github.com/zzziCode/small-spring)中

<!--more-->

## 原因

​		在给bean进行属性填充时，之前的操作都是填充字符串，而这不符合真实情况，真正的bean内部的属性多种多样，而配置时一般都配置的是`String`类型的变量，这就需要将`String`类型的属性值转换成目标类型的属性值，于是提出了类型转换模块，并在现有的模块中引入类型转换的内容，实现属性值的转换

## 思路

为了引入类型转换服务，一共经历了下面几步：

1. 首先需要在xml文件中配置类型转化服务的相关模块，便于项目中能够感知到并且调用这些服务
2. 在`refresh`方法中执行完所有的方法，对所有的bean进行实例化的时候，此时先判断当前项目中是否配置了类型转换的服务，这里是按照名称判断，代表后面的项目在使用类型转换服务时，对应的类型转换服务的`beanName`都是统一的名称
3. 如果存在类型转换服务，那么就调用`getBean`方法创建并拿到这个类型转换服务的bean对象，如果这个bean对象内部还需要一些其他的属性，在创建过程中会一并填充
4. 类型转换服务的属性填充完毕之后，触发了这个bean的初始化方法`afterPropertiesSet`，内部创建了一个`DefaultConversionService`类型转换服务的对象，并且将xml配置文件中配置的类型转换器保存到这个类型转换服务的`converts`容器中便于后期使用
5. 之后将这个拿到的类型转换服务的对象保存到`AbstractBeanFactory`类中的`conversionService`中，后期属性填充时直接拿到这个类型转换服务中的类型转换器来进行类型转换，也就是拿到着之前保存到`DefaultConversionService`的`converts`中的类型转换器
6. 执行其余的正常bean的创建过程，重点在**属性填充**的过程中，下面以`husband`的创建为例介绍如何进行类型的转换：
   - 属性填充时拿到当前bean的所有需要填充的属性，如果当前属性依赖其他的bean，那么就调用`getBean`获取到这个所依赖的bean对象，然后进行属性填充
   - 如果当前属性是一个普通属性，那么就拿到这个属性的真正类型，也就是`targetType`
   - 根据拿到的`targetType`和`sourceType`来尝试找到匹配的类型转换器
   - 调用类型转换服务中封装的`convert`方法将当前的`value`从`sourceType`转换成`targetType`完成类型转换
   - 进行属性填充

7. 完成bean的创建，执行自己的业务逻辑

> 核心就是调用`DefaultConversionService`内部的一些方法实现类型的转换
>
> **类型转换服务工厂**中保存了**类型转换服务的**对象
>
> **类型转换器工厂**中保存了所有的类型转换器
>
> **类型转换服务工厂**的**初始化**方法触发时会创建一个**类型转换服务**的对象，然后将**类型转换器工厂**中的所有的**类型转换器**保存到这个**类型转换服务**中的`converts`容器中，后面调用这个容器中的类型转换器就可以完成对类型的转换

### 类的变化

#### 新增的类

1. `Converter`：类型转换器需要实现这个接口，具体的类型之间的转换逻辑在内部的`convert`方法中定义，例如可以将`String`类型的数据转换成`LocalDate`类型的数据

   ![image-20231119140934313](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310072.png)

2. `ConverterFactory`：实现这个接口的类可以根据其内部的`getConverter`方法得到一个类型转换器，相当于内部对类型转换器进行了封装，对外提供统一的接口：

   ![image-20231119141153157](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310076.png)

3. `ConverterRegistry`：可以将转换器注册保存到一个容器中，然后外部使用时直接调用容器中注册保存的转换器使用即可：

   ![image-20231119141405012](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310078.png)

4. `GenericConverter`：核心就是这个接口内部的`ConvertiblePair`类。保存了一个源类型和一个目标类型，后期拿这个类做保存转换器的容器的键，从而针对不同的类型组合有不同的键，内部还提供了一个`convert`方法，最后在使用转换器的功能时，都是调用这个`convert`方法，内部调用转换器的`convert`方法来完成类型的转换：

   ![image-20231119195814115](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310079.png)

5. `ConversionService`：实现这个接口需要实现两个方法，分别判断给定的两个类型之间是否可以转换，以及对外提供调用的`convert`接口，在这个`convert`接口中调用前面的`getConverter`方法得到转换器，然后再调用`GenericConverter`接口中的`convert`方法，内部进一步调用转换器的`convert`方法执行类型的转换，相当于对类型转换服务做了封装，一个`convert`方法经过了三次封装：

   ![image-20231119141845268](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310080.png)

6. `GenericConversionService`：类型转换服务的核心类，内部定义了一个容器，可以向其中保存所有的转换器，也可以从容器中拿到想要使用的转换器进行类型的转换。除了这个容器之外，还有一些辅助的方法可以保存类型转换器，获取类型转换器，触发类型转换器：

   ![image-20231119200942875](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310081.png)

7. `DefaultConversionService`继承了`GenericConversionService`类，内部指定默认的转换器是`String`转成`Number`的转换器：

   ![image-20231119201300911](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310082.png)

8. `ConvertersFactoryBean`：是一个`FactoryBean`的子类，内部调用`getObject`方法可以得到配置的所有的类型转换器，也就是说这个类中封装了所有的类型转换器，这里需要自己手动定义好类型转换器，然后在配置到这个类中：

   ![image-20231119201552500](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310083.png)

9. `ConversionServiceFactoryBean`：xml配置文件的入口，内部保存了一个通用的类型转换服务`GenericConversionService`的对象，在初始化方法中初始化这个对象，并且将其在xml文件中依赖的`ConvertersFactoryBean`中的所有类型转换器保存到类型转换服务`GenericConversionService`的对象中，相当于本类只是将类型转换器保存到类型转换服务中：

   ![image-20231119201719143](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310084.png)

10. `NumberUtils`：内部可以将`String`转换成指定的`Number`类型，一共有`Byte`, `Short`, `Integer`, `Long`, `BigInteger`, `Float`, `Double`, `BigDecimal`八种类型可以转换：

    ![image-20231119202025296](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310085.png)

11. `StringToNumberConverterFactory`：内部定义了一个将`String`转换成`Number`的转换器，主要是调用`NumberUtils`中的转换方法完成对类型的转换：

    ![image-20231119202109123](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310086.png)

12. `StringToLocalDateConverter`：内部定义了一个将`String`转换成`LocalDate`的转换器，这个类最终在类型转换器工厂中保存，最后在类型转换服务工厂中被保存到了类型转换服务中的`converts`的容器中便于后期使用：

    ![image-20231119202237544](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310087.png)

#### 修改的类

1. `ConfigurableBeanFactory`：新增了**获取**和**保存**类型转换服务的待实现方法：

   ![image-20231119203119689](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310088.png)

2. `BeanFactory`：新增一个待实现的`containsBean`方法，判断bean注册信息中有没有指定的bean注册信息：

   ![image-20231119203331610](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310089.png)

3. `AbstractBeanFactory`：新增了上面提到的三个方法的实现，并且新增一个`ConversionService`类型的变量存储xml中配置的类型转换服务：

   ![image-20231119203429918](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310090.png)

4. `AbstractApplicationContext`：修改`refresh`中实例化全部bean的方法，在其中添加从xml配置文件中获取类型转换服务并保存的功能，从原来的`preInstantiateSingletons`变成了现在的`finishBeanFactoryInitialization`：

   ```java
   protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
       // 设置类型转换器
       if (beanFactory.containsBean("conversionService")) {
           Object conversionService = beanFactory.getBean("conversionService");//拿到注册的类型转换服务
           if (conversionService instanceof ConversionService) {
               beanFactory.setConversionService((ConversionService) conversionService);
           }
       }
   
       // 提前实例化单例Bean对象
       beanFactory.preInstantiateSingletons();
   }
   ```

5. `AbstractAutowireCapableBeanFactory`：在`applyPropertyValues`方法中引入类型转换的功能，从之前保存的类型转换服务中判断是否可以转换，可以转换的话，调用对应的转换器从而实现类型转换的功能

   ```java
   protected void applyPropertyValues(String beanName, Object bean, BeanDefinition beanDefinition) {
       try {
           	//。。。
               // 类型转换，重点就是在这里
               else {
                   //需要什么类型，当前是什么类型
                   Class<?> sourceType = value.getClass();
                   //按照属性名从bean中拿到这个属性真正的类型是什么
                   Class<?> targetType = (Class<?>) TypeUtil.getFieldType(bean.getClass(), name);
                   ConversionService conversionService = getConversionService();//拿到刚才保存的类型转换服务
                   if (conversionService != null) {
                       if (conversionService.canConvert(sourceType, targetType)) {
                           //将填充的属性转换成真正需要的类型
                           //比如将String转换成int
                           value = conversionService.convert(value, targetType);
                       }
                   }
               }
   
               // 反射设置属性填充
               BeanUtil.setFieldValue(bean, name, value);
           }
       } catch (Exception e) {
           throw new BeansException("Error setting property values：" + beanName + " message：" + e);
       }
   }
   ```

   重点就是在普通属性填充时，新增了类型转换

​		经过上面类的新增和修改，顺利的在属性填充的过程中引入了类型转换的功能，xml文件中以两个工厂类为入口，一个是类型转换服务工厂，其中保存了一个类型转换服务的变量和一个容器，容器中保存了所有注册的类型转换器。在初始化方法中进·行初始化这个类型转换服务的变量，然后将容器中所有的类型转换器注册到类型转换服务中的`converts`容器中，后期统一调用类型转换服务中的`converts`获取到匹配的类型转换器使用即可

### bean的创建和获取

下面从bean的创建和获取的角度来说明项目中如何引入类型转换模块，项目中有一个`husband`的bean对象，内部有两个属性，其中`marriageDate`属性的类型是`LocalDate`，需要进行类型转换，为了引入类型转化模块，首先在xml文件中配置了类型转换服务工厂的bean以及类型转换器工厂的bean，下面介绍bean创建和获取的流程，重点介绍类型转换的过程：

1. 读取配置文件，得到xml文件中配置的bean的注册信息：

   ![image-20231120123936648](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310091.png)

2. 执行`refresh`方法中其他的方法，并实例化所有的bean对象，现在的实例化所有对象的方法被封装到了`finishBeanFactoryInitialization`方法中：

   ![image-20231120124045068](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310092.png)

3. 获取到类型转换服务工厂对象，一共经历下面几步：

   - 创建空的类型转换服务工厂对象：

     ![image-20231120124239983](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310093.png)

   - 将其保存到第三级缓存中，并且执行属性填充，主要是填充xml配置文件中指定所依赖的类型转换器工厂，类型转换器工厂中保存的是一个`String`转换成`LocalDate`的转换器：

     ![image-20231120124439759](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310094.png)

   - 执行初始化前的逻辑之后，执行初始化方法：

     ![image-20231120124519540](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310095.png)

   - 初始化方法内部先创建一个默认的类型转换服务对象：

     ![image-20231120124600833](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310096.png)

   - 创建默认类型转换服务对象会初始化当前类型转换服务工厂中的`conversionService`属性，而创建这个默认的类型转换服务对象调用其内部的无参构造，无参构造中向类型转换服务中的容器中保存了一个**默认的转换器**，是`String`转换成`Number`的转换器：

     ![image-20231120124819029](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310097.png)

   - 创建完默认的类型转换服务对象之后，调用`registerConverters`方法将当前工厂对象中的`converts`中从xml文件中读取到的所有类型转换器**保存到默认类型转换服务**中的`converts`：

     ![image-20231120125026077](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310098.png)

   - 完成初始化，执行初始化后的逻辑，尝试给当前的类型转换服务工厂对象进行`AOP`，并将其保存到单例池中，完成bean对象的创建：

     ![image-20231120125154693](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310099.png)

4. 将获取到的类型转换服务工厂对象保存到当前项目中，便于后期使用：

   ![image-20231120125333846](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310100.png)

5. 实例化所有的bean对象，主要是`husband`的实例化，一共经历下面几步：

   - 创建空bean：

     ![image-20231120125444795](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310101.png)

   - 将其保存到三级缓存中，然后对空bean进行**属性填充**，这是最重要的步骤，在这里引入类型转换的过程：

     ![image-20231120125557859](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310102.png)

   - 填充第一个属性，拿到的值本身是`String`，要被填充的属性需要的类型也是`String`，所以不需要类型转换，**直接填充**：

     ![image-20231120125735713](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310103.png)

   - 填充第二个属性，拿到的值本身是`String`，要被填充的属性需要的类型是`LocalDate`，所以需要进行类型转换：

     ![image-20231120125840106](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310104.png)

   - 拿到上面保存的类型转换服务，**先**判断两个属性能否转换，可以的话**再**根据内部的转换器进行转换，**最后**进行属性填充：

     ![image-20231120130120624](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310105.png)

6. 属性填充完成之后，执行后续的步骤，然后将其保存到单例池中，后续从单例池中拿到这个bean对象执行自己的业务逻辑，执行`toString`方法的结果如下，可以发现类型确实被转换成功：

   ![image-20231120130256224](https://zzzi-img-1313100942.cos.ap-beijing.myqcloud.com/img/202311201310106.png)

## 总结

在本节中我们引入了类型转换模块，改进了bean属性填充的功能，从而使得bean可以拥有各种类型 的数据，但是前提是这些类型转换器需要自己手动编写并且配置到类型转换器工厂中，之后就可以愉快的进行类型转换了，xml的入口类有两个，第一个是类型转换器工厂类，内部将所有的类型转换器注册到一个set中返回，第二个是类型转换服务工厂类，内部将上面的类型转换器工厂类注入，并且定义一个类型转换服务对象，从而将注入的类型转换器工厂中的所有类型转换器转移到类型转换服务的`converters`容器中保存，从而便于项目使用
