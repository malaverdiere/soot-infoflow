package soot.jimple.infoflow.test.securibench;


public class TestCaseCreator {

	public static void main(String[] args) {
		String pathAndClassWithoutNumber = "securibench.micro.basic.Basic";
		int numberOfTests = 42;
		
		String classname = pathAndClassWithoutNumber.substring(pathAndClassWithoutNumber.lastIndexOf(".")+1);

		for(int i=1; i<=numberOfTests; i++){
			System.out.println("@Test");
			System.out.println("public void "+classname.toLowerCase()+i+ "() {");
			System.out.println("Infoflow infoflow = initInfoflow();");
			System.out.println("List<String> epoints = new ArrayList<String>();");
			System.out.println("epoints.add(\"<"+pathAndClassWithoutNumber+i+ ": void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>\");");
			System.out.println("infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);");
			System.out.println("checkInfoflow(infoflow);");
			System.out.println("}");
			System.out.println();
		}
	}
}
