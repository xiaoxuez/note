## 类型和语法

### 类型篇

#####  typeof

typeof运算符可查看值的类型，返回值为string。

需要注意的是，typeof null === "object" 的结果为true,即null其实是一个"假值"，另外，对函数也可以进行typeof, 如typeof function a(){} === "function"的结果为true。

##### 类型

JavaScript的内置类型有7种，null, undefined, boolean, number, string, object, symbol。

变量是没有类型的，只有值才有。故一个变量可多次赋值不同类型。

obejct的"子类型"有function,数组的类型也是object  

+ function可以拥有属性，如函数对象的length属性是其声明的参数的个数。
+ 数组通过数字进行索引，可以容纳任何类型(不同类型)，不需要预先设定大小，键值如果是字符串类型，则转换为数组对象的属性，但若是可强制类型转换为十进制的字符串类型，会被当做数字索引来处理，如

	```
	var a = [];
	a[0] = 1;
	a["s"] = 2;
	//a.length =1, a.s = 2
	a["2"] = 2;
	//a[2] = 2,a.length = 3, a[1] = "undefined"
	```

变量在未持有值的时候为undefined, 在未声明的时候为undeclared, typeof返回的二者的类型都是undefined，因为typeof有一个特殊的安全防范机制。故typeof可用在检查变量上。

访问不存在的对象属性不会产生undeclared，只会是undefined。

string是不可变的，string的位上的访问也可以是[index]访问。

一个很常见的面试题，字符串的反转，可a.split("").reverse().join("")实现。string没有reverse方法，数组有reverse方法。

数字类型number,没有严格意义上的整数，使用的是双精度格式，即42.0 == 42，故在运算上，处理带小数的数字时需要注意，0.1 + 0.2的结果是一个严格逼近0.3的值，但不一定是0.3，解决的方式是使用近似判断，即允许误差范围，这个值通常是2^-52，再介绍两个api. toFixed可指定小数部分的显示位数，toPrecision可指定有效数位的显示位数。整数的安全范围是2^53 - 1。


特殊数值的使用，包括null, undefined, void运算符，NaN, 无穷数(Infinity,-Infinity),0,-0... 就不一一说明了，还是翻书吧。

关于引用方面，赋值的时候，简单类型都是值复制的方式，复合类型(对象和函数)，是复制引用的方式