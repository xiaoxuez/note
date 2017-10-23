## Template.json

这个文件即定义[Index Templates](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-templates.html)。

> Index templates allow you to define templates that will automatically be applied when new indices are created. 


### 结构分析

#### 外层结构：

	{
		"template": "nginx_elastic_stack_example",
		"settings": {}
		"mappings": {}
	}

+ template: 准确说来，应该叫template pattern，为能匹配到模板的index name pattern。如下面的示例
	
		//The settings and mappings will be applied to any index name that matches the te* pattern.
		"template": "te*",
		
+ settings: [可设置项](https://www.elastic.co/guide/en/elasticsearch/reference/current/index-modules.html), 常见的设置项如

		"settings" : {
	        "index" : {
	            "number_of_shards" : 3, //定义一个索引的主分片个数，默认值是5, 这个配置在索引创建后不能修改。
	            "number_of_replicas" : 2 //每个主分片的复制分片个数，默认是1。这个配置可以随时在活跃的索引上修改。
	          
	        }
	    }
	    
	   以上书写格式也可写成
	   
		   "settings" : {
		        "index.number_of_shards": 3
		    }
		    
	另外，主分片个数的意义在于，分片是存储'索引'下文档的容器，分片的个数就决定了存储的大小。

+ mappings: 对这个的解释引用一下原文比较恰当。

	>Mapping is the process of defining how a document, and the fields it contains, are stored and indexed.
	 
		"mappings": {
			"type1": {  //定义类型type1
			}
		}
	
#### Mapping 结构分析

>Each index has one or more mapping types, which are used to divide the documents in an index into logical groups

mapping type包含以下几项：

 +  [Meta-fields](https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-fields.html)，来自文档的元数据字段，以下划线开头，包括_index, \_type, \_id, _source等..
 +  properties，列出文档中可能包含的字段的映射
 +  [参数项](https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-params.html)，控制如何动态处理新的字段，如dynamic_templates，analyzer等..

其中，Field的数据类型可以为：
  
 + 简单数据类型，例如text，keyword，date，long，double，boolean，ip..其中，keyword和text的区别对应的是index explicit和index full text content。
 + 嵌套的对象或[nested](https://www.elastic.co/guide/en/elasticsearch/reference/current/nested.html)
 + 特别的类型，例如[geo_point](https://www.elastic.co/guide/en/elasticsearch/reference/current/geo-point.html)，[geo_shape](https://www.elastic.co/guide/en/elasticsearch/reference/current/geo-shape.html)， [completion](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-suggesters-completion.html)。
 

 
##### Dynamic Mapping

你可以不用定义mapping,fields， 也可以使用es的search，这都归功于的dynamic mapping。dynamic mapping包括\_default\_mapping， Dynamic field mappings，Dynamic templates。[先粘官方链接吧](https://www.elastic.co/guide/en/elasticsearch/reference/current/dynamic-mapping.html)

+ \_default\_mapping: 

	>The default mapping, which will be used as the base mapping for any new mapping types, can be customised by adding a mapping type with the name \_default_ to an index，

	如示例

		"mappings": {
		    "_default_": {  
		      "_all": {
		        "enabled": false
		      }
		    },
		    "user": {}, //user继承自_default_
		    "blogpost": {  //blogpost继承自_default_并重写了"_all"
		      "_all": {
		        "enabled": true
		      }
		    }
		  }

+ Dynamic field mappings: 一般说来，当文档中出现之前未见过的field，es会自动添加新field到映射中，这个功能可以通过设置进行disable，在这个功能是enabled的前提下，es添加新field时会转换类型，由json的数据类型转换到es的数据类型，如string-->text或keyword。其中，有的复杂类型的检测，可以自定义检测格式，例如自定义date的数据格式。官方有[示例](https://www.elastic.co/guide/en/elasticsearch/reference/current/dynamic-field-mapping.html)。

+ [Dynamic templates](https://www.elastic.co/guide/en/elasticsearch/reference/current/dynamic-templates.html#dynamic-templates): 这个重在使用，故直接贴上了链接，官方有示例使用说明。上文提到es自动添加新field到映射中，dynamic templates为定义一些添加模板，既然是模板，就要定义匹配模板的东西..例如match\_mapping\_type为指定数据类型，以及通过field name进行全匹配或正则匹配或全虚拟路径匹配(如a.b.*)，然后再加上mapping决定添加的新的field的特性。


### 参数字段分析

待完善补全.. 暂且列出现过的..

#### Mapping

+ fields: 适用于当一个字段有多种类型，如当搜索时，希望它是一个text，当排序或者聚合时，希望它是一个keyword。

		 "city": {
	          "type": "text",
	          "fields": {
	            "raw": { 
	              "type":  "keyword"
	            }
	          }
	        }
	    
	    //用于搜索时使用city,排序或聚合时使用city.raw,
	     "aggs": {
		    "Cities": {
		      "terms": {
		        "field": "city.raw" 
		      }
		    }
		  }

+ norms: 耗内存，如果不关心score，就置为false，尤其是字段仅仅用于filter或aggregation
 > Norms store various normalization factors that are later used at query time in order to compute the score of a document relatively to a query.
 
+ index: 决定field的value的index方式，值为analyzed(default, treat as full-text field), not_analyzed (treat as keyword field), no。

+ ignore\_above: Strings longer than the ignore_above setting will not be indexed or stored.

+ \_all: 将所有values的内容以空格进行存储，即\_all的内容大概为["..", ".."],可进行分析和索引，但不能存储。
 
+ dynamic：控制新字段是否被动态添加，所谓新字段，是较之前消息组成的字段而言。



