package soot.jimple.infoflow.test.utilclasses;

public class ClassWithFinal<E> {
	 public final E[] a;
	 final String b;
	 
	 public ClassWithFinal(String c, boolean e){
			if (c==null)
	            throw new NullPointerException();
			 b = c;
			 a = null;
		 }
	
	public ClassWithFinal(E[] value){
		 if (value==null)
             throw new NullPointerException();
		a = value;
		b = "";
	}
	
	public E[] getArray(){
		return a;
	}
	
	public String getString(){
		return b;
	}
}
