class TestGlobalVariable{
	static String[] target;
	static String[] test2;
	String[] test3;
	
	public static void main(String[] args){
		target = args;
		String[] animal = target;
		String[] x = returnArgument(target); //does not work yet
		//String[] result = localVar.clone();
		
		setVariable(animal);
	}
	
	static String[] returnNull(String[] b) {
		return null;
	}
	
	static String[] returnArgument(String[] a) {
		return a;
	}
	
	static void setVariable(String[] par1) {
		test2 = par1;
	}
	
}