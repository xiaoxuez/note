## Elasticsearch - v5.6

Elasticsearch使用Lucene进行索引和搜索。Lucene，全文检索功能库。

### 基本概念

+ Cluster,集群
+ Node,节点
	
	节点跟集群的确定关系是通过集群的名字cluster.name
	
+ Index,索引
+ Type,类型
+ Document,文档
+ Shards/Replicas 分片/副本

	⭐️ 分片优势,水平扩容和提高性能和吞吐(分布式+并行)。    
	⭐️ 副本优势，提供高可用性(集群情况下分片和对应的副本决不会在同一个节点上)，扩展搜索量和吞吐。    
	⭐️ 副本的数量在索引建立后可以调整，但分片数量则不能再修改。
	
### 常用概念

+ 查询，过滤
	
	查询和过滤，查询的意思是有多匹配，返回结果包含相关度得分，过滤为匹配否，则不会计算相关度得分

+ analyze

	Lucene的目标是提供一个全文检索的功能库，对字段文本的词会进行分析，建立索引时，Lucene会使用你选择的分析器来处理你的文档内容，查询时，查询的字段同样会被同个分析器进行分析。当然，也可以选择不分析。
	
+ mapping

	映射定义的过程是一个文档,和它所包含的字段,存储和索引，字段功能上来说，是输入数据到真正存储结构的映射处理，包括字段分析器选择，字段类型等。

### 常用curl

+ 集群相关

	```
	//查询集群状态
	curl -XGET 'localhost:9200/_cat/health?v&pretty'
	
	//获取节点
	curl -XGET 'localhost:9200/_cat/nodes?v&pretty'
	```
	
+ 索引相关

	```
	//获得所有索引
	curl -XGET 'localhost:9200/_cat/indices?v&pretty'
	//创建索引
	curl -XPUT 'localhost:9200/customer?pretty&pretty'
	//删除索引
	curl -XDELETE 'localhost:9200/customer?pretty&pretty'
	//添加索引文件，当指定了id的时候使用PUT(/customer/external/2)，没指定使用POST
	curl -XPOST 'localhost:9200/customer/external?pretty&pretty' -H 'Content-Type: application/json' -d'
	{
	  "name": "Jane Doe"
	}'
	
	```	

+ 查询相关

	```
	//使用_search
	curl -XGET 'localhost:9200/bank/_search?pretty' -H 'Content-Type: application/json' -d'
{
  "query": { "match_all": {} }
}
'
	```
	
	关于查询和过滤相关的api在隔壁搜索dsl中有详细介绍。
	
	
+ analyze 相关

	```
	//使用索引某字段的分析器分析，可用于查看字段在底层具体结构 uri只能是索引/_analyze,字段加上type
	GET /test/_analyze?pretty 
	{
	  "field": "analyze.my_text", 
	  "text":  "John Smith"
	}
	```
	
	
	