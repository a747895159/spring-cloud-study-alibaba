
对于应用级团队，性能总是能满足需求即可，而不需要追求性能极限。


https://learning.snssdk.com/feoffline/toutiao_wallet_bundles/toutiao_learning_wap/online/article.html?item_id=6791308968099578376&app_name=news_article


# 1. 偏向锁、轻量级锁、重量级锁竞争

 ![](https://img2020.cnblogs.com/blog/1694759/202110/1694759-20211013180731349-1190196584.png)
 ![](https://img2020.cnblogs.com/blog/1694759/202110/1694759-20211013181026409-2075811936.png)   
 
# 2.高性能队列Disruptor
 
简书介绍：https://www.jianshu.com/p/bad7b4b44e48
        https://www.jianshu.com/p/1c0013e9bbad

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
	   
	- 采用无锁算法，避免频繁加锁、解锁的性能消耗。
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

# 5.1 PriorityBlockingQueue 队列是无解的，怎么实现数量限制？
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


















