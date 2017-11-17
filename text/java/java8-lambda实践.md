## Lambda实践

Lambda表达式（也称为闭包）是Java 8中最大和最令人期待的语言改变。它允许我们将函数当成参数传递给某个方法，或者把代码本身当作数据处理。

说明某个接口是函数式接口，Java 8 提供了一个特殊的注解@FunctionalInterface
。在java.util.function包下提供的都是常用的函数式接口，当然，都加了@FunctionalInterface
注解。接下来挑几个接口出来详细解释下。

#### 举个例子

```
Arrays.asList(5, 3, 2, 8, 1).forEach((a) -> System.out.println(a))
```

#### 接口介绍

+ Function

	接口方法主要应用于方法既有参数又有返回值的情况,内部非default的方法为
	
	```
	R apply(T t);
	```

+ Supplier

	接口方法主要应用于方法只有返回值，无参的情况,内部非default的方法为
	
	```
	T get();
	```

+ Consumer

	接口方法主要应用于方法只有参数，无返回值的情况,Consumer接口期望执行带有副作用的操作，即有可能改变参数的内部状态。内部非default的方法为
	
	```
	void accept(T t);
	```

+ Predicate 

	接口方法主要应用于方法根据输入参数进行判断，返回boolean的情况。内部非default的方法为

	```
	boolean test(T t);
	```

##### 举个例子

还是刚刚的例子    
```
Arrays.asList(5, 3, 2, 8, 1).forEach((a) -> System.out.println(a))
```    

forEach方法的声明：

```
default void forEach(Consumer<? super T> action) {
    Objects.requireNonNull(action);
    for (T t : this) {
        action.accept(t);
    }
}

```
整个使用就很明了了。

#### ::运算符

##### 举个例子

```
Arrays.asList(5, 3, 2, 8, 1).forEach((a) -> System.out.println(a))
Arrays.asList(5, 3, 2, 8, 1).forEach(System.out::println)
``` 

上面两行代码是等价的。一般而言，类的静态方法调用为Class::method,类的成员方法调用为Instance::method。System.out是PrintStream的实例，上例是对象::方法名的调用。

主要疑惑吧，是如下情况

```

 class LambdaTest {
    public void repair() {
        System.out.println( "Repaired " + this.toString() );
    }
    
    public void follow( final LambdaTest another ) {
	    System.out.println( "Following the " + another.toString() );
	}
	
	public static void main(String[] args) {
		LambdaTest t2 = new LambdaTest();
		LambdaTest t1 = new LambdaTest();

		Arrays.asList(t1).forEach(LambdaTest::repair);
		Arrays.asList(t1).forEach(t2::repair);
	}
 }
```

疑惑就在于repair的调用竟然能用LambdaTest::repair调用的方式，让我捋捋...


找了下官方的文档，::的使用有4种方式

|Kind|Example|
|:---|---|
|Reference to a static method | ContainingClass::staticMethodName
|Reference to an instance method of a particular object|Reference to an instance method of a particular object
|Reference to an instance method of an arbitrary object of a particular type|ContainingType::methodName
|Reference to a constructor|ClassName::new


对于第三中，什么叫引用一个任意一个特定类型的对象的实例方法...完全不能理解嘛..好吧再仔细理解一下，函数式接口的传入参数足够且各方面类型统一的情况下，就合理。例如

```
//forEach(e -> )传入参数只有一个，如果方法没有参数，则传入的参数可以用作于方法的引用对象存在
Arrays.asList(t1).forEach(LambdaTest::repair);

//compareToIgnoreCase 的定义为int compareToIgnoreCase(String str)
//函数式接口方法的定义为int compare(T o1, T o2)
//传入参数为2个，1个作用于compareToIgnoreCase的调用者，1个作用于compareToIgnoreCase的参数
String[] stringArray = { "Barbara", "James", "Mary", "John",
    "Patricia", "Robert", "Michael", "Linda" };
Arrays.sort(stringArray, String::compareToIgnoreCase);
```