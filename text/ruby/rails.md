## Rails, Hello World!

基于[rails入门](https://ruby-china.github.io/rails-guides/getting_started.html)的简要笔记和记录。

> Rails 是使用 Ruby 语言编写的 Web 应用开发框架，目的是通过解决快速开发中的共通问题，简化 Web 应用的开发。

### 基本结构

```
rails new blog //创建应用
```

对于文件夹的基本含义简单提一下，vendor包含第三方代码，如第三方gem; app包含整个应用的控制器，模型，视图，辅助方法，等...; config配置应用的路由数据库等；

```
bin/rails server //启动服务器
```

+ 控制器，控制器接受向应用发起的特定访问请求。

```
bin/rails generate controller Welcome index //创建控制器，该命令告诉控制器生成器创建一个包含“index”动作的“Welcome”控制器

bin/rails d controller Welcome //删除控制器
```

创建控制器时生成对应视图，控制器文件和相应路由(路由不存在的情况下)。

? 路由有存储的地方吗，或者可视化的地方？还是说路由就是个虚的..      
* 路由在config下路由表中有相应记录

感觉上是说路由的大单位是控制器，仔细来说就是路由跳转时路径应该为控制器+控制器内的动作。文章后面提到同一个控制器内的重定向，则可省略控制器，直接指定动作即可。

？ 控制器跟视图的关系？      
*  讲道理来说一个action一个视图，不过也可以没有，没有需要特定插件

凭感觉，简单总结一下，url -> 到路由表(config下的路由表)中定向到对应控制器的动作中，视图应该是动作的附属品？相应动作有视图的则显示视图，有没有的这个应该可能或许跟路由本身有一定的关系。

<font color='red'>辅助方法</font> params是代表表单提交的参数（或字段）的对象。让我们看一个示例 URL：http://www.example.com/?username=dhh&email=dhh@email.com。在这个 URL 中，params[:username] 的值是“dhh”，params[:email] 的值是“dhh@email.com”

+ 路由

```
rails routes
```
```
 Prefix       Verb   URI Pattern                 Controller#Action
welcome_index GET    /welcome/index(.:format)     welcome#index
articles      GET    /articles(.:format)          articles#index
              POST   /articles(.:format)          articles#create
```

+ 资源

> 资源是一个术语，表示一系列类似对象的集合，如文章、人或动物。资源中的项目可以被创建、读取、更新和删除，这些操作简称 CRUD（Create, Read, Update, Delete）。

```

Rails.application.routes.draw do
  resources :articles //声明资源
 
  root 'welcome#index'
end
```

```
bin/rails routes //查看应用的所有路由
```

资源的路由，包含所有CRUD动作

```
   articles GET    /articles(.:format)          articles#index
             POST   /articles(.:format)          articles#create
 new_article GET    /articles/new(.:format)      articles#new
edit_article GET    /articles/:id/edit(.:format) articles#edit
     article GET    /articles/:id(.:format)      articles#show
             PATCH  /articles/:id(.:format)      articles#update
             PUT    /articles/:id(.:format)      articles#update
             DELETE /articles/:id(.:format)      articles#destroy
```

前缀的使用呢，比如<font color='red'>辅助方法</font> articles_path会指向articles 前缀相关联的 URI 模式，如在表单中，则会向路由发起POST(articles#create)请求，一般情况下，会向路由发起GET(articles#index)请求.

+ 模型

模型应该是对象与数据库表的连接。

```
bin/rails generate model Article title:string text:text //创建模型

```

模型生成器会生成模型对象，以及数据库相关文件。


--


## 实践之路

### 基础

#### 变量

+ 局部变量：局部变量是在方法中定义的变量。
+ 实例变量：实例变量可以跨任何特定的实例或对象中的方法使用。变量名前置@
+ 类变量： 类变量可以跨不同的对象使用，变量名前置@@
+ 全局变量：跨类使用，变量名前置$


#### 方法
 
 + 定义类方法时，可使用类名.方法名进行定义，或使用self.方法名进行定义。
 + 方法定义在类中，默认标记为public。


### model的使用

+ 首先先说一下迁移，迁移目前感觉上来说是针对数据表的操作，而非对数据的操作，就是建表，修改表，添加删除字段一系列...感觉上来说是这个样子的。然后看看对数据的操作吧。
https://app.yinxiang.com/shard/s66/nl/2147483647/68cfd2c9-b81d-4940-bc29-0c8dd27d726e/
+ 继承自ApplicationRecord的类应该是默认继承了对表的一些操作。例如

	```
	  #增
	   a=AirguruSensorOut.new
	   a.latitude=23
	   a.longitude=123
	 	#或可写成块的形式
	 	a = AirguruSensorOut.new do |u|
	 		u.latitude=23
	 		u.longitude=123
	 	end
	 	a.save # =>会自动转换成 INSERT INTO `airguru_sensor_out` (`longitude`, `latitude`) VALUES (123.0, 23.0)
	 #删
	   a = AirguruSensorOut.find_by(name: 'beijing')
	   a.destroy
	 #改
	  	# 1. 如果之前就有实例，直接赋值后save即可，大概意思就是save的时候，会看看表中有没有重复主键，没有的话就用insert，有的话就用update。然后这个对象一直没变，一直save其实不会转换成任何数据库操作。感觉这一点好神奇.自己应该是有缓存，所以在修改相应属性后，update也只是变量更新，而不是全量更新。
	  	# 2. 还有方式就是使用update方法，即拿到具体实例后，直接update相应属性，一次更新多个属性时，使用update_all,如下
	  	user = User.find_by(name: 'David')
		user.update(name: 'Dave')
		User.update_all "max_login_attempts = 3, must_change_password = 'true'"
	 #查
	   # 返回所有用户组成的集合
		users = User.all
		# 返回第一个用户
		user = User.first
		#或者是find_by,where,find_by返回为满足条件的第一条记录
	```
	
	哇，感觉很方便哦。不用自己写sql语句。

+ 文档上默认使用的是ApplicationRecord，看同事继承的都是ActiveRecord::Base，仔细一点会发现在models文件夹下有application\_record.rb的文件，其实就是ApplicationRecord类，这个类的作用就是实现ActiveRecord::Base，作用是什么呢，好像是为了打猴子补丁的时候方便点，可以直接在application\_record.rb中进行添加。对于使用的话，就使用ApplicationRecord就好了，跟ActiveRecord::Base基本上没差。
+ 特定连接

 ```
 class PhantomStormDb < ApplicationRecord
  		establish_connection $PHANTOM_STORM_CONF
 end
 ```
 
+ 重写表名

	```
	self.table_name = "house_to_pc"
	```
+ model继承属性inheritance

  ```
    class StormAirguruPcToHouse < PhantomStormDb
	  self.table_name = "house_to_pc"
	  self.inheritance_column = :_type_disabled
	end
  ```
  这个属性是什么意思呢，就是model继承的话，会默认添加一个string的type字段，会记录子类的class名。如Firm继承自Company，当你使用 Firm.create(name: "37signals"), 会在companies表中添加一条type = “Firm”的记录. 当你查询Company.where(name: '37signals').first 的时候会返回一个Firm对象
  
+ 验证，在存入数据库之前对数据进行有效验证，类似

 ```
  validates :name, presence: true
  # 这个辅助方法检查指定的属性是否为非空值。它调用 blank? 方法检查值是否为 nil 或空字符串，即空字符串或只包含空白的字符串。
 ```
 
 
+ 爬坑之一，react里form进行post会自动顺溜一条get请求，解决方式为在form里加上属性data-remote="true"

