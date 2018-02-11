package storm;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.trident.TridentTopology;
import org.apache.storm.trident.operation.Function;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.operation.TridentOperationContext;
import org.apache.storm.trident.spout.IBatchSpout;
import org.apache.storm.trident.tuple.TridentTuple;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.util.Map;

public class BasicTest {

    public static void main(String[] args) {
        TridentTopology tridentTopology = new TridentTopology();
        tridentTopology.newStream("st-am", new Spout())
                .each(new Fields("key"), new Out(), new Fields());
        new LocalCluster().submitTopology("s", new Config(), tridentTopology.build());
    }


    public static class Spout implements IBatchSpout{

        @Override
        public void open(Map conf, TopologyContext context) {

        }

        @Override
        public void emitBatch(long batchId, TridentCollector collector) {
            String[] a = new String[]{"a", "b", "c", "d"};
            collector.emit(new Values(a[(int) (Math.random()*100) % a.length]));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
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
            return new Fields("key");
        }
    }

    public static class Out implements Function {

        @Override
        public void execute(TridentTuple tuple, TridentCollector collector) {
            System.out.println(tuple.getString(0));
            if (tuple.getString(0).equals("b")) {
                try {
                    throw new Exception();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void prepare(Map conf, TridentOperationContext context) {

        }

        @Override
        public void cleanup() {

        }
    }
}
