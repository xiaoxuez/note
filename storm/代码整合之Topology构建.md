#代码整合之Topology构建
####Topology提交
	
	// 集群
	StormSubmitter.submitTopologyWithProgressBar(arg[0], config, builder.createTopology());
	// 本地模拟
	LocalCluster cluster = new LocalCluster();
			cluster.submitTopology(TOPOLOGY_NAME, config, builder.createTopology());

####普通Topology构建
		
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout(SENTENCE_SPOUT_ID, spout, 2);
		builder.setBolt(SPLIT_BOLT_ID, spliteSentenceBolt, 2).setNumTasks(4).shuffleGrouping(SENTENCE_SPOUT_ID);
		builder.setBolt(COUNT_BOLT_ID, wordCountBolt, 4).fieldsGrouping(SPLIT_BOLT_ID, new Fields("word"));		builder.setBolt(REPORT_BOLT_ID, reportBolt).globalGrouping(COUNT_BOLT_ID);

#### 有保障机制的数据处理
见***Storm分布式实时计算模式*** 1.6节

简要介绍

+ setSpout/setBolt方法的***参数***之一是可设置并发为几个task,每个task指派各自的executor线程(自己的理解这个参数设置就是executor线程的数量)
+ setSpout/setBolt方法的***返回值***类型为Declarer。
	
	1. 可通过此对象调用***setNumTasks***设置task的数量，  
	如builder.setBolt(SPLIT_BOLT_ID, spliteSentenceBolt, 2).setNumTasks(4)的结果就是2个线程，每个线程指派2个task).   
	2. 另外，此对象的***fieldsGrouping和shuffleGrouping***方法可对数据流进行分组，数据分组的方式还有很多，可见1.5章节，此处使用了shuffleGrouping随机分和fieldsGrouping按字段分

#### Trident Topology构建
Trident是对Storm的更高一层的抽象,除了提供一套简单易用的流数据处理API之外，它以batch(一组tuples)为单位进行处理，这样一来，可以使得一些处理更简单和高效。在处理大数据时，我们在与database打交道时通常会采用批处理的方式来避免给它带来压力，而Trident恰恰是以batch groups的形式处理数据，并提供了一些聚合功能的API。

	public static StormTopology buildTopology() {
		TridentTopology topology = new TridentTopology();
		DiagnosisEventSpout spout = new DiagnosisEventSpout();
		Stream inputStream = topology.newStream("event", spout);
		inputStream.each(new Fields("event"), new DiseaseFilter())
				.each(new Fields("event"), new Function.CityAssignment(), new Fields("city"))
				.each(new Fields("event", "city"), new Function.HourAssignment(), new Fields("hour", "cityDiseaseHour"))
				.groupBy(new Fields("cityDiseaseHour"))
				.persistentAggregate(new OutbreakTrendFactory(), new CountAggregator(), new Fields("count")).newValuesStream()
				.each(new Fields("cityDiseaseHour", "count"), new Function.OutBreakDetector(), new Fields("alert"))
				.each(new Fields("alert"), new Function.DispatchAlert(), new Fields());
		return topology.build();
	}
	
简要介绍

#####Spout

+ Spout类型

	Trident spout必须成批地发送tuple,每个batch会分配一个唯一的事务标识符，spout基于约定决定batch的组成方式，spout有三种约定： 非事务型，事务型，非透明型，相对batch的组成方式如下：
	
	| spout类型      | batch可能有重复数据 | batch内容会变化|
	| ------------- |:-------------:|:----------:|
	| 非事务型       | 		✔️  	  |		✔️      |
	| 非透明型       | 				  |     ✔️     |
	| 事务型         | 				  |            |
	
	相对类型的spout接口如下：
	+ ***ITridentSpout***： The most general API that can support transactional or opaque transactional semantics. Generally you’ll use one of the partitioned flavors of this API rather than this one directly
	+ ***IBatchSpout***：A non-transactional spout that emits batches of tuples at a time
	+ ***IPartitionedTridentSpout***：A transactional spout that reads from a partitioned data source (like a cluster of Kafka servers)
	+ ***IOpaquePartitionedTridentSpout***： An opaque transactional spout that reads from a partitioned data source
	
书中的示例都使用的***ITridentSpout***，具体使用见***代码整合之ITridentSpout***

   
##### Trident运算类型有两种：filter和function

1. filter是过滤接口，可根据情况过滤tuple,
2. function读取tuple并且发送新的tuple, 在发送数据时，将新字段添加在tuple中，并不会删除或者变更已有的字段

添加运算是通过调用Stream的.each方法，通常来说.each方法参数一是inputFields,参数二是运算，若运算是function，则需要参数三是outputfields。同样，Trident Topology也有对流进行分组，通过调用.groupBy方法，返回类型为GroupStream

#####Trident Topology的持久化数据有两种途径
1. partitionPersist
2. persistentAggregate  
具体使用见***代码整合之持久化操作***