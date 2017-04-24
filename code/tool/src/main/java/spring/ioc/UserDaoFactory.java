package spring.ioc;

public class UserDaoFactory {
	public static UserDao getUserDao() {
		return new UserDaoImp();
	}
}
