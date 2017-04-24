package aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.log4j.Logger;

/**
 * 拦截处理的类
 * 
 * @author xiaoxuez
 *
 */
public class MyHandler implements InvocationHandler {

	private Object proxyObj;
	private static Logger log = Logger.getLogger(MyHandler.class);

	public Object bind(Object obj) {
		this.proxyObj = obj;
		//代理实例，需要obj是实现了某接口(见第二个参数)
		return Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(), this);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		log.info(("调用log日志方法"+method.getName())); //方法调用前插入日志
		Object result=method.invoke(proxyObj,args); //原方法
		return result;
	}

}
