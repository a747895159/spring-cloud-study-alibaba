https://www.toutiao.com/i6739473228365824526/?timestamp=1628307768&app=news_article_lite&use_new_style=1&req_id=20210807114247010135164071482DB9C3&share_token=56b52e6e-6aa1-4d72-87ed-22d312e79867&group_id=6739473228365824526

APOLLO动态线程池：

监听配置变化，通过线程池方法 重新set值。executor.setCorePoolSize、executor.setMaximumPoolSize、executor.setKeepAliveTime

# 1.Netty的特点
Netty是 一个异步事件驱动的网络应用程序框架，是一个高性能、异步事件驱动的 NIO 框架，基于 JAVA NIO 提供的 API 实现。主要有以下特点：
- **内存零Copy**: 尽量减少不必要的内存拷贝，实现了更高效率的传输
- **Reactor主从多线程模型**:
- **内存池的缓冲区重用机制**:  避免了频繁创建和销毁缓冲区带来的开销
- **高性能序列化协议Protobuf**


# 2. 内存零Copy

- 1.直接使用堆外内存，避免JVM 堆内存到堆外内存的数据拷贝。
- 2.Netty提供了组合Buffer对象，可以聚合多个ByteBuffer对象引用，包装过程中不会产生内存拷贝；
- 3.Netty的文件传输使用FileRegion实现文件传输，采用transferTo方法，它可以直接将文件缓冲区的数据发送到目标Channel，避免内核缓冲区和用户态缓冲区之间的数据拷贝;

![](https://img2020.cnblogs.com/blog/1694759/202111/1694759-20211124154253592-619985614.png)
![](https://img2020.cnblogs.com/blog/1694759/202111/1694759-20211124154352056-513503311.png)
![](https://img2020.cnblogs.com/blog/1694759/202111/1694759-20211124154415503-1737333085.png)


# 3.Netty的线程模型？
- **I/O多路复用模型**：**I/O多路复用就是使用一个或者几个线程来完成大量通道或者数据源的事件监控和处理。**有一个线程不断去轮询多个 socket 的状态，只有当 socket 真正有读写事件时，才真正调用实际的 IO 读写操作。
  - 节省资源：由于多个IO操作共享同一个线程，减少不必要的线程创建和销毁开销。Linux主要使用epoll、select、poll

- **Reactor模型**是对事件处理流程的一种模式抽象，事件驱动的设计模式，适用于异步事件处理，是对I/O多路复用模型的一种封装。

Netty通过**Reactor主从多线程模型**基于多路复用器接收并处理用户请求，内部实现了两个线程池，boss线程池和work线程池，其中boss线程池的线程负责处理请求的accept事件，当接收到accept事件的请求时，把对应的socket封装到一个NioSocketChannel中，并交给work线程池，其中work线程池负责请求的read和write事件，由对应的Handler处理。

- **单线程模型**：所有I/O操作都由一个线程完成，即多路复用、事件分发和处理都是在一个Reactor线程上完成的。既要接收客户端的连接请求,向服务端发起连接，又要发送/读取请求或应答/响应消息。一个NIO 线程同时处理成百上千的链路，性能上无法支撑，速度慢，若线程进入死循环，整个程序不可用，对于高负载、大并发的应用场景不合适。

- **多线程模型**：
  - 有一个NIO 线程 只负责监听服务端，接收客户端的TCP 连接、认证等操作；

  - 另外有一个NIO 线程池负责网络IO 的操作，即消息的读取、解码、编码和发送；1 个NIO 线程可以同时处理N 条链路，但是1 个链路只对应1 个NIO 线程，这是为了防止发生并发操作问题。但在并发百万客户端连接或需要安全认证时，一个Acceptor 线程可能会存在性能不足问题。

- **主从多线程模型**：
  - mainReactor线程池只负责接入认证、握手等操作；
  - SubReactor线程池，用于处理I/O 的读写等操作；




# 3.1Netty的主从多线程模型

Netty 的线程模型基于**主从 Reactor 多线程**，借用了 MainReactor 和 SubReactor 的结构，内部实现了两个线程池，boss线程池和work线程池，其中boss线程池的线程负责处理请求的accept事件，当接收到accept事件的请求时，把对应的socket封装到一个NioSocketChannel中，并交给work线程池，其中work线程池负责请求的read和write事件，由对应的Handler处理。

但是实际实现上 SubReactor 和 Worker 线程在同一个线程池中：

```
    // 创建工作线程组,每次创建NioEventLoopGroup时 默认启动了CPU处理器数的两倍, 可以指定线程数
    NioEventLoopGroup boosGroup = new NioEventLoopGroup();
    NioEventLoopGroup workerGroup = new NioEventLoopGroup();
    final ServerBootstrap serverBootstrap = new ServerBootstrap();
    // 组装NioEventLoopGroup
    serverBootstrap.group(boosGroup, workerGroup)
            // 设置channel类型为NIO类型
            .channel(NioServerSocketChannel.class)
            // 设置连接配置参数
            .option(ChannelOption.SO_BACKLOG, 1024)
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .childOption(ChannelOption.TCP_NODELAY, true)
            // 配置入站、出站事件handler
            .childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) {
                    // 配置入站、出站事件channel.  添加更多的 ChannelHandler
                    ch.pipeline().addLast("idleStateHandler", new IdleStateHandler(5, 0, 0));
                    ch.pipeline().addLast("idleStateTrigger", new ServerIdleStateTrigger());
                    ch.pipeline().addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                    ch.pipeline().addLast("frameEncoder", new LengthFieldPrepender(4));
                    ch.pipeline().addLast("decoder", new StringDecoder());
                    ch.pipeline().addLast("encoder", new StringEncoder());
                    ch.pipeline().addLast("bizHandler", new ServerBizHandler());
                }
            });
    // 绑定端口
    serverBootstrap.bind(8080).addListener(future -> {
        if (future.isSuccess()) {
            System.out.println("绑定成功!");
        } else {
            System.err.println("绑定失败!");
        }
    });
```

上面代码中的 bossGroup 和 workerGroup 是 Bootstrap 构造方法中传入的两个对象，这两个 group 均是线程池：

- 1）bossGroup 线程池则只是在 Bind 某个端口后，获得其中一个线程作为 MainReactor，专门处理端口的 Accept 事件，每个端口对应一个 Boss 线程；
- 2）workerGroup 线程池会被各个 SubReactor 和 Worker 线程充分利用。

![](https://img2024.cnblogs.com/blog/1694759/202406/1694759-20240604143158493-1522536066.png)




# 4. Reactor模式的实践者

||类型|Header|
|---|---|---|
|Nginx|HTTP/反向代理服务|使用单master多worker，属于**多Reactor模式**|
|Redis|内存数据库|使用**单线程Reactor模式**，6.0版本开始可以选择开启多线程I/O来切换为多Reactor模式|
|Node.js|开发框架(Javascript)|采用**单线程Reactor**，10.5.0版本后支持多Reactor模式，通过启动多个V8引擎进程实现。对开发者屏蔽了Handler概念，以回调的形式体现|
|Netty|开发框架(Java)|网络应用程序开发框架，使用经典的**多Reactor模式**，通过pipeline串联多个Handler形成相对清晰的业务流程|
|WebFlux（Project Reactor）|开发框架(Java)|主要基于Netty实现网络通信，也提供了一些主流中间件的非阻塞SDK。Spring 5.0版本WebFlux开始使用它实现Reactor模式的Web服务。响应式编程范式。|
|Dubbo|RPC框架(Java)||


# 5.Netty如何修复空轮询的？
- Selector的空轮询BUG，臭名昭著的epoll bug，是 JDK NIO的BUG。若结果为空，在没有wakeup或线的消息时，则发生空循环，CPU使用率100%。
  - 1、对Selector的select操作周期进行统计，每完成一次空的select操作进行一次计数，若在某个周期内连续发生N次(默认为512)空轮询，则触发了epoll死循环bug。
  - 2、重建Selector，判断是否是其他线程发起的重建请求，若不是则将原SocketChannel从旧的Selector上去除注册，重新注册到新的Selector上，并将原来的Selector关闭。

# 6.Proto Buffer特点

![](https://img2024.cnblogs.com/blog/1694759/202406/1694759-20240604163327124-1020718650.png)
