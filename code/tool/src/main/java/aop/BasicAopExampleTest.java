package aop;

/**
 * AOP: 面向切面编程,
 * AOP编程的基本实现  在调用findInfo方法前插入执行代码(log日志)。
 * 编程思想是， 在初始化实例对象的时候 返回具有代理的对象。 在调用对象方法的时候，会从代理方法中执行真正的方法。
 * Java中提供的动态代理类的实现是针对实现了某些接口的类，如果没有实现接口的话，不能创建代理类。要为没有实现接口的类实现aop的话使用CGLib,思想其实差不多
 * @author xiaoxuez
 *
 */
public class BasicAopExampleTest {
	public static void main(String[] args) {
		StudentInfoService student = (StudentInfoService) AopFactory.getAOPProxyedObject(StudentInfoServiceImpl.class);
		student.findInfo("Moxi");
		
		/* result: 
		 * [main] INFO  a.MyHandler - 调用log日志方法findInfo
		 * 你目前输入的名字是:Moxi
		 */
	}
}
