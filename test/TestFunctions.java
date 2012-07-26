
public class TestFunctions {
	
	public static void main(String[] args){
		String[] z100gh = args;
		String[] y3498 = z100gh.clone();
		Object x = args;
		String[] test = (String[]) x;
		String one = z100gh[0];
		String doubleString = one.concat(" "); //works
		int length = one.length();
		String sub = one.substring(1);
	
	
	
	//TODO: does not work:
	//doubleString = one + " "; //crashes
	}
}
