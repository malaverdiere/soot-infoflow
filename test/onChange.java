

public class onChange {

	public String dontcallmeoncreate(String test2){
		return test2;
		//System.out.println(f);
	}
	
	public void onChange1(String str){
		String b = str;
		String l = dontcallmeoncreate(b);
		String v = l;
	}

}
