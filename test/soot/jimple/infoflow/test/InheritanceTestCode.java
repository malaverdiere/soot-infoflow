package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.utilclasses.D1static;

public class InheritanceTestCode {
	
	public void testInheritance1(){
		boolean x = false;
		D1static d1 = new D1static("");
		x = d1.start();
		x= d1.taintIt();
		if(x){
			d1.toString();
		}
	}

}
