package storm;


import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.FailedException;
import org.apache.storm.topology.base.BaseTransactionalSpout;
import org.apache.storm.transactional.partitioned.IOpaquePartitionedTransactionalSpout;
import org.apache.storm.trident.Stream;
import org.apache.storm.trident.TridentTopology;
import org.apache.storm.trident.operation.Function;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.operation.TridentOperationContext;
import org.apache.storm.trident.spout.IOpaquePartitionedTridentSpout;
import org.apache.storm.trident.spout.IPartitionedTridentSpout;
import org.apache.storm.trident.spout.ISpoutPartition;
import org.apache.storm.trident.topology.TransactionAttempt;
import org.apache.storm.trident.tuple.TridentTuple;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by xiaoxuez on 2017/9/12.
 */
public class TridentReplayDataTest {
    public static void main(String[] args) {
        TridentTopology tridentTopology = new TridentTopology();
        tridentTopology.newStream("test", new OpaqueSpout())
                .each(new Fields("msg", "tx", "meta"), new Output(), new Fields());

        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("test", new Config(), tridentTopology.build());
    }


    public static class PartitionedTridentSpout implements IPartitionedTridentSpout {

        @Override
        public Coordinator getCoordinator(Map conf, TopologyContext context) {
            return new MyCoordinator();
        }

        @Override
        public Emitter getEmitter(Map conf, TopologyContext context) {
            return new MyEmitter();
        }

        @Override
        public Map<String, Object> getComponentConfiguration() {
            return null;
        }

        @Override
        public Fields getOutputFields() {
            return new Fields("msg", "tx", "meta");
        }
    }

    public static class MyEmitter implements IPartitionedTridentSpout.Emitter {

        String[] content = {"A", "B", "C", "D", "E", "F"};
        private long x = 0;

        @Override
        public List getOrderedPartitions(Object allPartitionInfo) {
//            return java.util.stream.Stream.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).collect(Collectors.toList());
            return Arrays.asList(
                    new Partition("0"),
                    new Partition("1"),
                    new Partition("2"),
                    new Partition("3")
            );
        }

        @Override
        public Object emitPartitionBatchNew(TransactionAttempt tx, TridentCollector collector, ISpoutPartition partition, Object lastPartitionMeta) {
            if (x <= 10) {
                emitPartitionBatch(tx, collector, partition, lastPartitionMeta);
            }
            return x ++;
        }

        @Override
        public void refreshPartitions(List partitionResponsibilities) {

        }

        @Override
        public void emitPartitionBatch(TransactionAttempt tx, TridentCollector collector, ISpoutPartition partition, Object partitionMeta) {
            collector.emit(new Values(content[(int) (Math.random() * 100 % 6)], tx, partitionMeta));
        }

        @Override
        public void close() {

        }
    }

    public static class MyCoordinator implements IPartitionedTridentSpout.Coordinator {

        @Override
        public Object getPartitionsForBatch() {
            return (int) Math.random() * 10 % 10;
        }

        @Override
        public boolean isReady(long txid) {
            return true;
        }

        @Override
        public void close() {

        }
    }

    public static class Output implements Function {

        @Override
        public void execute(TridentTuple tuple, TridentCollector collector) {
            Iterator x = tuple.iterator();
            while (x.hasNext()) {
                System.out.print(x.next() + " - ");
            }
            System.out.println();
            throw new FailedException();
        }

        @Override
        public void prepare(Map conf, TridentOperationContext context) {

        }

        @Override
        public void cleanup() {

        }
    }

    public static class Partition implements ISpoutPartition {

        private String id;
        public Partition(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }
    }


    public static class OpaqueSpout implements IOpaquePartitionedTridentSpout {

        @Override
        public Emitter getEmitter(Map conf, TopologyContext context) {
            return new OpaqueEmitter();
        }

        @Override
        public Coordinator getCoordinator(Map conf, TopologyContext context) {
            return new OpaqueCoordinator();
        }

        @Override
        public Map<String, Object> getComponentConfiguration() {
            return null;
        }

        @Override
        public Fields getOutputFields() {
            return new Fields("msg", "tx", "meta");
        }
    }

    public static class OpaqueEmitter implements IOpaquePartitionedTridentSpout.Emitter{

        String[] content = {"A", "B", "C", "D", "E", "F"};
        private long x = 0;

        @Override
        public Object emitPartitionBatch(TransactionAttempt tx, TridentCollector collector, ISpoutPartition partition, Object lastPartitionMeta) {
            String c = content[(int) (Math.random() * 100 % 6)];
            collector.emit(new Values( c, tx, lastPartitionMeta));
            return  c;
        }

        @Override
        public void refreshPartitions(List partitionResponsibilities) {

        }

        @Override
        public List getOrderedPartitions(Object allPartitionInfo) {
            return Arrays.asList(
                    new Partition("0"),
                    new Partition("1"),
                    new Partition("2"),
                    new Partition("3")
            );
        }

        @Override
        public void close() {

        }
    }

    public static class OpaqueCoordinator implements IOpaquePartitionedTridentSpout.Coordinator {

        @Override
        public boolean isReady(long txid) {
            return true;
        }

        @Override
        public Object getPartitionsForBatch() {
            return (int) Math.random() * 10 % 10;
        }

        @Override
        public void close() {

        }
    }
}
