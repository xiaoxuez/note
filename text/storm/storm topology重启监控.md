## 重启监控

虽然Topology发生runtime exception会自动重启，但有时候还是需要知道一下发生了重启。查询了很多监控进程的，有monit, Zabbix之类的。这里留点印象.. 虽然现在没有选择使用，但以后可能会用到，最后选择了使用最笨最原始的方法，就是写一个Thrift Client来请求Thrift Server，来获取你想得到的集群和Topology的相关数据。

#### 监控error

代码是从[网上博客中](http://blog.csdn.net/damacheng/article/details/42706605)粘来的。

先说环境吧，觉得最麻烦的地方在于编译Thrift Client代码。于是，我就说服自己跳过了这一步，storm-core中封装了Thrift的api,直接用好像省事很多，虽然不是很好

粘上代码后，加上发邮件的功能，打成jar包，写一个脚本执行jar包，再定期执行。

👇就逆序粘代码吧:

```
*/5 * * * * /root/storm_script/topology_monitor/monitor.sh
```

```
#!/bin/sh

java -cp /opt/apache/storm/apache-storm-1.0.3/lib/asm-5.0.3.jar:/opt/apache/storm/apache-storm-1.0.3/lib/clojure-1.7.0.jar:/opt/apache/storm/apache-storm-1.0.3/lib/disruptor-3.3.2.jar:/opt/apache/storm/apache-storm-1.0.3/lib/kryo-3.0.3.jar:/opt/apache/storm/apache-storm-1.0.3/lib/log4j-api-2.1.jar:/opt/apache/storm/apache-storm-1.0.3/lib/log4j-core-2.1.jar:/opt/apache/storm/apache-storm-1.0.3/lib/log4j-over-slf4j-1.6.6.jar:/opt/apache/storm/apache-storm-1.0.3/lib/log4j-slf4j-impl-2.1.jar:/opt/apache/storm/apache-storm-1.0.3/lib/minlog-1.3.0.jar:/opt/apache/storm/apache-storm-1.0.3/lib/objenesis-2.1.jar:/opt/apache/storm/apache-storm-1.0.3/lib/reflectasm-1.10.1.jar:/opt/apache/storm/apache-storm-1.0.3/lib/servlet-api-2.5.jar:/opt/apache/storm/apache-storm-1.0.3/lib/slf4j-api-1.7.7.jar:/opt/apache/storm/apache-storm-1.0.3/lib/storm-core-1.0.3.jar:/opt/apache/storm/apache-storm-1.0.3/lib/storm-rename-hack-1.0.3.jar:storm-monitor.jar monitor.Monitor
```

//storm的依赖没有打进jar包中，使用nimbus的storm依赖就好了~



```
public class Monitor {
    public static Nimbus.Client getClient(String nimbusHost, int nimbusPort) throws TTransportException {
        TSocket tsocket = new TSocket(nimbusHost, nimbusPort);
        TFramedTransport tTransport = new TFramedTransport(tsocket);
        TBinaryProtocol tBinaryProtocol = new TBinaryProtocol(tTransport);
        Nimbus.Client c = new Nimbus.Client(tBinaryProtocol);
        tTransport.open();
        return c;
    }

    public static void main(String[] args) {
        try {
            //local mine port is 6667, storm nimbus is 6627
            Nimbus.Client client = getClient("127.0.0.1", 6627);
            List<TopologySummary> topoSummaryList = client.getClusterInfo().get_topologies();

            for (TopologySummary topologySummary : topoSummaryList) {
                TopologyInfo topologyInfo =client.getTopologyInfo(topologySummary.get_id());
                Set<String> errorKeySet =topologyInfo.get_errors().keySet();
                StringBuilder stringBuilder = new StringBuilder();
                for (String errorKey : errorKeySet) {
                    List<ErrorInfo> listErrorInfo = topologyInfo.get_errors().get(errorKey);
                    for (ErrorInfo ei : listErrorInfo) {
                        // 发生异常的时间
                        long expTime = (long) ei.get_error_time_secs() * 1000;
                        // 现在的时间
                        long now = System.currentTimeMillis();

                        // 由于获取的是全量的错误堆栈，我们可以设置一个范围来获取指定范围的错误，看情况而定
                        // 如果超过5min，那么就不用记录了，因为5min检查一次
                        if (now - expTime <= 5 * 1000 * 60) {
                            stringBuilder.append(ei.get_error());
                            stringBuilder.append('\n');
                        }
                    }
                }

                if (stringBuilder.length() > 1) {
                   //发送邮件，在java文件夹下有发送邮件的
                    sendMail(topologyInfo.get_name(), stringBuilder.toString());
                }
            }

        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (AuthorizationException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }

    }
}
```