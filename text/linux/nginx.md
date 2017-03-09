## http设置

[参考路径](https://www.zybuluo.com/phper/note/89391)

#### 基本命令
 + nginx  启动
 + nginx -s reload  平滑重启
 + nginx.conf 配置文件
 
#### 负载均衡
upstream 设置。负载均衡，保证后端多个服务器的负载均衡。     
Nginx的负载均衡模块目前支持4种调度算法:
1. weight 轮询（默认）。每个请求按时间顺序逐一分配到不同的后端服务器，如果后端某台服务器宕机，故障系统被自动剔除，使用户访问不受影响。weight。指定轮询权值，weight值越大，分配到的访问机率越高，主要用于后端每个服务器性能不均的情况下。
2. ip_hash。每个请求按访问IP的hash结果分配，这样来自同一个IP的访客固定访问一个后端服务器，有效解决了动态网页存在的session共享问题。
3. fair。比上面两个更加智能的负载均衡算法。此种算法可以依据页面大小和加载时间长短智能地进行负载均衡，也就是根据后端服务器的响应时间来分配请求，响应时间短的优先分配。Nginx本身是不支持fair的，如果需要使用这种调度算法，必须下载Nginx的upstream_fair模块。
4. url_hash。按访问url的hash结果来分配请求，使每个url定向到同一个后端服务器，可以进一步提高后端缓存服务器的效率。Nginx本身是不支持url_hash的，如果需要使用这种调度算法，必须安装Nginx 的hash软件包。


#### 反向代理

通过server设置，通过访问外网端口，映射到内网端口(目前我用到这个是这样的功能)。

server {
  listen 80;
  server_name storm.huantengsmart.com.cn;
  location / {
	proxy_pass http://localhost:8080;
	proxy_set_header  X-Real-IP  $remote_addr;
  }
}