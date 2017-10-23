## Docker


### 基本命令

+ 搜索镜像， 查看镜像，获取镜像,删除镜像

```
	docker search x
	docker images  //显示所有
	
	docker pull x
	docker pull x:1.3  //指定版本
	docker pull respority/x  //指定其他注册服务器仓库
	docker rmi x  //删除镜像
```

+ 容器

```
  docker run --name my_zookeeper -d zookeeper:latest //创建并启动容器
     //参数选择 -d, 后台运行； -t 分配一个伪终端； -i让容器的标准输入保持打开
     // -P 随机映射端口， -p 5000:5000 定制映射端口
     // --link 容器互联
     // -v 挂载文件夹 -v /local/data:/dockercontainer/data
     // --volumes-from 从容器中加载数据卷，
    
     
  docker ps 
  	 //参数选择 -a -q能看到处于终止状态的容器的ID信息
  	 
  	 
  docker exec -it 0fea8540cddf /bin/bash 
  		//进入容器
  	 
  docker stop my_zookeeper  //终止容器，终止后能用start开启
  docker rm my_zookeeper //删除容器
```

+ 集群

  docker-compose?

### 使用Docker搭建zookeeper

基本的命令就不粘了，主要说一下整个体验和总结。

使用docker run的时候就会跑一个默认的容器，且启动zookeeper, 进入容器后，默认进入的是zookeeper的目录，启动zookeeper的conf文件，并不是zookeeper下的conf/zoo.cfg配置文件，因为啥也没..

```
bash-4.3# pwd
/zookeeper-3.4.10/conf
bash-4.3# ls
bash-4.3#

```

看了下log,发现配置文件在根目录下，同理，data也在根目录下，不过好奇的是log日志我倒是没找到，似乎只能通过docker logs来看了，emmmm...

```
bash-4.3# cd /conf/
bash-4.3# ls
configuration.xsl  log4j.properties   zoo.cfg            zoo_sample.cfg
bash-4.3#
```

然后，出现了两个疑问，修改conf文件，data怎么维护呢？大概看了搜索了下，解决方式比较简单又熟悉的是，将conf文件和data都挂载进去，恩，就是这么直接~！

