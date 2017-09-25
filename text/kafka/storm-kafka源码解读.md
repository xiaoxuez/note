## storm-kafka源码分析

### Producer

##### 回顾一下普通的Kafka Producer吧。

+ 配置项

```
	Properties props = new Properties();
	props.put("bootstrap.servers", KafkaProperties.KAFKA_SERVER_URL + ":" + KafkaProperties.KAFKA_SERVER_PORT);
	props.put("client.id", "DemoProducer");
	props.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
	props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
```

+ send msg

```
	 producer = new KafkaProducer<>(props);
	 // Async
	 producer.send(new ProducerRecord<>(topic,
	                    messageNo.getAndIncrement(),
	                    messageStr), Callback);
	//sync
	producer.send(new ProducerRecord<>(topic,
	                    messageNo.getAndIncrement()).get()
```

##### KafkaBolt

+ 配置项

```
 private static Properties newProps(final String brokerUrl, final String topicName) {
        return new Properties() {{
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
            put(ProducerConfig.CLIENT_ID_CONFIG, topicName);
        }};
    }
```

+ send msg

```
    Callback callback = null;
	
	if (!fireAndForget && async) {
	    callback = new Callback() {
	        @Override
	        public void onCompletion(RecordMetadata ignored, Exception e) {
	        //加入ack机制。callback回调是异步的，所以需要synchronized
	            synchronized (collector) {
	                if (e != null) {
	                    collector.reportError(e);
	                    collector.fail(input);
	                } else {
	                    collector.ack(input);
	                }
	            }
	        }
	    };
	}
	Future<RecordMetadata> result = producer.send(new ProducerRecord<K, V>(topic, key, message), callback);
	if (!async) {
	    try {
	        result.get();
	        // 加入ack机制。 future.get是同步的 故不需要添加同步锁
	        collector.ack(input);
	    } catch (ExecutionException err) {
	        collector.reportError(err);
	        collector.fail(input);
	    }
	} else if (fireAndForget) {
	    collector.ack(input);
	}

```

在基本producer的基础上加入了storm中的可靠性机制，ack/fail。其余的无异。

#### Trident State

在storm-kafka-client中，还提供了扮演state角色的kafka producer。在TridentKafkaState中定义了方法updateState，进行处理消息的send。另外TridentKafkaStateFactory类作为TridentKafkaState的Factory。下面主要剖析一下updateState方法。

```
//Trident是以小批量（batch）的形式在处理tuple
public void updateState(List<TridentTuple> tuples, TridentCollector collector) {
        String topic = null;
        try {
         long startTime = System.currentTimeMillis();
	     int numberOfRecords = tuples.size();
	     List<Future<RecordMetadata>> futures = new ArrayList<>(numberOfRecords);
            for (TridentTuple tuple : tuples) {
            	//获取topic -- 1
                topic = topicSelector.getTopic(tuple);
             //从tuple中读取信息 -- 2
                Object messageFromTuple = mapper.getMessageFromTuple(tuple);
             //从tuple中读取key -- 3
		 Object keyFromTuple = mapper.getKeyFromTuple(tuple);
				...
              // send 消息
		      Future<RecordMetadata> result = producer.send(new ProducerRecord(topic,keyFromTuple, messageFromTuple));
		      futures.add(result);
		  	
		  	... 
            int emittedRecords = futures.size();
            List<ExecutionException> exceptions = new ArrayList<>(emittedRecords);
            for (Future<RecordMetadata> future : futures) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    exceptions.add(e);
                }
            }
			
		  ... check and log error 
   }
```

注意的是以batch的形式处理，中间就可能有多个消息，采用的是future.get获得返回结果，是依次发，发完了再依次等待返回。而不是一边发一边等。

**1.topicSelectorgetTopic的实现KafkaTopicSelector** 

```
public class DefaultTopicSelector implements KafkaTopicSelector {
    private static final long serialVersionUID = -1172454882072591493L;
    private final String topicName;

    public DefaultTopicSelector(final String topicName) {
        this.topicName = topicName;
    }

    @Override
    public String getTopic(TridentTuple tuple) {
        return topicName;
    }
}
```   

**2.3.mapper的实现TridentTupleToKafkaMapper**

```
public class FieldNameBasedTupleToKafkaMapper<K, V> implements TridentTupleToKafkaMapper {

    public final String keyFieldName;
    public final String msgFieldName;

    public FieldNameBasedTupleToKafkaMapper(String keyFieldName, String msgFieldName) {
        this.keyFieldName = keyFieldName;
        this.msgFieldName = msgFieldName;
    }

    @Override
    public K getKeyFromTuple(TridentTuple tuple) {
        return (K) tuple.getValueByField(keyFieldName);
    }

    @Override
    public V getMessageFromTuple(TridentTuple tuple) {
        return (V) tuple.getValueByField(msgFieldName);
    }
}

```

如上是默认的select和map的实现，如果需要自定义select和map，实现对应的接口即可。



### Consumer

