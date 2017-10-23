## Elasticsearch 

Elasticsearch使用Lucene进行索引和搜索。Lucene，全文检索功能库。


### 基本概念

+ Index
	
	> An index is a collection of documents that have somewhat similar characteristics
	
+ Type

	> Within an index, you can define one or more types. A type is a logical category/partition of your index whose semantics is completely up to you.
	
+ Document

	> A document is a basic unit of information that can be indexed.

⭐️ 文档，是基本单元，相当于数据库表中的一条数据。指向文档的路径为index/type/id, type + id组成了文档的唯一标识符。	

+ Shards/Replicas

⭐️分片/副本, 跟Kafka中的分区，副本类似，支持水平扩容，提高吞吐，提供高可用性。

+ analyze

⭐️	建立索引时，Lucene会使用你选择的分析器来处理你的文档内容，查询时，查询的字段同样会被同个分析器进行分析。当然，也可以选择不分析。
	
+ mapping

⭐️	映射定义的过程是一个文档,和它所包含的字段,存储和索引，字段功能上来说，是输入数据到真正存储结构的映射处理，包括字段分析器选择，字段类型等。schema?


### SHOW TIME!

```
	Restful api 
	http://localhost:9200/_cat/indices
	GET _cat/indices
	
	//1. 添加数据，自动生成索引，以及对应mapping
	POST /test/show?pretty
	{
	  "name": "John Smith"
	}
	
	//2. 查看mapping keyword?
	//3. 搜索一下 match? term? name.keyword
	GET /test/show/_search?pretty
	{
	  "query": {
	    "term": {
	      "name": "John Smith"
	    }
	  }
	}
	
	//4. 看一下具体存储结构
	GET /test/_analyze?pretty
	{
	  "field": "show.name", 
	  "text":  "John Smith"
	}
		
```

### 搜索

搜索分为全文搜索和精确搜索。即分析和不分析。
https://www.elastic.co/guide/en/elasticsearch/reference/5.4/query-dsl-match-query-phrase-prefix.html看右侧的查询分类

最后看一下线上kibana数据，使用Lucene语法查询一条数据。