package aop;

public class StudentInfoServiceImpl implements StudentInfoService{

	@Override
	public void findInfo(String studentName) {
		System.out.println("你目前输入的名字是:"+studentName);
	}

}
