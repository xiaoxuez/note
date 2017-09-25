package kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by xiaoxuez on 2017/8/9.
 *
 * Kafka Producer
 */
public class Producer {
    private final KafkaProducer<Integer, String> producer;
    private final String topic;
    private final Boolean isAsync;
    private static AtomicInteger messageNo = new AtomicInteger(100);

    public Producer(String topic, Boolean isAsync) {
        Properties props = new Properties();
        props.put("bootstrap.servers", KafkaProperties.KAFKA_SERVER_URL + ":" + KafkaProperties.KAFKA_SERVER_PORT);
        props.put("client.id", "DemoProducer");
        props.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<>(props);
        this.topic = topic;
        this.isAsync = isAsync;
    }


    public void sendMsg() {
        String messageStr = "Message_" + messageNo;
        long startTime = System.currentTimeMillis();
        if (isAsync) { // Send asynchronously
            producer.send(new ProducerRecord<>(topic,
                    messageNo.getAndIncrement(),
                    messageStr), new ProducerCallback(startTime, messageNo.get(), messageStr));
        } else { // Send synchronously
            try {
                producer.send(new ProducerRecord<>(topic,
                        messageNo.getAndIncrement(),
                        messageStr)).get();
                System.out.println("Sent message: (" + messageNo.get() + ", " + messageStr + ")");

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * async的作用呢，是send之后返回是异步还是阻塞。所以，如果是异步的话，就需要等.....不然还没发出去程序就跑完了..
     * 发送是新线程....
     */
    public static void testAsyncTrue() throws InterruptedException {
        Producer producer = new Producer("consumerTestForUneven", true);
        producer.sendMsg();
        Thread.sleep(1000);
    }


    public static void testAsyncFalse() {
        Producer producer = new Producer("consumerTestForUneven", false);
        producer.sendMsg();
    }

    public static void main(String[] args) throws InterruptedException {
        testAsyncTrue();
//        testAsyncFalse();
    }
}
