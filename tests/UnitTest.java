package soot.jimple.interproc.ifds;

import soot.jimple.interproc.ifds.Main;

class UnitTest{
	
	
	@Test
	public static runSootWithTest(){
		String[] args[] = new String[];
		args[0] = "-w";
		args[1] = "-no-bodies-for-excluded";
		args[2] = "-cp";
		args[3] = "test";
		args[4] = "TestLocalVariable";
		
		Main.main(args)
		
	}
	
}