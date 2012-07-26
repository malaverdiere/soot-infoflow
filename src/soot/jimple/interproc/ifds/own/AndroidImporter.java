package soot.jimple.interproc.ifds.own;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

//TODO: improvement - check if parameters are correct...
//TODO: check subclassing of classes (for example MySocket extends java.net.ServerSocket)
public class AndroidImporter {

	public static void main(String[] args) throws IOException{
		Hashtable<String, MethodName> methodCalls = new Hashtable<String, MethodName>();
		readAPIcalls(args[0], methodCalls);
		System.out.println(methodCalls.size());
	}
	
	static void readAPIcalls(String fileName, Hashtable<String, MethodName> table) throws IOException{
		
		File file = new File(fileName);
		FileReader fReader = new FileReader(file);
		BufferedReader bReader = new BufferedReader(fReader);
		String line = "";
		while (line != null){
			line = bReader.readLine();
			String methodName = "";
			String classString = "";
			if(line != null && line != "" && line.contains("(")){
				String temp = line.substring(0, line.indexOf('('));
				if(temp.contains(".")){
					classString = temp.substring(0, temp.lastIndexOf('.'));
					methodName = temp.substring(temp.lastIndexOf('.')+1);
					System.out.println(methodName + " " + classString);
					if(table.contains(methodName)){
						table.get(methodName).addClass(classString);
						
					}else{
						table.put(methodName, new MethodName(methodName, classString));
					}
				}
				//TODO: filter <init>
			}
			
		}
		
	}
}
