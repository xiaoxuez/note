## rpc

rpc， 远程过程调用协议。



#### rpc实现方式

***rpc的实现方式有很多种，常见的有http、tcp，除此之外，websocket、ipc(进程间通信协议)，inproc(进程通信协议)也可称之为rpc的实现***。ipc和inproc的区别是ipc在同一个物理机器上，可以实现进程通信，inproc是在内存中。上述协议都只是实现rpc选择的不同传输协议。

0基础的网络知识让我真是…   简单提一嘴http、tcp、websocket，增强记忆。

首先是tcp协议，通讯协议(传输层)。tcp协议的运行可分为三个阶段，连接创建、数据传送、连接终止。具体实现如三次握手、四次挥手等。常用的通讯协议中，除了tcp协议之外，还有udp协议。二者对比差距具体再记一篇别的笔记说明吧。

http协议，属于建立在通讯协议之上的传输协议(应用层)，http协议的通讯协议可选择tcp或udp等，一般普遍都是选择tcp作为通讯协议。那么，http所实现的传输协议的具体包括什么呢？emm, 主要是一些规范吧，例如包括请求方法包括GET、POST、PUT等8中方式(http/1.1协议)，请求包含三个部分，状态行、请求头、消息主体。

websocket协议，基于tcp的传输协议。咦，那不是和http属于同级，是的。http是只能有客户端向服务端发起请求，做不到服务端主动向客户端推送消息。所以，websocket就产生了，websocket为在单个tcp连接上的全双工通讯协议(全双工的意思是双方可以同时通讯)。同时，websocket的握手阶段也是采用http协议，与http有良好的兼容性。

再提一嘴，json、xml、protobuf等是序列化方式。



言归正传。

#### Golang中的rpc使用

哟西，一起出发吧。

官方封装的rpc包中，包含了基本的使用。

先看一下基本示例吧

```
//方法接收者，int只是一个辅助作用
type Cal int

//api方法要求，方法接收者和参数都需要exported, 参数为两个，第一个为调用参数，第二个为返回参数，如果error不为nil,则返回error
func (c *Cal) Add(args *Args, result *int) error {
	*result = args.A + args.B
	return nil
}

//最简单的方式，首先是Register，然后在conn或io的位置进行绑定调用即可，rpc.HandleHTTP是调用http包中的方法，设置为http的handle方法。
func MakeHttp() {
	rpc.Register(new(Cal))
	rpc.HandleHTTP()

	l, e := net.Listen("tcp", ":1234")
	if e != nil {
		log.Fatal("listen error:", e)
	}
	go http.Serve(l, nil)
}
```

rpc包中有一个server结构体，主要使用都是通过server进行，直接调用rpc包的方法的话，都是使用的默认的server。所以基本套路就是，server的register进行注册，然后server再跟conn/io连接应用即可。

如果不使用默认的server，也不使用HandleHTTP，进行内部设置的话，见代码

```
func MakeServerHttp() {
	handler := rpc.NewServer()
	handler.Register(new(Cal))
	l, e := net.Listen("tcp", ":1234")
	if e != nil {
		log.Fatal("listen error:", e)
	}
	go http.Serve(l, http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		handler.ServeHTTP(w, r)
	}))
}
```

http.Serve的方法参数第二个为handlerFunc，与设置http的handle异曲同工。

除了选取http作为rpc的载体，再看一个选取tcp作为rpc载体的例子。

```
func MakeTcp() {
	rpc.Register(new(Cal))
	tcpAddr, err := net.ResolveTCPAddr("tcp", ":1234")
	if err != nil {
		log.Fatal("listen error:", err)
	}
	l, e := net.ListenTCP("tcp", tcpAddr)
	if e != nil {
		log.Fatal("listen error:", e)
	}
	go func(l *net.TCPListener) {
		for {
			conn, err := l.Accept()
			if err != nil {
				continue
			}
			rpc.ServeConn(conn)
		}
	}(l)
}
```

关于客户端的调用代码，如。

```
	client, err := rpc.DialHTTP("tcp", ":1234")
	if err != nil {
		t.Fatal(err.Error())
	}
	args := &Args{1, 2}
	var result int
	err := client.Call("Cal.Add", args, &result)
```



#### 插话

http作为rpc的载体的实现。

首先，服务端主要是通过设置http.Handle方法实现，从http出发，http.Handle是设置路由，所以，从这一角度出发，rpc是设置的一个http路由。在rpc.HandleHTTP的源码中可以看到

```
// HandleHTTP registers an HTTP handler for RPC messages on rpcPath,
// and a debugging handler on debugPath.
// It is still necessary to invoke http.Serve(), typically in a go statement.
func (server *Server) HandleHTTP(rpcPath, debugPath string) {
	http.Handle(rpcPath, server)
	http.Handle(debugPath, debugHTTP{server})
}
```

HandleHTTP注册http handler，默认的rpcPath为"/goRPC"

然后客户端的实现，rpc.DialHTTP的代码为

```
// DialHTTPPath connects to an HTTP RPC server
// at the specified network address and path.
func DialHTTPPath(network, address, path string) (*Client, error) {
	var err error
	conn, err := net.Dial(network, address)
	if err != nil {
		return nil, err
	}
	//CONNECT是http的方法，用于请求服务器，path为/goRPC
	io.WriteString(conn, "CONNECT "+path+" HTTP/1.0\n\n")

	// Require successful HTTP response
	// before switching to RPC protocol.
	resp, err := http.ReadResponse(bufio.NewReader(conn), &http.Request{Method: "CONNECT"})
	if err == nil && resp.Status == connected {
		return NewClient(conn), nil
	}
	if err == nil {
		err = errors.New("unexpected HTTP response: " + resp.Status)
	}
	conn.Close()
	return nil, &net.OpError{
		Op:   "dial-http",
		Net:  network + " " + address,
		Addr: nil,
		Err:  err,
	}
}
```





##### http connect方法

Connect连接处理为建立一个目标服务器的连接，一旦连接建立完成，后续请求会直接转发。

Connect方法常用于代理时创建通道，也用于https是创建可信通道~



