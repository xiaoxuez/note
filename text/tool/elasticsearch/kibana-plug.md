## Kibana插件第一视角

### Kibana插件大概类型有
	
+ visTypes 视图组件，Visualize
+ app 应用组件，如timeline
+ hacks, Any module that should be included in every application
+ chromeNavControls,
+ [更多](https://www.elastic.co/guide/en/kibana/current/development-uiexports.html)...

#### 首要目标是visTypes

---


### 全军出击

+ git clone kibana，切换到与es对应的版本
+ npm install, 
	
	+ git切换 http

	```
	git config --global url."https://".insteadOf "git://"
	```
	
	+ chromedriver失败
	
	```
	npm ERR! chromedriver@2.32.3 install: `node install.js`
	```
	
	 解决方法
	 
	```
	npm install chromedriver --chromedriver_cdnurl=https://npm.taobao.org/mirrors/chromedriver
	```
	
	+ 默认dev 启动方式会使用ssl,所以是https,如果需要修改的话，可以修改\kibana\src\cli\serve\serve.js文件。
	
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

+ npm start


### First Blood

自定义visTypes的话，在kibana/plugins下新建文件夹，在新建的文件夹下执行

```
sao kibana-plugin
```

可生成对应文件夹格式，如果是visTypes的话，app component，translation files，an hack component， a server API都选no就好了

+ new TemplateVisType参数解析

```
return new TemplateVisType({
    name: 'extended_metric',
    title: 'Extended Metric',
    description: 'Based on the core Metric-Plugin but gives you the ability' +
      'to output custom aggregates on metric-results.',
    icon: 'fa-calculator',
    template: extendedMetricVisTemplate, //视图模板
    params: { //Options选项
      defaults: {
        handleNoResults: true,
        fontSize: 60,
        outputs: [
          {
            formula: 'metrics[0].value * metrics[0].value',
            label: 'Count squared',
            enabled: true
          }
        ]
      },
      editor: metricVisParamsTemplate //Options视图模板
    },
    schemas: new Schemas([ //Data相关
      {
        group: 'metrics',
        name: 'metric',
        title: 'Metric',
        min: 1,
        defaults: [
          { type: 'count', schema: 'metric' }
        ]
      }
    ])
  });
```

上面的例子来源于[地址](https://github.com/ommsolutions/kibana_ext_metrics_vis)，一个vis插件。


看着AngularJS的语法，我想静静。

目前看起来，vis包括主要的内容包括

+ schema, 为左边栏Data部分，选择已有的组件即可。一般来说，metric 数值聚合肯定是 Y 轴；bucket 聚合肯定是 X 轴；而在此基础上，Kibana4 还可以让 bucket 有不同效果，也就是 Schema 里的 segment(默认), group 和 split。根据效果不同，这里是各有增减的，比如饼图就不会有 group。

+ Options(params),为左边栏Options部分，包括渲染html

+ 显示可视化视图html

+ controller, 为html的逻辑部分, V <-> C是双向绑定的。


先梳理一下需求，最好是在es聚合数据拿到之后，到view的那层中间将数据做修改。然后我需要找到数据处理的代码。既然这样，去看看原始vis组件吧。[参考](https://sunyonggang.gitbooks.io/elkstack-guide-cn/content/kibana/v4/source-code-analysis/visualize_app.html)

+ src/core\_plugins/kbn\_vislib\_vis\_types下方是各组件的定义部分
+ src/ui/public/vislib是vis具体渲染，vis组件的controller和html渲染主要就是在这个文件下，然而我没找到比较明显的controller,渲染主要是d3进行渲染

感觉很茫然..

走不下去的时候，又倒回来重新走。解析一下上面粘的代码，首先是TemplateVisType。


```
export default function TemplateVisTypeFactory(Private) {
  const VisType = Private(VisVisTypeProvider);
  const TemplateRenderbot = Private(TemplateVisTypeTemplateRenderbotProvider);

	// ·。第一，继承VisType，增加了template检测
  _.class(TemplateVisType).inherits(VisType);
  function TemplateVisType(opts = {}) {
    TemplateVisType.Super.call(this, opts);

    this.template = opts.template;
    if (!this.template) {
      throw new Error('Missing template for TemplateVisType');
    }
  }
	// ·。第二，增加了createRenderbot，下面会对比看看，重写的意义
  TemplateVisType.prototype.createRenderbot = function (vis, $el, uiState) {
    return new TemplateRenderbot(vis, $el, uiState);
  };

  return TemplateVisType;
}

```

然后是看看VislibVisType，基本vis继承的是这个，跟TemplateVisType应该是差不多的

```
export default function VislibVisTypeFactory(Private) { 
	...
	const updateParams = function (params) {
		const updateIfSet = (from, to, prop, func) => {
	      if (from[prop]) {
	        to[prop] = func ? func(from[prop]) : from[prop];
	      }
	    };
	    ...
	})
	...
	// ·。第一，继承VisType，看到responseConverter默认是转换为点线图数组的。
	_.class(VislibVisType).inherits(VisType);
   function VislibVisType(opts = {}) {
     VislibVisType.Super.call(this, opts);
	
     if (this.responseConverter == null) {
       this.responseConverter = pointSeries;
     }
	
     this.listeners = opts.listeners || {};
   }
	// ·。第二，增加了createRenderbot
	   VislibVisType.prototype.createRenderbot = function (vis, $el, uiState) {
	   // 在返回新VislibRenderbot对象之前，对vis.params进行了相应的转换和判断，比如把一些params下的属性对应移到params.seriesParams[0]或者params.valueAxes[0]，params.categoryAxes[0]下
	    updateParams(vis.params);
	    return new VislibRenderbot(vis, $el, uiState);
	};
}
```

好啦，再看看他们都实现的VisType的庐山真面目

```
export default function VisTypeFactory(Private) {
  /**
   * Provides the visualizations for the vislib 👈官方注释很重要。大概意思是输入参数为angular module,输出为vis的class
   *
   * @module vislib
   * @submodule VisTypeFactory
   * @param Private {Object} Loads any function as an angular module
   * @return {Function} Returns an Object of Visualization classes
   */
  return {
    pie: Private(VislibVisualizationsPieChartProvider),
    point_series: Private(VislibVisualizationsPointSeriesProvider)
  };
}

```

好，其实一点没看到，比如继承那个语法就很蒙圈...不过暂时连蒙带猜的总结下吧。就是讲一个angular module搞到vis的class的过程。具体是怎么搞的，可能一会会看到，可能不会。接下来看看VislibRenderbot的代码。

```
	//具体就不贴了，主要定义了几个类似生命周期的方法，如
	VislibRenderbot.prototype._createVis
	VislibRenderbot.prototype._getVislibParams
	VislibRenderbot.prototype.destroy 
	VislibRenderbot.prototype.updateParams 
	//看到render方法了，贴一下具体的代码，看起来重要的是，buildChartData方法和vislibVis对象
	VislibRenderbot.prototype.render = function (esResponse) {
	    this.chartData = this.buildChartData(esResponse);
	    return AngularPromise.delay(1).then(() => {
	      this.vislibVis.render(this.chartData, this.uiState);
	      this.refreshLegend++;
	    });
	  };

```

好吧..再往下追，很多很多..就不挨着解释了，大概说一下，vislibVis类中主要是选择和绘制图。在src/ui/visualization下是各个组件确切的绘图代码，他们都继承自_chart.js,在_chart.js中看到render方法调用自身draw方法，故实现_chart的类实现draw方法即可。好啦，源码分析在这里就差不多了，其实还是没有找到自己想找的东西。这个时候在github上看到了另一个vis的插件。

Vis类中是渲染了，然后render的参数，data -> {Object} Elasticsearch query results, 要找data从哪里来的话，就开始找类似vis.render的调用，搜了下，调用位置在VislibRenderbot的\_createVis方法中，恩，这就跟一开始联系起来了，又回到了VislibRenderbot上了。然后继续找vislibrenderbot实例化的位置，来到了src/ui/public/visualize/visualize.js，看起来这像是高地了。

```
  $scope.$watch('esResp', prereq(function (resp) {
    if (!resp) return;
    $scope.renderbot.render(resp); //es请求回来的结果传给了renderbot
  }));
```

倒回去看看VislibRenderbot的代码

```
  VislibRenderbot.prototype.render = function (esResponse) {
    this.chartData = this.buildChartData(esResponse);
    return AngularPromise.delay(1).then(() => {
      this.vislibVis.render(this.chartData, this.uiState);
      this.refreshLegend++;
    });
  };
```

好! 好像稍微有点眉目了。通过render方法把es的数据传给了renderbot，renderbot通过buildChartData进行搞事完了就是最后的chartData了，也就是vis.render中的data了。


恩! 仔细一想，好像还是有点乱，再捋捋。VislibVisType的方法createRenderbot返回的是VislibRenderbot,然后VislibRenderbot的是跟visualize连接的核心，在visualize中，VislibRenderbot进行初始化，传入vislibVis的html元素，和es返回的data,然后在VislibRenderbot内部进行绘制。默认的VislibRenderbot内部data会进行buildChartData转换。

#### 示例extended_metric

然后再看TemplateVisType, 这个是自定义时候的VislibVisType，对应的renderbot为TemplateRenderbot,定义的render为，就是裸数据，在$scope中。

```
  TemplateRenderbot.prototype.render = function (esResponse) {
    this.$scope.esResponse = esResponse;
  };
```

最开始提到了extended_metric插件，然后以这个为例，看看数据的流向。在controller中

```
  // watches
  $scope.$watch('esResponse', function (resp) {
    if (resp) {
      calcOutputs.length = 0;
      metrics.length = 0;
      for (let key in metrics) {
        if (metrics.hasOwnProperty(key)) {
          delete metrics[key];
        }
      }
      $scope.processTableGroups(tabifyAggResponse($scope.vis, resp));
    }
  });

```

啊虽然是找到了数据的来源和流向，可是，又是Angular.. Angular基础为零的我心好累...不过也算是了然了。然后具体看看extended_metric插件拿到数据之后做的事

```
  $scope.processTableGroups = function (tableGroups) {
    tableGroups.tables.forEach(function (table) {
      table.columns.forEach(function (column, i) {
        const fieldFormatter = table.aggConfig(column).fieldFormatter();
        let value = table.rows[0][i]; //es数据存在table.rows内
        let formattedValue = isInvalid(value) ? '?' : fieldFormatter(value);     //数据转换后拿出label, formattedValue
        const metric = {
          label: column.title,
          value: value,
          formattedValue: formattedValue
        };
        metrics.push(metric);
        metrics[column.title] = metric;
      });
    });
    updateOutputs();
  };
```

上面两段代码是相连的，首先processTableGroups的参数是tabifyAggResponse($scope.vis, resp)，tabifyAggResponse是一个内部已有的方法，主要是把es返回的已知结构的数据，转换成标准格式，这里选择的是table,如果是line的话，可以看看point_series.js。转换包括参数包括数据之外，还有vis,转换完成得到的数据中包含es数据之外，还包括选项卡中的数据，如设置的label等。

#### 示例line\_sg

示例[地址](https://github.com/sbeyn/kibana-plugin-line-sg)

然后分析一下，顺便巩固一下上面的知识。

在line\_sg.js中，熟悉的控件，TemplateVisType，包含params和schemas。

在视图line\_sg.html中,使用c3绘制，只有几行代码，看似很简单，我想控制绘制的代码应该在controller中，然后看看\_params.html,是Option的选项卡，主要应该是视图的配置相关。然后看看controller,在视图controller中，前面很大一部分是关于要画的准备工作，以数据流向为接入点

```
  $scope.$watch('esResponse', function (resp) {
      if (resp) {
        console.log(resp);
        metrics.length = 0; //重置metrics，重置数组的方式是修改length,emmm学到了
        $scope.processTableGroups(tabifyAggResponse($scope.vis, resp)); //还是使用的数据表格形式，为什么大家都选这个转换形式？我猜可能是因为数据比较好拿出来..数据还是集中的，数据拿出来搞成自己格式的，与视图相关的数据都在metrics中，
        $scope.showGraph(); //从metrics中拿出数据，做图
      }
    });
```


这个是示例是使用c3来完成绘制的示例，因为和需求的方波刚好可以吻合，就具体细节学习下吧。首先将代码拷入plugins文件夹，启动kibana,查看效果。发现不怎么好看，另外选择chart_types=step似乎不工作，看看代码吧。


首先尝试是，设置params之后，点击重绘，没有效果。

仔细看了看插件的绘制和c3的使用，看了看线上的kibana，发现line是真的可以有step..而且图形没有错...其实本来line画图就有方波。好吧，还是先看看这个插件为什么设置params之后，点击重绘，没有触发重绘吧。结论是没有监听变量的变化..$scope.$watch、$watchController等监听方法，虽然没有看到具体代码，大概是，侧边栏部分没有修改的话，那个重绘的按钮就无法点击，那个按钮的点击事件应该是触发watch的监听事件的，有修改，就会触发监听事件，要重绘的话其实就是在监听事件的回调中加入视图重绘的部分，其实数据会自动绑定，只是需要个刷新机制。









----------



```
                                                 /===-_---~~~~~~~~~------____
                                                |===-~___                _,-'
                 -==\\                         `//~\\   ~~~~`---.___.-~~
             ______-==|                         | |  \\           _-~`
       __--~~~  ,-/-==\\                        | |   `\        ,'
    _-~       /'    |  \\                      / /      \      /
  .'        /       |   \\                   /' /        \   /'
 /  ____  /         |    \`\.__/-~~ ~ \ _ _/'  /          \/'
/-'~    ~~~~~---__  |     ~-/~         ( )   /'        _--~`
                  \_|      /        _)   ;  ),   __--~~
                    '~~--_/      _-~/-  / \   '-~ \
                   {\__--_/}    / \\_>- )<__\      \
                   /'   (_/  _-~  | |__>--<__|      |
                  |0  0 _/) )-~     | |__>--<__|      |
                  / /~ ,_/       / /__>---<__/      |
                 o o _//        /-~_>---<__-~      /
                 (^(~          /~_>---<__-      _-~
                ,/|           /__>--<__/     _-~
             ,//('(          |__>--<__|     /                  .----_
            ( ( '))          |__>--<__|    |                 /' _---_~\
         `-)) )) (           |__>--<__|    |               /'  /     ~\`\
        ,/,'//( (             \__>--<__\    \            /'  //        ||
      ,( ( ((, ))              ~-__>--<_~-_  ~--____---~' _/'/        /'
    `~/  )` ) ,/|                 ~-_~>--<_/-__       __-~ _/
  ._-~//( )/ )) `                    ~~-'_/_/ /~~~~~~~__--~
   ;'( ')/ ,)(                              ~~~~~~~~~~
  ' ') '( (/
    '   '  `

```



---------

后面整理了下，思路稍微清晰一点的。


开发插件的话，都需要一定的模板，官方推荐的生成模板的脚手架[sao](https://github.com/saojs/sao),具体使用[样例](https://github.com/elastic/template-kibana-plugin)。还有插件开发的[教程](https://www.gitbook.com/book/trumandu/kibana-plugin-development-tutorial/details)。篇幅原因，就省去教程系列，主要看看结构，源码什么的..

### visTypes

Visualize插件的开发，组成主要是es6,angular(听说angular1和2完全没关系，特地指出kibana采用的是angular1)。kibana的Visualize组件的绘制主要是通过[d3](https://d3js.org/)这个伟大的可视化库。


#### 插件样例

从众多优秀的vis插件中选择了一个比较简单的[样例](https://github.com/ommsolutions/kibana_ext_metrics_vis)来做分析(主要是人家有图)。效果如gif图，用文字来说明的话，就是实现了一个可计算的插件，选择metric值，然后填入计算公式，得出值。(因为见识有限，以前总说在kibana中实现计算很复杂，现在看来真是啪啪(*10)打脸)。


extended\_metric\_vis.js很明显，是这个插件的大头。稍微粘一点代码。

```
	return new TemplateVisType({
	    name: 'extended_metric',
	    title: 'Extended Metric',
	    description: 'Based on the core Metric-Plugin but gives you the ability' +
	      'to output custom aggregates on metric-results.',
	    icon: 'fa-calculator',
	    template: extendedMetricVisTemplate, //视图模板.html
	    params: { //Options选项，预定义的Options选项及值，在对应视图中会有具体使用
	      defaults: {
	        handleNoResults: true,
	        fontSize: 60,
	        outputs: [
	          {
	            formula: 'metrics[0].value * metrics[0].value',
	            label: 'Count squared',
	            enabled: true
	          }
	        ]
	      },
	      editor: metricVisParamsTemplate //Options选项卡视图模板.html
	    },
	    schemas: new Schemas([ //Data相关，这里就不是自由发挥的地界了，属于半命题类型，根据自己需要的数据选择对应类型，基本
	    					//就是metric，bucket的组合，一般来说，metric 数值聚合肯定是 Y 轴；bucket 聚合肯定是 X 轴；
	      {           //而在此基础上，还可以让 bucket 有不同效果，也就是 Schema 里的 segment(默认), group 和 split。
	        group: 'metrics',
	        name: 'metric',
	        title: 'Metric',
	        min: 1,
	        defaults: [
	          { type: 'count', schema: 'metric' }
	        ]
	      }
	    ])
	  });
```

如果稍微看了下vis插件的教程的话，就会很熟悉，因为模板就差不多是这样，利用TemplateVisType生成对应实例，基本备注都在代码里了，就不再啰嗦了。

然后根据上面的代码直接的找到两个html，extended\_metric\_vis.html和extended\_metric\_vis\_params.html, 很显示，就是view,会看到伟大的angular进行mvc的管理，然后去看c => extended\_metric\_vis\_controller.js。这个代码里，主要是angular与其控制器的绑定，跳过这个，进到方法里。

```
$scope.$watch('esResponse', function (resp) {
    if (resp) {
      calcOutputs.length = 0;
      metrics.length = 0;
      //清空metrics
      for (let key in metrics) {
        if (metrics.hasOwnProperty(key)) {
          delete metrics[key];
        }
      }
      $scope.processTableGroups(tabifyAggResponse($scope.vis, resp));
    }
  });
```

很明显，这里是接受es查询结果的代码，结论很明显是，获得es查询结果只需要监听esResponse变量就可以。然后看看拿到结果的处理processTableGroups方法。首先是tabifyAggResponse是按照已知的es查询结果结构生成与视图(data选项卡)强相关的数据结构，为什么说强相关呢，比如tabifyAggResponse后的结果主要包括columns和rows，分别是data选项卡的分组和对应的数据，就拿这个例子来说，如果新建了两个metric, columns里就是这两个metric的属性，包含title(即输入的label)等，对应index的rows里的数据就是对应metric的结果。转换之后，在processTableGroups方法里，只需要把对应的数据拿出来组合下，比如，例子里就是以columns中的title为键，以metric的结果为值，组成数据到metrics(装数据的对象，下方会用到)中。

view有了，数据也有了，还差最后一步，就是怎么把公式套进去？在这里，我不得不再次感叹下js的伟大。

```
  try {
    const func = Function("metrics", "return " + output.formula); //<<<<=这里是亮点
    output.value = func(metrics) || "?";
  } catch (e) {
    output.value = '?';
  }
```

output.formula为输入的公式，类型为string，然后一句string的字符串就变成方法了？！是的。很神奇。有了方法，把数据套进去，答案就出来了。

大概内容就是这样，可能还有些细节需要注意，比如，修改了data/option点击那么重绘的按钮，重新计算，需要监听对应数据，如

```
	//updateOutputs为计算的过程
	$scope.$watchCollection('vis.params.outputs', updateOutputs);
```


#### 源码样例

接下来就看看稍微肤浅点的源码，时间和能力确实有限啊。比如上面出现过的TemplateVisType，整体理解下结构和运作。

+ src/core\_plugins/kbn\_vislib\_vis\_types
	
  自带的vis组件定义的地方。还包括options选项卡的html,js等等，看看line.js的部分代码
  
  ```
  return new VislibVisType({
    name: 'line',
    title: 'Line',
    image,
    description: 'Emphasize trends',
    category: VisType.CATEGORY.BASIC,
    params: {
      defaults: {
        grid: {
          categoryLines: false,
          style: {
            color: '#eee'
          }
        },
        ...
      },
      positions: ['top', 'left', 'right', 'bottom'],
      ...
      editor: pointSeriesTemplate,
      ...
      schemas: new Schemas([
      {
        group: 'metrics',
        name: 'metric',
        title: 'Y-Axis',
        min: 1,
        aggFilter: ['!geo_centroid'],
        defaults: [
          { schema: 'metric', type: 'count' }
        ]
      },
     	....
    ])
  
  ```

 和上方extended\_metric\_vis.js粘出来的代码很相似吧，只不过一个是使用的TemplateVisType，一个是VislibVisType，其实这两个类也很相似，都是继承vis\_type。不过从这点上来看，定义vis组件的套路其实差不多，区别呢，就是官方的组件的话视图绘制的部分应该是内定的，直接选择出来绘制就好了(在后面会看到选择的代码)，但是自定义vis组件的话，视图部分是需要自己自定义的，而不是选择原有的，所以自定义需要TemplateVisType对象，传入template视图。

+ src/public/ui/vis/vis\_type

	定义基本属性和方法createRenderbot。继承这个的话需要实现createRenderbot方法返回Renderbot对象。
  
+ src/public/ui/vislib\_vis\_type/vislib\_vis\_type

	继承vis_type。
	
    ```
		...
		const updateParams = function (params) {
			const updateIfSet = (from, to, prop, func) => {
		      if (from[prop]) {
		        to[prop] = func ? func(from[prop]) : from[prop];
		      }
		    };
		    ...
		})
		...
		// ·。第一，继承VisType，看到responseConverter默认是转换为点线图数组的。
		_.class(VislibVisType).inherits(VisType);
	   function VislibVisType(opts = {}) {
	     VislibVisType.Super.call(this, opts);
		
	     if (this.responseConverter == null) {
	       this.responseConverter = pointSeries;
	     }
		
	     this.listeners = opts.listeners || {};
	   }
		// ·。第二，实现createRenderbot
		   VislibVisType.prototype.createRenderbot = function (vis, $el, uiState) {
		   // 在返回新VislibRenderbot对象之前，对vis.params进行了相应的转换和判断，比如把一些params下的属性对应移到params.seriesParams[0]或者params.valueAxes[0]，params.categoryAxes[0]下
		    updateParams(vis.params);
		    return new VislibRenderbot(vis, $el, uiState);
		};
	
	```
	可以看到基本就是params的转换，和返回VislibRenderbot作为Renderbot对象，那么，久闻的Renderbot的作用？预知后事如何，请看后面..
	
+ src/public/ui/vislib\_vis\_type/vislib\_renderbot

	恩，Renderbot中定义了需实现的一些方法，VislibRenderbot就继承Renderbot，故就直接看VislibRenderbot的代码了。
	
	```
	  VislibRenderbot.prototype.render = function (esResponse) {
	    this.chartData = this.buildChartData(esResponse);
	    return AngularPromise.delay(1).then(() => {
	      this.vislibVis.render(this.chartData, this.uiState); //绘制
	      this.refreshLegend++;
	    });
	  };
	
	```
	例如render方法,通过调用vislibRenderbot.render方法实现绘制。
	看到这里，不知道有没有看到加了注释那句代码，绘制的主要工作应该就在这行代码，那么好奇一下this.vislibVis又是什么呢？找到this.vislibVis的初始化
	
	```
	this.vislibVis = new vislib.Vis(this.$el[0], this.vislibParams);
	```
	
	在Vis中，主要是visConfig和handler, handler绘制，visConfig选择生成相应的数据结构。对于visConfig的代码
	
	```
	//src/public/ui/vislib/vis_config.js
	const visType = visTypes[visConfigArgs.type];
	const typeDefaults = visType(visConfigArgs, this.data); // <= 数据结构更改
	
	//visTypes => src/public/ui/vislib/types
	  return {
	    histogram: pointSeries.column,
	    horizontal_bar: pointSeries.column,
	    line: pointSeries.line,
	    pie: Private(VislibLibTypesPieProvider),
	    area: pointSeries.area,
	    point_series: pointSeries.line,
	    heatmap: pointSeries.heatmap,
	  };
	```
	
	handler绘制,handler的render方法中,可以看到chart.render
	
	```
	// render in handler
	render() {
		...
		const chart = new self.ChartClass(self, this, chartData);
		...
		chart.render();
	}
	
	//ChartClass in handler
	this.ChartClass = chartTypes[visConfig.get('type')];
	
	//chartTypes from src/public/ui/vislib/visualizations/vis_types
	//这里面是进行可视化的地方，可视化对象都继承自Chart类，components/vislib/visualizations/_chart
	  Chart.prototype.render = function () {
	      var selection = d3.select(this.chartEl);
	      selection.selectAll('*').remove();
	      selection.call(this.draw());
	   };
    //也就是说，各个可视化对象，只需要用 d3.js 或者其他绘图库，完成自己的 draw() 函数，就可以了！可以在本文件夹下查看具体一些绘制代码
	```
	
+ src/public/visualize/visualize

	```
	  $scope.$watch('esResp', prereq(function (resp) {
        if (!resp) return;
        $scope.renderbot.render(resp);
      }));
	```
   很明显，visualize.js是Visualization的大Boss，通过代码可以看出，visualize与vis_types交互主要通过调用renderbot的各种方法，包括render。
   
 
大致总结一下，要定义vis组件的话，返回VislibVisType或TemplateVisType对象，这两个类内部都会生成对应的renderbot对象，visualize与通过调用renderbot对象进行一系列的操作，如果是自定义组件的话，返回的TemplateVisType对象需要具有template属性，为图表视图模板(html), 官方自带的组件的话，会根据类型进行选择视图。对于视图的绘制(包括修改选项卡，重绘)，visualize会调用renderbot.render方法，VisRenderbot的render方法即重绘..在上方粘贴的代码也可以看到，TemplateRenderbot的render方法是将esResponse数据绑定到当前作用域的esResponse变量上，所以自定义插件中要完成绘制的话，需要监听当前作用域的esResponse变量。

好啦，官方自带的组件的代码介绍和自开发插件的代码介绍就到这里就结束啦。


