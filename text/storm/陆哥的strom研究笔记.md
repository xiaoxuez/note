##Storm研究

###MVN的项目构建代码

* 配置阿里云的镜像文件, 记的要改里面的localrepo地址.
https://github.com/ae6623/Zebra/blob/master/maven-repo-settings-ali.xml
* 实践证明这篇文章是最好的http://awanke.github.io/2016/08/15/wiki-maven/, 即便是阿里的mvn库也不过如此.

```
mvn archetype:generate 
-DgroupId=storm.blueprints 
-DartifactId=Chapter1 
-DpackageName=storm.blueprints.chapter1.v1 
-DarchetypeCatalog=http://maven.aliyun.com/nexus/content/groups/public/ 
-DinteractiveMode=false -X
```

后面两段极其重要.第一段可以防止你卡死在typeCatalog的fetch上,第二段可以省掉那个恶心的interactive交互创建方式.

###Heron构建完成
两下就搞定了,前后不足10分钟...
heron明显架构上就很重编排和部署,很给力.
heron完全兼容Storm的topology,我想想还是先搞storm好了
heron支持用python写topology,这很好.


###storm-starter MVN构建完成
mvn -T 4 clean install 

一直遇到一个诡异的Plugin错误,三个小时都没搞定这个问题.
删插件,删maven,直到最后怒删maven仓库才修复了这个bug,严重怀疑是上一篇文章里面的那个国外的repo有问题,sha1对不上.又不知道出错的包在哪.
现在最佳的解决依赖的方法是坚持用阿里云的repo,所有阿里云找不到的repo写一个新的pom.xml统一去repo2的源下.


###storm-starter topologies构建
至少我目前知道有两种写法, 一种是Java写法,另外一种是Clojure写法.
heron的写法貌似更多,支持python.

```
storm-starter topologies can be run with the maven-exec-plugin. For example, to compile and run WordCountTopology in local mode, use the command:

$ mvn compile exec:java -Dstorm.topology=storm.starter.WordCountTopology
You can also run clojure topologies with Maven:

$ mvn compile exec:java -Dstorm.topology=storm.starter.clj.word_count
```

###Storm本地集群搭建
去storm官网下的包里面自带一个命令行工具,就有全部的本地集群.
我又重新用brew搞了一个.

###三本书的评价
阿里写的那本Storm实战什么已然是渣渣了,等于一本流水账.而且storm的版本也最老,0.8.2,现在的变化已经太多了.估计也就最后一章可以读,其他部分可扔.
大数据流式计算像是学校里面的人写出来的,还可以.
另外一本讲的很好,0,9几的版本.

###Storm架构
* nimbus
* supervisor
* ui
* zookeeper可选

###Clojure
这门语言就是Java下的Lisp啊, 怪不得有人学,不是纯FP.如果clojure靠谱的话,PhantomIFTTT的语法设计可抛.

###高级问题
包括并发,DPRC,事务一致,水平扩展,容错等.