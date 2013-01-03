package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.TelephonyManager;


/**
 * Thrown to indicate that an index of some sort (such as to an array, to a
 * string, or to a vector) is out of range.
 * <p>
 * Applications can subclass this class to indicate similar exceptions.
 *
 * @author  Frank Yellin
 * @since   JDK1.0
 */
public
class IndexOutOfBoundsException extends RuntimeException {
    private static final long serialVersionUID = 234122996006267687L;

    
    public void method(){
    	 String s = TelephonyManager.getDeviceId();
         IndexOutOfBoundsException e = new IndexOutOfBoundsException(s);
         System.out.println(e.getMessage());
    }
    /**
     * Constructs an <code>IndexOutOfBoundsException</code> with no
     * detail message.
     */
    public IndexOutOfBoundsException() {
        super();
       
        
    }

    /**
     * Constructs an <code>IndexOutOfBoundsException</code> with the
     * specified detail message.
     *
     * @param   s   the detail message.
     */
    public IndexOutOfBoundsException(String s) {
        super(s);
    }
}
