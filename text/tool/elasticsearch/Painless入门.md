## Painless

> Painless is a scripting language developed and maintained by Elastic and optimized for Elasticsearch.


### 数据类型

+ def

  动态数据类型，默认值为null。
  
+ 其余的数据类型，和java基本相同。基本数据类型都有，对象类型的，Map,List也都有。基本api也都是java相应的api

跟Java的关系。（Java8）
>Extends Java’s syntax to provide Groovy-style scripting language features that make scripts easier to write.



### 示例

+ 遍历数组

  ```
  	for(def item :  doc['cmd'].values) {
  	  
  	}
  
  ```
  
  数据取出来是个对象，如doc['cmd'],需要取相应的值需要调用对应属性，数组的话则是values,数值的话value,日期类的话date
  
+ date类型操作

  ```
   return doc['begin_at'].date.hourOfDay
  ```
  这个date的类型对应到java里具体是啥.没搞清楚.有点像Calender的变体，Calendar能获得的属性都有


### 调试

关于调试，一直是我很在意又无奈的事。

Kibana添加script fields的话，使用Dev Tools先进行script的验证，看看有没有语法错之类的，成功之后再把scipt部分粘贴进去。简单的转换示例

```
GET /test/_search
{
  "query": {
    "match_all": {}
  },
  "script_fields": {
    "test_script": {
      "script": {
        "lang": "painless",
        "inline": "return doc['begin_at'].date.hourOfDay"
      }
    }
  }, 
  "size": 1
}

```

官方说明的调试是Debug.explain

```
PUT /hockey/player/1?refresh
{"first":"johnny","last":"gaudreau","goals":[9,27,1],"assists":[17,46,0],"gp":[26,82,1]}

POST /hockey/player/1/_explain
{
  "query": {
    "script": {
      "script": "Debug.explain(doc.goals)"
    }
  }
}

```

by responding

```
{
   "error": {
      "type": "script_exception",
      "to_string": "[1, 9, 27]",
      "painless_class": "org.elasticsearch.index.fielddata.ScriptDocValues.Longs",
      "java_class": "org.elasticsearch.index.fielddata.ScriptDocValues$Longs",
      ...
   },
   "status": 500
}
```
