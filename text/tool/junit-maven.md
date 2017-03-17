1. 新建测试类，new JUnit Case Test
2. 新建类时选择的几个方法的解释。    
    + setUp： 测试前的初始化工作
    + tearDown: 测试完成后垃圾回收工作
    + constructor: 构造方法
    
3. 几种标注的介绍。
	+  @Test,表示这个是测试方法
	+  @Before,这个方法会在每个测试方法前都执行一次
	+  @After,这个方法会在每个测试方法后都执行一次
	+  @Ignore, 表示这个方法在测试的时候会被忽略
	+  @BeforeClass, 只在测试用例初始化时执行
	+  @AfterClass,，当所有测试执行完毕之后进行收尾工作
	+  每个测试类只能有一个方法被标注为 @BeforeClass 或 @AfterClass ，并且该方法必须是 Public 和 Static 的。
	+  @Test(timeout  =   1000 )  限时测试
	+  @Test(expected  =  ArithmeticException. class ) 异常测试
	+  参数化测试，这个以代码来解释怎么用。下面的测试数据是3组，会有3次结果。 
	
	
			public class SquareTest {
			
				private static Calculator calculator = new Calculator();
				private int param;
				private int result;
				
				@Parameters
			      public   static  Collection data()  {
			          return  Arrays.asList( new  Object[][] {
			                  { 2 ,  4 } ,
			                  { 0 ,  0 } ,
			                  {－ 3 ,  9 } ,
			         } );
			     }
			     
				// 构造函数，对变量进行初始化
				public SquareTest(int param, int result) {
					this.param = param;
					this.result = result;
				}
				
				@Test
				public void square() {
					calculator.square(param);
					assertEquals(result, calculator.getResult());
				}
			
			}
			

4. 使用mvn test进行测试
	