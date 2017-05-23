package test.tool;

import java.util.Arrays;
import java.util.Map;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.trident.TridentTopology;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.spout.IBatchSpout;
import org.apache.storm.trident.testing.FixedBatchSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

public class ConcurrencyRedis {
	
	public static class MySpout implements IBatchSpout {

		@Override
		public void open(Map conf, TopologyContext context) {
			
		}

		@Override
		public void emitBatch(long batchId, TridentCollector collector) {
			for (int i = 0; i < 200; i++) {
				collector.emit(new Values(""));
			}
		}

		@Override
		public void ack(long batchId) {
			
		}

		@Override
		public void close() {
			
		}

		@Override
		public Map<String, Object> getComponentConfiguration() {
			return null;
		}

		@Override
		public Fields getOutputFields() {
			return new Fields("msg");
		}
		
	}
	
	public static void main(String[] args) {
		TridentTopology buidler = new TridentTopology();
		buidler.newStream("xx", new MySpout())
		.shuffle()
		.each(new Fields("msg"), new RedisTestFunction(), new Fields("")).parallelismHint(30);
		
		LocalCluster cluter=  new LocalCluster();
		Config config = new Config();
		cluter.submitTopology("aa", config, buidler.build());
		
	}
}
