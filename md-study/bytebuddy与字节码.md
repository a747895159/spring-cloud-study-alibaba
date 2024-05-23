
[TOC]



skywalking大神分析： https://blog.csdn.net/qq_40378034/article/details/121882943



# 1.bytebuddy 字节码增强原理,  skywalking 底层使用的
https://blog.csdn.net/wanxiaoderen/article/details/107079741?spm=1001.2014.3001.5501

本身就是 java 命令的一个参数（即 -javaagent）。-javaagent 参数之后需要指定一个 jar 包，这个 jar 包需要同时满足下面两个条件：
在 META-INF 目录下的 MANIFEST.MF 文件中必须指定 premain-class 配置项。
premain-class 配置项指定的类必须提供了 premain() 方法。
Premain-class: main方法执行前执行的agent类.Agent-class: 程序启动后执行的agent类.Can-Redefine-Classes: agent是否具有redifine类能力的开关，true表示可以，false表示不可以.
Can-Retransform-Classes: agent是否具有retransform类能力的开关，true表示可以，false表示不可以.Can-Set-Native-Method-Prefix: agent是否具有生成本地方法前缀能力的开关，trie表示可以，false表示不可以.
Boot-Class-Path: 此路径会被加入到BootstrapClassLoader的搜索路径.

    <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>2.4.3</version>
            <configuration>             <!-- 自动将所有不使用的类排除-->
                <minimizeJar>true</minimizeJar>
            </configuration>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                    <configuration>
                        <shadedArtifactAttached>true</shadedArtifactAttached>
                        <shadedClassifierName>biz</shadedClassifierName>
                    </configuration>
                </execution>
            </executions>
        </plugin>


在 Java 虚拟机启动时，执行 main() 函数之前，虚拟机会先找到 -javaagent 命令指定 jar 包，然后执行 premain-class 中的 premain() 方法。用一句概括其功能的话就是：main() 函数之前的一个拦截器。



rebasing：对应 ByteBuddy.rebasing() 方法。当使用 rebasing 方式增强一个类时，Byte Buddy 保存目标类中所有方法的实现，也就是说，当 Byte Buddy 遇到冲突的字段或方法时，会将原来的字段或方法实现复制到具有兼容签名的重新命名的私有方法中，而不会抛弃这些字段和方法实现。从而达到不丢失实现的目的。
redefinition：对应 ByteBuddy.redefine() 方法。当重定义一个类时，Byte Buddy 可以对一个已有的类添加属性和方法，或者删除已经存在的方法实现。如果使用其他的方法实现替换已经存在的方法实现，则原来存在的方法实现就会消失。



# 2.Java代码生成库
- Java Proxy: JDK自带的一个代理工具，它允许为实现一系列接口的类代理类。要求目标类必须实现一个接口。
- CGLIB: 早起Java类库，也是强大的库,很复杂，后续维护类度差。基于ASM的字节码二次封装类库。
- Javassist :使用对开发者来说非常友好的,它使用Java源代码字符串和Javassist提供的简单API，共同拼凑出用户的Java类。Javassist自带编译器,性能上比不上Javac。在动态组合字符串以实现比较复杂的逻辑容器出错。
  是一个底层字节码的修改工具,性能上比不上AMS,比ASM易于上手。
- Byte Buddy : 提供一种非常灵活且强大的领域特定语言,通过编写简单的Java代码即可创建自定义的运行时类。是一种基于切面方式实现类的行为增强,基于ASM的字节码二次封装类库。
    - subclass：对应 ByteBuddy.subclass() 方法, 生成目标类的子类。在子类方法中插入动态代码。
    - rebasing：对应 ByteBuddy.rebase() 方法。当使用 rebasing 方式增强一个类时，Byte Buddy 保存目标类中所有方法的实现，也就是说，当 Byte Buddy 遇到冲突的字段或方法时，会将原来的字段或方法实现复制到具有兼容签名的重新命名的私有方法中，而不会抛弃这些字段和方法实现。从而达到不丢失实现的目的。
    - redefinition：对应 ByteBuddy.redefine() 方法。当重定义一个类时，Byte Buddy 可以对一个已有的类添加属性和方法，或者删除已经存在的方法实现。如果使用其他的方法实现替换已经存在的方法实现，则原来存在的方法实现就会消失。
    - 应用场景：AOP切面、类代理、监控(SkyWalking)

- ASM : 基于字节码编程的方式处理，每一步代码操作都是操作字节码指令，上手难度大，实现方式复杂，需要了解JVM虚拟机相关规范知识。
    - 性能指数最高,应用于 全链路监控、破解工具包、Spring获取类原数据、CGLIB底层.


# 3. Java Agent介绍

java agent是基于JVMTI机制实现JVM启动时与运行时加载代理。从Jdk5开始引入的

JVMTI全称JVM Tool Interface，是JVM暴露出来的一些供用户扩展的接口集合，JVMTI是基于事件驱动的，也就是JVM每执行到一定的逻辑时就会调用一些事件的回调接口(如果回调接口存在)，这些接口就可以被开发者扩展自己的逻辑。


- 启动时加载实现方式,JVM将首先寻找方法[1]，如果没有发现方法[1]，再寻找方法[2]。

```
public static void premain(String agentArgs, Instrumentation inst); 
public static void premain(String agentArgs);
```

启动时修改主要是在jvm启动时，执行native函数的Agent_OnLoad方法，在方法执行时，执行如下步骤：：

• 创建InstrumentationImpl对象
• 监听ClassFileLoadHook事件
• 调用InstrumentationImpl的loadClassAndCallPremain方法，在这个方法里会去调用javaagent里MANIFEST.MF里指定的Premain-Class类的premain方法

![](https://img2022.cnblogs.com/blog/1694759/202206/1694759-20220608160012865-793445777.png)


- 运行时加载Attach机制(阿里Arthas、VisualVM、JProfile)：从Jdk6开始引入的，jvm提供一种jvm进程pid间通信的能力，能让一个进程传命令给另外一个进程，并让它执行内部的一些操作。查看应用 load、内存、gc、线程的状态信息，并能在不修改应用代码的情况下，对业务问题进行诊断，包括查看方法调用的出入参、异常，监测方法执行耗时，类加载信息等，大大提升线上问题排查效率。

```
public static void agentmain(String agentArgs, Instrumentation inst); 
public static void agentmain(String agentArgs);
```

JPLISAgent：全称为Java programming Language Instrumatation Service Agent。

![](https://img2022.cnblogs.com/blog/1694759/202206/1694759-20220609113853092-780241676.png)

![](https://img2022.cnblogs.com/blog/1694759/202206/1694759-20220616123708521-892655234.png)



# 4.bytebuddy介绍

![](https://img2022.cnblogs.com/blog/1694759/202205/1694759-20220511175517832-800511668.png)


所有的操作依赖DynamicType.Builder进行,创建不可变的对象

* subclass 接受目标类，返回继承目标类的子类
* redefine
  重定义一个类时，Byte Buddy 可以对一个已有的类添加属性和方法，或者删除已经存在的方法实现。新添加的方法，如果签名和原有方法一致，则原有方法会消失。
* rebase
  类似于redefine，但是原有的方法不会消失，而是被重命名，添加后缀 $original，这样，就没有实现会被丢失。重定义的方法可以继续通过它们重命名过的名称调用原来的方法


## ElementMatchers(ElementMatcher)

* 提供一系列的元素匹配的工具类(named/any/nameEndsWith等等)
* ElementMatcher(提供对类型、方法、字段、注解进行matches的方式,类似于Predicate)
* Junction对多个ElementMatcher进行了and/or操作

## DynamicType(动态类型,所有字节码操作的开始,非常值得关注)

* Unloaded(动态创建的字节码还未加载进入到虚拟机,需要类加载器进行加载)

* Loaded(已加载到jvm中后,解析出Class表示)

* Default(DynamicType的默认实现,完成相关实际操作)

  bytebudyy提供了四种有关classLoader的行为 working with Unloaded，(Wrapper,child-first，injection)
  Unloaded只是说明，bytebuddy可以使用没有加载进内存里面的类。
  Wrapper，child-first，injection是说明将一个定义好的类加载进内存的策略



* FixedValue(方法调用返回固定值、对象的引用)

* MethodDelegation(方法调用委托,支持两种方式: Class的static方法调用、object的instance method方法调用)

Default Method   jdk8以后支持接口设置default方法。这里是设置default方法的范式。

![](https://img2022.cnblogs.com/blog/1694759/202205/1694759-20220511192243569-812420468.png)



Specific method 一些特殊的使用。一些特殊使用中现有方法不满足是，可以直接使用MethodCall方法

![](https://img2022.cnblogs.com/blog/1694759/202205/1694759-20220511200754467-2099637737.png)



AccessingField 就是属性的的set和get方法，为类构造这两个方法



![](https://img2022.cnblogs.com/blog/1694759/202205/1694759-20220511200827351-1714163259.png)



## Builder(用于创建DynamicType,相关接口以及实现后续待详解)

* MethodDefinition
* FieldDefinition
* AbstractBase

## 常用注解说明

| 注解          | 说明                                                         |
| ------------- | ------------------------------------------------------------ |
| @Argument     | 绑定单个参数                                                 |
| @AllArguments | 绑定所有参数的数组                                           |
| @This         | 当前被拦截的、动态生成的那个对象                             |
| @Super        | 当前被拦截的、动态生成的那个对象的父类对象                   |
| @Origin       | 可以绑定到以下类型的参数：Method 被调用的原始方法 Constructor 被调用的原始构造器 Class 当前动态创建的类 MethodHandle MethodType Field 拦截的字段 |
| @DefaultCall  | 调用默认方法而非super的方法                                  |
| @SuperCall    | 用于调用父类版本的方法                                       |
| @Super        | 注入父类型对象，可以是接口，从而调用它的任何方法             |
| @RuntimeType  | 可以用在返回值、参数上，提示ByteBuddy禁用严格的类型检查      |
| @Empty        | 注入参数的类型的默认值                                       |
| @StubValue    | 注入一个存根值。对于返回引用、void的方法，注入null；对于返回原始类型的方法，注入0 |
| @FieldValue   | 注入被拦截对象的一个字段的值                                 |
| @Morph        | 类似于@SuperCall，但是允许指定调用参数                       |


## ClassLoadingStrategy(类加载器策略)

如果不指定ClassLoadingStrategy，Byte Buffer根据你提供的ClassLoader来推导出一个策略，内置的策略定义在枚举ClassLoadingStrategy.Default中

+ WRAPPER：创建一个新的Wrapping类加载器
  默认的加载策略。新建当前classLoader的子classLoader。使用子classLoader来加载新定义的类。
  好处是被加载的类可以看见所有父classloader的类。
  缺点新的 ClassLoader，意味着新的namespace。这意味着可以加载两个具有相同名称的类，只要这些类由两个不同的类加载器加载即可。这样，即使两个类都代表相同的类实现，Java虚拟机也不会将这两个类视为相等。
  这意味着，如果两个类都未使用同一类加载器加载，则该类example.Foo将无法访问另一个类的程序包私有方法example.Bar。
  同样，如果example.Bar扩展了example.Foo，任何覆盖的包级别私有方法将不起作用，但将委派给原始实现。


+ CHILD_FIRST：类似上面，但是子加载器优先负责加载目标类
  和warpper类似，也是新建classLoader。wrapper的缺点是，类的寻找往往是委派给父classLoader，如果父classloader由同名类。那么定义的类永远不被被加载。
  所有child-first就是直接去加载。破坏了双亲委派。

+ INJECTION：利用反射机制注入动态类型
  利用反射，利用反射调用classloader的加载器去直接加载构造好的类型，而不用经过find & load过程。
  wrpper和child-first的好处是，我们加载的类都会记录在manifest文件里，这个文件就是生成的jar包中，包含的class信息。使用inject不会在manifest生成信息。
  有时加载想要获取已经被加载类的字节码，记录在manifest中的类可以通过ClassLoader::getResourceAsStream读取。但是inject是用反射注入的，就没办法取到。






![](https://img2022.cnblogs.com/blog/1694759/202205/1694759-20220511175956398-2086644241.png)



## AOP

byebyte 可以实现aop的功能。在前面的讲解中，没有提到过ASM，这部分也是前面那些api的底层。

其中源码中Adivce中封装的注解可以用来实现AOP

![](https://img2022.cnblogs.com/blog/1694759/202205/1694759-20220511193119513-908949565.png)




###  @Advice.OnMethodEnter

被标注此注解的方法会被织入到被bytebudy增强的方法前调用

1.  **Advice.Argument**
    用来获取 被执行对象的field或者参数
2.  **Advice.This**
    是当前advice对象的载体，标注之后，方法内部可以获取目前advice对象。

###  @ Advice.OnMethodExit

1.  **Advice.Argument**
    用来获取 被执行对象的field或者参数
2.  **Advice.This**
    是当前advice对象的载体，标注之后，方法内部可以获取目前advice对象。
3.  **Advice.Return**
    给一个参数标识Advice.Return 后可以接收返回结果。
4.  **Advice.Thrown**
    给一个参数标识`Advice.Thrown` 后可以接收抛出的异常。如果方法引发异常，则该方法将提前终止。
    如果由`Advice.OnMethodEnter`注释的方法引发异常，则不会调用由`Advice.OnMethodExit`方法注释的方法。如果检测的方法引发异常，则仅当`Advice.OnMethodExit.onThrowable()`属性设置为true（默认值）时，才调用由`Advice.OnMethodExit`注释的方法。





# 5.bytebuddy注解列表



## 一、 注解列表

| 注解             |                                                              | 值                               |                                                              |
| :--------------- | :----------------------------------------------------------- | :------------------------------- | :----------------------------------------------------------- |
| `@OnMethodEnter` | 表示这个方法会在，进入目标方法时调用，这个注解声明的方法必须是`static`。当目标的方法是`constructor`构造器时，`@This`只能写`field`，不能读field，或者调用方法 | skipOn\(\)                       | 跳过一些方法                                                 |
|                  |                                                              | prependLineNumber\(\)            | 如果为true，会改目标方法的行号                               |
|                  |                                                              | inline\(\)                       | 标识方法应该被内联到目标的方法中去                           |
|                  |                                                              | suppress\(\)                     | 忽略某些异常                                                 |
| `@OnMethodExit`  | 表示这个方法会在，目标方法结束时调用，这个注解声明的方法必须是`static`。如果方法提前终止了，那么这个就不i呗调用 | repeatOn\(\)                     | 标识目标方法是否被重复执行                                   |
|                  |                                                              | onThrowable\(\)                  | 一般被织入的方法抛出了某些异常，可以有响应的`handler`处理    |
|                  |                                                              | backupArguments\(\)              | 备份所有被执行方法的类型，开始会影响效率                     |
|                  |                                                              | inline\(\)                       | 标识方法应该被内联到目标的方法中去                           |
|                  |                                                              | suppress\(\)                     | 忽略某些异常                                                 |
| `@This`          | 表示被注解的参数，应该是被修改对象的引用，`不能用在静态方法和构造器上` | optional\(\)                     | 决定被修改的类型是否需要被设置为null，如果目标方法类型是`static`或者在一个`constructor` |
|                  |                                                              | readOnly\(\)                     | 只读不能修改                                                 |
|                  |                                                              | typing\(\)                       | 类型转化，默认要求静态转化，就是转化不能改变类型。动态是可以改变 |
| `@Argument`      | 被标注到目标类型的参数上，表示被标注的参数会被value\(\)代表的索引拿到 |                                  |                                                              |
|                  |                                                              | readOnly\(\)                     | 只读                                                         |
|                  |                                                              | typing\(\)                       | 转换这个类型使用的方式，默认是静态转换\(类型不会被改动\)，动态转换是void.class可以被改成其他类 |
|                  |                                                              | optional\(\)                     | 备选值，如果索引不存在，会使用这个提供的值。默认是关闭的     |
| `@AllArguments`  | 使用一个数组来包含目标方法的参数，目标的参数必须是一个数组。 |                                  |                                                              |
|                  |                                                              | readOnly\(\)                     | 只读                                                         |
|                  |                                                              | typing\(\)                       | 类型开关                                                     |
| `@Return`        | 标注在参数上，来引用目标方法的返回值                         | readOnly\(\)                     | 只读                                                         |
|                  |                                                              | `typing()`                       | 类型转化，默认是静态转换                                     |
| `@Thrown`        | 获取抛出的异常                                               |                                  |                                                              |
|                  |                                                              | readOnly\(\)                     | 只读                                                         |
|                  |                                                              | typing\(\)                       | 默认是动态转化，可以改变类型                                 |
| `@FieldValue`    | 被注解的参数，可以引用，目标method内的定义的局部变量         | String value\(\)                 | 局部变量的名称                                               |
|                  |                                                              | declaringType\(\)                | 被声明的类型                                                 |
|                  |                                                              | readOnly\(\)                     | 只读的                                                       |
|                  |                                                              | typing\(\)                       | 默认是静态转化                                               |
| `@Origin`        | 使用一个String代表目标方法的                                 | String value\(\) default DEFAULT | 默认值是`""`                                                 |
| `@Enter`         | 标注在参数上，指向被标注`@OnMethodEnter`的advice方法的返回值， |                                  |                                                              |
|                  |                                                              | readOnly\(\)                     | 只读标记                                                     |
|                  |                                                              | typing\(\)                       | 转换                                                         |
| `@Exit`          | 标注在参数上，指向被标注`@OnMethodExit`的advice方法的返回值， |                                  |                                                              |
|                  |                                                              | readOnly\(\)                     | 只读标记                                                     |
|                  |                                                              | typing\(\)                       | 转换                                                         |
| `@Local`         | 声明被注解的参数当做一个本地变量，被`Byte Buddy`，织入目标方法中。本地变量可以被`@OnMethodEnter` 和 `@link OnMethodExit`读写。然而如果本地变量被exit advice 引用了，它必须也在enter 的advice所声明。就是用来交换变量的。 | String value\(\)                 | name                                                         |
| `@StubValue`     | mock值，总是返回一个设定的值                                 |                                  |                                                              |
| `@Unused`        | 让被标记的参数，总是返回默认值，比如int 返回0， 其他类型返回null |                                  |                                                              |







![](https://img2022.cnblogs.com/blog/1694759/202205/1694759-20220511203232474-452644162.png)









## 参考引用

* java.lang.instrument解析  https://blog.csdn.net/wanxiaoderen/article/details/106544131
* 字节码学习指南  https://blog.csdn.net/wanxiaoderen/article/details/106517812
*
