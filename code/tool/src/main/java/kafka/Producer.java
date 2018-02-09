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
    public final KafkaProducer<Integer, String> producer;
    private  String topic;
    private final Boolean isAsync;
    private static AtomicInteger messageNo = new AtomicInteger(100);

    public Producer(String topic, Boolean isAsync) {
        Properties props = new Properties();
        props.put("bootstrap.servers", KafkaProperties.KAFKA_SERVER_URL + ":" + KafkaProperties.KAFKA_SERVER_PORT);
        props.put("client.id", "DemoProducer");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<>(props);
        this.topic = topic;
        this.isAsync = isAsync;
    }

    public void setTopic(String topic) {
        this.topic = topic;
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

    public void sendMsg(String key, String value) {
        long startTime = System.currentTimeMillis();
        if (isAsync) { // Send asynchronously
            producer.send(new ProducerRecord(topic,
                    key,
                    value), new ProducerCallback(startTime, 0, value));
        } else { // Send synchronously
            try {
                producer.send(new ProducerRecord(topic,
                        key,
                        value)).get();
                System.out.println("Sent message: (" + key + ", " + value + ")");

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
//        testAsyncTrue();
//        testAsyncFalse();
        Producer producer = new Producer("consumerTestForUneven", false);
//        producer.setTopic("ag_sensor");
        //{}
//        producer.sendMsg("ag_sensor", "{\"pm25\": 40, \"sensor_id\": 19}");
//        {"actions":[{"action":"predict"}],"serial":"test1234","context":"asdfe","return_partitions":[1,2],"service_id":1}
//        {"actions":[{"info":{"type":11,"dial":0.3},"action":"control"}],"serial":"test1234","context":"asdfe","return_partitions":[1,2],"service_id":1}
        producer.setTopic("ld_control_result");
        producer.sendMsg("ld_control_result", "{\"result\": 101,\"serial\":\"ldctrl665247824\",\"context\":\"asdfe\", \"details\":[]}");
//        producer.sendMsg("ld_control_result","{\"actions\":[{\"info\":{\"type\":11,\"dial\":0.3},\"action\":\"control\"}],\"serial\":\"test1234\",\"context\":\"asdfe\",\"return_partitions\":[0],\"service_id\":1}");
        producer.producer.close();
    }
}
