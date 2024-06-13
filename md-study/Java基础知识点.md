
对于应用级团队，性能总是能满足需求即可，而不需要追求性能极限。


https://learning.snssdk.com/feoffline/toutiao_wallet_bundles/toutiao_learning_wap/online/article.html?item_id=6791308968099578376&app_name=news_article

# 0.1 List相关知识
ArrayList 默认容量是10,扩容为原来的1.5倍


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

# 5.线程池任务，如何设置优先级？

换掉 线程池里边的工作队列，使用 优先级的无界阻塞队列 ，去管理 异步任务。

* **ArrayBlockingQueue** ：使用数组实现的有界阻塞队列，特性先进先出
* **LinkedBlockingQueue** ：使用链表实现的阻塞队列，特性先进先出，可以设置其容量，默认为Interger.MAX_VALUE，特性先进先出
* **PriorityBlockingQueue** ：二叉树最小堆的实现，实现的具有优先级的无界阻塞队列。提交的任务需具备排序能力。
* **DelayQueue** ：无界阻塞延迟队列，队列中每个元素均有过期时间，当从队列获取元素时，只有过期元素才会出队列。队列头元素是最块要过期的元素。
* **SynchronousQueue** ：一个不存储元素的阻塞队列，每个插入操作，必须等到另一个线程调用移除操作，否则插入操作一直处于阻塞状态

# 5.1 PriorityBlockingQueue 队列是无界的，怎么实现数量限制？
- PriorityBlockingQueue是无界的，它的offer方法永远返回true。会带来OOM风险、最大线程数失效、拒绝策略失效。
- 可以继承PriorityBlockingQueue ， 重写一下这个类的offer方法，如果元素超过指定数量直接返回false，否则调用原来逻辑。
    - PriorityQueue在默认情况下是一个最小堆，如果使用最大堆调用构造函数就需要传入 Comparator 改变比较排序的规则。
    - PriorityQueue实现了Queue接口，但它并不是一个队列，也不是按照“先入先出”的顺序删除元素的。PriorityQueue是一个堆，每次调用函数remove或poll都将删除位于堆顶的元素。


# 6.布隆过滤器与BitMap(  位图)

- 布隆过滤器(Bloom Filter):  通过使用多个哈希函数将元素映射到位数组的不同位置上，并将这些位置的比特位设为1。它适用于可以接受一定误报率的场景。相当于是一个不太精确的set集合,判断元素是否存在，存在误判率。**存在 不一定存在，不存在 一定不存在**. 
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


# 7.ThreadLocalMap内存泄露的原因？要如何避免？

- 每个Thread拥有一个ThreadLocalMap，每个ThreadLocalMap中以ThreadLocal为key，值为value。每个ThreadLocalMap存储的“Key-Value对”数量比较少。
- ThreadLocalMap底层使用开放地址法(线性探针)。当Thread实例销毁后，ThreadLocalMap也会随之销毁。

###  ThreadLocalMap中Entry的 Key被设计成弱引用类型

> key`设计为弱引用`是为了尽最大努力避免内存泄漏，`解决的是ThreadLocal对象的内存泄露问题`。
> ThreadLocal的设计者考虑到了某些线程的生命周期较长，比如线程池中的线程。由于存在Thread -> ThreadLocalMap -> Entry这样一条强引用链，如果key不设计成弱引用类型，是强引用的话，key就一直不会被GC回收，一直不会是null，Entry就不会被清理。
> (ThreadLocalMap根据key是否为null来判断是否清理Entry。因为key为null时，引用的ThreadLocal实例不可达会被回收。value又只能通过ThreadLocal的方法来访问，此时相当于value也没用处了。所以，可以根据key是否为null来判断是否清理Entry。)

**弱引用解决的是ThreadLocal对象的内存泄露问题，但value还存在内存泄露的风险。**

### 内存泄露的原因：

> 由于ThreadLocalMap和线程的生命周期是一致的，当线程资源长期不释放，`即使ThreadLocal本身由于弱引用机制已经被回收掉了，但value还是驻留在线程的ThreadLocalMap的Entry中`。即存在key为null，但value却有值的无效Entry，导致内存泄漏。

- ThreadLocal自身采取的措施,`开发人员需要注意： 所以，通常建议每次使用完ThreadLocal后，立即调用remove方法`。：

> 1.由于ThreadLocalMap中Entry的 Key 使用了弱引用，在下次GC发生时，就可以使那些没有被其他强引用指向、仅被Entry的Key 所指向的ThreadLocal实例能被顺利回收。并且，在Entry的Key引用被回收之后，其Entry的Key值变为null。
> 2.但实际上，ThreadLocal内部已经为我们做了一定的防止内存泄漏的工作。ThreadLocalMap提供了一个expungeStaleEntry方法，该方法在`每次调用ThreadLocal的get、set、remove方法时都会执行清理工作`，即ThreadLocal内部已经帮我们做了对key为null的Entry的清理工作：擦除Entry(置为null)，同时检测整个Entry数组将key为null的Entry一并擦除，然后重新调整索引。
> 3.但是必须需要调用这三个方法才会触发清理，很可能我们使用完之后就不再做任何操作了(set/get/remove)，这样就不会触发内部的清理工作。


### 示例：

```
public void funcA() {
    //创建一个线程本地变量
    ThreadLocal<Integer> local = new ThreadLocal<>();
    //设置值
    local.set(100);
    //获取值
    local.get();
    //函数末尾
    }
```

![](https://img2024.cnblogs.com/blog/1694759/202405/1694759-20240531134234369-1551470544.png)

# 7.1 ThreadLocal的性能问题，及内存泄露要如何避免？

- 在高并发的环境下，要尽量复用、重用ThreadLocal变量，避免在高频率的操作中频繁地创建和销毁它们。编程规范：推荐使用 `static final` 修饰ThreadLocal对象。

```
// privite 缩小使用的范围，尽可能不让他人引用
private static final ThreadLocal<Foo> LOCAL_FOO = new ThreadLocal<Foo>();
```

![](https://img2024.cnblogs.com/blog/1694759/202405/1694759-20240531145056592-29295737.png)


-  **由于static final 修饰TheadLocal对象实例，属于常量，不会被回收。导致ThreadLocalMap中Entry的Key所引用的ThreadLocal对象实例，一直存在强引用。**  导致上述JDK解决key内存泄露问题都是失效的。**使用完后必须使用remove()进行手动释放。**

### ThreadLocal继承性相关其他类

- InheritableThreadLocal : 是JDK提供的ThreadLocal的子类。允许父线程中的InheritableThreadLocal变量的值被子线程继承。
- TransmittableThreadLocal ：是阿里巴巴开源的一个框架，跨线程传递：能够在多线程传递中保持变量的传递性，确保在父线程和子线程之间正确传递ThreadLocal变量。


# 8.ArrayList 与LinkedList 区别
- 数据结构：
  - ArrayList基于动态数组实现，支持随机访问，通过索引访问元素的时间复杂度为O(1)。
  - LinkedList基于双向链表实现，随机访问较慢，插入和删除较快。
- 内存使用：
  - ArrayList由于需要连续内存分配，可能会导致更多的内存碎片
  - LinkedList每个节点除了存储数据外，还需要额外的存储空间来保存前后节点的引用，更费内存。
- 实现接口：
  - ArrayList实现RandomAccess接口，支持二分查找
  - LinkedList还额外实现了Deque接口，因此它可以作为双端队列使用；
- 局部性接口：
  - ArrayList由于其连续内存布局，能够更好地利用CPU缓存，特别是在进行遍历操作时。
  - LinkedList由于内存分布不连续，可能导致CPU缓存命中率较低，影响遍历效率。

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


















