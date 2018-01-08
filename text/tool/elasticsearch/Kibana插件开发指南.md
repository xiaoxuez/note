## Kibana插件开发指南

本文档参考自以下资源

+ [trumandu-tutorial](https://trumandu.gitbooks.io/kibana-plugin-development-tutorial/content/)
+ [timroes.de](https://www.timroes.de/2015/12/02/writing-kibana-4-plugins-basics/)


### Kibana开发环境搭建

+ github上下载kibana的源代码，切换到对应版本，kibana的版本很重要，kibana在升级的过程中api有可能会变化，所以插件的开发需要针对特定的版本，kibana多版本的话，插件也需要多版本。（本文全局变量kibana是5.3.4版本）。
+ npm install安装依赖。

	- 如果公司防火墙限制从github下载依赖的话，git下载的方式将ssh替换成http

		```
		git config --global url."https://".insteadOf "git://"
		//或者在.gitconfig文件中添加
		[url "https://"]
    insteadOf = git://
		```
	
	- 安装过程中，可能会有依赖下载失败，或欠缺依赖，有提示的话，按照提示来单独下载就好了，没提示的话，愿谷歌保佑你。

+ npm start, 默认dev 启动方式会使用ssl,所以是https,如果需要修改的话，可以修改\kibana\src\cli\serve\serve.js文件。修改位置如下：

	```
	if (opts.dev) {
	    set('env', 'development');
	    set('optimize.lazy', true);
	
	    // if (opts.ssl) {
	    //   set('server.ssl.enabled', true);
	    // }
	
	    // if (opts.ssl && !has('server.ssl.certificate') && !has('server.ssl.key')) {
	    //   set('server.ssl.certificate', DEV_SSL_CERT_PATH);
	    //   set('server.ssl.key', DEV_SSL_KEY_PATH);
	    // }
	  }
	```
	
### 加入插件

将代码放在kibana的plugins文件夹下，Kibana会自动watch这些文件的changes。(只能直接把代码/文件夹放过来，并不能用软连接)。

当你修改了代码，kibana会自动重新打包，会需要点时间，在命令行的console上能够看到如下提示

```
restarting server due to changes in
 - "installedPlugins/tr-k4p-clock/index.js"
server log [21:49:59.323] [info][status][plugin:tr-k4p-clock] Status changed from uninitialized to green - Ready
[...]
server log [21:49:59.421] [info][listening] Server running at http://0.0.0.0:5601
optmzr log [21:50:07.177] [info][optimize] Lazy optimization started
optmzr log [21:50:13.834] [info][optimize] Lazy optimization success in 6.66 seconds
```

重新打包后，刷新浏览器就可以看到你的修改了(小吐槽，刷新kibana是个最耗费时间的操作)。

#### 基本插件

每个插件都是一个npm module,所以至少需要两个文件，package.json和index.js。

一个package.json的示例如下, 最好的话，文件夹名称，name保持一致，如下可以通过plugins/kibana\_gm\_cal\_vis/进入文件夹,具体可见index示例,

```
{
  "name": "kibana_gm_cal_vis",
  "version": "0.1.0",
  "kibana": {
    "version": "5.3.4" 
  }
}
```

一个index.js的示例如下

```
module.exports = function(kibana) {
  return new kibana.Plugin({
    uiExports: {
      visTypes: ['plugins/kibana_gm_cal_vis/gm_cal']
    } 
  });
};
```

#### 插件的安装

在Kibana上安装插件

```
bin/kibana plugin --install plugin-name -u https://url.to/plugin
```

如果是文件夹，直接放在plugins文件夹下就好了。

kibana-docker的话，可以把文件夹挂载到/usr/share/kibana/plugins/plugin-name，但插件更新的话需要重新run重新挂载，不知道是不是我操作有问题..


