## Go context

在 Go http包的Server中，每一个请求在都有一个对应的 goroutine 去处理。 请求处理函数通常会启动额外的 goroutine 用来访问后端服务，比如数据库和RPC服务。 用来处理一个请求的 goroutine 通常需要访问一些与请求特定的数据，比如终端用户的身份认证信息、 验证相关的token、请求的截止时间。 当一个请求被取消或超时时，所有用来处理该请求的 goroutine 都应该迅速退出，然后系统才能释放这些 goroutine 占用的资源。


### 看使用吧

context包提供了一些函数，协助从现有的Context对象创建新的Context对象，这些 Context 对象形成一棵树：当一个 Context 对象被取消时，继承自它的所有 Context 都会被取消。

在提供的获取现有的Context接口中，一个是Background()，一个是TODO()

```

/*
    TODO返回一个非空，空的上下文
    在目前还不清楚要使用的上下文或尚不可用时
*/
context.TODO()
/*
    Background返回一个非空，空的上下文。
    这是没有取消，没有值，并且没有期限。
    它通常用于由主功能，初始化和测试，并作为输入的顶层上下文
*/
context.Background()

```

利用现有context创建新的对象，使用的方法为WithCancel、WithTimeout，一个是退出的方法是手动cancel,一个是根据时间超时来作为退出的标准。

另外context包还具有WithDeadline和WithValue方法，WithDeadline跟WithTimeout类似，是根据时间来作为退出标准，WithDeadline设置的时间是确切的时间，WithTimeout是相对值。WithValue是在 Context 中设置一个 map，拿到这个 Context 以及它的后代的 goroutine 都可以拿到 map 里的值，可作为传值使用。context包里的方法是线程安全的。

看一个示例

```
// 在 handle 环境中使用 
func handleSearch(w http.ResponseWriter, req *http.Request) {
    // ctx is the Context for this handler. Calling cancel closes the
    // ctx.Done channel, which is the cancellation signal for requests
    // started by this handler.
    var (
        ctx    context.Context
        cancel context.CancelFunc
    )
    // 获取参数 ...
    timeout, err := time.ParseDuration(req.FormValue("timeout"))
    if err == nil {
        // The request has a timeout, so create a context that is
        // canceled automatically when the timeout expires.
        // 获取成功, 则按照参数设置超时时间
        ctx, cancel = context.WithTimeout(context.Background(), timeout)
    } else {
        // 获取失败, 则在该函数结束时结束 ...
        ctx, cancel = context.WithCancel(context.Background())
    }
    // ----------------
    // 这样随着cancel的执行,所有的线程都随之结束了 ...
    go A(ctx) +1
    go B(ctx) +2
    go C(ctx) +3
    // ----------------
    defer cancel() // Cancel ctx as soon as handleSearch returns.
}

// 监听 ctx.Done() 结束 ...
func A(ctx context.Context) int {
    // ... TODO
    select {
    case <-ctx.Done():
            return -1
    default:
        // 没有结束 ... 执行 ...
    }
}
```



--

顺便粘一下另外的结束标志

	// trap SIGINT to trigger a shutdown.
	signals := make(chan os.Signal, 1)
	signal.Notify(signals, os.Interrupt)
	
	case <-signals:
			return