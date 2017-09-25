package kafka;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * Created by xiaoxuez on 2017/8/9.
 */
public class ProducerCallback implements Callback {

    private final long startTime;
    private final int key;
    private final String message;


    public ProducerCallback(long startTime, int key, String message) {
        this.startTime = startTime;
        this.key = key;
        this.message = message;
    }


    @Override
    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
        long elapsedTime = System.currentTimeMillis() - startTime;
        if (recordMetadata != null) {
            System.out.println(
                    "message(" + key + ", " + message + ") sent to partition(" + recordMetadata.partition() +
                            "), " +
                            "offset(" + recordMetadata.offset() + ") in " + elapsedTime + " ms");
        } else {
            e.printStackTrace();
        }
    }
}
