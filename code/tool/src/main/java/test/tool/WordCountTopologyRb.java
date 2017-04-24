package test.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.ShellBolt;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

public class WordCountTopologyRb {
	public static class SentenceSpout extends BaseRichSpout {

		/** pending的存在意义：
		 *  有保障机制的数据处理的存在。
		 *  具体为spout需要记录所有由他发送出去的tuple,当消息处理完成之后应该确认应答(ack)
		 *  当在bolt处发生错误时，需要报错(fail)，spout会收到错误，应该重发该条消息，
		 */
		//ConcurrentHashMap 比Hashtable更容易写并发，
		private ConcurrentHashMap<UUID, Values> pending;
		private SpoutOutputCollector collector;
		private String[] sentences = {
				"my dog has fleas",
				"i like cold beverages",
				"the dog ate my homework",
				"dont't have a cow man",
				"i don't think i like fleas",
		};
		private int index = 0;
		
		public void nextTuple() {
			// TODO Auto-generated method stub
			if (index < sentences.length) {
				Values values = new Values(sentences[index]);
				UUID mUuid = UUID.randomUUID();
				pending.put(mUuid, values);
				// emit第一个参数类型为List,
				this.collector.emit(values, mUuid);
				index++;
			}
//			if (index >= sentences.length) {
//				index = 0;
//			}
			Utils.sleep(1);
		}

		public void open(Map arg0, TopologyContext arg1, SpoutOutputCollector arg2) {
			// TODO Auto-generated method stub
			this.collector = arg2;
			pending = new ConcurrentHashMap<UUID, Values>();
		}

		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			// TODO Auto-generated method stub
			declarer.declare(new Fields("sentence"));
		}
		
		@Override
		public void ack(Object msgId) {
			this.pending.remove(msgId);
		}

		@Override
		public void fail(Object msgId) {
			this.collector.emit(this.pending.get(msgId), msgId);
		}
		
	}
	
//	public static class SplitSentenceBolt extends BaseRichBolt {
//		private OutputCollector collector;
//		public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
//			// TODO Auto-generated method stub
//			this.collector = collector;
//		}
//
//		public void execute(Tuple input) {
//			// TODO Auto-generated method stub
//			String sentence = input.getStringByField("sentence");
//			String[] words = sentence.split(" ");
//			for (String word : words) {
//				this.collector.emit(input, new Values(word));
//			}
//			this.collector.ack(input);
//		}
//
//		public void declareOutputFields(OutputFieldsDeclarer declarer) {
//			declarer.declare(new Fields("word"));
//		}
//	}
	
	public static class SplitSentenceBolt extends ShellBolt implements IRichBolt {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		 public SplitSentenceBolt() {
		      super("ruby", "splitsentence.rb");
		    }

		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			// TODO Auto-generated method stub
			declarer.declare(new Fields("word"));
		}

		@Override
		public Map<String, Object> getComponentConfiguration() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	public static class WordCountBolt extends BaseRichBolt {
		private OutputCollector collector;
		private HashMap<String, Long> counts = null;
		public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
			this.collector = collector;
			this.counts = new HashMap<String, Long>();
		}

		public void execute(Tuple input) {
			String word = input.getStringByField("word");
			Long count = this.counts.get(word);
			if ( count == null) {
				count = 0L;
			}
			count ++;
			this.counts.put(word, count);
			this.collector.emit(new Values(word, count));
		}

		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			declarer.declare(new Fields("word", "count"));
		}
		
	}
	
	public static class ReportBolt extends BaseRichBolt {

		private HashMap<String, Long> counts = null;

		public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
			counts = new HashMap<String, Long>();
		}

		public void execute(Tuple input) {
			String word = input.getStringByField("word");
			Long count = input.getLongByField("count");
			this.counts.put(word, count);
			System.out.println("---  word --");
		}
		
		/**
		 * ReportBolt为最后的Bolt,不再有下游，所以应该没有任何输出
		 */
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
		
		}
		
		@Override
		public void cleanup() {
			super.cleanup();
			
			System.out.println("---  Final Counts --");
			List<String> keys = new ArrayList<String>();
			keys.addAll(this.counts.keySet());
			Collections.sort(keys);
			for (String key : keys) {
				System.out.println(key + " : " + this.counts.get(key));
			}
			
			System.out.println("---  Over --");
		}
		
	}
	
	
	private static final String SENTENCE_SPOUT_ID = "sentence-spout";
	private static final String SPLIT_BOLT_ID = "split-bolt";
	private static final String COUNT_BOLT_ID = "count-bolt";
	private static final String REPORT_BOLT_ID = "report-bolt";
	private static final String TOPOLOGY_NAME = "word-count-topology";
	
	public static void main(String[] args) throws Exception{
		SentenceSpout spout = new SentenceSpout();
		SplitSentenceBolt spliteSentenceBolt = new SplitSentenceBolt();
		WordCountBolt wordCountBolt = new WordCountBolt();
		ReportBolt reportBolt = new ReportBolt();
		
		TopologyBuilder builder = new TopologyBuilder();
		//并发机制，默认是一个线程分配一个Task(Bolt,Spout的实例)，也可以自己设置并发机制,数字解释如下
		//1. spout设置了2个线程，默认线程中的Task数量是1，所以这样设置的直接结果是将spout并发成了2个Task(再通俗一点的含义是会有两个SentenceSpout的实例进行喷发数据, 比如如果上述句子只喷发一次，出现a的次数最后是2，因为a被发送了两次)
		builder.setSpout(SENTENCE_SPOUT_ID, spout, 2);	
		// 2. spliteSentenceBolt 设置的线程数量是2， 又设置了总共产生4个Task，故结果是每个线程分2个Task
		builder.setBolt(SPLIT_BOLT_ID, spliteSentenceBolt, 2).setNumTasks(4).shuffleGrouping(SENTENCE_SPOUT_ID);
		builder.setBolt(COUNT_BOLT_ID, wordCountBolt, 4).fieldsGrouping(SPLIT_BOLT_ID, new Fields("word"));
		//这里解释一下为什么wordCountBolt分组是按字段值分，当存在线程并发的时候，就是有多个wordCountBolt实例存在的时候，必须保证相同字段的Tuple被分到同一个wordCountBolt实例中，不然数量就会少，比如数据源并发2，随机分，实例1和实例2均匀收到1个a，在发送给report后就会统计成1个a，真相应该是2个
		builder.setBolt(REPORT_BOLT_ID, reportBolt).globalGrouping(COUNT_BOLT_ID);
		
		Config config = new Config();
		config.setDebug(true);
		//设置虚拟机的个数:进程
		//		config.setNumWorkers(2);
		if (args != null && args.length > 0) {
		    StormSubmitter.submitTopologyWithProgressBar(args[0], config, builder.createTopology());
		}else {
			config.setMaxTaskParallelism(4);
			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology(TOPOLOGY_NAME, config, builder.createTopology());
			
			Utils.sleep(10000);
			cluster.killTopology(TOPOLOGY_NAME);
			cluster.shutdown();
		}
	}
}
