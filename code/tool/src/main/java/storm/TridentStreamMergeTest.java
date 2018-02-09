package storm;

import clojure.lang.Obj;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.trident.Stream;
import org.apache.storm.trident.TridentTopology;
import org.apache.storm.trident.operation.Function;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.operation.TridentOperationContext;
import org.apache.storm.trident.spout.IBatchSpout;
import org.apache.storm.trident.tuple.TridentTuple;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by xiaoxuez on 2017/11/10.
 */
public class TridentStreamMergeTest {


    public static void main(String[] args) {
        TridentTopology tridentTopology = new TridentTopology();
        Stream stream1 = tridentTopology.newStream("one", new OneStream())
                .each(new Fields("one-msg"), new OutputFunction(), new Fields("one-out"));

        Stream stream2 =  tridentTopology.newStream("two", new TwoStream())
                .each(new Fields("two-msg"), new OutputFunction(), new Fields("two-out"));

        tridentTopology.merge(stream2, stream1)
                .each(new Fields("two-out"), new OutputFunction(), new Fields());
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("merge-test", new Config(), tridentTopology.build());

    }


    public static class OneStream implements IBatchSpout {

        @Override
        public void open(Map conf, TopologyContext context) {

        }

        @Override
        public void emitBatch(long batchId, TridentCollector collector) {
            collector.emit(new Values("ABCDEF"));
            Utils.sleep(1000);
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
            return new Fields("one-msg");
        }
    }


    public static class TwoStream implements IBatchSpout {

        @Override
        public void open(Map conf, TopologyContext context) {

        }

        @Override
        public void emitBatch(long batchId, TridentCollector collector) {
            String[] content = {"1", "2", "3", "4", "5", "6"};
            String c = content[(int) (Math.random() * 100 % 6)];
            collector.emit(new Values(c));

            Utils.sleep(5000);
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
            return new Fields("two-msg");
        }
    }


    public static class OutputFunction implements Function {

        @Override
        public void execute(TridentTuple tuple, TridentCollector collector) {
            Iterator<Object> iterable = tuple.iterator();
            while(iterable.hasNext()) {
                Object x = iterable.next();
                System.out.println( x);
                collector.emit(new Values(x));
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
