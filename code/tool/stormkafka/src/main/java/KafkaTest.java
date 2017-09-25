import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;
import org.apache.storm.kafka.spout.KafkaSpoutRetryExponentialBackoff;
import org.apache.storm.kafka.spout.KafkaSpoutRetryService;
import org.apache.storm.kafka.spout.trident.KafkaTridentSpoutOpaque;
import org.apache.storm.trident.TridentTopology;
import org.apache.storm.tuple.Fields;

import java.util.concurrent.TimeUnit;


public class KafkaTest {
	public static void main(String[] args) {
		TridentTopology topology = new TridentTopology();

        KafkaTridentSpoutOpaque kafkaS = new KafkaTridentSpoutOpaque(newKafkaSpoutConfig());

		topology.newStream("kafka-stream", kafkaS)
			.each(new Fields("topic", "partition", "offset", "key", "value"), new OutputFunction(), new Fields(""));

        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("kafka-test", new Config(), topology.build());
	}


	public static KafkaSpoutConfig<String,String> newKafkaSpoutConfig() {
        return KafkaSpoutConfig.builder("localhost:9092", "TcpReceive")
				.setMaxPartitionFectchBytes(200)
				.setGroupId("kafkaSpoutTestGroup_" + System.nanoTime())
//				.setRecordTranslator(JUST_VALUE_FUNC, new Fields("str"))
				.setRetry(newRetryService())
				.setOffsetCommitPeriodMs(10_000)
				.setFirstPollOffsetStrategy(KafkaSpoutConfig.FirstPollOffsetStrategy.EARLIEST)
				.setMaxUncommittedOffsets(250)
                .build();
    }

	protected static KafkaSpoutRetryService newRetryService() {
		return new KafkaSpoutRetryExponentialBackoff(new KafkaSpoutRetryExponentialBackoff.TimeInterval(500L, TimeUnit.MICROSECONDS),
				KafkaSpoutRetryExponentialBackoff.TimeInterval.milliSeconds(2), Integer.MAX_VALUE, KafkaSpoutRetryExponentialBackoff.TimeInterval.seconds(10));
	}
}
