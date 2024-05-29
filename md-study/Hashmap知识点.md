[TOC]

# 1. HashMap的数据结构

+ 数组+链表/红黑树, 链表长度大于8(且数组长度大于64)时, 链表树化(扩容时, 桶元素小于6, 树退化为链表)
+ 初始容量16，扩容因子0.75时,数组长度为2的倍数, 数据迁移
+ JDK7 采用的是**头插法**,扩容时，改变了原队列的顺序，多线程并发会产生**环形链表死循环**问题。头插法：效率高O(1),满足时间局部性原理(最近插入的最有可能被使用)。
  - 头插法死循环原因: 插入时形成的是倒序链表，扩容时又变成正序链表。扩容时会造成死循环，同时get获取数据时，也会进入死循环。
+ JDK8 采用的是**尾插法**, 效率 O(N),**解决并发死循环**。链表原顺序。高并发下HashMap还是不安全的，会出现其它问题（如扩容丢数据),比如 hashmap下槽位为null时,数据丢失。
+ hashMap 扩容大小为什么是2的幂： (n - 1) & hash算法，如果n不为2的幂次方，易产生空间浪费，(n - 1) & hash 约等于  hash % n ，2进制运算远高于取模。
+ JDK 1.8 中，是通过 hashCode() 的高 16 位异或低 16 位实现的：(h = k.hashCode()) ^ (h >>> 16)，主要是从速度，功效和质量来考虑的，减少系统的开销，也不会造成因为高位没有参与下标的计算，从而引起的碰撞。

# 2.hashMap 扩容大小为什么是2的幂?

- (n - 1) & hash算法，如果n不为2的幂次方，易产生空间浪费，(n - 1) & hash 约等于  hash % n ，2进制运算远高于取模

# 3.HashMap中put的过程

![](https://img2020.cnblogs.com/blog/1694759/202108/1694759-20210826122959476-229662677.png)

![](https://img-blog.csdnimg.cn/1424df0149644c54891ebc96eec38880.png)

![](https://img2024.cnblogs.com/blog/1694759/202405/1694759-20240529104227357-321212328.png)


# 3.1HashMap中resize()的过程

![](https://img-blog.csdnimg.cn/7a70e0392bca4670aa9fa67771e26c60.png)


# 4.红黑树特点

+ 每个节点非红即黑

+ 根节点总是黑色的

+ 如果节点是红色的，则它的子节点必须是黑色的（反之不一定）

+ 每个叶子节点都是黑色的空节点（NIL节点）

+ 从根节点到叶节点或空子节点的每条路径，必须包含相同数目的黑色节点（即相同的黑色高度）

+ 红黑树能够以O(log2(N))的时间复杂度进行搜索、插入、删除操作。此外,任何不平衡都会在3次(插入2次)旋转之内解决。这一点是AVL所不具备的 。

  ![](https://img2020.cnblogs.com/blog/1694759/202108/1694759-20210826095256562-1174766132.png)
  ![](https://img2020.cnblogs.com/blog/1694759/202108/1694759-20210826095308809-908082306.png)

# 5.拉链法导致的链表过深问题为什么不用二叉查找树代替，而选择红黑树？为什么不一直使用红黑树？

+ 二叉查找树在特殊情况下会变成一条线性结构（这就跟原来使用链表结构一样了，造成很深的问题），遍历查找会非常慢。而红黑树在插入新数据后可能需要通过左旋，右旋、变色这些操作来保持平衡，引入红黑树就是为了查找数据快，解决链表查询深度的问题，我们知道红黑树属于平衡二叉树，但是为了保持“平衡”是需要付出代价的，但是该代价所损耗的资源要比遍历线性链表要少，所以当长度大于8的时候，会使用红黑树，如果链表长度很短的话，根本不需要引入红黑树，引入反而会慢。
+ HashMap 使用的是链地址法; TheadLocalMap使用的是开放地址法（线性探查）

# 6.HashMap 和 HashTable 有什么区别？

+ HashMap 是线程不安全的，HashTable 是线程安全的；
+ 由于线程安全，所以 HashTable 的效率比不上 HashMap；
+ HashMap最多只允许一条记录的键为null，允许多条记录的值为null，而 HashTable不允许；
+ HashMap 默认初始化数组的大小为16，HashTable 为 11，前者扩容时，扩大两倍，后者扩大两倍+1；
+ HashMap 需要重新计算 hash 值，而 HashTable 直接使用对象的 hashCode
+ HashTable 是使用 synchronize 关键字加锁的原理。而针对 ConcurrentHashMap，在 JDK 1.7 中采用 分段锁的方式；JDK 1.8 中直接采用了CAS（无锁算法）+ synchronized。

# 7.JDK7与JDK8  ConcurrentHashMap 的不同实现

+ JDK1.7里面分段锁(ReentrantLock + Segment + HashEntry)，每个Segment都是继承于Reentrantlock的，在对该segment进行操作时，获取锁，结束操作释放锁。**头插法**

  - Segment数组的大小默认为16,也就是说最大只支持16个并发。
  - 计算size(): 总个数=每个Segment中的个数相加。 先不加锁,连续计算两次,若两次相同 即可返回。若不同，对每个Segment 进行加锁，重新计算个数。

  ![](https://img2020.cnblogs.com/blog/1694759/202108/1694759-20210826110926780-1221341867.png)

+ JDK1.8里面，没有用segment，而是用 **Node(或红黑树)+CAS+synchronized **实现的。锁粒度更低。**尾插法**

  - 查询时间复杂度变小：从原来的遍历链表O(n)，变成遍历红黑树O(logN)。

  - 计算size(): 取无并发时baseCount+并发时countCells数据长度叠加。

    - baseCount 主要记录无并发时（counterCells 数组未初始化）,size变化直接写到此字段。
    - 产生并发后 counterCells 则进行初始化后，后续size都写到 counterCells数组中。

    ```
       // 1. counterCells 数组未初始化，在没有线程争用时，将 size 的变化写入此字段
       // 2. 初始化 counterCells 数组时，没有获取到 cellsBusy 锁，会再次尝试将 size 的变化写入此字段
       private transient volatile long baseCount;
       
       // 用于同步 counterCells 数组结构修改的乐观锁资源
       private transient volatile int cellsBusy;
       
    
       // counterCells 数组一旦初始化，size 的变化将不再尝试写入 baseCount
       // 可以将 size 的变化写入数组中的任意元素
       // 可扩容，长度保持为 2 的幂
       private transient volatile CounterCell[] counterCells;
    ```
    - CounterCell 使用了@Contended的注解解决伪共享问题。使用这个注解进行**填充缓存行**优化(将8个字节的Long填充成64字节，CPU Cache Line的大小通常被设定为64字节)，提高多线程并发性能。LongAdder and Striped64也是使用@Contented
    - **伪共享问题** ：多个线程同时读写同一个缓存行的不同变量时导致的 CPU 缓存失效。尽管这些变量之间没有任何关系，但由于在主内存中邻近，存在于同一个缓存行之中，它们的相互覆盖会导致频繁的缓存未命中，引发性能下降。
    
  ![](https://img2020.cnblogs.com/blog/1694759/202108/1694759-20210826110903191-1554787732.png)

- JDK7先扩容完成再添加新元素,JDK8是先添加再扩容,JDK7存储位置以后按新的容量重新计算index,JDK 8是根据以前索引+原容量推断出来。

# 8.JDK8中 ConcurrentHashMap什么场景会是使用 synchronized ？

+ 先看下putVal 原代码

  ```
  final V putVal(K key, V value, boolean onlyIfAbsent) {
       if (key == null || value == null) throw new NullPointerException();
       int hash = spread(key.hashCode());
       int binCount = 0;
       for (Node<K,V>[] tab = table;;) {
           Node<K,V> f; int n, i, fh;
           if (tab == null || (n = tab.length) == 0)
               tab = initTable();
           
           //①此处对Node数组f进行赋值, 并且判断当前元素hash是否在Node数据中，不存在，则创建一个Node
           else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
               if (casTabAt(tab, i, null,
                            new Node<K,V>(hash, key, value, null)))
                   break;                   // no lock when adding to empty bin
           }
           else if ((fh = f.hash) == MOVED)
               tab = helpTransfer(tab, f);
           else {
               V oldVal = null;
           //②此处锁住了链表头结点 f,锁住的是hash冲突的那条Node链表
               synchronized (f) {
                   if (tabAt(tab, i) == f) {
                       if (fh >= 0) {
                           binCount = 1;
                           for (Node<K,V> e = f;; ++binCount) {
                               K ek;
                               if (e.hash == hash &&
                                   ((ek = e.key) == key ||
                                    (ek != null && key.equals(ek)))) {
                                   oldVal = e.val;
                                   if (!onlyIfAbsent)
                                       e.val = value;
                                   break;
                               }
                               Node<K,V> pred = e;
                               if ((e = e.next) == null) {
                                   pred.next = new Node<K,V>(hash, key,
                                                             value, null);
                                   break;
                               }
                           }
                       }
                       else if (f instanceof TreeBin) {
                           Node<K,V> p;
                           binCount = 2;
                           if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,
                                                          value)) != null) {
                               oldVal = p.val;
                               if (!onlyIfAbsent)
                                   p.val = value;
                           }
                       }
                   }
               }
               if (binCount != 0) {
                   if (binCount >= TREEIFY_THRESHOLD)
                       treeifyBin(tab, i);
                   if (oldVal != null)
                       return oldVal;
                   break;
               }
           }
       }
       addCount(1L, binCount);
       return null;
   }
  
  ```

+ 注意synchronized上锁的对象,请记住,synchronized是靠对象的对象头和此对象对应的monitor来保证上锁的,也就是对象头里的重量级锁标志指向了monitor,而monitor呢,内部则保存了一个当前线程,也就是抢到了锁的线程.

+ ②处（已经锁到Node节点）出现并发争抢的可能性一般不高，哪怕有争抢，基于synchronized的轻量级锁(30-50次自旋)基本能拿到锁，不会升级为重量级锁。而当前线程也不会挂起，线程减少了挂起和唤醒的上线文切换的开销。

+ ②处也可以换成 ReentrantLock 也可以实现锁细化。但是lock 不会自旋，拿不到锁 就会挂起，这样就会多处线程上线文切换问题。当然也可以用tryLock，但是tryLock时间又不太好定义。

+ **如果是线程并发量不大的情况下,那么Synchronized因为自旋锁,偏向锁,轻量级锁的原因,不用将等待线程挂起,偏向锁甚至不用自旋,所以在这种情况下要比ReentrantLock高效**。


# 9.JDK8中为啥使用红黑树，不直接使用AVL平衡二叉树？
- 1. 平衡二叉树时间复杂度O(logN)。
- 2. AVL树必须保证Max(最大树高-最小树高) <= 1所以在插入的时候很容易出现不平衡的情况，一旦这样，就需要进行旋转以求达到平衡。需要花大量时间调整树的平衡。
- 3. 红黑树也是一种自平衡二叉查找树，红黑树继承了AVL可自平衡的优点，同时在查询速率和调整耗时中寻找平衡，放宽了树的平衡条件。牺牲了部分平衡性以换取插入/删除操作时少量的旋转操作，整体来说性能要优于AVL树。