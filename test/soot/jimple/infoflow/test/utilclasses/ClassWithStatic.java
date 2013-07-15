package soot.jimple.infoflow.test.utilclasses;

public class ClassWithStatic {
	private static String staticTitle;
	public static String staticString;

	public String getTitle() {
		return staticTitle;
	}

	public void setTitle(String title) {
		ClassWithStatic.staticTitle = title;
	}
	
}
