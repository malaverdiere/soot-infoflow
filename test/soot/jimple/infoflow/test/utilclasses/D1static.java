package soot.jimple.infoflow.test.utilclasses;

import soot.jimple.infoflow.test.android.ConnectionManager;

public class D1static extends C1static {
	
	public D1static(String c) {
		super(c);
	}
	
	public boolean taintIt(){
		ConnectionManager cm = new ConnectionManager();
		cm.publish(field1);
		return true;
	}

}
