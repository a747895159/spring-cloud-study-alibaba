

# 1.SpringBoot：注解@ConditionalOnClass(X.class),X不存在时 会不会报错？

- @ConditionalOnClass通常与@Configuration 结合使用，意思是当classpath中存在某类时满足条件。
- 第三方jar包中maven依赖是有对应X的jar的，只是POM文件是<optional>true</optional>不依赖传递。第三方jar都是编译好的。项目启动不回编译错误。
- 当我们项目中自定义的，如果没有对应的X.class编译会直接报错的。
- 我们也可以不引用 对应的X.Class jar包。直接用 @ConditionalOnClass(name=包路径.类名)的方式

