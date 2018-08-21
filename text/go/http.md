## Http



主要看一下官方封装的http的使用。

#### 服务端

golang 的标准库 `net/http` 提供了 http 编程有关的接口，封装了内部TCP连接和报文解析的复杂琐碎的细节，使用者只需要和 `http.request` 和 `http.ResponseWriter` 两个对象交互就行。也就是说，我们只要写一个 handler，handler需要实现ServeHTTP方法，请求会通过参数传递进来，而它要做的就是根据请求的数据做处理，把结果写到 Response 中。完美！

```
package main

import (
	"io"
	"net/http"
)

type helloHandler struct{}

func (h *helloHandler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	w.Write([]byte("Hello, world!"))
}

func main() {
	http.Handle("/", &helloHandler{})
	http.ListenAndServe(":12345", nil)
}
```

http包中接收Handler的方式除了Handle方法实现ServeHTTP的handler实例之外，还可以使用HandlerFunc方法

```
func helloHandler(w http.ResponseWriter, req *http.Request) {
	io.WriteString(w, "hello, world!\n")
}

func main() {
	//HandleFunc参数二要求传入的是方法，参数为(w http.ResponseWriter, req *http.Request)，
	http.HandleFunc("/", helloHandler)
	http.ListenAndServe(":12345", nil)
}
```

其实，HandleFunc只是一个适配器..

```
type HandlerFunc func(ResponseWriter, *Request)

// ServeHTTP calls f(w, r).
func (f HandlerFunc) ServeHTTP(w ResponseWriter, r *Request) {
	f(w, r)
}
```

自动给 `f` 函数添加了 `HandlerFunc` 这个壳，最终调用的还是 `ServerHTTP`。

http包的内部，其实同样是有一个默认的server实例，操作的都是server的方法~

然后，http包还提供了另外一个server，`ServeMux`，`Mux` 是 `multiplexor` 的缩写，就是多路传输的意思（请求传过来，根据某种判断，分流到后端多个不同的地方）。`ServeMux `可以注册多了 URL 和 handler 的对应关系，并自动把请求转发到对应的 handler 进行处理。

```
func helloHandler(w http.ResponseWriter, r *http.Request) {
	io.WriteString(w, "Hello, world!\n")
}

func echoHandler(w http.ResponseWriter, r *http.Request) {
	io.WriteString(w, r.URL.Path)
}

func main() {
	mux := http.NewServeMux()
	mux.HandleFunc("/hello", helloHandler)
	mux.HandleFunc("/", echoHandler)

	http.ListenAndServe(":12345", mux)
}
```

注意到的是http.ListenAndServe的第二个参数类型为Handler啊，故`ServeMux` 也是是 `Handler` 接口的实现，也就是说它实现了 `ServeHTTP` 方法。

关于`ServeMux` ，有几点要说明：

- URL 分为两种，末尾是 `/`：表示一个子树，后面可以跟其他子路径； 末尾不是 `/`，表示一个叶子，固定的路径
- 以`/` 结尾的 URL 可以匹配它的任何子路径，比如 `/images` 会匹配 `/images/cute-cat.jpg`
- 它采用最长匹配原则，如果有多个匹配，一定采用匹配路径最长的那个进行处理
- 如果没有找到任何匹配项，会返回 404 错误
- `ServeMux` 也会识别和处理 `.` 和 `..`，正确转换成对应的 URL 地址

你可能会有疑问？我们之间为什么没有使用 `ServeMux` 就能实现路径功能？那是因为 `net/http` 在后台默认创建使用了 `DefaultServeMux`。



##### 看一下源码解析



##### 首先来看 `http.ListenAndServe()`:

```
func ListenAndServe(addr string, handler Handler) error {
	server := &Server{Addr: addr, Handler: handler}
	return server.ListenAndServe()
}

```

这个函数其实也是一层封装，创建了 `Server` 结构，并调用它的 `ListenAndServe` 方法，那我们就跟进去看看：

```
// A Server defines parameters for running an HTTP server.
// The zero value for Server is a valid configuration.
type Server struct {
	Addr           string        // TCP address to listen on, ":http" if empty
	Handler        Handler       // handler to invoke, http.DefaultServeMux if nil
	......
}

// ListenAndServe listens on the TCP network address srv.Addr and then
// calls Serve to handle requests on incoming connections.  If
// srv.Addr is blank, ":http" is used.
func (srv *Server) ListenAndServe() error {
	addr := srv.Addr
	if addr == "" {
		addr = ":http"
	}
	ln, err := net.Listen("tcp", addr)
	if err != nil {
		return err
	}
	return srv.Serve(tcpKeepAliveListener{ln.(*net.TCPListener)})
}

```

`Server` 保存了运行 HTTP 服务需要的参数，调用 `net.Listen` 监听在对应的 tcp 端口，`tcpKeepAliveListener` 设置了 TCP 的 `KeepAlive` 功能，最后调用 `srv.Serve()`方法开始真正的循环逻辑。我们再跟进去看看 `Serve` 方法：

```
// Serve accepts incoming connections on the Listener l, creating a
// new service goroutine for each.  The service goroutines read requests and
// then call srv.Handler to reply to them.
func (srv *Server) Serve(l net.Listener) error {
	defer l.Close()
	var tempDelay time.Duration // how long to sleep on accept failure
    // 循环逻辑，接受请求并处理
	for {
         // 有新的连接
		rw, e := l.Accept()
		if e != nil {
			if ne, ok := e.(net.Error); ok && ne.Temporary() {
				if tempDelay == 0 {
					tempDelay = 5 * time.Millisecond
				} else {
					tempDelay *= 2
				}
				if max := 1 * time.Second; tempDelay > max {
					tempDelay = max
				}
				srv.logf("http: Accept error: %v; retrying in %v", e, tempDelay)
				time.Sleep(tempDelay)
				continue
			}
			return e
		}
		tempDelay = 0
         // 创建 Conn 连接
		c, err := srv.newConn(rw)
		if err != nil {
			continue
		}
		c.setState(c.rwc, StateNew) // before Serve can return
         // 启动新的 goroutine 进行处理
		go c.serve()
	}
}

```

最上面的注释也说明了这个方法的主要功能：

- 接受 `Listener l` 传递过来的请求
- 为每个请求创建 goroutine 进行后台处理
- goroutine 会读取请求，调用 `srv.Handler`

```
func (c *conn) serve() {
	origConn := c.rwc // copy it before it's set nil on Close or Hijack

  	...

	for {
		w, err := c.readRequest()
		if c.lr.N != c.server.initialLimitedReaderSize() {
			// If we read any bytes off the wire, we're active.
			c.setState(c.rwc, StateActive)
		}

         ...

		// HTTP cannot have multiple simultaneous active requests.[*]
		// Until the server replies to this request, it can't read another,
		// so we might as well run the handler in this goroutine.
		// [*] Not strictly true: HTTP pipelining.  We could let them all process
		// in parallel even if their responses need to be serialized.
		serverHandler{c.server}.ServeHTTP(w, w.req)

		w.finishRequest()
		if w.closeAfterReply {
			if w.requestBodyLimitHit {
				c.closeWriteAndWait()
			}
			break
		}
		c.setState(c.rwc, StateIdle)
	}
}

```

看到上面这段代码 `serverHandler{c.server}.ServeHTTP(w, w.req)`这一句了吗？它会调用最早传递给 `Server` 的 Handler 函数：

```
func (sh serverHandler) ServeHTTP(rw ResponseWriter, req *Request) {
	handler := sh.srv.Handler
	if handler == nil {
		handler = DefaultServeMux
	}
	if req.RequestURI == "*" && req.Method == "OPTIONS" {
		handler = globalOptionsHandler{}
	}
	handler.ServeHTTP(rw, req)
}

```

哇！这里看到 `DefaultServeMux` 了吗？如果没有 handler 为空，就会使用它。`handler.ServeHTTP(rw, req)`，Handler 接口都要实现 `ServeHTTP` 这个方法，因为这里就要被调用啦。

也就是说，无论如何，最终都会用到 `ServeMux`，也就是负责 URL 路由的家伙。前面也已经说过，它的 `ServeHTTP` 方法就是根据请求的路径，把它转交给注册的 handler 进行处理。这次，我们就在源码层面一探究竟。



#####  handler的两个参数，`http.Request` 和 `http.ResponseWriter`。

Request 就是封装好的客户端请求，包括 URL，method，header 等等所有信息，以及一些方便使用的方法

ResponseWriter 是一个接口，定义了三个方法：

- `Header()`：返回一个 Header 对象，可以通过它的 `Set()` 方法设置头部，注意最终返回的头部信息可能和你写进去的不完全相同，因为后续处理还可能修改头部的值（比如设置 `Content-Length`、`Content-type` 等操作）
- `Write()`： 写 response 的主体部分，比如 `html` 或者 `json` 的内容就是放到这里的
- `WriteHeader()`：设置 status code，如果没有调用这个函数，默认设置为 `http.StatusOK`， 就是 `200` 状态码



##### 扩展

虽然 `net/http` 提供的各种功能已经满足基本需求了，但是很多时候还不够方便，比如：

- 不支持 URL 匹配，所有的路径必须完全匹配，不能捕获 URL 中的变量，不够灵活
- 不支持 HTTP 方法匹配
- 不支持扩展和嵌套，URL 处理都在都一个 `ServeMux` 变量中

#### 看一下有哪些第三方库

+ Gorilla Mux

  Gorilla 提供了很多网络有关的组件， Mux 就是其中一个，负责 HTTP 的路由功能。这个组件弥补了上面提到的 `ServeMux` 的一些缺陷，支持的功能有：

  - 更多的匹配类型：HTTP 方法、query 字段、URL host 等
  - 支持正则表达式作为 URL path 的一部分，也支持变量提取功能
  - 支持子路由，也就是路由的嵌套，`SubRouter` 可以实现路由信息的传递
  - 并且和 `ServeMux` 完全兼容

  ```
  r := mux.NewRouter()
  r.HandleFunc("/products/{key}", ProductHandler)
  r.HandleFunc("/articles/{category}/", ArticlesCategoryHandler)
  r.HandleFunc("/articles/{category}/{id:[0-9]+}", ArticleHandler)

  ```

+ httprouter

  httprouter 和 `mux` 一样，也是扩展了自带 `ServeMux` 功能的路由库。它的主要特点是速度快、内存使用少、可扩展性高（使用 radix tree 数据结构进行路由匹配，路由项很多的时候速度也很快）。

  ```
  package main

  import (
      "fmt"
      "github.com/julienschmidt/httprouter"
      "net/http"
      "log"
  )

  func Index(w http.ResponseWriter, r *http.Request, _ httprouter.Params) {
      fmt.Fprint(w, "Welcome!\n")
  }

  func Hello(w http.ResponseWriter, r *http.Request, ps httprouter.Params) {
      fmt.Fprintf(w, "hello, %s!\n", ps.ByName("name"))
  }

  func main() {
      router := httprouter.New()
      router.GET("/", Index)
      router.GET("/hello/:name", Hello)

      log.Fatal(http.ListenAndServe(":8080", router))
  }
  ```

+ /gin-gonic/gin

  Web-api框架支持中间件、路由等，使用起来算是很方便的了。性能上的话，文档上直接跟httprouter进行了对比，很优秀的。

+ beego

  说到web框架，就不得不提beego.. mvc结构，很强。



#### 客户端

```
func httpGet() {
    resp, err := http.Get("http://www.01happy.com/demo/accept.php?id=1")
    if err != nil {
        // handle error
    }
 
    defer resp.Body.Close()
    body, err := ioutil.ReadAll(resp.Body)
    if err != nil {
        // handle error
    }
 
    fmt.Println(string(body))
}

func httpPost() {
    resp, err := http.Post("http://www.01happy.com/demo/accept.php",
        "application/x-www-form-urlencoded",
        strings.NewReader("name=cjb"))
    if err != nil {
        fmt.Println(err)
    }
 
    defer resp.Body.Close()
    body, err := ioutil.ReadAll(resp.Body)
    if err != nil {
        // handle error
    }
 
    fmt.Println(string(body))
}
//有时需要在请求的时候设置头参数、cookie之类的数据，就可以使用http.Do方法。
func httpDo() {
    client := &http.Client{}
 
    req, err := http.NewRequest("POST", "http://www.01happy.com/demo/accept.php", strings.NewReader("name=cjb"))
    if err != nil {
        // handle error
    }
 
    req.Header.Set("Content-Type", "application/x-www-form-urlencoded")
    req.Header.Set("Cookie", "name=anny")
 
    resp, err := client.Do(req)
 
    defer resp.Body.Close()
 
    body, err := ioutil.ReadAll(resp.Body)
    if err != nil {
        // handle error
    }
 
    fmt.Println(string(body))
    
  }
   
  
  
  func httpDetail() {
    var netTransport = &http.Transport{
        Dial: (&net.Dialer{
            Timeout:   10 * time.Second,
            KeepAlive: 30 * time.Second,
        }).Dial,
        TLSHandshakeTimeout:   5 * time.Second,
        ResponseHeaderTimeout: 10 * time.Second,
        ExpectContinueTimeout: 1 * time.Second,
    }
    var netClient = &http.Client{
        Timeout:   time.Second * 30,
        Transport: netTransport,
    }

    // get
    response, _ := netClient.Get("http://www.golangnote.com/")
    defer response.Body.Close()

    if response.StatusCode == 200 {
        body, _ := ioutil.ReadAll(response.Body)
        fmt.Println(string(body))
    }
  }
    
```

