## Go基本语法和api的记录

### 基本语法

+ 变量声明

  ```
   //短变量声明，用在函数内
   s := ""
   
   //var声明，默认初始化为""
   var s string 
   
   //多个变量 
   var s, sep string
   
   //初始化,初始化使用表达式可省略类型，自动从初始值中获得类型
   var i,j int = 1,2
   var x, y, z = true, false, "hi"
   
   //方法参数的声明，类型在后
   func add(x int, y int){
   }
   
   //方法参数中，当两个或多个连续的是同一类型，除了最后一个类型之外，其他都可以省略
   func add(x, y int){
   }
   
   //方法返回值的声明, 可有类型，可有名字
   func swap(x, y string) (string, string) {
		return y, x
	}
	func split(sum int) (x, y int) {
		x = sum * 4 / 9
		y = sum - x
		//没有参数的return返回各个返回变量的当前值(x,y)
		return 
	}

  ```
  
### 基本类型

+ bool

+ string

+ 整数类型

  int, int8, int16  int32  int64

+ 无符号整数类型
 
  uint uint8 uint16 uint32 uint64 uintptr

+ byte 

  unit8的别名

+ rune

  int32的别名，代表一个Unicode码

+ 浮点型

  float32, float64

+ complex型, （向量类型）

  complex64 complex128
  
int，uint 和 uintptr 类型在32位的系统上一般是32位，而在64位系统上是64位。当你需要使用一个整数类型时，你应该首选 int，仅当有特别的理由才使用定长整数类型或者无符号整数类型。

用于complex计算的包在math/cmplx下。


#### 初始化默认值

+ 数值类型0
+ 布尔类型false
+ 字符串""

直接由值进行初始化时，会自动推导类型，但值为数字常量时，类型可能是int、float64或 complex128，数字常量的经度决定了类型，如

```
 i := 32 //int
 f := 2.123 //float64
 g := 0.863 + 0.5i //complex128
```

#### 转换

Go在不同类型之间需要显示转换。如，

```
 i := 42
 f := float64(i)
```


### 常量

使用const关键字，可以是字符、字符串、布尔或数字类型的值。不能使用 := 语法定义。


### 方法

+ 给结构体定义方法,**方法接收者**出现在func关键字和方法名之间 

```
type Vertex struct {
	X, Y float64
}

func (v *Vertex) Abs() float64 {
	return math.Sqrt(v.X*v.X + v.Y*v.Y)
}

func main() {
	v := &Vertex{3, 4}
	fmt.Println(v.Abs())
}

```

+ 你可以对包中的 任意 类型定义任意方法，而不仅仅是针对结构体，但不能对来自其他包的类型或基础类型定义方法。

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


对于定义方法接收者是否是指针(如上例的f MyFloat 和 v *Vertex)的唯一意义在于，指针的话能修改原始值，不是指针方法中操作的是副本参数，不能修改原始值。



+ 数组

数组（slice）初始化需要指定长度或直接赋值，有长度和容量两个概念，长度为真实数据的长度，容量为大小，初始分配一个容量，当追加元素导致容量不够是，会分配一个更大的数组，返回的slice会指向这个新分配的数组。故对数组进行追加，需要使用再赋值的手段，x = append(x, 1)

