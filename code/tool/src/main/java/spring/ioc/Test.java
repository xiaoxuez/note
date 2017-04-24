package spring.ioc;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Test {
	public static void main(String[] args) {
		
		ApplicationContext context = new ClassPathXmlApplicationContext("spring/ioc/beans.xml");
		// 简单的实现
		UserDao dao = (UserDao) context.getBean("userDaoImpl");
		dao.save();
		
		//通过静态工厂创建
		UserDao daoFactory = (UserDao) context.getBean("userDaoImplFactoryStaticMethod");
		daoFactory.save();
		
		//通过实例工厂创建， 与静态工厂的区别是方法不是静态方法
		UserDao daoFactory2 = (UserDao) context.getBean("userDaoImplFactoryInstanceMethod");
		daoFactory.save();
		
		//对象的生命周期,bean节点的scope属性来控制对象的生命周期，它包含两个可选值：
		//1）singleton，表明系统中对于同一个对象，只保留一个实例。
		//2）prototype，表明系统中每次获取bean时，都新建一个对象。
		UserDao daoScopeSin = (UserDao) context.getBean("userDaoImplScopeSingleton");
		UserDao daoScopeSin1 = (UserDao) context.getBean("userDaoImplScopeSingleton");
		System.out.println(" singleton " + (daoScopeSin == daoScopeSin1));
		
		UserDao daoScopePro = (UserDao) context.getBean("userDaoImplScopePrototype");
		UserDao daoScopePro1 = (UserDao) context.getBean("userDaoImplScopePrototype");
		System.out.println(" prototype " + (daoScopePro == daoScopePro1));
		
		//设置对象属性
		
	}
}
