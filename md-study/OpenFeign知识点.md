


# 1.FeignClient注解如何有对应实现类的？ [参考大神博客](https://blog.csdn.net/it_lihongmin/article/details/109027896?spm=1001.2014.3001.5501):https://blog.csdn.net/it_lihongmin/article/details/109027896?spm=1001.2014.3001.5501

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

	- 获取@FeignClient 上指定的 configuration(指定 编码 Encoder、解码 Decoder、Contract)
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
		
		
	


# 2.FeignClient方法调用，动态执行的过程
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

# 4.Feign Hystrix Ribbon 关系

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

# 5.Feign的原理
+ 以JAVA注解的方式定义的远程调用API接口(JDK代理类)，最终转换成HTTP的请求形式，然后将HTTP的请求的响应结果，解码成JAVA Bean，放回给调用者。

	![](https://img2020.cnblogs.com/blog/1694759/202109/1694759-20210906174823643-1905489410.png)

+ @EnableFeignClients 注解上有 **@Import(FeignClientsRegistrar.class)**。将@FeignClients注解的类以FeignClientFactoryBean类型的BeanDifinotion注册到Ioc容器(注入的FeignClientFactoryBean类型)
+ 反射InvocationHandler 核心代理接口, Feign提供一个默认的 **FeignInvocationHandler** 类，该类处于 feign-core 核心jar包中。当起启动Hystrix时，会使用 **HystrixInvocationHandler**。
+ 从代理类中取到方法对应的MethodHandler方法处理器去执行.Feign提供了默认 SynchronousMethodHandler 实现类
+ SynchronousMethodHandler.invoke 方法 会对 请求编码,根据注入的Client实现类 去执行和解码。
	- LoadBalancerFeignClient 类：内部使用 Ribben 负载均衡技术完成URL请求处理的feign.Client 客户端实现类。使用了delegate包装代理模式。
	![](https://img2020.cnblogs.com/blog/1694759/202109/1694759-20210906180338353-605563650.png)


	![](https://img2020.cnblogs.com/blog/1694759/202109/1694759-20210906180604651-82914352.png)


# 6.如何开启日志争强？
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
# 7.feign接口指定配置
- 通过 @FeignClient 注解中属性 configuration 配置指定编码 Encoder 与解码 Decoder，作用域仅限当前单例













