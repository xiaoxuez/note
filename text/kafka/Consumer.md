

## Consumer

High Level Consumer将从某个Partition读取的最后一条消息的offset存于Zookeeper中. 这个offset基于Consumer Group的名称.
Consumer Group是整个Kafka集群全局的. 所以要特别小心在新的逻辑启动之前要关闭所有的旧的逻辑(消费者进程).
当**新的消费者**加入同一个消费组时,Kafka会添加这个消费者的线程到要消费的topic的可用线程集合中,并且触发re-balance.

High Level Consumer可以(应该)是多线程的应用程序.线程模型是以topic的partitions数量为中心的,不过有些规则:

+ 如果线程数量多于partition的数量，有部分线程无法消费该topic下任何一条消息
+ 如果线程数量少于partition的数量，有一些线程会消费多个partition的数据
+ 如果线程数量等于partition的数量，则正好一个线程消费一个partition的数据

当添加更多的消费者进程/线程会触发re-balance,导致partition的分配发生了变化。

Consumer及Consumer Group: 

+ 同一条消息会被多个ConsumerGroup消费,所以有多个ConsumerGroup,每个ConsumerGroup只有一个Consumer,实现了广播.
通常不同的ConsumerGroup的消费处理逻辑是不同的,这样同一份数据源(消息)交给不同的处理逻辑.
+ 一个ConsumerGroup有多个Consumer,一条消息只会被这个ConsumerGroup的一个消费者所消费.实现了单播.

实现示例1：

```
ConsumerConfig conf = new ConsumerConfig(props);
//这里的ConsumerConnector其实是ZookeeperConsumerConnector
ConsumerConnector consumer = kafka.consumer.Consumer.createJavaConsumerConnector(conf);
//消费的topic和对应的线程数
Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
topicCountMap.put(topic, new Integer(1));
//消息流,每个消费者线程都对应了一个消息流,消息会放入消息流的阻塞队列中
Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
//每个消费者线程都对应了一个消息流,故一个topic可能存在多个流，跟线程数量有关
List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);
KafkaStream<byte[], byte[]> stream = streams.get(0); 

消费者迭代器,只有迭代器开始迭代获取数据时,才会返回给消费者
ConsumerIterator<byte[], byte[]> it = stream.iterator();
while (it.hasNext()){
    System.out.println("message: " + new String(it.next().message()));
}
```

```
//迭代器期望是线程的，如
public class ConsumerTest implements Runnable {
    private KafkaStream m_stream;
    private int m_threadNumber;
 
    public ConsumerTest(KafkaStream a_stream, int a_threadNumber) {
        m_threadNumber = a_threadNumber;
        m_stream = a_stream;
    }
 
    public void run() {
        ConsumerIterator<byte[], byte[]> it = m_stream.iterator();
        while (it.hasNext())
            System.out.println("Thread " + m_threadNumber + ": " + new String(it.next().message()));
        System.out.println("Shutting down Thread: " + m_threadNumber);
    }
}
```

```
//多线程的情况下创建线程池帮忙管理

public void run(int a_numThreads) {
    Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
    topicCountMap.put(topic, new Integer(a_numThreads));
    Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
    List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);
 
 
    // now launch all the threads
    //
    executor = Executors.newFixedThreadPool(a_numThreads);
 
    // now create an object to consume the messages
    //
    int threadNumber = 0;
    for (final KafkaStream stream : streams) {
        executor.execute(new ConsumerTest(stream, threadNumber));
        threadNumber++;
    }
}
```

**最后，线程应该正常退出。另外，启用自动提交后，对consumer.shutdown（）的调用将提交最终的偏移量。**

上述的完整源代码

```
package com.test.groups;
 
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
 
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
 
public class ConsumerGroupExample {
    private final ConsumerConnector consumer;
    private final String topic;
    private  ExecutorService executor;
 
    public ConsumerGroupExample(String a_zookeeper, String a_groupId, String a_topic) {
        consumer = kafka.consumer.Consumer.createJavaConsumerConnector(
                createConsumerConfig(a_zookeeper, a_groupId));
        this.topic = a_topic;
    }
 
    public void shutdown() {
        if (consumer != null) consumer.shutdown();
        if (executor != null) executor.shutdown();
        try {
            if (!executor.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                System.out.println("Timed out waiting for consumer threads to shut down, exiting uncleanly");
            }
        } catch (InterruptedException e) {
            System.out.println("Interrupted during shutdown, exiting uncleanly");
        }
   }
 
    public void run(int a_numThreads) {
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, new Integer(a_numThreads));
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);
 
        // now launch all the threads
        //
        executor = Executors.newFixedThreadPool(a_numThreads);
 
        // now create an object to consume the messages
        //
        int threadNumber = 0;
        for (final KafkaStream stream : streams) {
            executor.submit(new ConsumerTest(stream, threadNumber));
            threadNumber++;
        }
    }
 
    private static ConsumerConfig createConsumerConfig(String a_zookeeper, String a_groupId) {
        Properties props = new Properties();
        props.put("zookeeper.connect", a_zookeeper);
        props.put("group.id", a_groupId);
        props.put("zookeeper.session.timeout.ms", "400");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
 
        return new ConsumerConfig(props);
    }
 
    public static void main(String[] args) {
        String zooKeeper = args[0];
        String groupId = args[1];
        String topic = args[2];
        int threads = Integer.parseInt(args[3]);
 
        ConsumerGroupExample example = new ConsumerGroupExample(zooKeeper, groupId, topic);
        example.run(threads);
 
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ie) {
 
        }
        example.shutdown();
    }
}
```


恩...以上是旧版本的api，0.9.0之后的新版本中，管理消费者组不再通过zookeeper，所以新的消费者完全使用java编写,它不再依赖scala运行时环境和zookeeper。

具体解释和功能见使用新Kafka消费客户端。

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

