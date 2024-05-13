

# 1.SpringBoot：注解@ConditionalOnClass(X.class),X不存在时 会不会报错？

- @ConditionalOnClass通常与@Configuration 结合使用，意思是当classpath中存在某类时满足条件。
- 第三方jar包中maven依赖是有对应X的jar的，只是POM文件是<optional>true</optional>不依赖传递。第三方jar都是编译好的。项目启动不回编译错误。
- 当我们项目中自定义的，如果没有对应的X.class编译会直接报错的。
- 我们也可以不引用 对应的X.Class jar包。直接用 @ConditionalOnClass(name=包路径.类名)的方式

# 2.Tomcat连接数相关配置

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

# 3.SpringBoot的 jar 可以直接运行的原理
- **结构**: Spring Boot的JAR文件不是传统的单一JAR，而是采用了所谓的“胖JAR”或“自包含JAR”结构。这种JAR内部包含了一个BOOT-INF目录与相关所有的依赖内容。
  ![](https://img2024.cnblogs.com/blog/1694759/202405/1694759-20240513181546440-886139466.png)

- **嵌入式服务器**: Spring Boot默认使用嵌入式的Servlet容器，如Tomcat，这使得应用不需要额外部署到独立的服务器上即可运行。

- **MANIFEST.MF**：
    - 包含Start-Class属性，指明应用的主启动类，通常是带有@SpringBootApplication注解的类。
    - 包含Main-Class属性，指定启动器类（如org.springframework.boot.loader.JarLauncher或org.springframework.boot.loader.WarLauncher），这个类负责解析JAR结构，加载应用的类和资源，然后执行Start-Class指定的主类。

![](https://img2024.cnblogs.com/blog/1694759/202405/1694759-20240513181929237-1016757966.png)

- **类加载器**: Spring Boot使用自定义的类加载器（如LaunchedURLClassLoader）来处理JAR内的类加载，以便正确地隔离应用的类与依赖库的类。BOOT-INF/classes下的应用类优先于BOOT-INF/lib中的依赖库类加载。





