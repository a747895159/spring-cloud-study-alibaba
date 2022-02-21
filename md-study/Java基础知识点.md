
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

# 4.Java为什么要打破双亲委派模式？SPI机制

![](https://img2020.cnblogs.com/blog/1694759/202111/1694759-20211103165215393-1428034378.png)

双亲委派模型就是将类加载器进行分层。在触发类加载的时候，当前类加载器会从低层向上委托父类加载器去加载。每层类加载器在加载时会判断该类是否已经被加载过，如果已经加载过，就不再加载了。这种设计能够避免重复加载类、核心类被篡改等情况发生。

- 以JDBC为例,DriverManager是在 rt.jar中 由启动类加载器加载。 但是他的实现是采用SPI机制 在ClassPath下面。JDK引入了**线程上下文类加载器(TCCL：Thread Context ClassLoader)**,打破双亲委派模式,利用线程上下文类加载器去加载所需要的SPI代码。
  TCCL是从JDK1.2开始引入的，可以通过 java.lang.Thread 类中的 getContextClassLoader()和 setContextClassLoader(ClassLoader cl) 方法来获取和设置线程的上下文类加载器。如果没有手动设置上下文类加载器，线程将继承其父线程的上下文类加载器，初始线程的默认上下文类加载器是 Application ClassLoader。










