## java8的一些特性

### Stream

[详解](https://www.ibm.com/developerworks/cn/java/j-lo-java8streamapi/)

+ 示例1，查询满足特定条件的某一个对象，传统使用for循环，if(){return}

```
//List<Article> articles
public Optional<Article> getFirstJavaArticle() {  
    return articles.stream()
        .filter(article -> article.getTags().contains("Java"))
        .findFirst();
}
```

+ 示例2，查询满足特定条件的集合，传统使用for循环，加入list，返回list

```
public List<Article> getAllJavaArticles() {  
	return articles.stream()
	    .filter(article -> article.getTags().contains("Java"))
	    .collect(Collectors.toList());
}

```

+ 示例3， 查询分组，传统使用for循环，返回Map<String, List<>>结构

```
public Map<String, List<Article>> groupByAuthor() {  
    return articles.stream()
        .collect(Collectors.groupingBy(Article::getAuthor));
}
```

+ 示例4， 查询所有tags, tags为Article的一个属性，类型为List,传统为返回Set，for循环一次加入set

```
public Set<String> getDistinctTags() {  
    return articles.stream()
        .flatMap(article -> article.getTags().stream())
        .collect(Collectors.toSet());
}
```

+ 示例5， 数值流的构建，如for(int i =0 ; i < ; i ++)

```
IntStream.of(new int[]{1, 2, 3}).forEach(System.out::println);
IntStream.range(1, 3).forEach(System.out::println);
IntStream.rangeClosed(1, 3).forEach(System.out::println);

```

+ 示例6， 找出全文的单词，转小写， distinct 来找出不重复的单词并排序。

```

List<String> words = br.lines().
	 flatMap(line -> Stream.of(line.split(" "))).
	 filter(word -> word.length() > 0).
	 map(String::toLowerCase).
	 distinct().
	 sorted().
	 collect(Collectors.toList());
br.close();

```

#### 常见Api

+ filter

+ map
+ mapToInt, mapToDouble, mapToLong,目前理解上来说，toInt, toLong之类的是为了后续的操作，比如mapToInt返回的是类型明确是IntStream，就可以调用IntStream中的api。<font color="red">需要注意的是，对于基本数值型，目前有三种对应的包装类型 Stream：IntStream、LongStream、DoubleStream。当然我们也可以用 Stream<Integer>、Stream<Long> >、Stream<Double>，但是 boxing 和 unboxing 会很耗时，所以特别为这三种基本数值型提供了对应的 Stream。</font>

+ flatMap, 与map的区别最大在于适用于1对多的情况，既会扩大流，map是1对1的关系,如示例4
+ flatMapToInt， flatmapToDouble， flatmapToLong

+ toArray
+ collect 用于Stream转换成其他类型的数据，如Set,List等

+ distinct 返回不重复的数组组成的流，如示例6
+ sorted 排序，


```
List<Person> personList2 = persons.stream()
		.sorted((p1, p2) -> p1.getName().compareTo(p2.getName()))
		.collect(Collectors.toList());

```

+ forEach  跟map不同的是，没有返回新流，只是遍历的作用。forEach 不能修改自己包含的本地变量值，也不能用 break/return 之类的关键字提前结束循环，forEach 是 terminal 操作，因此它执行后，Stream 的元素就被“消费”掉了，就是forEach后面不能再对流进行处理了，相应的功能是peek,peek遍历，并且能返回新流

+ peek


+ limit， limit 返回 Stream 的前面 n 个元素；skip 则是扔掉前 n 个元素（它是由一个叫 subStream 的方法改名而来）。
+ skip

+ forEachOrdered
+ reduce
+ count
+ anyMatch, allMatch, noneMatch，查询是否有符合条件的

+ findFirst, findAny
+ empty
+ of
+ iterate

```
//生成一个等差数列
Stream.iterate(0, n -> n + 3)
	.limit(10)
	.forEach(x -> System.out.print(x + " "));

//0 3 6 9 12 15 18 21 24 27
```

<font color="red">与 Stream.generate 相仿，在 iterate 时候管道必须有 limit 这样的操作来限制 Stream 大小。</font>

+ generate

```
//生成 10 个随机整数

Random seed = new Random();
Supplier<Integer> random = seed::nextInt;
Stream.generate(random).limit(10).forEach(System.out::println);
//Another way
IntStream.generate(() -> (int) (System.nanoTime() % 100)).
limit(10).forEach(System.out::println);
```


+ concat


### Lambda

恩时间来不及了，先补代码，之后要是想起了 有时间再补具体的了。

自定义lambda

```
//定义方法
@FunctionalInterface
public interface Consumer<T> {
	void accept(T t);
}

void forEach(Consumer<? super T> action) {
    Objects.requireNonNull(action);
       for (T t : this) {
          action.accept(t);
       }
}

//使用
forEach(() -> {})


```


### Clock