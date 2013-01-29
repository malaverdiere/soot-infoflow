package soot.jimple.infoflow.test.utilclasses;

import soot.jimple.infoflow.test.android.ConnectionManager;

public class ClassWithField2 extends ClassWithField {
	
	public ClassWithField2(String s){
		super(s);
	}

	public void taintIt(){
		ConnectionManager cm = new ConnectionManager();
		cm.publish(field);
	}
}
