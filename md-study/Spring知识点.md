
https://blog.csdn.net/it_lihongmin/article/details/109027896?spm=1001.2014.3001.5501


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

# 3.SpringBean 生命周期
- 1.实例化：Spring IoC容器首先会实例化一个Bean。这通常是通过反射来完成的，Spring会根据配置（如XML或注解）中指定的Bean类型来创建相应的实例。
- 2.属性注入（依赖注入）：在Bean实例化后，Spring IoC容器会将其依赖项（其他Bean）注入到该Bean中。这可以通过setter方法、构造函数或字段注入来完成。
- 3.BeanNameAware接口（可选）：如果Bean实现了BeanNameAware接口，Spring IoC容器会调用其setBeanName方法，并传递该Bean在容器中的唯一名称。
- 4.BeanFactoryAware接口（可选）：如果Bean实现了BeanFactoryAware接口，Spring IoC容器会调用其setBeanFactory方法，并传递当前BeanFactory的实例，允许Bean获取其他Bean的引用。
- 5.ApplicationContextAware接口（可选，仅适用于ApplicationContext容器）：如果Bean实现了ApplicationContextAware接口，Spring IoC容器（实际上是一个ApplicationContext）会调用其setApplicationContext方法，并传递当前ApplicationContext的实例。
- 6.**初始化阶段**：
  - 如果实现BeanPostProcessor接口，先执行postProcessBeforeInitialization。
  - 如果Bean上有@PostConstruct注解的方法，这个方法会被执行。
  - 如果Bean实现了InitializingBean接口，调用afterPropertiesSet()方法。
  - 如果XML存在init-method配置，执行其相关方法。
  - 如果实现BeanPostProcessor接口，执行BeanPostProcessor的postProcessBeforeInitialization()方法。
- 7.使用Bean：此时Bean已经准备好并可以被应用程序的其他部分使用。
- 8.**销毁阶段**：销毁通常发生在ApplicationContext关闭时.
  - 如果Bean实现了DisposableBean接口，容器关闭时调用destroy()方法。
  - 如果Bean上有@PreDestroy注解的方法，这些方法会在容器关闭前执行。
  - 如果XML存在destroy-method配置，执行其相关方法。


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


# 41.FeignClient注解如何有对应实现类的？
+ 项目启动类或扫描的对应config上有 @EnableFeignClients 代表开启Feign。然后 @FeignClients注解的类才能被扫描注入到Spring容器。
+ @EnableFeignClients 注解上有 **@Import(FeignClientsRegistrar.class)**。将@FeignClients注解的类以FeignClientFactoryBean类型的BeanDifinotion注册到Ioc容器。
    + 对@EnableFeignClietns全家配置的解析，适用于所有的@FeignClient
    + 将@FeignClient注册成 FactoryBean类型的Bean
  ```
      @Override
      public void registerBeanDefinitions(AnnotationMetadata metadata,BeanDefinitionRegistry registry) {
          // 对@EnableFeignClietns全家配置的解析，适用于所有的@FeignClient
          registerDefaultConfiguration(metadata, registry);
          // 将@FeignClient注册成 FactoryBean类型的Bean
          registerFeignClients(metadata, registry);
      }
      
  ```

+ 2、FeignRibbonClientAutoConfiguration（先于 FeignAutoConfiguration）实现对应的底层Http线程池【httpclient、okhttp、默认类型】，以及超时参数等信息。spring-cloud-starter-openfeign基于Spring的 SPI自动启用加载

  ```
      // 1、ILoadBalancer和Feign类存在才加载该Bean FeignRibbonClientAutoConfiguration
      @ConditionalOnClass({ ILoadBalancer.class, Feign.class })
      @Configuration
      // 2、当前的FeignRibbonClientAutoConfiguration先于FeignAutoConfiguration加载
      @AutoConfigureBefore(FeignAutoConfiguration.class)
      // 3、加载配置FeignHttpClientProperties的属性
      @EnableConfigurationProperties({ FeignHttpClientProperties.class })
      // 4、有顺序的加载HttpClient、okhttp类型（前提【都】是引入了包和启动配置）、最后优先级是加载默认项，后面详细分析该部分
      @Import({ HttpClientFeignLoadBalancedConfiguration.class,
              OkHttpFeignLoadBalancedConfiguration.class,
              DefaultFeignLoadBalancedConfiguration.class })
      public class FeignRibbonClientAutoConfiguration {
      
          ...
      }
  ```

+ 3.FeignAutoConfiguration加载要晚于 FeignRibbonClientAutoConfiguration。这里装配了两个比较重要的Bean： HasFeatures和FeignContext，特别是FeignContext会在后面FeignClientFactoryBean#getObject中使用到。
    - 根据是否开启 feign.hystrix.enabled =true， 开启了，则使用 HystrixTargeter代理，否则使用DefaultTargeter。

  ```
      @Configuration
      @ConditionalOnClass(name = "feign.hystrix.HystrixFeign")
      protected static class HystrixFeignTargeterConfiguration {
          @Bean
          @ConditionalOnMissingBean
          public Targeter feignTargeter() {
              return new HystrixTargeter();
          }
      }

      @Configuration
      @ConditionalOnMissingClass("feign.hystrix.HystrixFeign")
      protected static class DefaultFeignTargeterConfiguration {
          @Bean
          @ConditionalOnMissingBean
          public Targeter feignTargeter() {
              return new DefaultTargeter();
          }
      }
  
  ```

+ 4.启动spring refresh方法最后会将单例非懒加载的所有Bean，包括FactoryBean类型（则包含了注入的FeignClientFactoryBean类型）

+ 5.FeignClientFactoryBean#getObject(getTarget())方法

    - 获取@FeignClient 上指定的 configuration(指定 编码 Encoder、解码 Decoder、Contract、RequestInterceptor、Request.Options、Retryer)
      ```
          protected Feign.Builder feign(FeignContext context) {
              FeignLoggerFactory loggerFactory = get(context, FeignLoggerFactory.class);
              Logger logger = loggerFactory.create(this.type);
           
              // @formatter:off
              Feign.Builder builder = get(context, Feign.Builder.class)
                      // required values
                      .logger(logger)
                      .encoder(get(context, Encoder.class))
                      .decoder(get(context, Decoder.class))
                      .contract(get(context, Contract.class));
              // @formatter:on
           
              configureFeign(context, builder);
           
              return builder;
          }
      ```
    - 配置全局的日志，加、解密器，验证器等，最后configureFeign如下，处理了链接超时、读取超时等配置项
      ```
          protected void configureFeign(FeignContext context, Feign.Builder builder) {
              FeignClientProperties properties = applicationContext.getBean(FeignClientProperties.class);
              if (properties != null) {
                  if (properties.isDefaultToProperties()) {
                      configureUsingConfiguration(context, builder);
                      configureUsingProperties(properties.getConfig().get(properties.getDefaultConfig()), builder);
                      configureUsingProperties(properties.getConfig().get(this.name), builder);
                  } else {
                      configureUsingProperties(properties.getConfig().get(properties.getDefaultConfig()), builder);
                      configureUsingProperties(properties.getConfig().get(this.name), builder);
                      configureUsingConfiguration(context, builder);
                  }
              } else {
                  configureUsingConfiguration(context, builder);
              }
          }
      ```
    - 根据Targeter（获取子类DefaultTargeter或HystrixTargeter），使用InvocationHandlerFactory工厂创建代理对象SynchronousMethodHandler





# 42.FeignClient方法调用，动态执行的过程
	当启用  feign.hystrix.enabled =true， 开启了，则使用 HystrixTargeter代理。

![](https://img2020.cnblogs.com/blog/1694759/202109/1694759-20210904132646251-1169218744.png)

- 创建HystrixCommand对象，内置了两个回调函数。Http请求的真正调用HystrixInvocationHandler.this.dispatch.get(method).invoke(args);
- getFallback方法，预制了我们配置的降级方法或降级方法工厂
- 再调用该对象的execute或toObservable().toCompletable()方法，执行请求。
  ![](https://img2020.cnblogs.com/blog/1694759/202109/1694759-20210904133012053-127059761.png)

- SynchronousMethodHandler#invoke：
    - 1、获取RequestTemplate类型的对象，为Ribbon的封装
    - 2、如果我们引入了spring-retry重试机制，那么根据Retryer对象在try finally创建重试代码
    - 3、executeAndDecode执行请求和编解码
        - 遍历所有的RequestInterceptor拦截器进行处理
        - 获取装饰器LoadBalancerFeignClient进行请求处理，真正的处理过程会交给我们根据优先级配置的 ApacheHttpClient、OkHttpClient或Client.Default。
    - 4、最后除了对编解码进行处理，还有http 404等请求状态作处理

  ```
      public Object invoke(Object[] argv) throws Throwable {
      //创建RestTemplate ,对请求参数进行 encode()
      RequestTemplate template = buildTemplateFromArgs.create(argv);
      Retryer retryer = this.retryer.clone();
      while (true) {
        try {
          return executeAndDecode(template);
        } catch (RetryableException e) {
          retryer.continueOrPropagate(e);
          if (logLevel != Logger.Level.NONE) {
            logger.logRetry(metadata.configKey(), logLevel);
          }
          continue;
        }
      }
    }

  ```

# 43.Feign Hystrix Ribbon 关系

![](https://img2020.cnblogs.com/blog/1694759/202109/1694759-20210904135305303-2123464886.png)

```
feign:
  #替换掉JDK默认HttpURLConnection实现的 Http Client
  httpclient:
    enabled: true
  hystrix:
    enabled: true
  client:
    config:
      default:
       #连接超时时间
        connectTimeout: 5000
       #读取超时时间
        readTimeout: 5000
```

- Hystrix主要被用于实现实现微服务之间网络调用故障的熔断、过载保护及资源隔离等功能

```
hystrix:
propagate:
  request-attribute:
    enabled: true
command:
  #全局默认配置, 单个服务 可以指定serviceId
  default:
    #线程隔离相关
    execution:
      timeout:
        #是否给方法执行设置超时时间，默认为true。一般我们不要改。
        enabled: true
      isolation:
        #配置请求隔离的方式，这里是默认的线程池方式。还有一种信号量的方式semaphore，使用比较少。
        strategy: threadPool
        thread:
          #方式执行的超时时间，默认为1000毫秒，在实际场景中需要根据情况设置
          timeoutInMilliseconds: 10000
          #发生超时时是否中断方法的执行，默认值为true。不要改。
          interruptOnTimeout: true
          #是否在方法执行被取消时中断方法，默认值为false。没有实际意义，默认就好！
          interruptOnCancel: false
    circuitBreaker:   #熔断器相关配置
      enabled: true   #是否启动熔断器，默认为true，false表示不要引入Hystrix。
      requestVolumeThreshold: 20     #启用熔断器功能窗口时间内的最小请求数，假设我们设置的窗口时间为10秒，
      sleepWindowInMilliseconds: 5000    #所以此配置的作用是指定熔断器打开后多长时间内允许一次请求尝试执行，官方默认配置为5秒。
      errorThresholdPercentage: 50   #窗口时间内超过50%的请求失败后就会打开熔断器将后续请求快速失败掉,默认配置为50
```

- Ribbon在功能包括客户端负载均衡器及用于中间层通信的客户端。全局配置：

```
ribbon:
    eager-load:
      enabled: true
    #说明：同一台实例的最大自动重试次数，默认为1次，不包括首次
    MaxAutoRetries: 1
    #说明：要重试的下一个实例的最大数量，默认为1，不包括第一次被调用的实例
    MaxAutoRetriesNextServer: 1
    #说明：是否所有的操作都重试，默认为true
    OkToRetryOnAllOperations: true
    #说明：从注册中心刷新服务器列表信息的时间间隔，默认为2000毫秒，即2秒
    ServerListRefreshInterval: 2000
    #说明：使用Apache HttpClient连接超时时间，单位为毫秒
    ConnectTimeout: 3000
    #说明：使用Apache HttpClient读取的超时时间，单位为毫秒
    ReadTimeout: 3000

```

- 单个服务配置 注册中心的服务名:ribbon...

```
wms-ibd-center:
    ribbon:
      OkToRetryOnAllOperations: true # 对所有操作请求都进行重试
      MaxAutoRetries: 2               # 对当前实例的重试次数
      MaxAutoRetriesNextServer: 0    # 切换实例的重试次数
      ConnectTimeout: 3000            # 请求连接的超时时间
      ReadTimeout: 3000               # 请求处理的超时时间

```

- 在Spring Cloud中使用Feign进行微服务调用分为两层：Hystrix的调用和Ribbon的调用，Feign自身的配置会被覆盖。
  而如果开启了Hystrix，那么Ribbon的超时时间配置与Hystrix的超时时间配置则存在依赖关系，因为涉及到Ribbon的重试机制，所以一般情况下都是Ribbon的超时时间小于Hystrix的超时时间

- Hystrix的超时时间=Ribbon的重试次数(包含首次) * (ribbon.ReadTimeout + ribbon.ConnectTimeout)

- Ribbon重试次数(包含首次)= 1 + ribbon.MaxAutoRetries  +  ribbon.MaxAutoRetriesNextServer  + (ribbon.MaxAutoRetries * ribbon.MaxAutoRetriesNextServer)
  Ribbon的重试次数=1+(1+1+1)=4，所以Hystrix的超时配置应该>=4*(3000+3000)=24000毫秒。在Ribbon超时但Hystrix没有超时的情况下，Ribbon便会采取重试机制；而重试期间如果时间超过了Hystrix的超时配置则会立即被熔断（fallback）。

- **如果不启用Hystrix，Feign的超时时间则是Ribbon的超时时间，Feign自身的配置也会被覆盖**。

# 44.Feign的原理
+ 以JAVA注解的方式定义的远程调用API接口(JDK代理类)，最终转换成HTTP的请求形式，然后将HTTP的请求的响应结果，解码成JAVA Bean，放回给调用者。

![](https://img2020.cnblogs.com/blog/1694759/202109/1694759-20210906174823643-1905489410.png)

+ @EnableFeignClients 注解上有 **@Import(FeignClientsRegistrar.class)**。将@FeignClients注解的类以FeignClientFactoryBean类型的BeanDifinotion注册到Ioc容器(注入的FeignClientFactoryBean类型)
+ 反射InvocationHandler 核心代理接口, Feign提供一个默认的 **FeignInvocationHandler** 类，该类处于 feign-core 核心jar包中。当起启动Hystrix时，会使用 **HystrixInvocationHandler**。
+ 从代理类中取到方法对应的MethodHandler方法处理器去执行.Feign提供了默认 SynchronousMethodHandler 实现类
+ SynchronousMethodHandler.invoke 方法 会对 请求编码,根据注入的Client实现类 去执行和解码。
    - LoadBalancerFeignClient 类：内部使用 Ribben 负载均衡技术完成URL请求处理的feign.Client 客户端实现类。使用了delegate包装代理模式。
      ![](https://img2020.cnblogs.com/blog/1694759/202109/1694759-20210906180338353-605563650.png)


![](https://img2020.cnblogs.com/blog/1694759/202109/1694759-20210906180604651-82914352.png)


# 45.如何开启日志增强？
- Feign的日志争强是对Http请求打印详细信息的,有4个级别：
    - NONE(默认、不打印任何日志)、
    - BASIC(仅记录请求方法、URL、响应状态码及执行时间)
    - HEADERS(除了BASIC中定义的信息之外，还有请求和响应的头信息)
    - FULL(除了HEADERS中定义的信息之外，还有请求和响应的正文及元数据)
- 1.先可以代码中全局配置：
    ```
        @Bean
        public Logger.Level feignLevel(){
            retun Logger.Level.FULL;
        }
    ```
- 2.yaml中设置指定包的接口日志级别，cn.myjszl.service这是个包名,也可以指定特定的接口
    ```
        logging:
          level:
            cn.myjszl.service: debug
    ```
# 46.feign接口指定配置
- 通过 @FeignClient 注解中属性 configuration 配置指定编码器（Encoder）、解码器（Decoder）、契约(接口方法映射Http路径)（Contract）、请求拦截器（RequestInterceptor）、请求选项（Request.Options）和重试器（Retryer），作用域仅限当前单例













