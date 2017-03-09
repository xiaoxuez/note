## 困惑

1. Trident spout的类型决定batch的组成方式(详情可见***代码整合之Topology构建*** -> Trident Topology构建 -> spout类型), 或者是***Storm分布式实时计算模式***的3.3节。疑问是书中的示例为何都选择使用的ITridentSpout，ITridentSpout是透明型或者事务型，也就是batch可能出现重复，我并没有理解到batch在什么情况下会出现重复，也就无法理解选择事务型spout的原因。(注：连接kafka的队列的spout是IOpaquePartitionedTridentSpout)。总之就是不太理解什么时候需要选择何种spout类型

2. Trident  spout并没有真正发射tuple，只是将这项工作分配给BatchCoordinator和	Emitter，Emitter负责发送tuple，BatchCoordinator负责管理批次和元	数据，Emitter需要依靠元数据来恰当地进行批次的数据重放。这句话出自3.3节，疑问是**重放**何时发生，什么叫做重放，换句话说，重放会发生什么，是数据的重复提交吗，提交的数据又是？

3. 不理解的概念，书中多次出现Cassandra作为深度存储器，何为深度存储器，与MySQLite的区别是？

4. 何处该引入设置多并发，举个例子
 