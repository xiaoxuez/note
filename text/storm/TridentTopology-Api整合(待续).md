## Trident Topology Api 整合

### Api


**添加运算(filter and Function)**  

+ Stream each(Fields inputFields, Filter filter)  
+ Stream each(Fields inputFields, Function function, Fields functionFields)

****

**字段过滤**：经过Function会新增field字段s，需求是只保留需要的field  

+ Stream project(Fields keepFields)

****

**并行度设置**：指定Topology的并行度，即用多少线程执行这个任务。

+ Stream parallelismHint(int hint)

它设置它前面(/后面)所有操作的并发度，直到遇到分区操作为止。因为在Trident中，分区操作是作为Bolt划分的分界点，所以，设置并行度，是以Spout/Bolt为单位的，所以设置的并行度应该是前面Bolt的并行度，至于Bolt到哪里结束，这个并行度就会影响到哪里**example1**

****

**groupBy** : 将tuple进行分组，返回值是GroupedStream

+ GroupedStream groupBy(Fields fields）

****

**partitionBy** : 分区操作之一, 确保相同字段列表的数据被划分到同一个分区

+ Stream partitionBy(Fields fields)

****

**partitionBy和GroupBy的区别**
这两者都是将tuples按照某field值进行归纳，partitionBy只是仅仅将归纳的结果丢到对应的分区中，GroupBy的返回值是GroupedStream，是将归纳的各个结果组成了对应的集合。使用聚合的情况下，聚合的概念是将一个集合的tuples组合到一个单独的字段中，聚合的输出结果应该是字段只有一个的Stream,若是在聚合前使用了GroupBy，那么对tuples分成了多个集合，聚合后的输出结果就是含分组字段和聚合字段的Stream。  
*针对分区和分组 + 聚合的例子见***example2**

**分区**，定义我们的tuple如何被route到下一处理层，分区的对象是一个batch的tuples(数据是按batch发送的，所以每次分区的对象是当前的batch,而且是将数据(tuples进行分区),分区的解释应该跟并发度结合起来，多个线程的情况下，按分区的field值将相同tuple分到同一个线程中。所以，没有设置并发度的情况下，分区基本是没有意义的

****

**分区操作**

+ Stream shuffle(): 通过随机分配算法来均衡tuple到各个分区.
+ Stream localOrShuffle()
+ Stream global(): 所有的tuple都被发送到一个分区，这个分区用来处理整个Stream
+ Stream batchGlobal(): 一个Batch中的所有tuple都被发送到同一个分区，不同的Batch会去往不同的分区
+ Stream broadcast(): 每个tuple都被广播到所有的分区，这种方式在drcp时非常有用，比如在每个分区上做stateQuery
+ Stream identityPartition() 
+ Stream partition(Grouping grouping): ：通过一个自定义的分区函数来进行分区，这个自定义函数实现了 backtype.storm.grouping.CustomStreamGrouping
+ Stream partitionBy(Fields fields): 根据指定的字段列表进行划分，确保相同字段列表的数据被划分到同一个分区

****

**partitionAggregate** ：聚合操作，前提是在partition上，一个batch被分成多个partition后，每个partition都会单独运行partitionAggregate中指定的聚合操作

+ Stream partitionAggregate(Fields inputFields, CombinerAggregator agg, Fields functionFields)
+ Stream partitionAggregate(Aggregator agg, Fields functionFields)
+ more...

****

**aggregate** ：隐含了一个global分区操作，也就是它做的是全局聚合操作。它针对的是整个batch的聚合计算。这会随机启动一个单独的线程来进行这个聚合操作。

+ Stream aggregate(Fields inputFields, Aggregator agg, Fields functionFields)
+ Stream aggregate(CombinerAggregator agg, Fields functionFields)
+ more...

****

**partitionAggregate/aggregate的区别**

partitionAggregate和aggregate的区别是aggregate有隐含的分区操作(global), partitionAggregate没有分区操作，所以如果当前是多并发，使用partitionAggregate的结果是各个分区中有自己的结果，使用aggregate的结果是只有一个结果(因为使用了全局分区，都分到了一起)

*针对分区和分组 + 聚合的例子见***example2**

****

**partitionPersist** ：持久化操作，持久化数据的更新应该在updater中，提交的话应该是在State的commit中，并发度>1时，这个操作应该在分区的前提下进行

+ TridentState partitionPersist(StateFactory stateFactory, StateUpdater updater, Fields functionFields)
+ TridentState partitionPersist(StateSpec stateSpec, Fields inputFields, StateUpdater updater, Fields functionFields) 
+ more...

****

**persistentAggregate** ：持久化操作，含聚合功能，持久化的更新应该在Map中。调用的聚合函数是aggregate();

+ TridentState persistentAggregate(StateFactory stateFactory, CombinerAggregator agg, Fields functionFields)
+ TridentState persistentAggregate(StateFactory stateFactory, Fields inputFields, CombinerAggregator agg, Fields functionFields)
+ more...

**getOutputFields**: 所有字段

+ Fields getOutputFields()

**applyAssembly**：暂时没找到相关介绍，但看Api代码，有可能是配置。

+ Stream applyAssembly(Assembly assembly)

**stateQuery** : state查询

+ Stream stateQuery(TridentState state, Fields inputFields, QueryFunction function, Fields functionFields)

当调用了持久化操作后(persistentAggregate、partitionPersist),会返回TridentState的对象(以下称这个对象为state)，这个state里记录了持久的所有结果，如，在统计单词的示例中，state代表的是所有的单词的数量，可以理解成一个记录了所有持久化结果的对象，使用**stateQuery**方法可以在state中进行查询。  
*示例见example3*


**setCPULoad**: 设置当前线程cpu分配比例，默认是10(0~100表示百分比)。

**setMemoryLoad**:  可设置当前线程onheap/offheap的内存比例。为创建缓存onheap的大小和清除缓存offheap剩余的大小，默认onheap为128，offheap为0。

**map** : 可视为运算之一，含义为map...Returns a stream consisting of the result of applying the given mapping function to the values of this stream.运算的输入是数据tuple, 输出是新发射的values...

**flatMap** : 这个跟map有点类似，区别在于适用于一对多的情况。拿wordCount的例子来说，句子分割成单词的部分就适合使用flatMap,单词若是每个单词都转换成小写的功能就适合使用map。    
[示例地址](https://github.com/apache/storm/blob/master/examples/storm-starter/src/jvm/org/apache/storm/starter/trident/TridentMapExample.java)



**ChainedAggregatorDeclarer**

**minBy**
**min**
**maxBy**
**max** : 看起来内部实现的是聚合，应该是根据比较器，直接聚合返回min/max的tuple.

**comparableAggregateStream**

**peek** :    Returns a stream consisting of the trident tuples of this stream, additionally performing the provided action on each trident tuple as they are consumed from the resulting stream. This is mostly useful for debugging to see the tuples as they flow past a certain point in a pipeline.

**tumblingWindow**  tumbling窗口技术

**slidingWindow**  平滑窗口技术

**window** 窗口    
[窗口技术的使用和例子](http://www.cnblogs.com/swanspouse/p/5130117.html)    
[官方讲解](http://storm.apache.org/releases/1.0.0/Windowing.html)    
窗口技术用于在一定间隔中统计最近数据，滑动窗口中，窗口长度是5条，边缘是3条，数据是12345678进入的会分成，12345， 45678这样的。窗口长度和边缘都可以是长度或者是时间。例如10s间隔统计最近20条的数据

**addTriggerField**

### Example

####example1 -- 并发度设置

	topology.newStream("spout", spout)  
	      .each(new Fields("actor", "text"), new PerActorTweetsFilter("dave"))  
	      .parallelismHint(5)  
	      .each(new Fields("actor", "text"), new Utils.PrintFilter());  
	      
转换成了默认的一个Spout连接一个Bolt，设置并发度后，一共有5个Spout Task和5个Bolt Task在并发运行。

	topology.newStream("spout", spout)  
      .parallelismHint(2)  
      .shuffle()   //分区操作，随机分，
      .each(new Fields("actor", "text"), new PerActorTweetsFilter("dave"))  
      .parallelismHint(5)  
      .each(new Fields("actor", "text"), new Utils.PrintFilter());  

设置Spout的并发度是2，其后的是5。注意的是，如果这段代码没有 .parallelismHint(2) 这句，即没有设置Spout的并发度是2，那么，Spout的并发度是1，后面parallelismHint(5) 设置并发度是5只能影响到shuffle()分区之后的部分。

				.partitionBy(new Fields("word"))
				.parallelismHint(3)
				.each(new Fields("word"), new Utils.PrintFilter())

如此设置是设置partitionBy的并行度为3，因为涉及到了分区操作，所以partiotionBy后面的.each操作是在另一个Bolt中，该Bolt的并行度为1.就是说Utils.PrintFilter()中的并行度是1.


#### example2 -- partitionBy/groupBy + aggregate/partitionAggregate

[代码地址](https://github.com/xiaoxuez/Storm/tree/master/src/main/java/com/partition)
##### 示例1  partitionBy + partitionAggregate
 
	 topology.newStream("sentence-spout", new WordSpout())
			.shuffle()
			.each(new Fields("sentence"), new WordSplitFunction(), new Fields("word"))
			.partitionBy(new Fields("word"))
			.parallelismHint(3)
			.partitionAggregate(new Fields("word"), new CountAggrate(), new Fields("count"))
			.each(new Fields("count"), new Utils.PrintFilter());
			
结果为：("count"), tuple只有这一个字段。partition为分区，num为分区总数。

	[{think=1, ate=1, don't=1, cow=1, my=2, dog=2}]partition = 0num = 3
	[{a=1, like=2, cold=1, has=1, man=1, fleas=2}]partition = 2num = 3
	[{the=1, beverages=1, homework=1, have=1, i=3, dont't=1}]partition = 1num = 3
	
##### 示例2	 groupBy + aggregate
	topology.newStream("sentence-spout", new WordSpout())
				.shuffle()
				.each(new Fields("sentence"), new WordSplitFunction(), new Fields("word"))
				.groupBy(new Fields("word"))
				.aggregate(new Fields("word"), new Count(), new Fields("count"))
				.each(new Fields("word", "count"), new Utils.PrintFilter());

结果是：("word", "count"),因为是按单词进行分组，所以结果为单词和出现的次数

	[a, 1] 
	[think, 1]
	[like, 2] 
	...
	
**结果总结**：   
1. 聚合出来的Steam中只有一个字段"count",没有其余以前的字段。所以需要保存聚合结果，以简单数据类型保存的话，肯定就会混淆。此处使用的是Map.  
2. 最开始理解partitionBy、groupBy、aggregate、partitionAggregate的时候进入了一个误区，就是将他们几个结合起来理解，其实应该直接按照各自的功能进行单独理解，partitionBy就是分区，将tuples按照指定字段的值分区，groupBy是分组，返回值是GroupedStream，可视为Stream的分组集合，主要作用是其后进行聚合的情况下，是在各个小组内分别进行的聚合，aggregate和partitionAggregate都是聚合，不同是aggregate有隐含的分区操作。


#####example3 -- stateQuery
	TridentTopology topology = new TridentTopology();
			//统计下面4个句子中各个单词出现的次数，保存的结果为TridentState类型
			TridentState state = topology.newStream("sentence-spout", new FixedBatchSpout(new Fields("sentence"), 5, 
					new Values("the cow jumped over the moon"),
					new Values("the man went to the store and bought some candy"),
					new Values("four score and seven years ago"),
					new Values("how many apples can you eat")))
				.shuffle()
				.each(new Fields("sentence"), new WordSplitFunction(), new Fields("word"))
				.groupBy(new Fields("word"))
				.persistentAggregate(new MemoryMapState.Factory(), new Count(), new Fields("count"));
				
			//发送单词，并查询这个单词在上面句子中的统计结果
			topology.newStream("state-query", new WordSpout())
				.each(new Fields("sentence"), new WordSplitFunction(), new Fields("word"))
				.stateQuery(state, new Fields("word"), new MapGet(), new Fields("count"))
				.each(new Fields("count"), new FilterNull())
				.each(new Fields("word", "count"), new Utils.PrintFilter());
			Config config = new Config();
			config.setDebug(false);
			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology("messages-to-operation", config,topology.build());
		
结果为：

	[the, 4]
	..
	
材料参考：  
1. [Trident API总结](http://blog.csdn.net/wb81074/article/details/50150699) 和[Trident详细介绍](http://www.bubuko.com/infodetail-467560.html)中的例子是一样的，[例子地址](https://github.com/pereferrera/trident-hackaton/)