## KafKa 消费者新版Api

[原版英文文章地址](http://www.confluent.io/blog/tutorial-getting-started-with-the-new-apache-kafka-0.9-consumer-client)


[中文翻译地址,作者:任何忧伤,都抵不过世界的美丽](http://zqhxuyuan.github.io/2016/02/20/Kafka-Consumer-New/)

好了，下面开始粘贴了。

当kafka最初创建的时候,它内置了scala版本的producer和consumer客户端.在使用的过程中我们渐渐发现了这些APIs的限制.比如,我们有”high-level”的消费者API,可以支持消费组和故障处理,但是不支持更多更复杂的场景需求. 我们也有一个简单的消费者客户端(SimpleConsumer,即low-level),可以支持自定义的控制,但是需要应用程序自己管理故障和错误处理.所以我们决定重新设计这些客户端,它的目标是要能实现之前使用旧的客户端不容易实现甚至无法实现的场景,还要建立一些API的集合,来支持长时间的拉取消息(译注: 即消费者通过poll方式保持长时间的消息拉取).第一阶段是发布在0.8.1中生产者API(KafkaProducer)的重写,最近发布的0.9完成第二阶段新的消费者API(KafkaConsumer).基于新的消费组协调协议(group coordination protocol),新的消费者API带来了以下的优势:

+ 简洁的统一API:
	
	 新的消费者结合了旧的API中”simple”和”high-level”消费者客户端两种功能,能够同时提供消费者协调(高级API)和lower-level的访问,来构建自定义的消费策略.
+ 更少的依赖:

	 新的消费者完全使用java编写,它不再依赖scala运行时环境和zookeeper.在你的项目中可以作为一个轻量级的库
	 
+ 更好的安全性: 

	0.9版本实现了安全性扩展,目前只支持新的消费者API
	
+ 新的消费者还添加了一些协议: 

	管理一组消费者处理进程的故障容忍.之前这部分功能通过java客户端频繁地和zookeeper进行交互.部分复杂的逻辑导致很难使用其他语言构建出完整的客户端.现在新的协议的出现使得这部分非常容易实现,现在已经实现了C的客户端.

尽管新的消费者使用了全新设计的API和新的协调协议,基本概念并没有多大差别.所以熟悉旧的消费者客户端的用户理解新的API并不会有很大的困难. 不过还是有一些微妙的细节需要关注, 特别是消费组管理和线程模型.这篇文章会覆盖新的消费者的基本用法,并解释这些细节。

首先复习下一些基本概念.在kafka中,每个topic会被分成一系列的logs,叫做partitions(逻辑上topic是由partitions组成).Producers写到这些logs的尾部,Consumers以自己的步调读取logs.kafka扩展topic的消费是通过将partitions分布在一个消费组,多个消费者共享了相同的组标识.<font color= purple>（旁白）</font>图片就省略了，毕竟是复习..

Kafka服务器端并不会记录消费者的消费位置，而是由消费者自己决定如何保存其消费的offset. 0.8.2版本之前消费者会将其消费位置记录zookeeper中，在后面的新版本中，消费者为了缓解zookeeper集群的压力，在Kafka服务器端添加了一个名字是__consusmer_offsets的内部topic,简称为offset topic，他可以用来保存消费者提交的offset，当出现消费者上线或者下线时会触发消费者组的rebalance操作，对partitions重新进行分配，等待rebalance完成之后，消费者就可以读取offset topic中的记录的offset，并从此offset开始继续消费。你也可以根据业务需求将offset存储在别的存储介质中，比如数据库等

旧的消费者依赖于zookeeper管理消费组(译注:ZookeeperConsumerConnector->ZKRebalancerListener),新的消费者使用了消费组协调协议. 对于每个消费组,会选择一个brokers作为消费组的协调者(group coordinator).协调者负责管理消费者组的状态. 它的主要工作是负责协调partition的分配(assignment): 当有新成员加入,旧成员退出,或者topic的metadata发生变化(topic的partitions改变).重新分配partition叫做消费组的平衡(group rebalance)当消费组第一次被初始化时,消费者通常会读取每个partition的最早或最近的offset.然后顺序地读取每个partition log的消息.在消费者读取过程中,它会提交已经成功处理的消息的offsets. 下图中消费者的位置在6位置,最近提交的offset则在位置1.


<font color=purple>（旁白段落）</font>很明显，我又把图片省略了，但图片上的几个概念还是很重要的，就介绍下图片上的几个概念，关于消费者的信息记录，有**lastCommitOffset，Current Position, High Watermark, log end offset(leo)**,log end offset是Leader的最新一条的offset,High Watermark是成功拷贝到log的所有副本节点的最近消息的offset,从消费者角度来看的话，HW的位置就是可消费的最后位置，最关键的就是lastCommitOffset和Current Position，这两个的位置前后关系到对消息可靠性的选择，lastCommitOffset是消费者上次提交的消费记录，Current Position是消费者正在消费的日志记录，那么如果消费者是<font color=red>先提交记录</font>，在进行消费，很明显Current Position < lastCommitOffset,就可能会出现消息丢失的情况，但保证了消息只被消费一次。如果消费者是<font color=red>先消费</font>，再提交记录，Current Position > lastCommitOffset, 保证消息至少被消费一次，但会出现消费多次的情况。后面还会对这两种情况进行详细分析。

当一个partition被分配给消费组中的其他消费者,(新的消费者)初始位置会设置为(原始消费者)最近提交的offset.如果示例中的消费者突然崩溃,接管partition的组中其他成员会从offset=1的位置开始消费(lastCommitOffset=1).这种情况下,新的消费者不得不从offset=1的位置开始,重新处理消息直到崩溃的消费者的offset=6的位置.上图中还有两个log中重要的位置信息. Log End Offset是写入log中最后一条消息的offset+1.
High Watermark是成功拷贝到log的所有副本节点的最近消息的offset(译注: 实际上是partition的所有ISR节点).从消费者的角度来看,最多只能读取到High watermark的位置,为了防止消费者读取还没有完全复制的数据造成数据丢失.(译注:如果消费者读取了未完全复制的数据,但是这部分数据之后丢失了,导致读取不该读的消息,所以应该读取完全复制的数据)


#### 配置和初始化

```
<dependency>
 <groupId>org.apache.kafka</groupId>
 <artifactId>kafka-clients</artifactId>
 <version>0.9.0.0</version>
</dependency>
```

就像旧的生产者和消费者,需要配置一个初始brokers列表,能够让消费者发现集群中的其他brokers.
但并不需要指定所有的ervers, 客户端会根据初始brokers找出集群中存活的所有brokers(译注:类似gossip协议).
在本例中,我们假设broker运行在本地(所以只有一个broker),同时还要告诉消费者怎么序列化消息的keys和values.
最后,为了能够加入到一个消费组,需要为消费者指定一个group id. 随着文章的深入,我们会介绍更多的配置.

```
Properties props = new Properties();
props.put("bootstrap.servers", "localhost:9092");
props.put("group.id", "consumer-tutorial");
props.put("key.deserializer", StringDeserializer.class.getName());
props.put("value.deserializer", StringDeserializer.class.getName());
KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

```

#### topic订阅

为了能够消费消息,应用程序需要指定要订阅的topics. 下面的示例中,我们订阅了”foo”和”bar”两个topics:

```
consumer.subscribe(Arrays.asList("foo", "bar"));

```

消费者订阅主题之后,这个消费者会和消费组中的其他成员共同协调,来得到分配给它的partition(每个消费者都会分配partition).这一切都是在你开始消费消息的时候被自动处理. 后面我们会向你展示如何使用assign API手动地分配partitions.但是要注意: 同一个消费者实例是不能混合自动和手动的partition分配.subscribe方法不是增量的:你必须包括你想要消费的完整的topics列表.你可以在任何时候修改订阅的topics集合.任何之前订阅的topics都会被新的列表替换.

#### 基本的poll循环

消费者需要并行地抓取数据,这是因为多个topics的多个partitions是分布在多个brokers上的.可以使用API的风格,类似于unix中的poll和select调用: 一旦topics注册在消费者实例上,所有将来的协调,平衡和数据获取都是通过在一个事件循环中调用一个poll方法来驱动的.这是一种简单而且高效的实现方式,可以只在一个线程中就能完成所有的IO请求.消费者订阅一个topic之后,你需要启动一个事件循环来得到partition的分配,并且开始抓取数据.看起来有点复杂,但你要做的仅仅只是在一个循环中调用poll,剩下的工作消费者自己会处理.每次poll调用都会返回分配给属于这个消费者的partitions的消息集.
下面的示例中展示了一个基本的poll循环,当消息到达的时候, 打印出offset和抓取到的记录的消息内容.

```
try {
  while (running) {
    ConsumerRecords<String, String> records = consumer.poll(1000);
    for (ConsumerRecord<String, String> record : records)
      System.out.println(record.offset() + ": " + record.value());
  }
} finally {
  consumer.close();
}
```

poll调用会返回基于当前位置的抓取记录(译注:每次抓取都会产生新的offset,下次抓取时,以新的offset为基础).当第一次创建消费组时,position的值会根据重置策略为每个partition设置为最早或最近的offset.当消费者开始提交offsets,在这之后的每次rebalance都会重置position为上一次提交的offset.传递给poll方法的参数控制了消费者在当前位置因为等待消息的到来而阻塞的最长时间.一旦有可用的记录(新的消息)消费者就会立即返回,如果没有可用的记录,则会一直等待直到超时才返回.消费者被设计为在自己的线程中运行,在没有外部同步的情况下,使用多线程是不安全的,不建议尝试使用.在本例中,我们使用了一个标志位,当应用程序关闭时,会从poll循环中跳出(译注:以类似钩子的方式).
当标志位被其他线程设置为false,事件循环会在poll返回时立即退出,不管返回什么记录,应用程序都会结束处理.当应用程序结束的时候,你应该总是要关闭消费者(译注:类似资源在使用后最终要释放,比如连接对象和文件句柄).这部分工作不仅仅是清理已经使用的socket连接,也确保了消费者及时通知协调者它已经从消费组中退出(要rebalance).
本例中使用了一个相对较小的timeout,来确保在关闭消费者时,不会有太多的延迟.
相应地,你可以设置较长的timeout,这时应该使用wakeup调用来从事件循环中退出.

```
try {
  while (true) {
    ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
    for (ConsumerRecord<String, String> record : records)
      System.out.println(record.offset() + “: ” + record.value());
  }
} catch (WakeupException e) {
  // ignore for shutdown
} finally {
  consumer.close();
}
```


上面的代码中,我们更改了timeout为Long.MAX_VALUE,意味着消费者会无限制地阻塞,直到有下一条记录返回的时候.这时如果使用标志位也是无法退出循环的,所以只能由触发关闭的线程调用consumer.wakeup来中断进行中的poll,这个调用会导致抛出WakeupException. wakeup在其他线程中调用是安全的(消费者线程中就这个方法是线程安全的).注意:如果当前没有活动的poll,这个异常会在下次调用是才会抛出.本例中我们捕获了这个异常防止它传播给上层调用.所以中断事件循环有两种方式:

+ 较小的timeout, 通过使用标志位来控制
+ 较长的timeout, 调用wakeup来退出循环

#### 完整的示例

下面的示例中,我们构建了一个Runnable任务,初始化消费者,订阅topics,执行poll无限循环,直到外部关闭这个消费者.

```
public class ConsumerLoop implements Runnable {
  private final KafkaConsumer<String, String> consumer;
  private final List<String> topics;
  private final int id;

  public ConsumerLoop(int id, String groupId,  List<String> topics) {
    this.id = id;
    this.topics = topics;
    Properties props = new Properties();
    props.put("bootstrap.servers", "localhost:9092");
    props.put(“group.id”, groupId);
    props.put(“key.deserializer”, StringDeserializer.class.getName());
    props.put(“value.deserializer”, StringDeserializer.class.getName());
    this.consumer = new KafkaConsumer<>(props);
  }
 
  @Override
  public void run() {
    try {
      consumer.subscribe(topics);

      while (true) {
        ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
        for (ConsumerRecord<String, String> record : records) {
          Map<String, Object> data = new HashMap<>();
          data.put("partition", record.partition());
          data.put("offset", record.offset());
          data.put("value", record.value());
          System.out.println(this.id + ": " + data);
        }
      }
    } catch (WakeupException e) {
      // ignore for shutdown 
    } finally {
      consumer.close();
    }
  }

  public void shutdown() {
    consumer.wakeup();
  }
}
```

```
# bin/kafka-topics.sh --create --topic consumer-tutorial --replication-factor 1 --partitions 3 --zookeeper localhost:2181
# bin/kafka-verifiable-producer.sh --topic consumer-tutorial --max-messages 200000 --broker-list localhost:9092
```

然后创建一个Driver客户端程序,设置一个消费组有三个成员,所有的消费者订阅了刚刚创建的相同的topic

```
public static void main(String[] args) { 
  int numConsumers = 3;
  String groupId = "consumer-tutorial-group"
  List<String> topics = Arrays.asList("consumer-tutorial");
  ExecutorService executor = Executors.newFixedThreadPool(numConsumers);

  final List<ConsumerLoop> consumers = new ArrayList<>();
  for (int i = 0; i < numConsumers; i++) {
    ConsumerLoop consumer = new ConsumerLoop(i, groupId, topics);
    consumers.add(consumer);
    executor.submit(consumer);
  }

  Runtime.getRuntime().addShutdownHook(new Thread() {
    @Override
    public void run() {
      for (ConsumerLoop consumer : consumers) {
        consumer.shutdown();
      } 
      executor.shutdown();
      try {
        executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        e.printStackTrace;
      }
    }
  });
}
```

这个示例提交了三个可运行的消费者线程给executor. 每个线程都有单独的编号,这样你就可以看到哪个线程接收了什么数据.
当停止Driver应用程序时,shutdown钩子会被调用(译注:这是在主线程里,而消费者的线程则是其他的线程,这里模拟了多线程),
就会通过wakeup停止三个消费者线程,然后等待它们关闭.
运行上面的程序,你会看到所有线程都会读取到数据, 下面是输出的一部分(第一个数字是消费者编号):

```
2: {partition=0, offset=928, value=2786}
2: {partition=0, offset=929, value=2789}
1: {partition=2, offset=297, value=891}
2: {partition=0, offset=930, value=2792}
1: {partition=2, offset=298, value=894}
2: {partition=0, offset=931, value=2795}
0: {partition=1, offset=278, value=835}
2: {partition=0, offset=932, value=2798}
0: {partition=1, offset=279, value=838}
1: {partition=2, offset=299, value=897}
1: {partition=2, offset=300, value=900}
1: {partition=2, offset=301, value=903}
1: {partition=2, offset=302, value=906}
1: {partition=2, offset=303, value=909}
1: {partition=2, offset=304, value=912}
0: {partition=1, offset=280, value=841}
2: {partition=0, offset=933, value=2801}

```

输出结果显示了所有三个partitions的消费情况.每个partition分配给其中的一个线程(正好三个线程三个partitions).在每个partition中,你会看到offset是不断增加的(译注:验证了同一个partition的offset是被顺序消费的).

#### 消费者的活跃度

作为消费组的一部分,每个消费者会被分配它订阅的topics的一部分partitions.就像在这些partitions上加了一个组锁.只要锁被持有,组中的其他成员就不会读取他们(译注:每个partition都对应唯一的消费者,partition锁只属于唯一的消费者).当你的消费者是正常状态时,当然是最好不过了,因为这是防止重复消费的唯一方式.但如果消费者失败了,你需要释放掉那个锁,这样可以将partitions分配给其他健康的成员.

kafka的消费组协调协议使用心跳机制解决了这个问题.在每次rebalance,所有当前generation的成员都会定时地发送心跳给group协调者.只要协调者持续接收到心跳,它会假设这个成员是健康的. 每次接收到心跳,协调者就开始或者重置计时器.如果时间超过了,没有收到消费者的心跳,协调者标记消费者为死亡状态,并触发组中其他的消费者重新加入,来重新分配partitions.计时器的时间间隔就是session timeout,即客户端应用程序中配置的session.timeout.ms，session timeout确保应用程序崩溃或者partition将消费者和协调者进行了隔离的情况下锁会被释放.

注意应用程序的失败(进程还存在)有点不同,因为消费者仍然会发送心跳给协调者,并不代表应用程序是健康的.消费者的轮询循环被设计为解决这个问题. 所有的网络IO操作在调用poll或者其他的阻塞API,都是在前台完成的.消费者并不使用任何的后台线程. 这就意味着消费者的心跳只有在调用poll的时候才会发送给协调者.如果应用程序停止polling(不管是处理代码抛出异常或者下游系统崩溃了),就不会再发送心跳了,最终就会导致session超时(没有收到心跳,计时器开始增加), 然后消费组就会开始平衡操作.唯一存在的问题是如果消费者处理消息花费的时间比session timeout还要长,就会触发一个假的rebalance.
可以通过设置更长的session timeout防止发生这样的情况.默认的超时时间是30秒,设置为几分钟也不是不行的.更长的session timeout的缺点是,协调者会花费较长时间才能检测到真正崩溃的消费者.

#### 消息发送语义

当消费组第一次创建时,初始offset会根据配置项auto.offset.reset策略设置. 一旦消费者开始处理消息,它会根据应用程序的需要正常滴提交offset(可以是设置自动提交offset,或者手动提交.可以将offset存储在kafka或者外部存储中).在之后的每一次rebalance,position都会被设置为在当前组中为这个partition最近提交的offset(即offset针对组级别).如果消费者已经成功处理了一批消息,但是为这批消息提交offsets之前崩溃了,其他消费者会接着最近提交的offset处重复工作.更加频繁地提交offsets,在发生崩溃的情况下重复消费消息的情况就越少发生(处理完消息后及时地提交offset是明智之举).目前为止,我们假设开启了自动提交offset的策略.当设置enable.auto.commit=true(这也是默认值),消费者会根据配置项auto.commit.interval.ms的值定时地触发自动提交offset的行为.通过减少提交时间间隔,你可以限制在发生崩溃事件时,消费者需要重新处理的消息数量(越经常提交,越不容易重复).如果要使用消费者的commit API,首先需要关闭自动提交的配置项:

```
props.put("enable.auto.commit", "false");

```

commit API很容易使用,但是怎么和poll循环结合起来才是关键. 下面的示例中包含了完整的循环逻辑,以及提交细节.手动方式处理commits最简单的方式是使用同步方式的提交API,下面的示例读取消息,处理消息,然后提交offsets.

```
try {
  while (running) {
    ConsumerRecords<String, String> records = consumer.poll(1000);
    for (ConsumerRecord<String, String> record : records)
      System.out.println(record.offset() + ": " + record.value());

    try {
      consumer.commitSync();
    } catch (CommitFailedException e) {
      // application specific failure handling
    }
  }
} finally {
  consumer.close();
}

```

使用不带参数的commitSync方法会在最近一次调用poll的返回值中提交offsets.这个方法是阻塞的(同步嘛),直到提交成功或者出现不可恢复的错误而失败.大部分情况下你需要关心的错误是消息处理的时间超过session timeout.这种情况发生时,协调者会将消费者从消费组中剔除出去,结果会抛出CommitFailedException.应用程序应该处理这种错误,比如尝试从上次成功提交的offset开始回滚任何因为消息消费引起的改变.通常情况下,你应该保证只有在消息成功被处理之后,才提交offset(但是offset是否能够成功完成是不一定的).如果消费者在提交offset之前崩溃了,那么已经成功处理的那部分消息(也是最近的消息)就不得不重新处理.如果提交策略能够保证最近提交的offset永远不会超过当前的position,你就能得到”至少一次”的消息发送语义.

通过更改提交策略使得当前position不会超过最近提交的offset(比如上图),你可以得到”最多一次”的语义.如果消费者在position赶上lastCommittedOffset之前就崩溃了(还没处理消息时就提前提交offset).那么这中间的那些消息就会丢失了(因此下次只会从lastCommitOffset开始,而不是current position).虽然有这样的缺点,但你能保证的是不会有消息被处理两次(所以说任何优点都是要牺牲一点代价的).
下面的示例中,只要更改提交offset和消息处理的顺序即可.

```
try {
  while (running) {
  ConsumerRecords<String, String> records = consumer.poll(1000);
  try {
    consumer.commitSync();
    for (ConsumerRecord<String, String> record : records)
      System.out.println(record.offset() + ": " + record.value());
    } catch (CommitFailedException e) {
      // application specific failure handling
    }
  }
} finally {
  consumer.close();
}
```

注意使用自动提交offset只提供”至少一次”的处理语义,因为消费者确保要提交的offsets的消息是已经返回给应用程序的.最坏情况下你需要重新处理的消息的数量是设置的提交间隔这段时间内的所有消息(因为没有提及offset需要重新处理消息).而使用commit API(即手动提交offset),你可以更加自由地控制在你可接受范围内重新处理的消息数量.
极端情况下,你可以在每条消息处理之后都提交一次offset(当然也是有代价的,就是更加频繁的IO操作):

```
try {
  while (running) {
    ConsumerRecords<String, String> records = consumer.poll(1000);
    try {
      for (ConsumerRecord<String, String> record : records) {
        System.out.println(record.offset() + ": " + record.value());
        consumer.commitSync(Collections.singletonMap(record.partition(), new OffsetAndMetadata(record.offset() + 1)));
      }
    } catch (CommitFailedException e) {
      // application specific failure handling
    }
  }
} finally {
  consumer.close();
}
```


在本例中,我们显示地传递要提交的offset给commitSync方法. committed offset总是应该是应用程序读取的下一条消息的offset.如果使用不带参数的commitSync,消费者会使用返回给应用程序的最近的offset+1作为提交的offset.这里不能使用它的原因是它会允许committed位置比实际处理的position要超前(而这里的情况刚好相反).很显然在每条消息处理之后都调用一次提交方法在大多数情况下并不是好的办法, 因为每次的提交请求发送给服务器,到返回结果之前处理消息的线程都不得不被阻塞住, 这当然是一大性能杀手. 更理想的方式应该是每个N条消息就提交一次,N可以为了更好的性能而调整
这个例子中的commitSync方法的参数是一个map,从topic partition到一个OffsetAndMetadata的实例.commit API允许你在每次提交时添加额外的元数据信息,比如记录提交的时间,发送请求的主机,或者应用程序需要的其他任何信息.替代提交每条接收到的消息的另外一种更理想的策略是当你完成处理每个partition的消息时才提交partition级别的offset.ConsumerRecords集合提供了访问其中的partitions集合的方法,以及访问每个partition的消息.下面代码模拟了这种策略.

```
try {
  while (running) {
    ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
    for (TopicPartition partition : records.partitions()) {
      List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);
      for (ConsumerRecord<String, String> record : partitionRecords)
        System.out.println(record.offset() + ": " + record.value());

      long lastoffset = partitionRecords.get(partitionRecords.size() - 1).offset();
      consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(lastoffset + 1)));
    }
  }
} finally {
  consumer.close();
}
```

目前为止我们主要专注于同步的提交API,消费者同时还暴露了一个异步的API: commitAsync.使用异步方式提交通常来说会获得更高的吞吐量,因为你的应用程序可以在提交返回之前开始处理下一批的消息.不过它的代价是你只能在之后的某个时刻才能发现有些commit可能是失败的(异步+回调是一种很好的结合).

```
try {
  while (running) {
    ConsumerRecords<String, String> records = consumer.poll(1000);
    for (ConsumerRecord<String, String> record : records)
      System.out.println(record.offset() + ": " + record.value());

    consumer.commitAsync(new OffsetCommitCallback() {
      @Override
      public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets,  Exception exception) {
        if (exception != null) {
          // application specific failure handling
        }
      }
    });
  }
} finally {
  consumer.close();
}
```

我们提供了一个回调函数给commitAsync,它会在消费者完成提交动作之后被调用(不管是提交成功还是失败都会调用).
当然如果你不关心提交的结果,你可以使用没有参数的commitAsync.


关于提交的异步和同步，在另一篇[文章](http://zqhxuyuan.github.io/2016/10/27/Kafka-Definitive-Guide-cn-04/)有提到同步和异步结合。

```
try {
  while (true) {
    ConsumerRecords<String, String> records = consumer.poll(100);
    for (ConsumerRecord<String, String> record : records) {
      //处理记录，略
    }
    consumer.commitAsync(); //1
  }
} catch (Exception e) {
  log.error("Unexpected error", e);
} finally {
  try {
    consumer.commitSync(); //2
  } finally {
    consumer.close();
  }
}
```

还提到了平衡监听器，这里也粘下来吧。

前面章节中说过提交偏移量时，消费者会在分区平衡之前或者退出时执行一些清理工作。如果你知道消费者即将失去一个分区的所有权，你应当要提交已处理完最近事件的偏移量。如果你的消费者维护了一个事件缓冲区，并且偶尔才会处理一次（比如在使用pause()功能时会使用currentRecords字典暂存记录），你也应当在失去分区的所有权之前处理目前为止收集的所有事件。也许还需要做其他的工作比如关闭文件句柄，释放数据库连接等等。

消费者API允许你在消费者所属的分区被添加和移除时，运行自定义的代码逻辑。可以通过在调用subscribe()方法时传递一个ConsumerRebalanceListener监听器来完成，该监听器接口有两个需要的方法：

+ public void onPartitionsRevoked(Collection<TopicPartition> partitions)

	会在平衡开始之前以及消费者停止消费消息之后调用。在这里通常要提交偏移量，这样无论下一个消费者是谁，它获得到分区后，就知道要从哪里开始。

+ public void onPartitionsAssigned(Collection<TopicPartition> partitions)

	会在分区重新分配给消费者之后，在消费者开始消费消息之前调用。

下面的示例展示了如何使用onPartitionsRevoked()方法在失去一个分区的所有权之前提交偏移量。后面我们会展示同时模拟使用了onPartitionsAssigned()方法的更复杂示例。

```
private Map<TopicPartition, OffsetAndMetadata> currentOffsets;

private class HandleRebalance implements ConsumerRebalanceListener { //1
  public void onPartitionsAssigned(Collection<TopicPartition> partitions){//2
  }

  public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
    consumer.commitSync(currentOffsets); //3
  }
}

try {
  consumer.subscribe(topics, new HandleRebalance()); //4

  while (true) {
    ConsumerRecords<String, String> records = consumer.poll(100);
    for (ConsumerRecord<String, String> record : records) {
      //处理记录，略
      currentOffsets.put(
            new TopicPartition(record.topic(), record.partition()),
            record.offset());
    }
    consumer.commitAsync(currentOffsets);
  }
} catch (WakeupException e) {
  // ignore, we're closing
} catch (Exception e) {
  log.error("Unexpected error", e);
} finally {
  try {
    consumer.commitSync(currentOffsets);
  } finally {
    consumer.close();
  }
}
```


不过Kafka API还允许你定位到指定的位置（在谈到提交时我们会说提交偏移量，在谈到定位时我们会说位置，位置这个概念用在现实生活中表示要到哪个地方，而偏移量更多表示的是处于一种什么状态，提交时主要关注的是状态数据，当然你不需要纠结这么多，位置和偏移量其实是相同的概念）。这种特性可以用在很多地方，比如回退几个消息重新处理，或者跳过一些消息（也许是一个时间敏感的应用程序，如果数据处理的进度落后太多时，你会想跳到最近的时间点，因为这些消息更能表示相关的当前状态）。但这种特性最令人兴奋的一个用例是：将偏移量存储到其他系统而不是Kafka中。

考虑下面的通用场景：应用程序从Kafka中读取事件（也许是一个网站的用户点击流）、处理数据（也许是清理机器点击，添加会话信息），然后存储结果到数据库、NoSQL或者Hadoop。假设我们真的不希望丢失任何数据，也不希望存储两份相同的数据。

如果将记录存储在数据库而不是Kafka，我们的应用程序怎么知道要从分配分区的哪里开始读取？这就是seek()方法发挥作用的地方。当消费者启动或者分配到新的分区，可以先去数据库中查询分区的最近偏移量，然后通过seek()方法定位到这个位置。
下面的代码是这种做法的基本骨架，我们使用了ConsumerRebalanceLister监听器和seek()方法，来确保从数据库中存储的偏移量开始处理。

```
//消费者平衡的监听器
public class SaveOffsetsOnRebalance implements ConsumerRebalanceListener{
  public void onPartitionsRevoked(Collection<TopicPartition> partitions){
    commitDBTransaction(); //1
  }
  public void onPartitionsAssigned(Collection<TopicPartition> partitions){
    for(TopicPartition partition: partitions)
      consumer.seek(partition, getOffsetFromDB(partition)); //2
    }
  }
}

//消费者主逻辑
consumer.subscribe(topics, new SaveOffsetOnRebalance(consumer));
consumer.poll(0);

for (TopicPartition partition: consumer.assignment())
  consumer.seek(partition, getOffsetFromDB(partition));   //3

while (true) {
  ConsumerRecords<String, String> records = consumer.poll(100);
  for (ConsumerRecord<String, String> record : records) {
    processRecord(record);
    storeRecordInDB(record);
    storeOffsetInDB(record.topic(),record.partition(),record.offset());//4
  }
  commitDBTransaction();
}
```

1. 我们使用了一个虚构的方法来保证提交事务操作到数据库中。这里的考虑是在处理记录的时候会插入记录和偏移量到数据库中，所以我们只需要在即将事务分区的所有权时提交这个事务，来确保这些信息被持久化。
2. 我们还有一个虚构的方法会从数据库中读取分区的偏移量，然后在获得新分区的所有权时通过消费者的seek()方法定位到这些记录。
3. 当消费者订阅主题并第一次启动时，立即调用一次无阻塞的poll(0)方法，来确保加入消费组，并且得到分配的分区。然后紧接着调用seek()定位到分配给我们（当前消费者）的分区的正确位置。注意seek()仅仅更新了我们要从哪里开始消费的位置，所以下一次调用poll()才会开始拉取正确的消息。如果在seek()时发生错误（比如偏移量不存在），调用poll()时就会抛出异常。
4. 又一个虚构的方法，这次我们更新了数据库中存储偏移量的一张表。这里我们假设更新记录的操作很快就完成了，所以我们在每条记录上都执行了更新操作。不过提交偏移量是比较慢的，所以我们只在一批数据都处理完成后才执行提交操作。不过这里面仍然有很多优化的方法。

<font color=purple>旁白</font>一开始看这段代码的时候，困惑在<font color=orange>为什么seek两次（2和3）</font>,后来仔细想想，2为**rebalance**的时候触发的seek,3为最开始初始化时候触发的seek,感觉自己说了两句废话。好吧，再说两句废话，消费者订阅topic, seek(2)的这一步可视为初始化，消费者组里rebalance的时候并不会重新订阅，重新初始化，正常来说代码已经运行到while循环里了，这时候是3的seek起作用，所以2，3作用的时机是不一样的，虽然写了两次。

#### 消费组检查

当一个消费组是活动的状态时,你可以通过命令行consumer-groups.sh检查partition的分配情况,以及消费进度.

```
 bin/kafka-consumer-groups.sh --new-consumer --describe --group consumer-tutorial-group --bootstrap-server localhost:9092

```

输出结果是这样的:

```
GROUP, TOPIC, PARTITION, CURRENT OFFSET, LOG END OFFSET, LAG, OWNER
consumer-tutorial-group, consumer-tutorial, 0, 6667, 6667, 0, consumer-1_/127.0.0.1
consumer-tutorial-group, consumer-tutorial, 1, 6667, 6667, 0, consumer-2_/127.0.0.1
consumer-tutorial-group, consumer-tutorial, 2, 6666, 6666, 0, consumer-3_/127.0.0.1

```

上面显示了分配给消费组的所有partitions,哪个consumer拥有了partition,partition最近提交的offset(current offset).partition的lag指的是log end offset和last committed offset的差距. 管理人员可以监视这些来确保消费组能赶上生产者.(译注:生产者写入消息,LEO会增加,消费者提交offset,会增加LCO,两者差距小说明消费者的消费速度能赶上生产者的生产速度)

#### 使用手动分配

在本篇文章开始前,我们提到新的消费者针对不需要消费组的场景实现了低级API,旧的消费者使用SimpleConsumer可以实现,但是它需要自己做很多的工作来处理错误处理. 现在使用新的消费者,你只需要分配你要读取的partitions,然后开始polling数据.下面的示例展示了如何分配一个topic的所有partitions(当然也可以静态分配一部分partitions给消费者).

译注:在旧的消费者中,高级API使用消费组提供的语义, 而低级API使用SimpleConsumer. 而新的消费者仍然统一使用poll方式.

```
List<TopicPartition> partitions = new ArrayList<>();
for (PartitionInfo partition : consumer.partitionsFor(topic))
  partitions.add(new TopicPartition(topic, partition.partition()));
consumer.assign(partitions);
```

和<font color=red>subscribe</font>类似,调用<font color=red>assign</font>的参数必须传递你要读取的所有partitions(订阅是指定你要读取的所有topics).一旦partitions被分配了(subscribe是让消费组动态分配partitions),poll循环和之前的方式是一模一样的.有一点要注意的是,所有offset提交请求都会经过group coordinator,不管是SimpleConsumer还是Consumer Group.所以如果你要提交offset,你还是必须要指定正确的group.id,防止和其他的消费者实例的group id发生冲突.如果一个simple consumer尝试提交offset,它的group id和一个活动的consumer group相同,协调者会拒绝这个提交.但是如果另外一个simple consumer实例和当前同样是simple consumer的实例有相同的group id,则是不会有问题的.

译注:消费组有group id,而simple consumer也会指定group id,但是simple consumer的group id不是指消费组.消费组和simple consumer是消费者消费消息的两种不同的实现,一个是high-level,一个是low-level.


