package test.tool;

/**
 * Hello world!
 *
 */
public class App 
{
	
	public static String  reverse(String pre) {
		if (pre == null || pre.length() <= 1) {
			return pre;
		}
		return reverse(pre.substring(1)) + pre.charAt(0);
	}
	public static void main(String[] args) {
		throw new NullPointerException();
	}
}
