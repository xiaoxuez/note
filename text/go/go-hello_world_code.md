## 代码部分

[指南](https://tour.go-zh.org/basics/)代码，方便记忆。

#### 函数方法

```
package main

import "fmt"

func add(x int, y int) int {
	return x + y
}

func add1(z string, x, y int) int {
	return x + y
}

//返回值
func swap(x, y string) (string, string) {
	return y, x
}

//命名返回值
func split(sum int) (x, y int) {
	x = sum * 4 / 9
	y = sum - x
	return
}

func main() {
	fmt.Println(add(42, 13))
}

```

#### 变量类型

+ 基本类型

```
package main

import (
	"fmt"
	"math/cmplx"
)

var (
	ToBe   bool       = false
	MaxInt uint64     = 1<<64 - 1
	z      complex128 = cmplx.Sqrt(-5 + 12i)
)

func main() {
	const f = "%T(%v)\n"
	fmt.Printf(f, ToBe, ToBe)
	fmt.Printf(f, MaxInt, MaxInt)
	fmt.Printf(f, z, z)
	
	//类型转换
	var x, y int = 3, 4
	var f float64 = math.Sqrt(float64(x*x + y*y))
	var z uint = uint(f)
	
	//常量
	const World = "世界"
}

```

+ 指针

```
//类型 *T 是指向类型 T 的值的指针。其零值是 nil 。
//& 符号会生成一个指向其作用对象的指针。
//* 符号表示指针指向的底层的值。

   i, j := 42, 2701

	p := &i         // point to i
	fmt.Println(p)
	fmt.Println(*p) // read i through the pointer
	*p = 21         // set i through the pointer
	fmt.Println(i)  // see the new value of i

	p = &j         // point to j
	*p = *p / 37   // divide j through the pointer
	fmt.Println(j) // see the new value of j

```

+ 结构体

```
type Vertex struct {
	X int
	Y int
}

func main() {
	fmt.Println(Vertex{1, 2})
	
	//变量声明
	var (
	v1 = Vertex{1, 2}  // 类型为 Vertex
	v2 = Vertex{X: 1}  // Y:0 被省略
	v3 = Vertex{}      // X:0 和 Y:0
	p  = &Vertex{1, 2} // 类型为 *Vertex
	)
}

```

+ 数组

```
func main() {
	var a [2]string
	a[0] = "Hello"
	a[1] = "World"
	fmt.Println(a[0], a[1])
	fmt.Println(a)
}

```

+ slice

```
//[]T 是一个元素类型为 T 的 slice。

	s := []int{2, 3, 5, 7, 11, 13}
	fmt.Println("s ==", s)
	
	//构造slice,
	a := make([]int, 5) //len(a)=5
	b := make([]int, 0, 5) //len(b)=0, cap(b)=5
	
	//slice 的零值是 nil
	var z []int
	fmt.Println(z, len(z), cap(z)) //nil,0,0
	
	//二维示例
	import (
		"fmt"
		"strings"
	)
	
	func main() {
		// Create a tic-tac-toe board.
		game := [][]string{
			[]string{"_", "_", "_"},
			[]string{"_", "_", "_"},
			[]string{"_", "_", "_"},
		}
	
		// The players take turns.
		game[0][0] = "X"
		game[2][2] = "O"
		game[2][0] = "X"
		game[1][0] = "O"
		game[0][2] = "X"
	
		printBoard(game)
	}
	
	func printBoard(s [][]string) {
		for i := 0; i < len(s); i++ {
		   //strings.Join api
			fmt.Printf("%s\n", strings.Join(s[i], " "))
		}
	}
	
	//切片
	s := []int{2, 3, 5, 7, 11, 13}
	fmt.Println("s[1:4] ==", s[1:4])  start <= index < end,即1到3元素
	// 省略下标代表从 0 开始
	fmt.Println("s[:3] ==", s[:3])
	// 省略上标代表到 len(s) 结束
	fmt.Println("s[4:] ==", s[4:])
	
	//添加元素
	s = append(s, 0)
	
	//遍历
	//for 循环的 range 格式可以对 slice 或者 map 进行迭代循环。
	for i, v := range pow {
		fmt.Printf("2**%d = %d\n", i, v)
	}
	
```

+ map

```
package main

import "fmt"

type Vertex struct {
	Lat, Long float64
}

//初始化为nil,需要make进行创建
var m map[string]Vertex

//静态赋值
var m1 = map[string]Vertex{
	"Bell Labs": Vertex{ //Vertex可省略
		40.68433, -74.39967,
	},
	"Google": Vertex{
		37.42202, -122.08408,
	},
}

func main() {
	m = make(map[string]Vertex)
	m["Bell Labs"] = Vertex{
		40.68433, -74.39967,
	}
	fmt.Println(m["Bell Labs"])
}

```

+ 函数

```
//函数也是值。他们可以像其他值一样传递，比如，函数值可以作为函数的参数或者返回值。
import (
	"fmt"
	"math"
)

func compute(fn func(float64, float64) float64) float64 {
	return fn(3, 4)
}

func main() {
	hypot := func(x, y float64) float64 {
		return math.Sqrt(x*x + y*y)
	}
	fmt.Println(hypot(5, 12))

	fmt.Println(compute(hypot))
	fmt.Println(compute(math.Pow))
}

//闭包
package main

import "fmt"

func adder() func(int) int {
	sum := 0
	return func(x int) int {
		sum += x
		return sum
	}
}

func main() {
	pos, neg := adder(), adder()
	for i := 0; i < 10; i++ {
		fmt.Println(
			pos(i),
			neg(-2*i),
		)
	}
}
//输出为，pos依次为，1，3，6，10.. neg依次为-2，-6，-12，-20

```

#### 结构

+ for循环

```

	sum := 0
	for i := 0; i < 10; i++ {
		sum += i
	}
	
	//忽略某些项
	sum := 1
	for ; sum < 1000; {
		sum += sum
	}
	
	//while循环
	for sum < 1000 {
		sum += sum
	}
	
	//死循环
	for {
	}
}
	
```

+ if

```
	if x < 0 {
		
	}
	
	//if可以在条件之前执行一个简单语句，变量定义的话作用域在if范围内包括else
	if v := math.Pow(x, n); v < lim {
		return v
	} else {
	   return v - 1
	}

```

+ switch

```
   //除非以 fallthrough 语句结束，否则分支会自动终止
	switch os := runtime.GOOS; os {
	case "darwin":
		fmt.Println("OS X.")
	case "linux":
		fmt.Println("Linux.")
	default:
		// freebsd, openbsd,
		// plan9, windows...
		fmt.Printf("%s.", os)
	}
	
	//没有条件的 switch 同 switch true 一样，可代替长if-then-else 链
	switch {
	case t.Hour() < 12:
		fmt.Println("Good morning!")
	case t.Hour() < 17:
		fmt.Println("Good afternoon.")
	default:
		fmt.Println("Good evening.")
	}

```

+ defer

```
//defer 语句会延迟函数的执行直到上层函数返回
// 输出为hello world
func main() {
	defer fmt.Println("world")

	fmt.Println("hello")
}

//延迟的函数调用被压入一个栈中。当函数返回时， 会按照后进先出的顺序调用被延迟的函数调用
//输出9 8 7 ...
func main() {
	fmt.Println("counting")

	for i := 0; i < 10; i++ {
		defer fmt.Println(i)
	}

	fmt.Println("done")
}

```


#### 方法

+ 在结构体上定义方法

```
package main

import (
	"fmt"
	"math"
)

type Vertex struct {
	X, Y float64
}

func (v *Vertex) Abs() float64 { //方法接收者
	return math.Sqrt(v.X*v.X + v.Y*v.Y)
}

func main() {
	v := &Vertex{3, 4}
	fmt.Println(v.Abs())
}
```

+ 在包中的任意类型定义方法

```
package main

import (
	"fmt"
	"math"
)

type MyFloat float64

func (f MyFloat) Abs() float64 {
	if f < 0 {
		return float64(-f)
	}
	return float64(f)
}


func main() {
	f := MyFloat(-math.Sqrt2)
	fmt.Println(f.Abs())
}

```

+ 接口实现

```

type Abser interface {
	Abs() float64
}

func main() {
	var a Abser
	f := MyFloat(-math.Sqrt2)
	v := Vertex{3, 4}

	a = f  // a MyFloat 实现了 Abser
	a = &v // a *Vertex 实现了 Abser

	// 下面一行，v 是一个 Vertex（而不是 *Vertex）
	// 所以没有实现 Abser。
	//a = v

	fmt.Println(a.Abs())
}

type MyFloat float64

func (f MyFloat) Abs() float64 {
	if f < 0 {
		return float64(-f)
	}
	return float64(f)
}


//实现 fmt 包中Stringer接口中的String方法，`fmt`包使用这个来进行输出
package main

import "fmt"

type Person struct {
	Name string
	Age  int
}

func (p Person) String() string {
	return fmt.Sprintf("%v (%v years)", p.Name, p.Age)
}

func main() {
	a := Person{"Arthur Dent", 42}
	z := Person{"Zaphod Beeblebrox", 9001}
	fmt.Println(a, z)
}

```

#### IO

+ Readers

```
package main

import (
	"fmt"
	"io"
	"strings"
)

func main() {
	r := strings.NewReader("Hello, Reader!")

	b := make([]byte, 8)
	for {
		n, err := r.Read(b)
		fmt.Printf("n = %v err = %v b = %v\n", n, err, b)
		fmt.Printf("b[:n] = %q\n", b[:n])
		if err == io.EOF {
			break
		}
	}
}
```

+ Http

```
package main

import (
	"fmt"
	"log"
	"net/http"
)

type Hello struct{}

func (h Hello) ServeHTTP(
	w http.ResponseWriter,
	r *http.Request) {
	fmt.Fprint(w, "Hello!")
}

func main() {
	var h Hello
	err := http.ListenAndServe("localhost:4000", h)
	if err != nil {
		log.Fatal(err)
	}
}

```

#### 其他

+ Image

```
package main

import (
	"fmt"
	"image"
)

func main() {
	m := image.NewRGBA(image.Rect(0, 0, 100, 100))
	fmt.Println(m.Bounds())
	fmt.Println(m.At(0, 0).RGBA())
}
```

+ goroutine

```
package main

import (
	"fmt"
	"time"
)

func say(s string) {
	for i := 0; i < 5; i++ {
		time.Sleep(100 * time.Millisecond)
		fmt.Println(s)
	}
}

func main() {
	go say("world")
	say("hello")
}
```

+ channel

```
package main

import "fmt"

func sum(a []int, c chan int) {
	sum := 0
	for _, v := range a {
		sum += v
	}
	c <- sum // 将和送入 c
}

func main() {
	a := []int{7, 2, 8, -9, 4, 0}

	c := make(chan int)
	go sum(a[:len(a)/2], c)
	go sum(a[len(a)/2:], c)
	x, y := <-c, <-c // 从 c 中获取

	fmt.Println(x, y, x+y)
	
	ch := make(chan int, 2) //设置缓冲区
}


//range遍历，不断接受，close关闭
func fibonacci(n int, c chan int) {
	x, y := 0, 1
	for i := 0; i < n; i++ {
		c <- x
		x, y = y, x+y
	}
	close(c)
}

func main() {
	c := make(chan int, 10)
	go fibonacci(cap(c), c)
	for i := range c {
		fmt.Println(i)
	}
}

//select等待，select 会阻塞，直到条件分支中的某个可以继续执行，这时就会执行那个条件分支。当多个都准备好的时候，会随机选择一个
func fibonacci(c, quit chan int) {
	x, y := 0, 1
	for {
		select {
		case c <- x:
			x, y = y, x+y
		case <-quit:
			fmt.Println("quit")
			return
		}
	}
}

func main() {
	c := make(chan int)
	quit := make(chan int)
	go func() {
		for i := 0; i < 10; i++ {
			fmt.Println(<-c)
		}
		quit <- 0
	}()
	fibonacci(c, quit)
}

//为了非阻塞的发送或者接收，可使用 default 分支：
	for {
		select {
		case <-tick:
			fmt.Println("tick.")
		case <-boom:
			fmt.Println("BOOM!")
			return
		default:
			fmt.Println("    .")
			time.Sleep(50 * time.Millisecond)
		}
	}
```
+ 互斥，互斥锁

```
package main

import (
	"fmt"
	"sync"
	"time"
)

// SafeCounter 的并发使用是安全的。
type SafeCounter struct {
	v   map[string]int
	mux sync.Mutex
}

// Inc 增加给定 key 的计数器的值。
func (c *SafeCounter) Inc(key string) {
	c.mux.Lock()
	// Lock 之后同一时刻只有一个 goroutine 能访问 c.v
	c.v[key]++
	c.mux.Unlock()
}

// Value 返回给定 key 的计数器的当前值。
func (c *SafeCounter) Value(key string) int {
	c.mux.Lock()
	// Lock 之后同一时刻只有一个 goroutine 能访问 c.v
	defer c.mux.Unlock()
	return c.v[key]
}

func main() {
	c := SafeCounter{v: make(map[string]int)}
	for i := 0; i < 1000; i++ {
		go c.Inc("somekey")
	}

	time.Sleep(time.Second)
	fmt.Println(c.Value("somekey"))
}
```





