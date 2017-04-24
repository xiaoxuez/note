package aop;

public class AopFactory {
	private static Object getClassInstance(Class<?> className) {
		Object obj = null;
		try {
			obj = className.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return obj;
	}

	public static Object getAOPProxyedObject(Class<?> clzName) {
		Object proxy = null;
		MyHandler handler = new MyHandler();
		Object obj = getClassInstance(clzName);
		if (obj != null) {
			proxy = handler.bind(obj);
		} else {
			System.out.println("Can't get the proxyobj");
			// throw
		}
		return proxy;
	}

}
