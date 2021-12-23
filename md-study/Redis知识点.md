
https://www.cnblogs.com/crazymakercircle/p/14731826.html


# 1.分布式缓存 Redis与DB数据一致性解决方案

- 缓存是通过牺牲强一致性来提高性能的,由CAP理论决定的。缓存系统适用的场景就是非强一致性的场景，它属于CAP中的AP。
- 双删机制: 先删缓存、**更新数据库、再删缓存**
- Canal+RocketMQ同步MySQL，由消息统一进行删除缓存。


# Redisson锁简介
- Redisson是一个在Redis的基础上实现的Java驻内存数据网格（In-Memory Data Grid）。它不仅提供了一系列的分布式的Java常用对象，还实现了可重入锁（Reentrant Lock）、公平锁（Fair Lock、联锁（MultiLock）、 红锁（RedLock）、 读写锁（ReadWriteLock）等，还提供了许多分布式服务。
- RLock结构 key就是UUID+threadId，hash结构的value就是重入值，在分布式锁时，这个值为1（Redisson还可以实现重入锁，那么这个值就取决于重入次数了）

