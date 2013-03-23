package soot.jimple.infoflow.test;

class TestIf{
	
	@SuppressWarnings("unused")
	public static void main(String[] args){
		int i = 2;
		if(i%2 == 0){
			String[] z = args;
		}else{
			String[] y = args;
		}
	}
	

}