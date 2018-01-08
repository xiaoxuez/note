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
	
### 索引管理操作
 
 + 关闭/打开索引。

 ```
 	POST /storm_2017-10-31/_close
 	curl -XPOST 'localhost:9200/storm_2017-10-31/_close'
 	curl -XPOST 'localhost:9200/storm_2017-10-31/_open'
 ```
 
 + 删除索引。

 ```
 	DELETE /index_name
 	curl -XDELETE 'localhost:9200/index_name?pretty&pretty'
 	
 ```
 
 + 修改索引副本数, 可以将一些不重要而且比较老的数据设置副本数为0以节省磁盘空间

 ```
 	curl -XPUT 'localhost:9200/<index_name>/_settings' -d '{"number_of_replicas": 0}'
    
 ```
 
### 案例

#### 某一索引为yellow原因及修复方法
 
[原文连接](https://www.datadoghq.com/blog/elasticsearch-unassigned-shards/)

+ 查看某一副本分片未分配的原因

```
curl -XGET localhost:9200/_cat/shards?h=index,shard,prirep,state,unassigned.reason| grep UNASSIGNED
```
 

+ 查看节点磁盘占用比例

```
curl -s 'localhost:9200/_cat/allocation?v'
```

+ 设置当磁盘占用率达到多少时不再分配分片

```
    curl -XPUT 'localhost:9200/_cluster/settings' -d
                      '{
                               "transient": {
                                 "cluster.routing.allocation.disk.watermark.low": "90%"
                                    }
                           }'
```


+ 内存分配问题

ES为运行在jvm上的java进程,很有可能报OOM,或者查看gc日志时发现回收达到了瓶颈，解决方法是加内存，但给ES加内存是指增加堆内存，但堆内存的分配最好小于机器总内存的一半，并且小于32G。ES使用的大内存除了堆内存外，Lucene使用时会占据文件缓存，故堆内存越大，可用的缓存空间就越小，磁盘换读的频率就越高。堆内存的分配还是小于总内存的一半，给文件缓存留点内存比较合适。

在查询时，Lucene会将索引部分加载到内存中，这意味着，查询的数据量决定了内存的使用，所以当天数增加，数据量增大的情况下，很容易出现OOM。

当GC出现瓶颈时，会出现ES无法响应的情况，Kibana也会出现对应提示(Status Red)。

使用中发现Kibana出现Status Red，首先查询集群状态，看节点是否工作正常，其次查看各节点日志，查看问题。

另外,swapping的情况对于性能来说是致命的，当内存资源不足时，Linux会将某些内容转移到swapping(硬盘)上，要使用这部分内容的时候，需要先搞到内存中，再使用。所以ES中，最好禁用swapping。
	
