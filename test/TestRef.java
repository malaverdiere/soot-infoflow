
public class TestRef {
	public static void main(String[] args){
		Testobject testo1 = new Testobject();
		testo1.name = args[0];
		//String notinfectedWorks = testo1.version; // works!
		//Testobject testo2 = testo1;
		//String infected1 = testo2.name; //works!
		Testobject testo3 = new Testobject();
		String notinfected = testo3.name;
	}
}
