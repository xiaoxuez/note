## Hello World

### 关于运行

基于刚入手的经验，运行总结两点。

+ 命令行运行scala,进入scala环境，需要下载scala resouce,如scala-2.12.3.tgz,配环境变量指向目录bin/scala,之后即可使用scala快捷方式，直接运行scala可进入命令行，运行脚本命令为

	```
	scala helloworld.scala
	```

+ IDEA中，目前看起来是只有object类型才能成为入口，入口依旧是main。另外运行scala console可在当前工作路径下进入命令行，在console中输入相应代码即可工作，确认键是**花号+Enter**

### Programming

定义变量，val,var。val为常量，不可再赋值，var为变量。

```
val msg = "Hello, world!"

val msg2: java.lang.String = "Hello again, world!"
```


+ 通过 Scala 的名为 args 的数组可以获得传递给 Scala 脚本的命令行参数

```
// 向第一个参数打招呼println("Hello, " + args(0) + "!")

$ scala helloarg.scala planet
```

+ while if的使用

```
// 打印出参数列表，以空格分隔参数
var i = 0;while (i < args.length) {  if (i != 0) {    print(" ");  }  print(args(i));  i += 1;} 
println();
```

+ 用 foreach 和 for 枚举

```
args.foreach(arg => println(arg))

args.foreach((arg: String) => println(arg))

args.foreach(println)


for (arg <- args)  println(arg)
  //arg的类型是val而不是var
```



+ 数组

```
//初始化
val greetStrings = new Array[String](3)
val numNames = Array("x", "y", "z")

//具体访问
greetStrings(0) = "hello";
greetStrings(1) = " to ";
greetStrings(2) = " Jim! ";

//遍历
for(i <- 0 to 2) 
	print(greetStrings(i))
```
**0 to 2**:  如果方法仅带一个参数，你可以不带点或括号的调用它，0 to 2其实是 (0).to(2), to是带一个Int参数的方法，但这个方法仅在显示指定方法调用的接受者才起作用，如上例接受者是i。作为扩展，**1 + 2**，其实也是调用Int对象中+方法，其实是(1).+(2)，从技术上讲，Scala没有操作符重载，因为它根本没有传统意义上的操作符，取而代之的是，诸如+，-，*，/这样的字符可以做方法名。

**greetStrings(0)**:数组也是类的实现，<font color="red">当你在一个或多个值或变量外使用括号时，Scala会把它转换成对名为apply的方法调用，这个原则不仅仅局限于数组</font>。所以greetStrings(0)会转换成greetStrings.apply(i)。构造初始化数组时，Array("x", "y", "z")，也是转化为Array.apply("x", "y", "z"),apply方法带可变数量个参数被定义在Array的伴生对象（companion object）上，即单例方法。<font color="red">当对带有括号并包括一到若干参数的变量赋值时，编译器将把它转化为对带有括号里参数和等号右边的对象的update方法的调用</font>，如，greetStrings(0) = "hello"将转化为greetStrings.update(0,"hello")

+ List

```
val oneTwo = List(1, 2)
val threeFour = List(3, 4)
val oneTwoThreeFour = oneTwo ::: threeFour

val twoThree = List(2, 3)
val oneTwoThree = 1 :: twoThree

val oneTwoThree = 1 :: 2 :: 3 :: Nil
```

Array是可变的对象，因为其元素值是可变的，但List是不可变对象序列。所以List中的元素是不可变的。    

**:::** : List的方法，实现叠加功能，类似 + 。为加两个List,返回新List
**::** : List的方法，实现叠加功能，为将新元素组合到已有List的最前端。示例中1 :: twoThree讲道理应该是twoThree :: 1，但<font color="red">如果一个方法被用作操作符标注，如a * b,那么方法被左操作数调用，为a.*(b), 除非方法名以冒号结尾，这种情况下，方法被右操作数调用，1 :: twoThree，为twoThree.::(1)如</font>。1 :: 2 :: 3 :: Nil的示例中，如果写成1 :: 2 :: 3会编译出错，因为3为Int类型，没有::方法，Nil可为是定义一个List的空类。

+ Tuple

```
val pair = (99, "Luftballons")println(pair._1)println(pair._2)
```

元组与List一样，也是不可变序列，不同点在于Tuple可以包含不同类型的元素，而列表应该是List[Int]或List[String]的样子，元组可以同时拥有Int和String，在方法里返回多个对象的时候，可以简单返回一个元组而不用包装一个JavaBean。对元组的访问采用._N,为何不能想List那样访问呢，是因为apply方法始终返回同样的类型，元组里的类型或许是不同的。另外**_N数字是基于1而不是基于0的**

+ Set和Map

```
var jetSet = Set("Boeing", "Airbus")jetSet += "Lear"println(jetSet.contains("Cessna"))

import scala.collection.mutable.Setval movieSet = Set("Hitch", "Poltergeist")movieSet += "Shrek"println(movieSet)

import scala.collection.immutable.HashSetval hashSet = HashSet("Tomatoes", "Chilies")println(hashSet + "Coriander")
```


```
import scala.collection.mutable.Mapval treasureMap = Map[Int, String]()treasureMap += (1 -> "Go to island.")treasureMap += (2 -> "Find big X on ground.")treasureMap += (3 -> "Dig.")
println(treasureMap(2))

val romanNumeral = Map(  1 -> "I", 2 -> "II", 3 -> "III", 4 -> "IV", 5 -> "V")println(romanNumeral(4))
```

数组是可变的，列表是不可变的。Set,Map既定义了可变的，也定义了不可变的，默认使用的是不可变的，如需使用可变的，需要显示引入，如import... Set类继承关系为Set下分immutable Set和mutabale Set，二者下方对应实现HashSet。Map的结构一样。❓疑问的地方在于，Set、Map的类型都是trait(可理解为接口),但示例中很明显构建了Set,Map实例，在Java中接口不能实例化不一样?我还没解惑❓    
⭐️首先解释下可变不可变对应Set,Map变量的定义，发现不可变定义用的var,而可变用的val,这里的mutable/imutable指的是具体对象的属性值的可变不可变，var,val是对象本身可变不可变，如val修饰的变量就不能再赋值，不能修改对象本身，但可以修改对象的属性值，故val修饰mutable变量，var修饰immutable变量，相对合理。


immutable jetSet += "Lear",行为为jetSet = jetSet + "Lear"，不能改变自身的值，只能先加再重新给变量赋新值。    
mutable movieSet += "Shrek", 行为为movieSet.+=("Shrek")，直接改变对象类中的值。

1 -> "Go to island."实际上为Int的 -> 方法，所以转换为(1).->("Go to island.")，<font color="red">这个->方法可以调用Scala程序里的任何对象，并返回一个包 键和值的二元元组</font>。

+ 插入一段广告

> Scala程序员的平衡感：
> 	崇尚val，不可变对象和没有副作用的方法。首先想到它们。只有在特定需要和判断之后才选择var,可变对象和有副作用的方法。


+ 一个打印脚本

```
import scala.io.Source
def widthOfLength(s: String) = s.length.toString.length
if (args.length > 0) {
  val lines = Source.fromFile(args(0)).getLines.toList
  val longestLine = lines.reduceLeft((a, b) => if (a.length > b.length) a else b)
  val maxWidth = widthOfLength(longestLine)
  for (line <- lines) {
    val numSpaces = maxWidth - widthOfLength(line)
    val padding = " " * numSpaces
    println(padding + line.length + " | " + line)
  }
}
else
  Console.err.println("Please enter filename")
```

```
//命令行运行
scala printCode.scala printCode.scala
```

行数数字右边对齐，所以需要计算行数几位数的最大值，在小的前面加上空格。Source.fromFile(args(0)).getLines返回的是Iterator[String]，枚举器一旦使用它完成遍历，枚举器就失效了，故需要调用tolist转换为List，就可以枚举任意次数，代价就是把文件中的所有行一次性贮存在内存里。计算最大宽度，为了避免使用var，所以使用reduceLeft传回最后一次的结果。


### 类和对象

+ 方法定义    

<font color="red">Java 里 每一个返回 void 的方法都被映射为 Scala 里返回 Unit 的方法    
public 是 Scala 的缺省访问级别。对应的，default是java的缺省访问级别</font>。    
定义返回类型为Unit的方法是显示定义返回类型，如f方法，或带大括号但不带等号的，如g方法。有返回值类型的方法，默认返回方法中最后一个计算得到的值

```
def f(): Unit = "this String gets lost"
def g() { "this String gets lost too" } //带有大括号但没有等号的，在 质上当作是显式定义 结果类型为 Unit 的方法
def h() = { "this String gets returned!" }

f: ()Unit
g: ()Unit
h: ()java.lang.String

//后来发现一点，方法参数也是有类型的，如果不写()，类型就是Unit,在方法调用的时候就不能带括号，如
def abs: Int = math.abs(self)

abs
//调用abs()会报错error: Unit does not take parameters  
//上面f的调用既可以使用f也可以使用f()

```

+ singleton object 单例对象

<font color="red">Scala里没有静态成员，替代品是单例对象，也叫类的伴生对象，定义单例对象的关键字的object,你必须在同一个源文件里定义类和它的伴生对象,类称之为是伴生对象的伴生类，单例对象会在第一次被访问的时候初始化。伴生对象的类型是由伴生类定义的，单例对象扩展了超类并可以混入特质，后面在继承的地方会提到</font>

```
import scala.collection.mutable.Map 
object ChecksumAccumulator {  private val cache = Map[String, Int]()  def calculate(s: String): Int =    if (cache.contains(s))      cache(s)    else {      val acc = new ChecksumAccumulator      for (c <- s)        acc.add(c.toByte)      val cs = acc.checksum()      cache += (s -> cs)      cs   } 
}

class ChecksumAccumulator {
}

ChecksumAccumulator.calculate("Every value is an object.")
```

+ standalone object 孤立对象

孤立对象可不与半生类共享名称，即单独定义，但在其内部要提供main方法。<font color="red">任何拥有合适签名的 main 方法的单例对象都可以 用来作为程序的入口点</font>

```
import ChecksumAccumulator.calculate object Summer {  def main(args: Array[String]) {    for (arg <- args)      println(arg + ": " + calculate(arg))  } }
 
 scalac ChecksumAccumulator.scala Summer.scala  //它把 Scala 源码编译成字节码，然后立刻通过类 装载器装载它们，并执行它们。所以需要先编译
 scala Summer of love  //Summer中需要由入口点，即main方法

```

+ Application 特质

特质 Application 声明了带有合适的签名的 main 方法，并由你的单例对象继承，使它可以像个 Scala 程序那样用。大括号之间的代码被收集进了单例对象的主构造器:primary constructor，并在类被初始化时被执行。

args 数组不可访问,多线程就需要显式的main方法的弊端使得只有当你的程序相对简单和单线程情况下你可以继承Application特质。

<font color="blue">很明显，这样的例子就是上面提到的单例对象混入特质，后面应该还会详细介绍，什么主构造器什么的..实在不懂</font>

```
import ChecksumAccumulator.calculateobject FallWinterSpringSummer extends Application {  for (season <- List("fall", "winter", "spring"))    println(season +": "+ calculate(season))}
```

### 基本类型和操作

+ 基本类型

|值类型|范围|
|:------:|:-----:|
|Byte|8 位有符号补码整数(-27~27-1)|
|Short|16 位有符号补码整数(-215~215-1)|
|Int|32 位有符号补码整数(-231~231-1)|
|Long|64 位有符号补码整数(-263~263-1)|
| Char|16 位无符号Unicode字符(0~216-1)|
| String|字符序列|
| Float|32 位 IEEE754 单精度浮点数|
| Double|64 位 IEEE754 单精度浮点数|
| Boolean|true 或 false|


总体来说，类型 Byte，Short， Int，Long和Char被称为整数类型:integral type。整数类型加上Float和Double被称 为数类型:numeric type。除了String归于java.lang包之外，其余所有的基 类型都是包scala的成员.int和Int是一回事，但推荐还是大写。

<font color="red">String 引入了一种特殊的语法。以同一行里的三个引号(""")开始和结束一条原始 字串。内部的原始字串可以包 无论何种任意字符，包括新行，引号和特殊字符，当然同 一行的三个引号除外。</font>

```
println("""Welcome to Ultamix 3000.           Type "HELP" for help.""")

//result
Welcome to Ultamix 3000.           Type "HELP" for help.
           
println("""|Welcome to Ultamix 3000.           |Type "HELP" for help.""".stripMargin)
           
//result

Welcome to Ultamix 3000.Type "HELP" for help.
```

+ Symbol类型

```
scala> val s = 'aSymbols: Symbol = 'aSymbolscala> s.nameres20: String = aSymbol

```+ 基本操作

Scala 里的操作符不是特殊的语言语法:任何方法都可以是操作符。当以操作符标注方式使用它，那它就是操作符，如

```
s indexOf ('o', 5)
```

操作符标注方式，分为前缀，中缀和后缀。前缀和后缀操作符都是一元:unary的:它们仅带一个操作数。前缀操作符，方法名在操作符字符上前缀了“unary\_”，Scala 会把表达式-2.0 转换成方法调用“(2.0).unary_-”。<font color="blue">这句话说的好别扭，我稍微有点不能理解，是方法定义就定义成了unary\_这个名字吗？看起来是这个样子。</font>

可以当作前缀操作符用的标识符只有+，-，!和~

在Scala里的与，或跟Java一样具有短路机制。<font color="red">或许你会想知道如果操作符都只是方法的话短路机制是怎么工作的呢。通常，进入方法之前所有的参数都会被评估，因此方法怎么可能选择不评估他的第二个参数呢?答案是因为所有的 Scala 方法都有延迟 其参数评估乃至取消评估的设置。这个设置被称为叫名参数:by-name parameter。</font>


+ 操作符优先级

|优先级降序|
|------|
|* / %||+-||:||=!||<>||&||^||\||

如果操作符以等号字符(=)结束 ，且操作符并非比较操作符<=，>=，==，或=， 那么这个操作符的优先级与赋值符(=)相同。也就是说，它比任何其他操作符的优先级都低

a +++ b *** c(这里a， b 和 c 是值或变量，而+++和***是方法)将被看作是 a +++ (b *** c)
x *= y + 1

与下面的相同:x *= (y + 1)


+ 富包装器

现在所有要知道的就是 章介绍过的每个基 类型，都有一个“富包装 器”可以提供许多额外的方法。

|基本类型| 富包装|
|------|------||Byte |scala.runtime.RichByte Short scala.runtime.RichShort
|Int |scala.runtime.RichInt Long scala.runtime.RichLong 
|Char |scala.runtime.RichChar String scala.runtime.RichString 
|Float |scala.runtime.RichFloat Double scala.runtime.RichDouble 
|Boolean |scala.runtime.RichBoolean

一些富操作

```
0 max 5  50 min 5   0-2.7 abs   2.7-2.7 round  31.5 isInfinity false

(1.0 / 0) isInfinity  true4 to 6     Range(4, 5, 6)"bob" capitalize  "Bob""robert" drop 2  "bert"
```