class TestLocalVariable{
	
	public static void main(String[] args){
		String[] cl = args.clone();
		String x = args[0];
		String[] z = returnNull(args);
		String[] y = returnArgument(args);
		String[] y2 = y;
	}
	
	static String[] returnNull(String[] a) {
		return null;
	}
	
	static String[] returnArgument(String[] a) {
		return a;
	}
	
}