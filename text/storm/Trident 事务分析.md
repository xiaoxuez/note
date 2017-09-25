## Trident 关于事务分析

恩，这个事情一直困扰了我很久，今天抽了点时间出来整理了下，大概清晰了一点了。


首先看看Trident的特性吧。

+ 通过小数据块（batch）的方式来处理 tuple。

+ 为每个batch提供一个唯一的id，这个 id 称为 “事务 id”（transaction id，txid）。如果需要对batch重新处理，这个batch上仍然会赋上相同的txid。

+ <font color="red">State</font> 的更新操作是按照 batch 的顺序进行的。也就是说，在 batch 2 完成处理之前，batch 3 的状态更新操作不会进行。<font color="grey">是State更新操作具有顺序，不是一般batch的处理！</font>

然后，根据以上的特性，衍生出Spout和State的事务性相关操作。

#### Spout

+ 非事务型 spout(Non-transactional spouts)

	非事务型 spout 不能为 batch 提供任何的安全性保证。非事务型 spout 有可能提供一种“至多一次”的处理模型，在这种情况下 batch 处理失败后 tuple 并不会重新处理；也有可能提供一种“至少一次”的处理模型，在这种情况下可能会有多个 batch 分别处理某个 tuple。总之，此类 spout 不能提供“恰好一次”的语义。
	
	<font color="grey">如IRichSpout</font>
	
+ 事务型 spout（Transactional spouts）

	- 每个 batch 的 txid 永远不会改变。对于某个特定的 txid，batch 在执行重新处理操作时所处理的 tuple 集和它的第一次处理操作完全相同。
   - 不同 batch 中的 tuple 不会出现重复的情况（某个 tuple 只会出现在一个 batch 中，而不会同时出现在多个 batch 中）。
   - 每个 tuple 都会放入一个 batch 中（处理操作不会遗漏任何的 tuple）。
	
	<font color="grey">操作失败重放的时候，batch的内容tuple集是完全一样的，就是操作成功的tuple也会在batch中，整个跟之前的batch一模一样，当然包括txid, 所以对数据的操作需要自定义实现判断是否数据已经操作过了，判断的标准自然就是txid了，因为txid是唯一的，某个tuple在某txid中出现是唯一的。</font>
	
+ 模糊事务型 spout（Opaque transactional spouts）

  - Every tuple is processed in exactly one batch.  - If a tuple is not processed in one batch, it would be processed in the next batch. But, the second batch doesn't have the same set of tuples as the  rst processed batch.

	<font color="grey">模糊事务型跟事务型的区别主要是batch的内容并不能保证是不变的。然后，就不能根据txid来判断是否操作过，然后，还是有疑惑没搞懂..👉 模糊事务型的这个数据操作一般根据txid, value, prevalue来处理，就是多存prevalue， 即txid相同的情况下，newvalue = prevalue + x, 不同的情况， newvalue = value + x,相当于是上次提交之后，数据又重放了一次，数据从上上次的结果作为当前，就直接作废了上次的提交。书上说需要这种情况可能是下游处理失败了，可是怎么个失败法.. 是我一直不能理解的啊.. 普通的bolt失败是调用failed,那trident怎么个失败，我？</font>
	
	<font color="grey">然后，就去查了下trident怎么算是个[失败法](https://svendvanderveken.wordpress.com/2014/02/05/error-handling-in-storm-trident-topologies/), 文章中大概提到了<font color="red">throw FailedException</font>就会导致数据的重放，另外关于模糊事务型保证一次处理的方案可以采取数据具有某primary key，比如kafka partition的offset，前提是不会产生重分区，不然就尴尬了= =. </font>
	
	
关于模糊事务型和事务性的选择主要看数据源，在重发的情况下，如果有一个或多个数据源不可用的话，事务性会一直等待知道所有的数据源都可用，模糊型会发生当前可用的数据分片，数据的处理照常进行。
	
#### State

上面关于事务型保证一次处理的数据操作，复杂的 txid 比对、多值存储等操作，Trident 已经在State中封装了所有的容错性处理逻辑。如MemcachedState.opaque，状态更新都会自动调整为批处理操作，这样可以减小与数据库的反复交互的资源损耗。如TransactionalMap,OpaqueMap, NonTransactionalMap。事务型，非事务型，模糊性对应上述spout中对应的数据处理。所以保证事务型，模糊型 需要对应spout + state搭配。


参考文章：

[trident-事务型](http://ifeve.com/storm-trident-state/)

[trident-数据重放](https://svendvanderveken.wordpress.com/2014/02/05/error-handling-in-storm-trident-topologies)

