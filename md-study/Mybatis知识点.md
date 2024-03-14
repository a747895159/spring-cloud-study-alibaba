
# 1.#{}和${}的区别是什么？

#{}是预编译处理，${}是字符串替换。
Mybatis在处理#{}时，会将sql中的#{}替换为?号。可以有效的防止SQL注入
Mybatis在处理${}时，就是把${}替换成变量的值。用于传入数据库对象，例如传入表名

# 2.Mapper接口里的方法，参数不同时，方法能重载吗？

- Mapper接口的全限名，就是映射XML文件中的namespace的值,接口的方法名，就是映射文件中MappedStatement的id值，接口方法内的参数，就是传递给sql的参数。Mapper接口是没有实现类的，当调用接口方法时，接口全限名+方法名拼接字符串作为key值，可唯一定位一个MappedStatement。
- Mapper接口里的方法，是不能重载的.工作原理是JDK动态代理，Mybatis运行时会使用JDK动态代理为Dao接口生成代理proxy对象，代理对象proxy会拦截接口方法，转而执行MappedStatement所代表的sql，然后将sql执行结果返回。
- 不同的Xml映射文件，如果配置了namespace，那么id可以重复；如果没有配置namespace，那么id不能重复。

# 3. Mybatis动态sql是做什么的？都有哪些动态sql？能简述一下动态sql的执行原理不？

提供了9种动态sql标签trim|where|set|foreach|if|choose|when|otherwise|bind,执行原理为，使用OGNL从sql参数对象中计算表达式的值，根据表达式的值动态拼接sql，以此来完成动态sql的功能。

# 4.简述Mybatis的插件plugin运行原理，以及如何编写一个插件。

- Mybatis仅可以编写针对ParameterHandler、ResultSetHandler、StatementHandler、Executor这4种接口的插件，Mybatis使用JDK的动态代理，为需要拦截的接口生成代理对象以实现接口方法拦截功能，每当执行这4种接口对象的方法时，就会进入拦截方法，具体就是InvocationHandler的invoke()方法，当然，只会拦截那些你指定需要拦截的方法。
- 动态代理，通过责任链的设计模式，层层代理的执行。这4个方法实例化了对应的对象之后，都会调用interceptorChain的pluginAll方法，InterceptorChain的pluginAll刚才已经介绍过了，就是遍历所有的拦截器，然后调用各个拦截器的plugin方法。
- 编写插件：实现Mybatis的Interceptor接口并复写intercept()方法，然后在给插件编写注解，指定要拦截哪一个接口的哪些方法即可，记住，别忘了在配置文件中配置你编写的插件。
- 举例：PageHelper、mybatis-plus 增加处理等框架都是这种实现

# 5.resultType 与 resultMap 的区别？
1）类的名字和数据库相同时，可以直接设置 resultType 参数为 Pojo 类
2）若不同，需要设置 resultMap 将结果名字和 Pojo 名字进行转换