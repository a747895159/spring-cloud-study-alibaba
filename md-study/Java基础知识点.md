
对于应用级团队，性能总是能满足需求即可，而不需要追求性能极限。


https://learning.snssdk.com/feoffline/toutiao_wallet_bundles/toutiao_learning_wap/online/article.html?item_id=6791308968099578376&app_name=news_article


# 1. 偏向锁、轻量级锁、重量级锁竞争

 ![](https://img2020.cnblogs.com/blog/1694759/202110/1694759-20211013180731349-1190196584.png)
 ![](https://img2020.cnblogs.com/blog/1694759/202110/1694759-20211013181026409-2075811936.png)   
 
# 2.高性能队列Disruptor
 
简书介绍：https://www.cnblogs.com/a747895159/articles/18111284

- Disruptor 是一款高性能的有界内存队列，目前应用非常广泛，Log4j2、SpringMessaging、HBase、Storm 都用到了 Disruptor，主要有：
	- 内存分配更加合理，使用 RingBuffer 数据结构，数组元素在初始化时一次性全部创建，提升缓存命中率；对象循环利用，避免频繁 GC。
	- [能够避免伪共享](https://www.cnblogs.com/a747895159/articles/15847768.html),提升缓存利用率。
	   + 为了提高读取速度，每个 CPU 有自己的缓存，CPU 读取数据后会存到自己的缓存里。而且为了节省空间，一个缓存行可能存储着多个变量，即伪共享。但是这对于共享变量，会造成性能问题：
         当一个 CPU 要修改某共享变量 A 时会先锁定自己缓存里 A 所在的缓存行，并且把其他 CPU 缓存上相关的缓存行设置为无效。但如果被锁定或失效的缓存行里，还存储了其他不相干的变量 B，其他线程此时就访问不了 B，或者由于缓存行失效需要重新从内存中读取加载到缓存里，这就造成了开销。所以让共享变量 A 单独使用一个缓存行就不会影响到其他线程的访问。
	   + 处理伪共享方案：一个CacheLine 64字节，8个long类型,可以将热点long类型变量前后各填充7个数量，不管无论如何加载Cache Line都不会产生伪共享问题。
	   + Java ConcurrentHashMap中使用注解@sun.misc.Contended 也是自动填充解决伪共享问题。CounterCell 存放并发数量的。（注：JVM 添加 -XX:-RestrictContended 参数后 @sun.misc.Contended 注解才有效）
	   ```
        /* ---------------- Counter support -------------- */
        
            /**
             * A padded cell for distributing counts.  Adapted from LongAdder
             * and Striped64.  See their internal docs for explanation.
             */
            @sun.misc.Contended static final class CounterCell {
                volatile long value;
                CounterCell(long x) { value = x; }
            }
        
            final long sumCount() {
                CounterCell[] as = counterCells; CounterCell a;
                long sum = baseCount;
                if (as != null) {
                    for (int i = 0; i < as.length; ++i) {
                        if ((a = as[i]) != null)
                            sum += a.value;
                    }
                }
                return sum;
            }
      ```
	   
	- 采用**RingBuffer**循环缓冲区，避免了传统队列中的锁竞争问题，避免频繁加锁、解锁的性能消耗。
	- 支持批量消费，消费者可以无锁方式消费多个消息。

# 3.java8新增的stream api迭代次数？
- 对于无状态的中间操作只会迭代一次,对于有状态的会迭代多次。
	![](https://img2020.cnblogs.com/blog/1694759/202111/1694759-20211103165032515-264003863.png)

# 4.Java为什么要打破双亲委派模式？

![](https://img2020.cnblogs.com/blog/1694759/202111/1694759-20211103165215393-1428034378.png)

双亲委派模型就是将类加载器进行分层。在触发类加载的时候，当前类加载器会从低层向上委托父类加载器去加载。每层类加载器在加载时会判断该类是否已经被加载过，如果已经加载过，就不再加载了，可以保证同一个类在使用中出现不相等场景。这种设计能够避免重复加载类、核心类被篡改等情况发生。

- 以JDBC为例,DriverManager是在 rt.jar中 由启动类加载器加载。 但是他的实现是采用SPI机制 在ClassPath下面。JDK引入了**线程上下文类加载器(TCCL：Thread Context ClassLoader)**,打破双亲委派模式,利用线程上下文类加载器去加载所需要的SPI代码。
  TCCL是从JDK1.2开始引入的，可以通过 java.lang.Thread 类中的 getContextClassLoader()和 setContextClassLoader(ClassLoader cl) 方法来获取和设置线程的上下文类加载器。如果没有手动设置上下文类加载器，线程将继承其父线程的上下文类加载器，初始线程的默认上下文类加载器是 Application ClassLoader。


# 5.线程池任务，如何设置优先级？

换掉 线程池里边的工作队列，使用 优先级的无界阻塞队列 ，去管理 异步任务。

* **ArrayBlockingQueue** ：使用数组实现的有界阻塞队列，特性先进先出
* **LinkedBlockingQueue** ：使用链表实现的阻塞队列，特性先进先出，可以设置其容量，默认为Interger.MAX_VALUE，特性先进先出
* **PriorityBlockingQueue** ：使用平衡二叉树堆，实现的具有优先级的无界阻塞队列
* **DelayQueue** ：无界阻塞延迟队列，队列中每个元素均有过期时间，当从队列获取元素时，只有过期元素才会出队列。队列头元素是最块要过期的元素。
* **SynchronousQueue** ：一个不存储元素的阻塞队列，每个插入操作，必须等到另一个线程调用移除操作，否则插入操作一直处于阻塞状态

- 使用PriorityBlockingQueue 作为 线程池的任务队列。
- 提交的任务 具备 排序能力。

# 5.1 PriorityBlockingQueue 队列是无界的，怎么实现数量限制？
- PriorityBlockingQueue是无界的，它的offer方法永远返回true。会带来OOM风险、最大线程数失效、拒绝策略失效。
- 可以继承PriorityBlockingQueue ， 重写一下这个类的offer方法，如果元素超过指定数量直接返回false，否则调用原来逻辑。
    - PriorityQueue在默认情况下是一个最小堆，如果使用最大堆调用构造函数就需要传入 Comparator 改变比较排序的规则。
    - PriorityQueue实现了Queue接口，但它并不是一个队列，也不是按照“先入先出”的顺序删除元素的。PriorityQueue是一个堆，每次调用函数remove或poll都将删除位于堆顶的元素。


# 6.布隆过滤器与BitMap(位图)

- 布隆过滤器(Bloom Filter):  相当于是一个不太精确的set集合,判断元素是否存在，存在误判率。**存在 不一定存在，不存在 一定不存在**. 
```
    String[] dataArr = {"a1", "b", "c", "d", "五"};
    //谷歌布隆过滤器（使用的过滤器，预期数据量，误判率越小越精准）
    BloomFilter<String> bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), 10000, 0.01);
    Arrays.stream(dataArr).forEach(bloomFilter::put);
    // 返回 true
    System.out.println(bloomFilter.mightContain("五"));
    // 返回 false
    System.out.println(bloomFilter.mightContain("是"));
```

- 位图(BitMap): 用于快速查找和存储大量数据的数据结构,非常适合于大规模数据的快速查找和去重.

![参考网址](https://www.cnblogs.com/a747895159/articles/18030189)


# 7.ThreadLocal内存泄露的原因？要如何避免？

`弱引用解决的是ThreadLocal对象的内存泄露问题，但value还存在内存泄露的风险。`
- 内存泄露的原因：

> 由于ThreadLocalMap和线程的生命周期是一致的，当线程资源长期不释放，`即使ThreadLocal本身由于弱引用机制已经被回收掉了，但value还是驻留在线程的ThreadLocalMap的Entry中`。即存在key为null，但value却有值的无效Entry，导致内存泄漏。

- ThreadLocal自身采取的措施：

> 但实际上，ThreadLocal内部已经为我们做了一定的防止内存泄漏的工作。ThreadLocalMap提供了一个expungeStaleEntry方法，该方法在`每次调用ThreadLocal的get、set、remove方法时都会执行清理工作`，即ThreadLocal内部已经帮我们做了对key为null的Entry的清理工作：擦除Entry(置为null)，同时检测整个Entry数组将key为null的Entry一并擦除，然后重新调整索引。
>
> 但是必须需要调用这三个方法才会触发清理，很可能我们使用完之后就不再做任何操作了(set/get/remove)，这样就不会触发内部的清理工作。

`开发人员需要注意： 所以，通常建议每次使用完ThreadLocal后，立即调用remove方法`。


# 7.1为什么ThreadLocalMap中key被设计成弱引用类型？

> key`设计为弱引用`是为了尽最大努力避免内存泄漏，`解决的是ThreadLocal对象的内存泄露问题`。
> ThreadLocal的设计者考虑到了某些线程的生命周期较长，比如线程池中的线程。由于存在Thread -> ThreadLocalMap -> Entry这样一条强引用链，如果key不设计成弱引用类型，是强引用的话，key就一直不会被GC回收，一直不会是null，Entry就不会被清理。
> (ThreadLocalMap根据key是否为null来判断是否清理Entry。因为key为null时，引用的ThreadLocal实例不可达会被回收。value又只能通过ThreadLocal的方法来访问，此时相当于value也没用处了。所以，可以根据key是否为null来判断是否清理Entry。)

# 7.2 ThreadLocal继承性问题？如何解决?

ThreadLocal不支持子线程继承，可以使用JDK中的InheritableThreadLocal来解决继承性问题。
对于线程池等场景，可以使用淘宝技术部哲良实现的TransmittableThreadLocal.



# 20. JavaAgent实现原理

java agent是基于**JVMTI (JVM Tool Interface)**机制实现的，其通过监听事件的方式获取Java应用运行状态，调用JVM TI提供的接口对应用进行控制。

- 1.**启动时加载方式`premain`**，-javaagent 参数之后需要指定一个 jar 包，在 META-INF 目录下的 MANIFEST.MF 文件中必须指定 `Premain-Class`配置项,启动时虚拟机先执行`premain()`方法
	- 使用场景：Skywalking、自定义业务增强Captain插件

- 2.**运行时加载Attach机制`agentmain`**，JVM不停机支持进程间pid通信，将agent对应的jar传给目标JVM。通过Attach Listener线程来进行加载。在 META-INF 目录下的 MANIFEST.MF 文件中必须指定 `Agent-Class`配置项
	- 使用场景：应用CPU、线程、GC信息诊断工具，如：阿里Arthas、VisualVM、JProfile


![](https://img2022.cnblogs.com/blog/1694759/202206/1694759-20220616123708521-892655234.png)

# 21. JavaAgent相关开发库

- 1.**Javassist **:使用简洁，比较老的类库，在动态组合字符串以实现比较复杂的逻辑容器出错。
- 2.**ASM库** :操作字节码指令，上手难度大，性能高，用于各工具底层实现（ByteBuddy、CGLIB）
- 3.**ByteBuddy** :强大灵活的开发库，基于切面方式实现类的增强，基于ASM二次封装。



| 字节码工具   | java-proxy                 | asm                          | Javassist                               | cglib                                  | bytebuddy                                                    |
| ------------ | -------------------------- | ---------------------------- | --------------------------------------- | -------------------------------------- | ------------------------------------------------------------ |
| 类创建       | 支持                       | 支持                         | 支持                                    | 支持                                   | 支持                                                         |
| 实现接口     | 支持                       | 支持                         | 支持                                    | 支持                                   | 支持                                                         |
| 方法调用     | 支持                       | 支持                         | 支持                                    | 支持                                   | 支持                                                         |
| 类扩展       | 不支持                     | 支持                         | 支持                                    | 支持                                   | 支持                                                         |
| 父类方法调用 | 不支持                     | 支持                         | 支持                                    | 支持                                   | 支持                                                         |
| 优点         | 容易上手，简单动态代理首选 | 任意字节码插入，几乎不受限制 | java原始语法，字符串形式插入，写入直观  | bytebuddy看起来差不多                  | 支持任意维度的拦截，可以获取原始类、方法，以及代理类和全部参数 |
| 缺点         | 功能有限，不支持扩展       | **学习难度大，编写代码量大** | 不支持jdk1.5以上的语法，如泛型，增强for | 正在被bytebuddy淘汰                    | 不太直观，学习理解有些成本，API非常多                        |
| 常见应用     | spring-aop，MyBatis        | cglib，bytebuddy             | Fastjson，MyBatis                       | spring-aop，EasyMock，jackson-databind | SkyWalking                                                   |



# 22.Captain 插件基于ByteBuddy实现了哪些功能，如何实现的？


















