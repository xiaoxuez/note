package kafka;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by xiaoxuez on 2017/8/10.
 */
public class Consumer implements  Runnable{


//    public void simpleConsumerDemo() {
//        SimpleConsumer simpleConsumer = new SimpleConsumer(KafkaProperties.KAFKA_SERVER_URL,
//                KafkaProperties.KAFKA_SERVER_PORT,
//                KafkaProperties.CONNECTION_TIMEOUT,
//                KafkaProperties.KAFKA_PRODUCER_BUFFER_SIZE,
//                KafkaProperties.CLIENT_ID);
//    }

    private final KafkaConsumer<String, String> consumer;
    private final List<String> topics;
    private final int id;

    public Consumer(int id, String groupId,  List<String> topics) {
        this.id = id;
        this.topics = topics;
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", groupId);
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        props.put("max.partition.fetch.bytes", 2000);
        this.consumer = new KafkaConsumer<>(props);

       // 2000 OnOrOffline 46 TcpReceive 30 TcpReceive + OnOrOffline = 76,所以结论是估计跟partition相关

    }

    @Override
    public void run() {
        try {
            consumer.subscribe(topics, new ConsumerRebalanceListener() {
                @Override
                public void onPartitionsRevoked(Collection<TopicPartition> collection) {

                }

                @Override
                public void onPartitionsAssigned(Collection<TopicPartition> collection) {
                    consumer.seekToBeginning(collection);
                    System.out.println("========= ");
                }
            });

            consumer.poll(0);

            consumer.seekToBeginning(consumer.assignment());



            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(200);

                for (ConsumerRecord<String, String> record : records) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("partition", record.partition());
                    data.put("offset", record.offset());
                    data.put("value", record.value());
                    data.put("topic", record.topic());
//                    System.out.println(record.serializedValueSize() + " " + record.serializedKeySize());
                    System.out.println(this.id + ": " + data);
                }
                System.out.println(" seek number :" + records.count());
                consumer.commitAsync();
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

//    bin/kafka-topics.sh --create --topic consumer-tutorial --replication-factor 1 --partitions 3 --zookeeper localhost:2181
    public static void main(String[] args) {
        int numConsumers = 5;
        String groupId = "consumer-testx-group";
        List<String> topics = Arrays.asList("OnOrOffline", "TcpReceive");
        ExecutorService executor = Executors.newFixedThreadPool(numConsumers);

        final List<Consumer> consumers = new ArrayList<>();
//        for (int i = 0; i < numConsumers; i++) {
            //一个消费者一个线程，对应这个场景3个partition，刚好一个partition
            Consumer consumer = new Consumer(0, groupId, topics);
            consumers.add(consumer);
            executor.submit(consumer);
//        }
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (Consumer consumer : consumers) {
                    consumer.shutdown();
                }
                executor.shutdown();
                try {
                    executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
