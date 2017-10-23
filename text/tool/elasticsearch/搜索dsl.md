## 查询ElasticSearch

基于书 ElasticSearch服务器开发(第二版) 和官网doc(5.5)做的相应整理。

粗略总结下，查询语句分为简单查询和复合查询，复合查询是由简单查询包装，简单查询可作为复合查询的子句，如bool查询示例。简单查询又分为full-text和exact，match相关的为full-text查询，查询的是经分析后的字段，term相关为确切查询为完整字段。

### 简单查询

简单查询为针对某一特定field为某一特定value进行查询，如match, term, range。

#### match_all

查询所有

```
 "query": {
        "match_all": {}
    }
```

#### match

+ match布尔查询

会将value拿出来加以分析，然后构建相应的查询。分析器默认为创建索引时相同的分析器。

```
	// 查询message字段有"this" or "is" or "a" or "test"
	"query": {
        "match" : {
            "message" : "this is a test"
        }
    }
```
如上例，分析后将产生4个text， 类似为多条件，使用operator可设置多条件的连接纽带，or/and，默认是or。minimum\_should\_match参数为设置满足条件最小数。analyzer参数设置分析器。另外，还支持相关fuzziness模糊查询，以及高频词/低频词查询，这个跟书上的差不多(3.3.5)。

+ match_phrase

跟match布尔查询类似，不同的是，从分析后的文本中构建短语查询。

```
//查询this test之间允许有2个词条的短语。则亦能匹配到"this is a test"
"query": {
        "match_phrase" : {
            "message" : {
                "query": "this test",
                "slop":2
            }
        }
    }
```
 
+ match\_phrase\_prefix

在match\_phrase的基础上增加了允许查询文本的最后一个词条只做前缀匹配。

```
  "query": {
        "match_phrase_prefix" : {
            "message" : "this is a t"
        }
    }
```

+ multi match query

与match查询一样，区别在于可以在多个field中进行查询（查询内容还是一个）。

```
	"query": {
	    "multi_match" : {
	      "query":    "this is a test", 
	      "fields": [ "subject", "message" ] 
	    }
	  }
```

+ common terms query

常用词查询，即将分析过后的词分为高频词和低频词进行查询...

+ query string query

支持Lucene查询语法。

+ simple query string query

跟上面的query string query差不多，不同的是错误时不会抛出异常，直接丢弃查询无效的部分。


#### term

term查询的话，就是确切的，未经分析的词条。

+ term query

```
//匹配title字段中含有crime一词的文档
 "query": {
    "term" : { "title" : "crime" } 
  }
```

+ terms query

多词条查询。

```
"query": {
     "terms" : {
        "user" : ["kimchy", "elasticsearch"]
        }
    }
```

#### range query

范围查询
```
//查询age在10到20岁之前的文档
 "query": {
        "range" : {
            "age" : {
                "gte" : 10,
                "lte" : 20
            }
        }
    }
    //gte : Greater-than or equal to
    //gt :  Greater-than
    //lte : Less-than or equal to
	//lt : Less-than
```

#### exist query

会滤掉给定字段上没有值的文档,即返回的文档再给定字段上一定有值。
```
 "query": {
        "exists" : { "field" : "user" }
    }
```

#### prefix query

前缀查询

```
	//查询user以ki开头的文档
	{ "query": {
	    "prefix" : { "user" : "ki" }
	  }
	}
```

#### Wildcard query

通配符查询，*，?.
```
 "query": {
        "wildcard" : { "user" : "ki*y" }
    }
```

#### regexp query

正则匹配

#### type query

```
 "query": {
        "type" : {
            "value" : "my_type"
        }
    }
```

#### ids query

```
        "ids" : {
            "type" : "my_type",
            "values" : ["1", "4", "100"]
        }
```

#### constant score query

为查询/过滤返回的文档返回一个常量得分。

### 复合查询

由简单查询包装或组合查询，来进行多个查询的逻辑组合，如bool。


+ bool query

```
// 查询user包含kimchy的， tag包含tech的，age在10到20之外的， 条件tag中有wow和elasticsearch至少有1个条件满足
 "query": {
    "bool" : {
      "must" : {
        "term" : { "user" : "kimchy" }
      },
      "filter": {
        "term" : { "tag" : "tech" }
      },
      "must_not" : {
        "range" : {
          "age" : { "gte" : 10, "lte" : 20 }
        }
      },
      "should" : [
        { "term" : { "tag" : "wow" } },
        { "term" : { "tag" : "elasticsearch" } }
      ],
      "minimum_should_match" : 1,
    }
  }
```

+ dis max query

最大分查询

+ function_score

> The function_score allows you to modify the score of documents that are retrieved by a query. 

+ boosting

> The boosting query can be used to effectively demote results that match a given query.


+ indices query

索引查询

```
 "query": {
        "indices" : {
            "indices" : ["index1", "index2"],
            "query" : { "term" : { "tag" : "wow" } },
            "no_match_query" : { "term" : { "tag" : "kow" } }
        }
    }
```


#### script query

```
"query": {
        "bool" : {
            "must" : {
                "script" : {
                    "script" : {
                        "inline" : "doc['num1'].value > params.param1",
                        "lang"   : "painless",
                        "params" : {
                            "param1" : 5
                        }
                    }
                }
            }
        }
    }
```
 
 
####  结构化

```
{
    QUERY_NAME: {
        ARGUMENT: VALUE,
        ARGUMENT: VALUE
    }
}
```

特定field时

```
{
    QUERY_NAME: {
        FIELD_NAME: {
            ARGUMENT: VALUE,
            ARGUMENT: VALUE,...
        }
    }
}
```

复合query时 包上简单结构的。如bool里包match和range

```
{
    "bool": {
        "must":     { "match": { "tweet": "elasticsearch" }},
        "must_not": { "match": { "name":  "mary" }},
        "should":   { "match": { "tweet": "full text" }},
        "filter":   { "range": { "age" : { "gt" : 30 }} }
    }
}
```

bool + bool

```
{
    "bool": {
        "must": { "match":   { "email": "business opportunity" }},
        "should": [
            { "match":       { "starred": true }},
            { "bool": {
                "must":      { "match": { "folder": "inbox" }},
                "must_not":  { "match": { "spam": true }}
            }}
        ],
        "minimum_should_match": 1
    }
}
```

curl -XGET 'localhost:9200/storm*/_analyze?field=message_infos.event.specificType' -d 'FRESH_AIR_VOLUME_LESS'