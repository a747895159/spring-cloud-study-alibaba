//延迟消息原理
https://www.cnblogs.com/heihaozi/p/13259125.html

//博客园文档
https://www.cnblogs.com/a747895159/articles/15544495.html

// 阿里云RocketMQ 使用指南
https://help.aliyun.com/document_detail/293595.html


// 源码分析RocketMQ消息消费机制----消费者拉取消息机制
https://blog.csdn.net/prestigeding/article/details/78885420


RocketMQ 顺序消息实现机制

https://www.jianshu.com/p/0ff1b6a3da36




# 2.一个消费者中多个线程又是如何并发的消费的,多个消费队列并发消费。


- 消费规则: 1个消费者可以消费多个消息队列，但一个消息队列同一时间只能被一个消费者消费.
- **Broker发现客户端列表有变化，通知所有Consumer执行Rebalance **
- 服务启动时,RebalanceService是以守护线程启动，每 waitInterval=20s 调用一次doRebalance()进行分配.

![](https://img2020.cnblogs.com/blog/1694759/202111/1694759-20211118204821594-2022285909.jpg)


- BROADCASTING：针对广播模式，分配 Topic 对应的所有消息队列。CLUSTERING 集群模式 ,topic与队列按照分配策略分配。
- 每一个消费应用会可能存在多个topic的consumer,每个consumer可以配置多个线程消费。
- 应用启动后，RebalanceService服务会每20秒进行一次队列重新负载，获取topic对应的topic与消费队列。然后将新的消费队列对应的PullRequest 放入 PullMessageService.pullRequestQueue队列中。

```
public class PullMessageService extends ServiceThread {
    private final InternalLogger log = ClientLogger.getLog();
   //当前拉取请求队列
    private final LinkedBlockingQueue<PullRequest> pullRequestQueue = new LinkedBlockingQueue<PullRequest>();
    private final MQClientInstance mQClientFactory;
   
   // ... 略
   
}

```

- PullMessageService类中也对应定时器，拉取队列消息。 参考 DefaultMQPushConsumerImpl.pullMessage方法实现。
- 如果消费者状态非Running、暂停、队列消息总数大于1000、已拉取队列的缓存大于100M, 消费者进度大于2000、异常等都会进入 延迟执行  this.executePullRequestLater(pullRequest, PULL_TIME_DELAY_MILLS_WHEN_FLOW_CONTROL) 。延迟执行逻辑还是 将PullRequest 放入 PullMessageService.pullRequestQueue队列中。

![](https://img2020.cnblogs.com/blog/1694759/202111/1694759-20211118204743940-2077611872.png)


![](https://img2020.cnblogs.com/blog/1694759/202111/1694759-20211118205035244-1825926438.png)

- 如果请求失败，则等待PULL_TIME_DELAY_MILLS_WHEN_EXCEPTION(3000)后再放回PullMessageService的待处理队列中；处理成功则进入2.
- 调用PullAPIWrapper对结果进行预处理
- 根据请求状态进行处理
  - 有新消息(FOUND)
    - 设置PullRequest下次开始消费的起始位置为PullResult的nextBeginOffset
    - 如果结果列表为空则不延迟，立马放到PullMessageService的待处理队列中，否则进入3
    - 将PullResult中的结果List<MessageExt>放入ProcessQueue的缓存中，并通知ConsumeMessageService处理
    - 将该PullRequest放回待处理队列中等待再次处理，如果有设置拉取的间隔时间，则等待该时间后再翻到队列中等待处理，否则直接放到队列中等待处理
  - 没有新消息(NO_NEW_MSG)
    - 设置PullRequest下次开始消费的起始位置为PullResult的nextBeginOffset
    - 如果缓存的待消费消息数为0，则更新offset存储
    - 将PullRequest立马放到PullMessageService的待处理队列中
  - 没有匹配的消息(NO_MATCHED_MSG)
    - 设置PullRequest下次开始消费的起始位置为PullResult的nextBeginOffset
    - 如果缓存的待消费消息数为0，则更新offset存储
    - 将PullRequest立马放到PullMessageService的待处理队列中
  - 不合法的偏移量(OFFSET_ILLEGAL)
    - 设置PullRequest下次开始消费的起始位置为PullResult的nextBeginOffset
    - 标记该PullRequset为drop
    - 10s后再更新并持久化消费offset；再通知Rebalance移除该MessageQueue


```
- 每次从消费进度 拉取32个队列消息拉取成功后，将消息按照消费数(默认为1)分批放入消费者线程池中。
public void submitConsumeRequest(final List<MessageExt> msgs,final ProcessQueue processQueue,final MessageQueue messageQueue,final boolean dispatchToConsume) {
   //用户消费队列数 默认1。 msgs的size 默认是32 
   final int consumeBatchSize = this.defaultMQPushConsumer.getConsumeMessageBatchMaxSize();
   if (msgs.size() <= consumeBatchSize) {
      ConsumeRequest consumeRequest = new ConsumeRequest(msgs, processQueue, messageQueue);
      try {
         //消费者开启的线程池
         this.consumeExecutor.submit(consumeRequest);
      } catch (RejectedExecutionException e) {
         this.submitConsumeRequestLater(consumeRequest);
      }
   } else {
      for (int total = 0; total < msgs.size(); ) {
         List<MessageExt> msgThis = new ArrayList<MessageExt>(consumeBatchSize);
         for (int i = 0; i < consumeBatchSize; i++, total++) {
            if (total < msgs.size()) {
               msgThis.add(msgs.get(total));
            } else {
               break;
            }
         }

         ConsumeRequest consumeRequest = new ConsumeRequest(msgThis, processQueue, messageQueue);
         try {
            this.consumeExecutor.submit(consumeRequest);
         } catch (RejectedExecutionException e) {
            for (; total < msgs.size(); total++) {
               msgThis.add(msgs.get(total));
            }

            this.submitConsumeRequestLater(consumeRequest);
         }
      }
   }
}
```


# 3.消息消费进度如何保存，包括MQ是如何知道消息是否正常被消费了。

- 消息消费完成后，需要将消费进度存储起来，即前面提到的offset。广播模式下，同消费组的消费者相互独立，消费进度要单独存储；集群模式下，同一条消息只会被同一个消费组消费一次，消费进度会参与到负载均衡中，故消费进度是需要共享的。
  - 消费进度相关类 OffsetStore：① LocalFileOffsetStore 本地存储消费进度的具体实现，给广播模式使用；② RemoteBrokerOffsetStore 给集群模式使用，将消费进度存储在broker。
   - 入口在org.apache.rocketmq.client.impl.consumer.ConsumeMessageConcurrentlyService#processConsumeResult中的最后一段逻辑:

```
public void processConsumeResult(final ConsumeConcurrentlyStatus status,final ConsumeConcurrentlyContext context,final ConsumeRequest consumeRequest) {
   int ackIndex = context.getAckIndex();

   if (consumeRequest.getMsgs().isEmpty())
      return;

   // 省略

   long offset = consumeRequest.getProcessQueue().removeMessage(consumeRequest.getMsgs());
   if (offset >= 0) {
      this.defaultMQPushConsumerImpl.getOffsetStore().updateOffset(consumeRequest.getMessageQueue(),
         offset, true);
   }
}
```

   - 消息消费完成后，将消息从ProcessQueue中移除，同时返回ProcessQueue中最小的offset，使用这个offset值更新消费进度，removeMessage返回的offset有两种情况，一是已经没有消息了，返回ProcessQueue最大offset+1，二是还有消息，则返回未消费消息的最小offset。
   - 举个例子，ProcessQueue中有offset为101-110的10条消息，如果全部消费完了，返回的offset为111；如果101未消费完成，102-110消费完成，则返回的offset为101，这种情况下如果消费者异常退出，会出现重复消费的风险，所以要求消费逻辑幂等。
   - 看RemoteBrokerOffsetStore的updateOffset()逻辑，将offset更新到内存中，这里RemoteBrokerOffsetStore使用ConcurrentHashMap保存MessageQueue的消费进度：
   - 在MQClientInstance启动的时候会注册定时任务，每5s执行一次persistAllConsumerOffset()，最终调用到persistAll().会将消费进度持久到远程Broker。

- **消费进度先保存在内存中,然后定时5s将数据持久化到远程Broker**，offset读取：内存读,读取不到再远程读; 远程Broker读。

# 4.RocketMQ 推拉模式实现机制。

-  pullMessage函数的参数是 final PullRequest pullRequest ，这是通过“长轮询”方式达到 Push效果的方法，长轮询方式既有 Pull 的优点。长轮询就是在Broker在没有新消息的时候才阻塞，阻塞时间默认设置是 15秒，有消息会立刻返回。
-  Pull方式问题就是循环拉取消息时间间隔不好设定，间隔太短，浪费资源；间隔太长 消息没被及时处理。
-  RocketMQ 是通过伪Push方式实现的推消息机制， 底层是使用“长轮询”方式达到 Push效果的,每次Pull一批消息放在队列中，然后交给子线程队列一个个处理。
   - 长轮询拉取有流量控制 防止消息在消费者堆积。每个MessageQueue都有个对象的ProcessQueue对象（个 TreeMap 和一个读写锁 ）,TreeMap 以Offset为key 以消息内容为value。读写锁控制着多线程对 TreeMap的并发访问。
   - 流量控制：①消息未处理个数 1000 ;②消息总大小100M；③Offset跨度 2000；

```
    public class ProcessQueue {
        private final ReadWriteLock lockTreeMap = new ReentrantReadWriteLock();
        //缓存的待消费消息,按照消息的起始offset排序
        private final TreeMap</*消息的起始offset*/Long, MessageExt> msgTreeMap = new TreeMap<Long, MessageExt>();
        //缓存的待消费消息数量
        private final AtomicLong msgCount = new AtomicLong();
        //缓存的待消费消息大小
        private final AtomicLong msgSize = new AtomicLong();
        private final Lock lockConsume = new ReentrantLock();
        /**
         * A subset of msgTreeMap, will only be used when orderly consume
         */
        private final TreeMap<Long, MessageExt> consumingMsgOrderlyTreeMap = new TreeMap<Long, MessageExt>();
        private final AtomicLong tryUnlockTimes = new AtomicLong(0);
        private volatile long queueOffsetMax = 0L;
        private volatile boolean dropped = false;
        //最近执行pull的时间
        private volatile long lastPullTimestamp = System.currentTimeMillis();
        //最近被客户端消费的时间
        private volatile long lastConsumeTimestamp = System.currentTimeMillis();
        private volatile boolean locked = false;
        private volatile long lastLockTimestamp = System.currentTimeMillis();
        //当前是否在消费，用于顺序消费模式，对并行消费无效
        private volatile boolean consuming = false;
        private volatile long msgAccCnt = 0;
    }
```

- DefaultMQPushConsumer  由系统控制读取操作，收到消息后自动调用用户线程的处理方法来处理
- DefaultMQPullConsumer  读取操作中的大部分功能由使用者自主控制，要注意Offset的保存与同步。


# 5.Rocketmq怎么保证队列完全顺序消费？


- 全局顺序消息：只有一个队列，性能较差；
- 分区顺序消息：多个队列,单一队列中消息顺序。生产者采用按key取模运算，将同一key消息都放到同一个队列。消费端要使用MessageListenerOrderly接口。但是会有问题，消息消费失败，无法跳过，当前队列消费暂停。性能也受影响。

![](https://img2020.cnblogs.com/blog/1694759/202111/1694759-20211119134643158-1229659813.png)

- RocketMQ消息消费模式如下图：

![](https://img2020.cnblogs.com/blog/1694759/202111/1694759-20211119134255577-809940184.png)

- 1.Broker中在类RebalanceLockManager d的静态变量 mqLockTable (变量类型为 ConcurrentMap)中存储了以消费组 为key ,以 ConcurrentMap (以消息主题，主题下队列为key，具体信息是消费者客户端id 为和客户端上次锁定时间 为value的 LockEntity 对象）为value的消费者锁定信息;
- 2.broker 接受请求后执行 RebalanceLockManager de tryLockBatch方法,执行顺序如下：
	+ 1.请求参数解析，解析成 要锁定的主题下队列集合和消费者ID；
	+ 2.遍历请求锁定的队列
	+ 3.通过 mqLockTable 判断单个队列是否已经锁定,即调用 LockEntry 的 isLocked 方法,主要是判断 clientId 是否是当前消费者ID,如果是就更新锁定时间，并加入已经锁定队列中,如果 mqLockTable 不存在 这个消费组或者当前锁定的clientId与请求的clientId 不相等，就加入未锁定队列;
	+ 4.判断未锁定队列是否为空，不为空,判断当前消费组是否在mqLockTable 中,不存在就创建,后启用 RebalanceLockManager的可重入锁，遍历未锁定队列.
	+ 5.执行第3步
	+ 6.释放 RebalanceLockManager 的可重入锁，返回当前锁定的信息。
- 3.consumer 上顺序消费的类有个定时任务，每隔20去向broke 发送它订阅的topic 的锁定请求。
- 4.consumer 上在获取到队列的消息的时候，让消费线程池去处理，处理前必须获取到本地队列的锁。参考：ConsumeMessageOrderlyService.ConsumeRequest 类。


# 6. RocketMQ 业务名词含义。

- 定时(延迟消息): broker有配置项messageDelayLevel，默认值为“1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h”，18个level。可以配置自定义messageDelayLevel。定时消息会暂存在名为SCHEDULE_TOPIC_XXXX的topic中，并根据delayTimeLevel存入特定的queue,即一个queue只存相同延迟的消息.
  broker会调度地消费SCHEDULE_TOPIC_XXXX，将消息写入真实的topic。
- 消息重试: 生产者在发送消息时，同步消息失败会重投，异步消息有重试，oneway没有任何保证。消息重投保证消息尽可能发送成功、不丢失，但可能会造成消息重复，消息重复在RocketMQ中是无法避免的问题。消息重复在一般情况下不会发生，当出现消息量大、网络抖动，消息重复就会是大概率事件。另外，生产者主动重发、consumer负载变化也会导致重复消息。

- NameServer：NameServer是一个非常简单的Topic路由注册中心，其角色类似Dubbo中的zookeeper，支持Broker的动态注册与发现。主要包括两个功能：Broker管理，NameServer接受Broker集群的注册信息并且保存下来作为路由信息的基本数据。
  然后提供心跳检测机制，检查Broker是否还存活；路由信息管理，每个NameServer将保存关于Broker集群的整个路由信息和用于客户端查询的队列信息。
  然后Producer和Conumser通过NameServer就可以知道整个Broker集群的路由信息，从而进行消息的投递和消费。NameServer通常也是集群的方式部署，各实例间相互不进行信息通讯。
  Broker是向每一台NameServer注册自己的路由信息，所以每一个NameServer实例上面都保存一份完整的路由信息。当某个NameServer因某种原因下线了，Broker仍然可以向其它NameServer同步其路由信息，Producer,Consumer仍然可以动态感知Broker的路由的信息。



- NameServer是一个几乎无状态节点，可集群部署，节点之间无任何信息同步。
  Broker部署相对复杂，Broker分为Master与Slave，一个Master可以对应多个Slave，但是一个Slave只能对应一个Master，Master与Slave 的对应关系通过指定相同的BrokerName，不同的BrokerId 来定义，BrokerId为0表示Master，非0表示Slave。Master也可以部署多个。每个Broker与NameServer集群中的所有节点建立长连接，定时注册Topic信息到所有NameServer。 注意：当前RocketMQ版本在部署架构上支持一Master多Slave，但只有BrokerId=1的从服务器才会参与消息的读负载。
  Producer与NameServer集群中的其中一个节点（随机选择）建立长连接，定期从NameServer获取Topic路由信息，并向提供Topic 服务的Master建立长连接，且定时向Master发送心跳。Producer完全无状态，可集群部署。
  Consumer与NameServer集群中的其中一个节点（随机选择）建立长连接，定期从NameServer获取Topic路由信息，并向提供Topic服务的Master、Slave建立长连接，且定时向Master、Slave发送心跳。Consumer既可以从Master订阅消息，也可以从Slave订阅消息，消费者在向Master拉取消息时，Master服务器会根据拉取偏移量与最大偏移量的距离（判断是否读老消息，产生读I/O），以及从服务器是否可读等因素建议下一次是从Master还是Slave拉取。
  启动NameServer，NameServer起来后监听端口，等待Broker、Producer、Consumer连上来，相当于一个路由控制中心。
  Broker启动，跟所有的NameServer保持长连接，定时发送心跳包。心跳包中包含当前Broker信息(IP+端口等)以及存储所有Topic信息。注册成功后，NameServer集群中就有Topic跟Broker的映射关系。
  收发消息前，先创建Topic，创建Topic时需要指定该Topic要存储在哪些Broker上，也可以在发送消息时自动创建Topic。
  Producer发送消息，启动时先跟NameServer集群中的其中一台建立长连接，并从NameServer中获取当前发送的Topic存在哪些Broker上，轮询从队列列表中选择一个队列，然后与队列所在的Broker建立长连接从而向Broker发消息。
  Consumer跟Producer类似，跟其中一台NameServer建立长连接，获取当前订阅Topic存在哪些Broker上，然后直接跟Broker建立连接通道，开始消费消息。    

# 7.Topic路由注册与剔除流程：

+ A.Broker 每30s向 NameServer 发送心跳包，心跳包中包含主题的路由信息(主题的读写队列数、操作权限等)，NameServer 会通过 HashMap 更新 Topic 的路由信息，并记录最后一次收到 Broker 的时间戳。
+ B.NameServer 以每10s的频率清除已宕机的 Broker，NameServer 认为 Broker 宕机的依据是如果当前系统时间戳减去最后一次收到 Broker 心跳包的时间戳大于120s。
+ C.消息生产者以每30s的频率去拉取主题的路由信息，即消息生产者并不会立即感知 Broker 服务器的新增与删除。


# 8.RebalaceService 线程：

![](https://img2020.cnblogs.com/blog/1694759/202111/1694759-20211116145625523-1563475991.png)

+ 其职责是负责消息消费队列的负载，默认以20s的间隔按照队列负载算法进行队列分配，如果此次分配到的队列与上一次分配的队列不相同，则需要触发消息队列的更新操作：
  * A.如果是新分配的队列，则创建 PullReqeust 对象(拉取消息任务)，添加到 PullMessageService 线程内部的阻塞队列 pullRequestQueue 中。如果该队列中存在拉取任务，则 PullMessageService 会向 Broker 拉取消息。
  * B.如果是上次分配但本次未分配的队列，将其处理队列 ProcessQueue 的状态设置为丢弃，然后 PullMessageService 线程在根据 PullRequest 拉取消息时首先会判断 ProcessQueue 队列的状态，如果是已丢弃状态，则直接丢弃 PullRequest 对象，停止拉取该队列中的消息，否则向Broker 拉取消息，拉取到一批消息后，提交到一个处理线程池，然后继续将 PullRequest 对象添加到 pullRequestQueue，即很快就会再次触发对该消息消费队列的再次拉取，这也是 RocketMQ 实现 PUSH 模式的本质。

- 消费者消费线程池处理完一条消息时，消费者需要向 Broker 汇报消费的进度，以防消息重复消费。这样当消费者重启后，指示消费者应该从哪条消息开始消费。并发消费模式下，由于多线程消费的缘故，提交到线程池消费的消息默认情况下无法保证消息消费的顺序。

- 在 PUSH 模式下，PullMessageService拉取完一批消息后，将消息提交到线程池后会“马不蹄停”去拉下一批消息，如果此时消息消费线程池处理速度很慢，处理队列中的消息会越积越多，占用的内存也随之飙升，最终引发内存溢出，更加不能接受的消息消费进度并不会向前推进，因为只要该处理队列中偏移量最小的消息未处理完成，整个消息消费进度则无法向前推进，如果消费端重启，又得重复拉取消息并造成大量消息重复消费。RocketMQ 解决该问题的策略是引入消费端的限流机制。

* RocketMQ 消息消费端的限流的两个维度：
      +  A.消息堆积数量:如果消息消费处理队列中的消息条数超过1000条会触发消费端的流控，其具体做法是放弃本次拉取动作，并且延迟50ms后将放入该拉取任务放入到pullRequestQueue中，每1000次流控会打印一次消费端流控日志。
      - B.消息堆积大小：如果处理队列中堆积的消息总内存大小超过100M，同样触发一次流控。




# 9.主从同步(HA):

![](https://img2020.cnblogs.com/blog/1694759/202111/1694759-20211116145413996-648336471.png)

A.首先启动Master并在指定端口监听；
B.客户端启动，主动连接Master，建立TCP连接；
C.客户端以每隔5s的间隔时间向服务端拉取消息，如果是第一次拉取的话，先获取本地commitlog文件中最大的偏移量，以该偏移量向服务端拉取消息；
D.服务端解析请求，并返回一批数据给客户端；
E.客户端收到一批消息后，将消息写入本地commitlog文件中，然后向Master汇报拉取进度，并更新下一次待拉取偏移量；
F.然后重复第3步；


# 10.事务消息:

![](https://img2020.cnblogs.com/blog/1694759/202111/1694759-20211116145454842-308649345.png)

RocketMQ事务消息的实现原理是类似基于二阶段提交与事务状态回查来实现的。事务消息的发送只支持同步方式，其实现的关键点包括：

+ A.在应用程序端，在一个本地事务中，通过发送消息API向Broker发送Prepare状态的消息，收到消息服务器返回成功后执行事件回调函数，在事件函数的职责就是记录该消息的事务状态，通常采用消息发送本地事务表，即往本地事务表中插入一条记录，如果业务处理成功，则消息本地事务中会存在相关记录；如果本地事务执行失败而导致事务回滚，此时本地事务状态中不存在该消息的事务状态。
+ B.消息服务端收到Prepare的消息时，如何保证消息不会被消费端立即处理呢？原来消息服务端收到Prepare状态的消息，会先备份原消息的主题与队列，然后变更主题为：RMQ_SYS_TRANS_OP_HALF_TOPIC，队列为0。
+ C.消息服务端会开启一个专门的线程，以每60s的频率从RMQ_SYS_TRANS_OP_HALF_TOPIC中拉取一批消息，进行事务状态的回查，其实现原理是根据消息所属的消息生产者组名随机获取一个生产者，向其询问该消息对应的本地事务是否成功，如果本地事务成功(该部分是由业务提供的事务回查监听器来实现)，则消息服务端执行提交动作；如果事务状态返回失败，则消息服务端执行回滚动作；如果事务状态未知，则不做处理，待下一次定时任务触发再检查。默认如果连续5次回查都无法得到确切的事务状态，则执行回滚动作。



# 11.RocketMQ 与 Kafka 区别

- 1.架构区别
	+ RocketMQ由NameServer、Broker、Consumer、Producer组成，NameServer之间互不通信，Broker会向所有的nameServer注册，通过心跳判断broker是否存活，producer和consumer 通过nameserver就知道broker上有哪些topic。
	+ Kafka的元数据信息都是保存在Zookeeper，新版本部分已经存放到了Kafka内部了，由Broker、Zookeeper、Producer、Consumer组成。

- 2.维度区别
	+ Kafka的master/slave是基于partition(分区)维度的，而RocketMQ是基于Broker维度的；Kafka的master/slave是可以切换的（主要依靠于Zookeeper的主备切换机制）RocketMQ无法实现自动切换，当RocketMQ的Master宕机时，读能被路由到slave上，但写会被路由到此topic的其他Broker上。

- 3.刷盘机制
	+ RocketMQ支持同步刷盘，也就是每次消息都等刷入磁盘后再返回，保证消息不丢失，但对吞吐量稍有影响。一般在主从结构下，选择异步双写策略是比较可靠的选择。

- 4.消息查询
	+ RocketMQ支持消息查询，除了queue的offset外，还支持自定义key。RocketMQ对offset和key都做了索引，均是独立的索引文件。

- 5.服务治理
	+ Kafka用Zookeeper来做服务发现和治理，broker和consumer都会向其注册自身信息，同时订阅相应的znode，这样当有broker或者consumer宕机时能立刻感知，做相应的调整；
	+ RocketMQ用自定义的nameServer做服务发现和治理，其实时性差点，比如如果broker宕机，producer和consumer不会实时感知到，需要等到下次更新broker集群时(最长30S)才能做相应调整，服务有个不可用的窗口期，但数据不会丢失，且能保证一致性。但是某个consumer宕机，broker会实时反馈给其他consumer，立即触发负载均衡，这样能一定程度上保证消息消费的实时性。
	
- 6.消费确认
	+ RocketMQ仅支持手动确认，也就是消费完一条消息ack+1，会定期向broker同步消费进度，或者在下一次pull时附带上offset。
	+ Kafka支持定时确认，拉取到消息自动确认和手动确认，offset存在zookeeper上。

- 7.消息回溯
	+ Kafka理论上可以按照Offset来回溯消息。
	+ RocketMQ支持按照时间来回溯消息，精度毫秒，例如从一天之前的某时某分某秒开始重新消费消息，典型业务场景如consumer做订单分析，但是由于程序逻辑或者依赖的系统发生故障等原因，导致今天消费的消息全部无效，需要重新从昨天零点开始消费，那么以时间为起点的消息重放功能对于业务非常有帮助。
	
- 8.RocketMQ特有
	+ 支持tag
	+ 支持事务消息、顺序消息
