package soot.jimple.infoflow.test.android;

public class AccountManager {

	public String getPassword(){
		
		return "123";
	}
	
	public String[] getUserData(String user){
		String[] userData = new String[2];
		userData[0] = user;
		userData[1] = getPassword();
		return userData;
	}
}
