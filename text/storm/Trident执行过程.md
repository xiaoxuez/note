## Trident 执行分析

#### Trident Topology是如何转换成普通的Topology
组成Trident的结构是图(由节点和边组成)。在TridentTopology.build()方法中，可以看到诸如类似的代码

     for(Node n: graph.vertexSet()) {
            if(n instanceof SpoutNode) {
                spoutNodes.add((SpoutNode) n);
            } else if(!(n instanceof PartitionNode)) {
                boltNodes.add(n);
            }
        }
        
很明显，节点有三种，SpoutNode, PartitionNode,  和BoltNode。此处的spout，bolt 非storm中的spout，bolt。在调用各种operation，如.newStream,.each或是.shuffle()等，都会产生一个Node节点。

下面以一段代码为示例做介绍。

	topology.newStream("sentence-spout", new WordSpout())
		.parallelismHint(3)
		.each(new Fields("sentence"), new WordSplitFunction(), new Fields("word"))
		.shuffle()
		.each(new Fields("word"), new Utils.PrintFilter());
		
首先，是newStream()方法, 贴上源码

	public Stream newStream(String txId, ITridentSpout spout) {
	        Node n = new SpoutNode(getUniqueStreamId(), spout.getOutputFields(), txId, spout, SpoutNode.SpoutType.BATCH);
	        return addNode(n);
	    }

很明显，是新增了SpoutNode, 并将节点添加入图的结构，并返回Stream对象。Stream类的成员变量在这里解释一下，方便其后面添加运算。

	Node _node;
	TridentTopology _topology;
	String _name;

_node是节点对象，所以很当然的，当调用了诸如运算操作进行添加Node之后返回的Stream对象的_node属性的值便是添加的这个Node。(其实每次返回的Stream对象都是重新new出来的，TridentTopology topology对象只需要记住Node和边的关系)。


[具体详情](http://www.cnblogs.com/hseagle/p/3490635.html)