package storm;

import org.apache.storm.kafka.StringScheme;
import org.apache.storm.kafka.ZkHosts;
import org.apache.storm.kafka.trident.OpaqueTridentKafkaSpout;
import org.apache.storm.kafka.trident.TridentKafkaConfig;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.trident.TridentTopology;
import org.apache.storm.tuple.Fields;


public class KafkaTest {
	public static void main(String[] args) {
		TridentTopology topology = new TridentTopology();
		TridentKafkaConfig kafkaConfig = new TridentKafkaConfig(new ZkHosts("127.0.0.1:2181"), "log-analysis", "storm");
		kafkaConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
		
		OpaqueTridentKafkaSpout kafkaS = new OpaqueTridentKafkaSpout(kafkaConfig);
		
		topology.newStream("kafka-stream", kafkaS)
			.each(new Fields("str"), new OutputFunction(), new Fields(""));
	}
}
