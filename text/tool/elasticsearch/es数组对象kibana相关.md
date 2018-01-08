## ES数组对象，以及Kibana相关操作

好久未写过博客，一晃就年底。

前两天需求是在Kibana里生成曲线，关键是数据类型是数组对象。稍微走了写歪路，下面从数据类型开始说起。

### 数据

+ mapping:

	```
	data: {
		properties: {
			data_value: {
				type: "long"
			},
			index: {
				type: "long"
			},
		}
	}
	``` 
+ 示例

	```
	//example1: 
	{data: [{index: 0, data_value: 200}, {index: 1, data_value: 300}]}
	
	//example2: 
	{data: [{index: 0, data_value: 200}]}
	```
	即data为数组对象，每次含有的index的有可能是多个，有可能是1个。
	
+ 数组对象在es中的存储

   ```
    //example1: 
    { 
      data: {
      	 index: [0, 1],
      	 data_value: [200, 300],
      }
    }
   ```
   
   在ElasticSearch中对数组对象的访问可通过data.index，data.data\_value的形式进行。示例中example1的存储如上所示，0和200，1和300之间的关系其实就不再存在了。
   
   
   当时我的需求是，需要对index进行分桶，针对每个index，求出metric。如在index=0的桶里，求data.data\_value的max,桶里边只有example1和example2的数据，理想中的max应该是200,但求出来发现是300...问题就在存储上，data.data_value在存储上其实是[200, 300],与index=0还是1一点关系都没有..
   
   如果非要实现对index=0下求data_value的max呢?
   
   
### Scripted Fields

当时需求是在Kibana上实现，故以下皆以Kibana作例，若是单纯的写ES的查询或聚合语句，可直接参考script内容。

[How to create Kibana Script Fields](https://www.elastic.co/guide/en/kibana/current/scripted-fields.html)
 
 在ES中支持查询返回一个script value,即在查询时就进行相应的script计算。[elasticsearch script fields starting](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-request-script-fields.html).
 
 
 既然是script，自然就灵活很多，可以通过添加新的字段，将上面的数组对象中的值转换为key-value格式的，那么在聚合统计操作中选择对应的key即可。
 
 具体操作为，在Kibana中选择Management中的Index Patterns,选中对应index,选择Scripted field，新建一个(Add Scripted Field)。Name填入如data\_index\_0,Language选择painless,Type选择为number。script内容为
 
 ```
  if(params['_source']['data'] != null) {
	  for(def item : params['_source']['data']) {
	     if(item.index == 0)
	       return item.data_value;
	  }
	}
return null;
 ```
 
 最后选择创建字段即可，在Discover页面中即可看到每条数据中都增加了data\_index\_0,其值为index=0时对应的data_value。这样就将对象数组中的数据搬出来以key-value的方式存在了。另外，官方在介绍Scripted Fields时说明尽量不要使用\_source字段，会使搜索变慢，以后在使用的过程中最好还是使用doc。如获得data.index的方式为doc['data.index'].values,返回值为List。