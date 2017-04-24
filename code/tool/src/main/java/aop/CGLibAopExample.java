package aop;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

/**
 * 这里使用的cglib库是来自spring中的，也有独立的cglib库。
 * @author xiaoxuez
 *
 */
public class CGLibAopExample {
	public static void main(String[] args) {
		AOPInstrumenter instrumenter=new AOPInstrumenter();
		StudentInfoServiceImpl student = (StudentInfoServiceImpl) instrumenter.getInstrumentedClass(StudentInfoServiceImpl.class);
		student.findInfo("Moxi");
	}

	public static class StudentInfoServiceImpl {
		public void findInfo(String name) {
			System.out.println("你目前输入的名字是:" + name);
		}
	}

	public static class AOPInstrumenter implements MethodInterceptor {

		private Logger log = Logger.getLogger(AOPInstrumenter.class);
		private Enhancer enhancer = new Enhancer();

		public Object getInstrumentedClass(Class clz) {
			enhancer.setSuperclass(clz);
			enhancer.setCallback(this);
			return enhancer.create();
		}

		@Override
		public Object intercept(Object arg0, Method method, Object[] arg2, MethodProxy proxy) throws Throwable {
			log.info("调用日志方法"+method.getName());
			Object result  = proxy.invokeSuper(arg0, arg2);
			return result;
		}

	}
}
