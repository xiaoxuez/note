## 统计症状及对症药。


<font color=blue>症状</font> Storm Topology出现周期性重启的情况，查看log, log字段为session timeout, 以及client close connection，然后就重启了。产生原因为Zookeeper生成的事务性日志耗尽了内存。

<font color=red>解决方法</font> 定期清理Zookeeper的事务性日志。


<font color=blue>症状</font> Topology heap Out Of Memory，分析heapdump后发现是数据库jdbc4Connection连接占据了大量heap。

<font color=red>解决方法</font> 确认所有ResultSet以及Statement的对象都正常close掉了，若仍然出现，使用dbcp，如BasicDataSource..每次使用完Connection都归还给线程池。


<font color=blue>症状</font> Topology 内存占用随时间上涨，导致的问题是worker的工作变慢..隐隐约约觉得是代码导致的..但不太知道从何查起..

<font color=red>解决方法</font>?