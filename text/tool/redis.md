### 有序集合的操作
[更多](https://redis.readthedocs.io/en/2.4/sorted_set.html)

score+member为一个基。数据成对存。可以理解为score就是为了member排序存在的，有用的数据还是member.

+ ***ZADD***。ZADD key score member [[score member] [score member] ...]
将一个或多个member元素及其score值加入到有序集key当中。会将score和member都添加到集合中，排序依据是score。  
如果某个member已经是有序集的成员，那么更新这个member的score值，并通过重新插入这个member元素，来保证该member在正确的位置上。    
score值可以是整数值或双精度浮点数。    
如果key不存在，则创建一个空的有序集并执行ZADD操作。
当key存在但不是有序集类型时，返回一个错误。

示例    

	ZADD page_rank 10 google.com   
	ZADD page_rank 9 baidu.com 8 bing.com
	
	in page_rank:
	1) "bing.com"
	2) "8"
	3) "baidu.com"
	4) "9"
	5) "google.com"
	6) "10"
		
+ ***ZREM***。 ZREM key member [member ...]
移除有序集key中的一个或多个成员，不存在的成员将被忽略。    
当key存在但不是有序集类型时，返回一个错误。  

示例    
    
	REM page_rank google.com
	

+ ***ZCARD***。 返回有序集key的基数。
当key存在且是有序集类型时，返回有序集的基数。   
当key不存在时，返回0。


### 无需集合
[更多](https://redis.readthedocs.io/en/2.4/set.html)

+ SADD。 SADD key member [member ...]
将一个或多个member元素加入到集合key当中，已经存在于集合的member元素将被忽略。    
假如key不存在，则创建一个只包含member元素作成员的集合。    
当key不是集合类型时，返回一个错误。    

+ SREM。 SREM key member [member ...]
移除集合key中的一个或多个member元素，不存在的member元素会被忽略。    
当key不是集合类型，返回一个错误。

+ SMEMBERS。 SMEMBERS key
返回集合key中的所有成员。



[Jedis相对应的操作](http://www.importnew.com/19321.html)