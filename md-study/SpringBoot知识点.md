

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



