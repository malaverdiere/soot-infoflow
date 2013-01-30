package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.utilclasses.D1static;

public class InheritanceTestCode {
	
	public void testInheritance1(){
		D1static d1 = new D1static("");
		d1.start();
		d1.taintIt();
	}

}
