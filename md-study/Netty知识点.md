



https://www.toutiao.com/i6739473228365824526/?timestamp=1628307768&app=news_article_lite&use_new_style=1&req_id=20210807114247010135164071482DB9C3&share_token=56b52e6e-6aa1-4d72-87ed-22d312e79867&group_id=6739473228365824526







https://www.toutiao.com/i6750653102128382476/?timestamp=1628307384&app=news_article_lite&use_new_style=1&req_id=202108071136230101351690753A2D6CEA&share_token=b64d7ce0-03b1-462d-a5bf-bcbf63f44646&group_id=6750653102128382476







https://www.toutiao.com/i6816271513050677771/?timestamp=1626828239&app=news_article_lite&use_new_style=1&req_id=202107210843580101351690863F2CDC3A&share_token=bdcd54cd-89a8-4098-a51a-c16c158cf001&group_id=6816271513050677771





APOLLO动态线程池：  

监听配置变化，通过线程池方法 重新set值。executor.setCorePoolSize、executor.setMaximumPoolSize、executor.setKeepAliveTime


# 1. Netty 的 零copy?

![](https://img2020.cnblogs.com/blog/1694759/202111/1694759-20211124154253592-619985614.png)
![](https://img2020.cnblogs.com/blog/1694759/202111/1694759-20211124154352056-513503311.png)
![](https://img2020.cnblogs.com/blog/1694759/202111/1694759-20211124154415503-1737333085.png)

- 在 Java NIO 中的通道（Channel）就相当于操作系统的内核空间（kernel space）的缓冲区，而缓冲区（Buffer）对应的相当于操作系统的用户空间（user space）中的用户缓冲区（user buffer）。
堆外内存（DirectBuffer）在使用后需要应用程序手动回收，而堆内存（HeapBuffer）的数据在 GC 时可能会被自动回收。因此，在使用 HeapBuffer 读写数据时，为了避免缓冲区数据因为 GC 而丢失，NIO 会先把 HeapBuffer 内部的数据拷贝到一个临时的 DirectBuffer 中的本地内存（native memory），这个拷贝涉及到
sun.misc.Unsafe.copyMemory() 的调用，背后的实现原理与 memcpy() 类似。 最后，将临时生成的 DirectBuffer 内部的数据的内存地址传给 I/O 调用函数，这样就避免了再去访问 Java 对象处理 I/O 读写。
- Netty 使用基于java使用对外内存，对于堆外直接内存的分配和回收，是一件耗时的操作,**Netty 提供了基于内存池的缓冲区重用机制**：
- 我们的数据传输一般都是通过TCP/IP协议实现的，在实际应用中，很有可能一条完整的消息被分割为多个数据包进行网络传输，而单个的数据包对你而言是没有意义的，只有当这些数据包组成一条完整的消息时你才能做出正确的处理，而Netty可以通过零拷贝的方式将这些数据包组合成一条完整的消息供你来使用。
- 因为CompositeChannelBuffer并没有将多个ChannelBuffer真正的组合起来，而只是保存了他们的引用，这样就避免了数据的拷贝，实现了Zero Copy。

# 2.Netty如何修复空轮询的？
- Selector的空轮询BUG，臭名昭著的epoll bug，是 JDK NIO的BUG。若结果为空，在没有wakeup或线的消息时，则发生空循环，CPU使用率100%。
    - 1、对Selector的select操作周期进行统计，每完成一次空的select操作进行一次计数，若在某个周期内连续发生N次空轮询，则触发了epoll死循环bug。
    - 2、重建Selector，判断是否是其他线程发起的重建请求，若不是则将原SocketChannel从旧的Selector上去除注册，重新注册到新的Selector上，并将原来的Selector关闭。

