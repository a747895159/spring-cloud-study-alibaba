
https://www.cnblogs.com/crazymakercircle/p/14731826.html


# 1.分布式缓存 Redis与DB数据一致性解决方案

- 缓存是通过牺牲强一致性来提高性能的,由CAP理论决定的。缓存系统适用的场景就是非强一致性的场景，它属于CAP中的AP。
- 双删机制: 先删缓存、**更新数据库、再删缓存**
- Canal+RocketMQ同步MySQL，由消息统一进行删除缓存。


# Redisson锁简介
- Redisson是一个在Redis的基础上实现的Java驻内存数据网格（In-Memory Data Grid）。它不仅提供了一系列的分布式的Java常用对象，还实现了可重入锁（Reentrant Lock）、公平锁（Fair Lock、联锁（MultiLock）、 红锁（RedLock）、 读写锁（ReadWriteLock）等，还提供了许多分布式服务。
- RLock结构 key就是UUID+threadId，hash结构的value就是重入值，在分布式锁时，这个值为1（Redisson还可以实现重入锁，那么这个值就取决于重入次数了）


# Redis中hash表扩容原理,与HashMap 的区别？
- 从数据结构的角度来看，redis的dict和java的HashMap很像，区别在于rehash：HashMap在resize时是一次性拷贝的，然后使用新的数组，而dict维持了2个dictht，平常使用ht[0]，一旦开始rehash则使用ht[0]和ht[1]，rehash被分摊到每次的dictAdd和dictFind等操作中。

- 当hash内部的元素比较拥挤时(hash碰撞比较频繁)，就需要进行扩容。扩容需要申请新的两倍大小的数组，然后将所有的键值对重新分配到新的数组下标对应的链表中(rehash)。如果hash结构很大，比如有上百万个键值对，那么一次完整rehash的过程就会耗时很长。这对于单线程的Redis里来说有点压力山大。所以Redis采用了渐进式rehash的方案。它会同时保留两个新旧hash结构，在后续的定时任务以及hash结构的读写指令中将旧结构的元素逐渐迁移到新的结构中。这样就可以避免因扩容导致的线程卡顿现象。

- ht是一个数组，有且只有俩元素ht[0]和ht[1];其中，ht[0]存放的是redis中使用的哈希表，而ht[1]和rehashidx和哈希表的 rehash有关。ht[0]，是存放数据的table，作为非扩容时容器。ht[1]，只有正在进行扩容时才会使用，它也是存放数据的table，长度为ht[0]的两倍。
扩容时，单线程A负责把数据从ht[0] copy到ht[1] 中。如果这时有其他线程
进行读操作：会先去ht[0]中找，找不到再去ht[1]中找。
进行写操作：直接写在ht[1]中。

- 不同的是，Redis的字典只能是字符串，另外他们rehash的方式不一样，因为Java的HashMap的字典很大时，rehash是个耗时的操作，需要一次全部rehash。Redis为了追求高性能，不能堵塞服务，所以采用了渐进式rehash策略。渐进式rehash会在rehash的同时，保留新旧两个hash结构，查询时会同时查询两个hash结构，然后在后续的定时任务以及hash操作指令中，循环渐进地将旧hash的内容一点点地迁到新的hash结构中。当搬迁完成了，就会使用新的hash结构取而代之。当hash移除最后一个元素后，该数据结构自动删除，内存被回收。

- hash结构也可以用来存储用户信息，与字符串需要一次性全部序列化整个对象不同，hash可以对用户结构中的每个字段单独存储。这样当我们需要获取用户信息时，可以进行部分获取。而以整个字符串的形式去保存用户信息的话，就只能一次性全部读取，这样就会浪费网络流量。但是hash结构的存储消耗要高于单个字符串。

- **两者对比**： 1.扩容所花费的时间对比： 一个单线程渐进扩容，一个多线程协同扩容。在平均的情况下，是ConcurrentHashMap 快。这也意味着，扩容时所需要 花费的空间能够更快的进行释放。 2.读操作，两者性能相差不多。 3.写操作，Redis的字典返回更快些，因为它不像ConcurrentHashMap那样去帮着扩容(当要写的桶位已经搬到了newTable时)，等扩容完才能进行操作。 4.删除操作，与写一样。



