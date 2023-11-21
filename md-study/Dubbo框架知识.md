[TOC]

# 1.RPC的理解

+ 含义：两台服务器间数据传输通信，客户端将方法、参数组装成能够在网络传输的消息体(序列化)，服务端将消息体反序列化转换成本地调用的方法与参数，然后再将执行结果序列化发送给消费方。
+ 序列化方式：JDK自带的序列化、hessian2、JSON、Kryo、FST、Protostuff，ProtoBuf等
+ RPC框架：Dubbo、Java RMI(Java 远程方法调用)、gRPC、thrift、SpringCloud

# 2.什么是Dubbo

- 是一款高性能、轻量级的开源 Java RPC 框架。
	面向接口代理的高性能RPC调用。
	智能容错和负载均衡。
	服务自动注册和发现。
	高度可扩展能力。
	运行期流量调度。
	可视化的服务治理与运维。
	
- 默认使用的序列化方式是 hession2。代码无侵入(Dubbo 基于Spring 的 Schema 扩展进行加载)，基于SPI 机制的实现。

- 提供各种负载均衡策略：权重随机选择(默认的)、最小活跃数(处理越快、性能越好、活跃数越少)、一致性Hash负载、加权轮询负载。

- 容错方案：Failover 失败自动切换(默认的)、Failfast 快速失败、Failsafe 失败安全 、Failback 失败自动恢复(定时重发)、Forking 并行调用(只要一个成功即可)、 Broadcast 广播调用(任意一台报错则报错)

  

- ![](https://img2020.cnblogs.com/blog/1694759/202108/1694759-20210804202854642-1643299790.png)  

  

  # 3.Dubbo 与 Spring Cloud 区别

  + dubbo 仅是一个RPC框架。而SpringCloud集成了RPC、注册中心Eureka、负载均衡Ribbon、服务网关、分布式配置、限流Hystrix、消息总线、链路追踪等，目标是微服务架构的一站式解决方案。dubbo也就相当于OpenFeign（声明式、模板化的HTTP客户端）+ 负载均衡Ribbon  。
  + dubbo使用Netty下NIO框架，基于TCP协议传输，采用Hession2 序列化 的。RPC性能上更高。
  + SpringCloud基于Rest风格的Http协议，采用Json序列化的。方式更灵活，服务方与调用方不存在强依赖。

![](https://img2020.cnblogs.com/blog/1694759/202108/1694759-20210804203947350-710517069.png)



# 4.Dubbo的SPI扩展

+ SPI(Service Provider Interface)服务提供商接口，**是一种动态替换发现服务实现者的机制**。 JDK 为SPI提供了工具类 java.util.ServiceLoader，指定加载**resource目录`META-INF/services`下，文件名就是服务接口的全限定名**。
  + 缺点：ServiceLoader也算是使用的延迟加载。但是通过遍历获取，接口的实现类全部实例化一遍，不灵活浪费。
  + 优点：不需要改动源代码可以实现扩展，解耦，对源代码无侵入。只要添加配置即可实现，符合开闭原则。
  + 举例：jdbc的 Driver 驱动。
+ Dubbo SPI对JDK SPI进行了扩展，**由原来的提供者类的全限定名列表改成了KV形式的列表，这也导致了Dubbo中无法直接使用JDK ServiceLoader**，所以，在Dubbo中有**ExtensionLoader是扩展点载入器，用于载入Dubbo中的各种可配置组件。Dubbo默认依次扫描\**`META-INF/dubbo/internal/、META-INF/dubbo/、META-INF/services/`三个classpath目录下的配置文件\**。\**配置文件以具体扩展接口全名命名。\****

![](https://img2020.cnblogs.com/blog/1694759/202108/1694759-20210804210317619-1097620275.png)


# 5.JDK SPI机制

- SPI：是一种将服务接口与服务实现分离以达到解耦可拔插、大大提升了程序可扩展性的机制。避免代码污染,实现某块可插拔。
- 缺点：ServiceLoader只提供了遍历的方式来获取目标实现类，没有提供按需加载的方法。
- 规范流程：
	- 1. 制定统一的规范（比如 java.sql.Driver）
    - 2. 服务提供商提供这个规范具体的实现，在自己jar包的META-INF/services/目录里创建一个以服务接口命名的文件，内容是实现类的全命名（比如：com.mysql.jdbc.Driver）。
	- 3. 平台引入外部模块的时候，就能通过该jar包META-INF/services/目录下的配置文件找到该规范具体的实现类名，然后装载实例化，完成该模块的注入。


# 6.Spring SPI机制

- Spring的SPI机制主要体现在SpringBoot中，SpringBoot的启动包含new SpringApplication和执行run方法两个过程。
- SpringFactoriesLoader 类 获取类路径下所有META-INF/spring.factories
- 将文件中 EnableAutoConfiguration的路径通过反射机制 全部自动加载

	![](https://img2020.cnblogs.com/blog/1694759/202108/1694759-20210821184926251-1939833322.png)
    
    ![](https://img2022.cnblogs.com/blog/1694759/202208/1694759-20220817134025106-452912629.png)

```
public static List<String> loadFactoryNames(Class<?> factoryClass, ClassLoader classLoader) {
		// 获取类的全限定名
		String factoryClassName = factoryClass.getName();
		try {
			// 获取类路径下所有META-INF/spring.factories
			Enumeration<URL> urls = (classLoader != null ? classLoader.getResources(FACTORIES_RESOURCE_LOCATION) :
					ClassLoader.getSystemResources(FACTORIES_RESOURCE_LOCATION));
			List<String> result = new ArrayList<String>();
			// 把加载的配置转换成Map<String, List<String>>格式
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				Properties properties = PropertiesLoaderUtils.loadProperties(new UrlResource(url));
				String factoryClassNames = properties.getProperty(factoryClassName);
				result.addAll(Arrays.asList(StringUtils.commaDelimitedListToStringArray(factoryClassNames)));
			}
			return result;
		}
		catch (IOException ex) {
			throw new IllegalArgumentException("Unable to load [" + factoryClass.getName() +
					"] factories from location [" + FACTORIES_RESOURCE_LOCATION + "]", ex);
		}
	}


```

- 对比JDK  ServiceLoader：
	- 1.都是XXXLoader。命名格式都一样。
	- 2. 一个是加载 META-INF/services/ 目录下的配置；一个是加载 META-INF/spring.factories 固定文件的配置。思路都一样。
	- 3. 两个都是利用ClassLoader和ClassName来完成操作的。不同的是Java的ServiceLoader加载配置和实例化都是自己来实现，并且不能按需加载；SpringFactoriesLoader既可以单独加载配置然后按需实例化也可以实例化全部。
	
# 7.如何实现自定一个Spring-Boot-Starter ?

- 1. 新建一个只有pom的starter工程，引入写好的自动配置模块。
- 2. 自动配置模块配置 spring.factories 文件，格式如上。
- 3. 具体实现自动配置逻辑。
- 4.当SpringBoot启动加载配置类的时候，就会把这些第三方的配置一起加载。无须用户手动配置以及包扫描路径问题。

	![](https://img2020.cnblogs.com/blog/1694759/202108/1694759-20210821190611886-252589774.png)



# 8.在Provider可以配置的属性

+ timeout，方法调用超时 ，如果消费者也配置了，以消费者为准。
  retries，失败重试次数，缺省是2（表示加上第一次调用，会调用3次）
  loadbalance，负载均衡算法（有多个Provider时，如何挑选Provider调用），缺省是随机（random）。
  actives，消费者端，最大并发调用限制，即当Consumer对一个服务的并发调用到上限后，新调用会Wait直到超时。
  group，针对接口多实现配置
  version  接口实现升级版本控制
  check=“false”消费者启动不检查是否可用，消费者配置。
+ 数据传输默认是8M，可通过payload 修改。 <dubbo:protocol name="dubbo" port="-1" payload = "8388608"/>
+ 以 timeout 为例，显示了配置的查找顺序，其它 retries, loadbalance, actives 等类似 。方法级优先，接口级次之，全局配置再次之。如果级别一样，则消费方优先，提供方次之。

# 9.dubbo底层实现原理

- 1.Dubbo缺省协议采用单一长连接和NIO异步通讯，适合于小数据量大并发的服务调用，以及服务消费者机器数远大于服务提供者机器数的情况。
 客服端一个线程调用远程接口，生成一个唯一的ID（比如一段随机字符串，UUID等），Dubbo是使用AtomicLong从0开始累计数字的
- 2.将打包的方法调用信息（如调用的接口名称，方法名称，参数值列表等），和处理结果的回调对象callback，全部封装在一起，组成一个对象object
- 3.向专门存放调用信息的全局ConcurrentHashMap里面put(ID, object)
- 4.将ID和打包的方法调用信息封装成一对象connRequest，使用IoSession.write(connRequest)异步发送出去
- 5.当前线程再使用callback的get()方法试图获取远程返回的结果，在get()内部，则使用synchronized获取回调对象callback的锁， 再先检测是否已经获取到结果，如果没有，然后调用callback的wait()方法，释放callback上的锁，让当前线程处于等待状态。
- 6.服务端接收到请求并处理后，将结果（此结果中包含了前面的ID，即回传）发送给客户端，客户端socket连接上专门监听消息的线程收到消息，分析结果，取到ID，再从前面的ConcurrentHashMap里面get(ID)，从而找到callback，将方法调用结果设置到callback对象里。
- 7.监听线程接着使用synchronized获取回调对象callback的锁（因为前面调用过wait()，那个线程已释放callback的锁了），再notifyAll()，唤醒前面处于等待状态的线程继续执行（callback的get()方法继续执行就能拿到调用结果了），至此，整个过程结束。

# 10.Dubbo 服务暴露的过程

Dubbo采用全Spring配置方式，透明化接入应用，对应用没有任何API侵入，只需用Spring加载Dubbo的配置即可，Dubbo基于Spring的Schema扩展进行加载。
基于 dubbo.jar 内的 META-INF/spring.handlers 配置，Spring 在遇到 dubbo 名称空间时，会回调 DubboNamespaceHandler。
所有 dubbo 的标签，都统一用 DubboBeanDefinitionParser 进行解析，基于一对一属性映射，将 XML 标签解析为 Bean 对象。
在 ServiceConfig.export() 或 ReferenceConfig.get() 初始化时，将 Bean 对象转换 URL 格式，所有 Bean 属性转成 URL 的参数。
然后将 URL 传给 协议扩展点，基于扩展点的 扩展点自适应机制，根据 URL 的协议头，进行不同协议的服务暴露或引用。

ServiceBean 同时也是service标签解析之后的bean之一，继承ServiceConfig
该Bean实现了很多spring接口，关于InitializingBean，DisposableBean，ApplicationContextAware，BeanNameAware。
Spring初始化完成Bean的组装，会调用InitializingBean的afterPropertiesSet方法，在Spring容器加载完成，会接收到事件ContextRefreshedEvent，调用ApplicationListener的onApplicationEvent方法。
在afterPropertiesSet中，和onApplicationEvent中，会调用export()，在export()中，会暴露dubbo服务，具体区别在于是否配置了delay属性，是否延迟暴露.
如果delay不为null，或者不为-1时，会在afterPropertiesSet中调用export()暴露dubbo服务，如果为null,或者为-1时，会在Spring容器初始化完成，接收到ContextRefreshedEvent事件，调用onApplicationEvent，暴露dubbo服务

# 11.Dubbo隐士传参

有些参数需要RPC带着传递，但是又不想写入到业务代码里。比如实现dubbo调用链。可是使用dubbo的隐式传参，可以通过 RpcContext (ThreadLocal 实现)上的 setAttachment 和 getAttachment 在服务消费方和提供方之间进行参数的隐式传递。实现filter 接口。
RpcContext.getContext().setAttachment("index", "1"); // 隐式传参，后面的远程调用都会隐式将这些参数发送到服务器端，类似cookie，用于框架集成，不建议常规业务使用


# 12.RPC 框架的实现原理，及 RPC 架构组件详解
1.从服务提供者的角度看：当提供者服务启动时，需要自动向注册中心注册服务；
2.当提供者服务停止时，需要向注册中心注销服务；
3.提供者需要定时向注册中心发送心跳，一段时间未收到来自提供者的心跳后，认为提供者已经停止服务，从注册中心上摘取掉对应的服务。
4.从调用者的角度看：调用者启动时订阅注册中心的消息并从注册中心获取提供者的地址；
5.当有提供者上线或者下线时，


























































