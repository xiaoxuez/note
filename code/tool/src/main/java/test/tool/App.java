package test.tool;

/**
 * Hello world!
 *
 */
public class App 
{
	public static App SAVE_HOOK = null;
	private int age;
	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
		System.out.println("finalize: " + this.age);
	}
	
    public static void main( String[] args ) throws InterruptedException
    {
//    	SAVE_HOOK = new App();
    	App app = new App();
    	app.age = 10;
    	app = null;
//    	SAVE_HOOK = null;
    	System.gc();
    	
    	Thread.sleep(500);
    	if (app != null) {
    		System.out.println(" not gc");
    	} else {
    		System.out.println(" gc ok!");
    	}
    }
}
