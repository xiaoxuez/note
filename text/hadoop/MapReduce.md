
## MapReduce编写

以WordCount例子说明
[MapReduce的工作流程详细解释](http://www.dataguru.cn/thread-188560-1-1.html)
### Map

	public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {

		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			StringTokenizer itr = new StringTokenizer(value.toString());
			while (itr.hasMoreTokens()) {
				word.set(itr.nextToken());
				context.write(word, one);
			}
		}
	}


+ Mapper的泛型声明为Mapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT>, Map到Reduce的过程中会进行排序，所以KEYOUT, VALUEOUT至少都应该实现Comparable(WritableComparable)。
+ map的调用, 读取文件按行读取，就是每一次map的value是当前行内容，key为行内容中第一个字符在整个文件中的下标。StringTokenizer为取String内容分单词的类。
+ 在所有map调用完排完序之后，会进行Reduce的调用。

    
### Reduce

	public static class IntSumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
		private IntWritable result = new IntWritable();
		public void reduce(Text key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			result.set(sum);
			context.write(key, result);
		}
	}
	
+ 同理, Reducer泛型的声明意义与Mapper一样。

### main方法
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length < 2) {
			System.err.println("Usage: wordcount <in> [<in>...] <out>");
			System.exit(2);
		}
		Job job = Job.getInstance(conf, "word count");
		job.setJarByClass(WordCount.class);
		job.setMapperClass(TokenizerMapper.class);
		job.setCombinerClass(IntSumReducer.class);
		job.setReducerClass(IntSumReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		
		
		for (int i = 0; i < otherArgs.length - 1; ++i) {
			FileInputFormat.addInputPath(job, new Path(otherArgs[i]));
		}
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[otherArgs.length - 1]));

		System.exit(job.waitForCompletion(true) ? 0 : 1);

		
为了简化命令行方式运行作业，Hadoop自带了一些辅助类。GenericOptionsParser是一个类，用来解释常用的Hadoop命令行选项，并根据需要，为Configuration对象设置相应的取值。通常不直接使用GenericOptionsParser，更方便的方式是：实现Tool接口，通过ToolRunner来运行应用程序，ToolRunner内部调用GenericOptionsParser。[ToolRunner解释](http://blog.csdn.net/jediael_lu/article/details/38751885),示例为WordStandardDeviation