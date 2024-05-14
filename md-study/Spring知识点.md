
# 1.简单的SpringIOC解决循环依赖的流程图
- 原理： 将注入的对象放入缓存中，将堆内存的物理地址暴露出去，循环引入对象直接获取地址即可。
- 如果是构造器方式注入，这不支持循环引入，直接报错。

![](https://img2020.cnblogs.com/blog/1694759/202108/1694759-20210821142210057-1202748366.png)



# 2.说说动态代理的实现方式和区别

动态代理是一种在运行时创建代理对象的方式，它可以在不修改原始类的情况下，为其添加额外的功能。在Java中，有两种常见的动态代理实现方式：基于接口的动态代理和基于类的动态代理。

1. **基于接口的动态代理** ：

* **实现方式** ：基于接口的动态代理是通过Java的`java.lang.reflect.Proxy`类实现的。该类提供了一个`newProxyInstance()`方法，通过传入目标类的接口、一个`InvocationHandler`对象和类加载器来创建代理对象。
* **实现原理** ：在运行时，当调用代理对象的方法时，实际上会被转发到`InvocationHandler`对象的`invoke()`方法中。在`invoke()`方法中，可以执行一些前置或后置操作，并最终调用目标对象的方法。
* **适用场景** ：基于接口的动态代理适用于目标对象实现了接口的情况。
* **基于类的动态代理** ：

* **实现方式** ：基于类的动态代理是通过使用第三方库（如CGLIB）来实现的。该库通过生成目标类的子类来创建代理对象。
* **实现原理** ：在运行时，当调用代理对象的方法时，实际上会被转发到子类中重写的方法中。在重写的方法中，可以执行一些前置或后置操作，并最终调用目标对象的方法。
* **适用场景** ：基于类的动态代理适用于目标对象没有实现接口的情况。

区别：

* 基于接口的动态代理要求目标对象实现接口，而基于类的动态代理可以代理任何类，无论是否实现接口。
* 基于接口的动态代理是通过Java标准库实现的，而基于类的动态代理需要使用第三方库。
* 基于接口的动态代理创建的代理对象是一个实现了目标接口的实例，而基于类的动态代理创建的代理对象是目标类的子类。
* 基于接口的动态代理执行效率相对较高，而基于类的动态代理执行效率较低。

总体而言，基于接口的动态代理更加灵活，并且是Java官方支持的方式；而基于类的动态代理在某些场景下更加方便，尤其是对于没有实现接口的类。




# 20.SpringBoot：注解@ConditionalOnClass(X.class),X不存在时 会不会报错？

- @ConditionalOnClass通常与@Configuration 结合使用，意思是当classpath中存在某类时满足条件。
- 第三方jar包中maven依赖是有对应X的jar的，只是POM文件是<optional>true</optional>不依赖传递。第三方jar都是编译好的。项目启动不回编译错误。
- 当我们项目中自定义的，如果没有对应的X.class编译会直接报错的。
- 我们也可以不引用 对应的X.Class jar包。直接用 @ConditionalOnClass(name=包路径.类名)的方式

# 21.Tomcat连接数相关配置

```
       server:
         tomcat:
          # 等待队列数,默认100
          accept-count: 100
          #最大链接数,默认10000。当为-1 不限制连接数 (AbstractEndpoint.class)
          max-connections: 10000
          threads:
            #最大线程数，默认200。一般根据CPU来的，1核≈200个。4核8G，可以配置为800
            max: 200
            #最小工作空闲线程数 默认10
            min-spare: 10
    
```
+ tomcat 最大可接受请求数 = 最大连接数(max-connections) + 等待队列数(accept-count). 当超过后，再来新的请求就会被tomcat拒绝（connection refused）。
+ Connection refused 表示服务器明确拒绝了连接，通常是因为没有应用程序在监听。
+ Connection timeout 表示客户端没有收到服务器的响应，可能是因为网络问题或服务器繁忙


# 22.SpringBoot的 jar 可以直接运行的原理
- **结构**: Spring Boot的JAR文件不是传统的单一JAR，而是采用了所谓的“胖JAR”或“自包含JAR”结构。这种JAR内部包含了一个BOOT-INF目录与相关所有的依赖内容。
  ![](https://img2024.cnblogs.com/blog/1694759/202405/1694759-20240513181546440-886139466.png)

- **嵌入式服务器**: Spring Boot默认使用嵌入式的Servlet容器，如Tomcat，这使得应用不需要额外部署到独立的服务器上即可运行。

- **MANIFEST.MF**：
    - 包含Start-Class属性，指明应用的主启动类，通常是带有@SpringBootApplication注解的类。
    - 包含Main-Class属性，指定启动器类（如org.springframework.boot.loader.JarLauncher或org.springframework.boot.loader.WarLauncher），这个类负责解析JAR结构，加载应用的类和资源，然后执行Start-Class指定的主类。

![](https://img2024.cnblogs.com/blog/1694759/202405/1694759-20240513181929237-1016757966.png)

- **类加载器**: Spring Boot使用自定义的类加载器（如LaunchedURLClassLoader）来处理JAR内的类加载，以便正确地隔离应用的类与依赖库的类。BOOT-INF/classes下的应用类优先于BOOT-INF/lib中的依赖库类加载。
