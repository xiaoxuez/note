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
```

创建控制器时生成对应视图，控制器文件和相应路由(路由不存在的情况下)。

? 路由有存储的地方吗，或者可视化的地方？还是说路由就是个虚的..

感觉上是说路由的大单位是控制器，仔细来说就是路由跳转时路径应该为控制器+控制器内的动作。文章后面提到同一个控制器内的重定向，则可省略控制器，直接指定动作即可。

？ 控制器跟视图的关系？

凭感觉，简单总结一下，url -> 到路由表(config下的路由表)中定向到对应控制器的动作中，视图应该是动作的附属品？相应动作有视图的则显示视图，有没有的这个应该可能或许跟路由本身有一定的关系。

<font color='red'>辅助方法</font> params是代表表单提交的参数（或字段）的对象。让我们看一个示例 URL：http://www.example.com/?username=dhh&email=dhh@email.com。在这个 URL 中，params[:username] 的值是“dhh”，params[:email] 的值是“dhh@email.com”

+ 路由

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


StormAirguruSensorHistory.where(type: 'executor').where(house_id: 11419).where("log like '%specificType%'").where("log like '%FRESH_AIR_VOLUME_MORE
%' or log like '%FRESH_AIR_VOLUME_LESS
%' or log like '%FRESH_AIR_MODE_TO_INDOOR
%' or log like '%FRESH_AIR_MODE_TO_OUTDOOR%'")