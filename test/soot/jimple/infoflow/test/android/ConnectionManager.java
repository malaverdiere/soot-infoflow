package soot.jimple.infoflow.test.android;

public class ConnectionManager {

	public void publish(String str){
		System.out.println(str);
		//publish on internet...
	}

	public void publish(int i){
		System.out.println(i + "");
		//publish on internet...
	}
}
