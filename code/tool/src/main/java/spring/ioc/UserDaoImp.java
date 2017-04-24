package spring.ioc;

public class UserDaoImp implements UserDao{

	@Override
	public void save() {
		System.out.println("      调用save方法");
	}
	

}
